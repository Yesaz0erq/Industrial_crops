package com.industrialcrops.block.entity;

import com.industrialcrops.block.WirelessEnergyTransmitterBlock;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.WirelessEnergyTransmitterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.*;

public final class WirelessEnergyTransmitterBlockEntity extends BlockEntity implements MenuProvider, GeoBlockEntity {
    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int TRANSFER_RATE = 100_000;
    public static final int DATA_COUNT = 8;
    private static final Direction[] INPUT_SIDES = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.wireless_crystal_transmitter.idle");
    private static final RawAnimation WORKING = RawAnimation.begin().thenLoop("animation.wireless_crystal_transmitter.working");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final InputEnergyStorage energy = new InputEnergyStorage();
    private final List<BlockPos> targets = new ArrayList<>();
    private int rangeRadius;
    private int rescanTicks;
    private int nextTarget;
    private int sentLastTick;
    private int activeTicks;

    public WirelessEnergyTransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIRELESS_ENERGY_TRANSMITTER.get(), pos, state);
    }

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> energy.getEnergyStored() >>> 16;
                case 2 -> rangeRadius;
                case 3 -> targets.size() & 0xffff;
                case 4 -> targets.size() >>> 16;
                case 5 -> sentLastTick & 0xffff;
                case 6 -> sentLastTick >>> 16;
                case 7 -> isWorking() ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) { }
        @Override public int getCount() { return DATA_COUNT; }
    };

    public static void tick(Level level, BlockPos pos, BlockState state, WirelessEnergyTransmitterBlockEntity machine) {
        if (!(level instanceof ServerLevel server)) return;
        Set<BlockPos> suppliers = machine.pullInputs(server);
        if (machine.rescanTicks-- <= 0) {
            machine.rebuildTargets(server);
            machine.rescanTicks = 39;
        }
        machine.sentLastTick = machine.distribute(server, suppliers);
        if (machine.sentLastTick > 0) machine.activeTicks = 8;
        else if (machine.activeTicks > 0) machine.activeTicks--;
        boolean active = machine.activeTicks > 0;
        if (state.getValue(WirelessEnergyTransmitterBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(WirelessEnergyTransmitterBlock.ACTIVE, active), 3);
        }
    }

    private Set<BlockPos> pullInputs(ServerLevel server) {
        Set<BlockPos> suppliers = new HashSet<>();
        int budget = Math.min(TRANSFER_RATE, ENERGY_CAPACITY - energy.getEnergyStored());
        for (Direction direction : INPUT_SIDES) {
            BlockPos pos = worldPosition.relative(direction);
            if (!isLoaded(server, pos)) continue;
            IEnergyStorage source = energyAt(server, pos, direction.getOpposite());
            if (source == null && !hasSidedEnergy(server, pos)) source = energyAt(server, pos, null);
            if (source == null || !source.canExtract()) continue;
            // Never wirelessly return energy to the adjacent supply, even when our buffer is full.
            suppliers.add(pos);
            if (budget <= 0) continue;
            int available = Math.min(budget, Math.max(0, source.extractEnergy(budget, true)));
            if (available <= 0) continue;
            int extracted = Math.min(available, Math.max(0, source.extractEnergy(available, false)));
            budget -= energy.receiveEnergy(extracted, false);
        }
        return suppliers;
    }

    private void rebuildTargets(ServerLevel server) {
        targets.clear();
        int chunkX = worldPosition.getX() >> 4, chunkZ = worldPosition.getZ() >> 4;
        for (int x = chunkX - rangeRadius; x <= chunkX + rangeRadius; x++) {
            for (int z = chunkZ - rangeRadius; z <= chunkZ + rangeRadius; z++) {
                LevelChunk chunk = server.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockEntity entity : List.copyOf(chunk.getBlockEntities().values())) {
                    if (entity.isRemoved() || entity instanceof WirelessEnergyTransmitterBlockEntity
                            || entity instanceof EnergyCableBlockEntity) continue;
                    if (receiver(server, entity.getBlockPos(), 0) != null) targets.add(entity.getBlockPos().immutable());
                }
            }
        }
        targets.sort(Comparator.comparingLong(BlockPos::asLong));
        if (!targets.isEmpty()) nextTarget = Math.floorMod(nextTarget, targets.size());
    }

    private int distribute(ServerLevel server, Set<BlockPos> suppliers) {
        int remaining = Math.min(TRANSFER_RATE, energy.getEnergyStored()), initial = remaining;
        int count = targets.size();
        if (remaining == 0 || count == 0) return 0;
        int start = Math.floorMod(nextTarget, count);
        for (int n = 0; n < count && remaining > 0; n++) {
            BlockPos pos = targets.get((start + n) % count);
            if (suppliers.contains(pos) || !isLoaded(server, pos)) continue;
            // Resolve capabilities afresh: removed/replaced machines and side configuration changes are safe.
            BlockEntity current = server.getBlockEntity(pos);
            if (current == null || current.isRemoved() || current instanceof WirelessEnergyTransmitterBlockEntity
                    || current instanceof EnergyCableBlockEntity) continue;
            int offer = Math.max(1, (remaining + count - n - 1) / (count - n));
            IEnergyStorage target = receiver(server, pos, offer);
            if (target == null) continue;
            int accepted = Math.min(offer, Math.max(0, target.receiveEnergy(offer, false)));
            energy.consume(accepted);
            remaining -= accepted;
        }
        nextTarget = (start + 1) % count;
        return initial - remaining;
    }

    private static @Nullable IEnergyStorage receiver(ServerLevel level, BlockPos pos, int amount) {
        boolean hasSidedCapability = false;
        for (Direction side : Direction.values()) {
            IEnergyStorage storage = energyAt(level, pos, side);
            hasSidedCapability |= storage != null;
            if (canReceive(storage, amount)) return storage;
        }
        // An unsided aggregate may bypass a machine's disabled faces. Use it only for unsided-only providers.
        if (hasSidedCapability) return null;
        IEnergyStorage storage = energyAt(level, pos, null);
        return canReceive(storage, amount) ? storage : null;
    }
    private static boolean hasSidedEnergy(ServerLevel level, BlockPos pos) {
        for (Direction side : Direction.values())
            if (energyAt(level, pos, side) != null) return true;
        return false;
    }
    private static boolean canReceive(@Nullable IEnergyStorage storage, int amount) {
        return storage != null && storage.canReceive() && (amount == 0 || storage.receiveEnergy(amount, true) > 0);
    }
    private static boolean isLoaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    private static @Nullable IEnergyStorage energyAt(ServerLevel level, BlockPos pos, @Nullable Direction side) {
        return com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ENERGY, pos, side);
    }
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == ForgeCapabilities.ENERGY && side != Direction.UP ? energyCapability.cast() : super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); energyCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); energyCapability = LazyOptional.of(() -> energy); }
    public @Nullable IEnergyStorage getEnergyStorage(@Nullable Direction side) { return side == Direction.UP ? null : energy; }
    public int getRangeRadius() { return rangeRadius; }
    public int getSentLastTick() { return sentLastTick; }
    public int getTargetCount() { return targets.size(); }
    public ContainerData getData() { return data; }
    public void setRangeRadius(int radius) {
        if (radius < 0 || radius > 1 || (level != null && level.isClientSide())) return;
        if (rangeRadius != radius) {
            rangeRadius = radius;
            targets.clear();
            rescanTicks = 0;
            nextTarget = 0;
            setChanged();
        }
    }
    public boolean isWorking() { return getBlockState().getValue(WirelessEnergyTransmitterBlock.ACTIVE); }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.wireless_energy_transmitter"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new WirelessEnergyTransmitterMenu(id, inventory, this);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("RangeRadius", rangeRadius);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        energy.setStored(tag.getInt("Energy"));
        rangeRadius = Math.max(0, Math.min(1, tag.getInt("RangeRadius")));
        targets.clear(); rescanTicks = 0; activeTicks = 0;
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "operation", 5,
                state -> state.setAndContinue(isWorking() ? WORKING : IDLE)));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    private final class InputEnergyStorage extends EnergyStorage {
        private InputEnergyStorage() { super(ENERGY_CAPACITY, TRANSFER_RATE, 0); }
        private void setStored(int value) { energy = Math.max(0, Math.min(capacity, value)); }
        private void consume(int amount) { if (amount > 0) { energy -= amount; setChanged(); } }
        @Override public int receiveEnergy(int amount, boolean simulate) {
            if (amount <= 0) return 0;
            int accepted = super.receiveEnergy(amount, simulate);
            if (!simulate && accepted > 0) setChanged();
            return accepted;
        }
    }
}
