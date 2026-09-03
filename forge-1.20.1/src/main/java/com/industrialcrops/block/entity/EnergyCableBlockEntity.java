package com.industrialcrops.block.entity;

import com.industrialcrops.block.EnergyCableBlock;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public final class EnergyCableBlockEntity extends BlockEntity {
    private static final int MAX_NETWORK_SIZE = 1024;
    public static final int BASIC_RATE = 10_000;
    public static final int ADVANCED_RATE = 100_000;
    private final CableEnergyStorage energy;

    public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CABLE.get(), pos, state);
        int rate = state.is(ModBlocks.ADVANCED_ENERGY_CABLE.get()) ? ADVANCED_RATE : BASIC_RATE;
        // One segment stores exactly its tier value: 10 kFE for copper and
        // 100 kFE for gold. Previously this was incorrectly multiplied by four.
        energy = new CableEnergyStorage(rate);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyCableBlockEntity cable) {
        if (level.isClientSide()) return;
        List<EnergyCableBlockEntity> network = cable.collectNetwork();
        if (network.isEmpty() || network.stream().anyMatch(other -> other.worldPosition.asLong() < pos.asLong())) return;
        cable.transferNetwork(network);
    }

    private List<EnergyCableBlockEntity> collectNetwork() {
        List<EnergyCableBlockEntity> result = new ArrayList<>();
        if (level == null) return result;
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(worldPosition); visited.add(worldPosition);
        while (!queue.isEmpty() && visited.size() <= MAX_NETWORK_SIZE) {
            BlockPos current = queue.removeFirst();
            if (level.getBlockEntity(current) instanceof EnergyCableBlockEntity cable) result.add(cable);
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (visited.add(next) && level.getBlockState(next).getBlock() instanceof EnergyCableBlock) queue.addLast(next);
            }
        }
        return result;
    }

    private void transferNetwork(List<EnergyCableBlockEntity> network) {
        if (level == null) return;
        int rate = network.stream().mapToInt(EnergyCableBlockEntity::transferRate).min().orElse(BASIC_RATE);
        int pooled = 0;
        for (EnergyCableBlockEntity cable : network) pooled += cable.energy.extractInternal(rate - pooled);

        record Endpoint(BlockPos pos, Direction side, IEnergyStorage storage) { }
        List<Endpoint> endpoints = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (EnergyCableBlockEntity cable : network) for (Direction direction : Direction.values()) {
            BlockPos endpointPos = cable.worldPosition.relative(direction);
            if (level.getBlockState(endpointPos).getBlock() instanceof EnergyCableBlock) continue;
            Direction side = direction.getOpposite();
            IEnergyStorage storage = com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ENERGY, endpointPos, side);
            String key = endpointPos.asLong() + ":" + side.ordinal();
            if (storage != null && seen.add(key)) endpoints.add(new Endpoint(endpointPos, side, storage));
        }

        Set<Endpoint> activeSources = new HashSet<>();
        for (Endpoint endpoint : endpoints) {
            if (pooled >= rate || !endpoint.storage.canExtract()) continue;
            int available = endpoint.storage.extractEnergy(rate - pooled, true);
            if (available > 0) {
                int extracted = endpoint.storage.extractEnergy(available, false);
                pooled += extracted;
                if (extracted > 0) activeSources.add(endpoint);
            }
        }
        for (Endpoint endpoint : endpoints) {
            if (pooled <= 0) break;
            if (activeSources.contains(endpoint) || !endpoint.storage.canReceive()) continue;
            pooled -= endpoint.storage.receiveEnergy(pooled, false);
        }
        // Keep the shared cable capacity distributed across all segments instead
        // of concentrating it in whichever segment happens to be the controller.
        List<EnergyCableBlockEntity> writable = new ArrayList<>(network);
        while (pooled > 0 && !writable.isEmpty()) {
            int share = Math.max(1, (pooled + writable.size() - 1) / writable.size());
            boolean movedAny = false;
            for (int i = writable.size() - 1; i >= 0 && pooled > 0; i--) {
                EnergyCableBlockEntity cable = writable.get(i);
                int moved = cable.energy.receiveInternal(Math.min(share, pooled));
                pooled -= moved;
                movedAny |= moved > 0;
                if (cable.energy.rawStored() >= cable.energy.rawCapacity()) writable.remove(i);
            }
            if (!movedAny) break;
        }
    }

    private int transferRate() { return getBlockState().is(ModBlocks.ADVANCED_ENERGY_CABLE.get()) ? ADVANCED_RATE : BASIC_RATE; }
    public IEnergyStorage getEnergyStorage() { return energy; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.putInt("Energy", energy.getEnergyStored());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); energy.setStored(tag.getInt("Energy"));
    }

    private int networkEnergyStored() {
        if (level == null) return energy.rawStored();
        long total = collectNetwork().stream().mapToLong(cable -> cable.energy.rawStored()).sum();
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    private int networkCapacity() {
        if (level == null) return energy.rawCapacity();
        long total = collectNetwork().stream().mapToLong(cable -> cable.energy.rawCapacity()).sum();
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    private int extractFromNetwork(int requested, boolean simulate) {
        int remaining = Math.min(Math.max(0, requested), transferRate());
        int extracted = 0;
        for (EnergyCableBlockEntity cable : collectNetwork()) {
            int available = Math.min(remaining, cable.energy.rawStored());
            if (!simulate && available > 0) cable.energy.extractInternal(available);
            extracted += available;
            remaining -= available;
            if (remaining <= 0) break;
        }
        return extracted;
    }

    private final class CableEnergyStorage extends EnergyStorage {
        CableEnergyStorage(int capacity) { super(capacity, ADVANCED_RATE, ADVANCED_RATE); }
        int extractInternal(int amount) {
            int moved = super.extractEnergy(Math.max(0, amount), false);
            if (moved > 0) setChanged();
            return moved;
        }
        int receiveInternal(int amount) {
            int moved = super.receiveEnergy(Math.max(0, amount), false);
            if (moved > 0) setChanged();
            return moved;
        }
        int rawStored() { return super.getEnergyStored(); }
        int rawCapacity() { return capacity; }
        void setStored(int amount) { energy = Math.max(0, Math.min(capacity, amount)); }
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int moved = super.receiveEnergy(Math.min(amount, transferRate()), simulate);
            if (moved > 0 && !simulate) setChanged(); return moved;
        }
        @Override public int extractEnergy(int amount, boolean simulate) {
            int moved = extractFromNetwork(amount, simulate);
            if (moved > 0 && !simulate) setChanged(); return moved;
        }
        @Override public int getEnergyStored() { return networkEnergyStored(); }
        @Override public int getMaxEnergyStored() { return networkCapacity(); }
    }
}
