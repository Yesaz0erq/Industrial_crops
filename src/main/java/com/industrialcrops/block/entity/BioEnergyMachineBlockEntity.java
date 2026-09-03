package com.industrialcrops.block.entity;

import com.industrialcrops.basic_pipe.PipeTransferUtil;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.BioEnergyMenu;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public abstract class BioEnergyMachineBlockEntity extends BlockEntity implements MenuProvider {
    public enum Kind { GENERATOR, BATTERY, INCINERATOR }
    public enum EnergySideMode { OUTPUT, INPUT, NONE }
    public enum FuelTier {
        NONE(0, 0), SEED(10_000, 50), CROP(40_000, 100), BLOCK_CROP(160_000, 250);
        final int energy;
        final int residue;
        FuelTier(int energy, int residue) { this.energy = energy; this.residue = residue; }
    }

    public static final int PROCESS_TICKS = 160;
    public static final int RESIDUE_CAPACITY = 1_000;
    public static final int GENERATOR_CAPACITY = 1_000_000;
    public static final int BATTERY_CAPACITY = 10_000_000;
    private static final int TRANSFER_RATE = 50_000;
    private static final int ALL_SIDES = (1 << Direction.values().length) - 1;
    private static final int MAX_NETWORK_NODES = 512;
    public static final int GENERATOR_UPGRADE_START = 1;
    public static final int UPGRADE_SLOT_COUNT = 4;

    private final Kind kind;
    private final TrackedEnergyStorage energy;
    private final ItemStackHandler inventory;
    private int progress;
    private int residue;
    private int burnTime;
    private int burnTimeTotal;
    private int energyOutputSides = ALL_SIDES;
    private int energyInputSides;
    private final IEnergyStorage[] sidedEnergy = new IEnergyStorage[Direction.values().length];
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> energy.getEnergyStored() >>> 16;
                case 2 -> energy.getMaxEnergyStored() & 0xFFFF;
                case 3 -> energy.getMaxEnergyStored() >>> 16;
                case 4 -> progress;
                case 5 -> PROCESS_TICKS;
                case 6 -> kind == Kind.INCINERATOR && level != null && !level.isClientSide()
                        ? displayResidue() : residue;
                case 7 -> RESIDUE_CAPACITY;
                case 8 -> burnTime;
                case 9 -> burnTimeTotal;
                case 10 -> kind.ordinal();
                case 11 -> currentTier().energy;
                case 12 -> energyOutputSides;
                case 13 -> energyInputSides;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> energy.setStored((energy.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
                case 1 -> energy.setStored((energy.getEnergyStored() & 0x0000FFFF) | ((value & 0xFFFF) << 16));
                case 4 -> progress = value;
                case 6 -> residue = value;
                case 8 -> burnTime = value;
                case 9 -> burnTimeTotal = value;
                case 12 -> energyOutputSides = value & ALL_SIDES;
                case 13 -> energyInputSides = value & ALL_SIDES;
                default -> { }
            }
        }
        @Override public int getCount() { return 14; }
    };

    protected BioEnergyMachineBlockEntity(BlockEntityType<?> type, Kind kind, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.kind = kind;
        int capacity = kind == Kind.BATTERY ? BATTERY_CAPACITY : kind == Kind.GENERATOR ? GENERATOR_CAPACITY : 0;
        int receive = kind == Kind.BATTERY ? TRANSFER_RATE : 0;
        int extract = kind == Kind.INCINERATOR ? 0 : TRANSFER_RATE;
        energy = new TrackedEnergyStorage(capacity, receive, extract);
        for (Direction direction : Direction.values()) {
            sidedEnergy[direction.ordinal()] = new DirectionalEnergyStorage(direction);
        }
        int slots = kind == Kind.BATTERY ? 0 : kind == Kind.GENERATOR ? 1 + UPGRADE_SLOT_COUNT : 1;
        inventory = new ItemStackHandler(slots) {
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return kind == Kind.GENERATOR && slot >= GENERATOR_UPGRADE_START
                        ? SpeedUpgradeHelper.isSpeedUpgrade(stack)
                        : BioEnergyMachineBlockEntity.this.isItemValid(stack);
            }
            @Override protected void onContentsChanged(int slot) { setChanged(); }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BioEnergyMachineBlockEntity machine) {
        if (level.isClientSide()) return;
        switch (machine.kind) {
            case GENERATOR -> machine.tickGenerator();
            case BATTERY -> machine.pushEnergyToNeighbors();
            case INCINERATOR -> machine.tickIncinerator();
        }
    }

    private void tickGenerator() {
        pushEnergyToNeighbors();
        ItemStack input = inventory.getStackInSlot(0);
        FuelTier tier = classifyFuel(input);
        boolean canRun = tier != FuelTier.NONE
                && residue < RESIDUE_CAPACITY
                && energy.getMaxEnergyStored() - energy.getEnergyStored() >= tier.energy;
        if (!canRun) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        progress += SpeedUpgradeHelper.progressStep(inventory, GENERATOR_UPGRADE_START,
                kind == Kind.GENERATOR ? UPGRADE_SLOT_COUNT : 0, PROCESS_TICKS);
        if (progress >= PROCESS_TICKS) {
            inventory.extractItem(0, 1, false);
            energy.receiveInternal(tier.energy);
            residue = Math.min(RESIDUE_CAPACITY, residue + tier.residue);
            progress = 0;
        }
        setChanged();
    }

    private void tickIncinerator() {
        BioEnergyMachineBlockEntity generator = findGeneratorWithResidue();
        if (generator == null) return;
        if (burnTime <= 0 && !consumeFuel()) return;
        if (generator.removeResidue(1) > 0) {
            burnTime--;
            setChanged();
        }
    }

    private boolean consumeFuel() {
        ItemStack stack = inventory.getStackInSlot(0);
        int fuelTicks = stack.getBurnTime(null);
        if (fuelTicks <= 0) return false;
        ItemStack remainder = stack.getCraftingRemainingItem();
        stack.shrink(1);
        if (stack.isEmpty() && !remainder.isEmpty()) inventory.setStackInSlot(0, remainder);
        burnTime = fuelTicks;
        burnTimeTotal = fuelTicks;
        setChanged();
        return true;
    }

    private void pushEnergyToNeighbors() {
        if (level == null || energy.getEnergyStored() <= 0) return;
        for (Direction direction : Direction.values()) {
            if (!isEnergyOutputEnabled(direction)) continue;
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    worldPosition.relative(direction), direction.getOpposite());
            if (target == null || !target.canReceive()) continue;
            int offered = Math.min(TRANSFER_RATE, energy.getEnergyStored());
            int accepted = target.receiveEnergy(energy.extractEnergy(offered, true), false);
            if (accepted > 0) energy.extractEnergy(accepted, false);
            if (energy.getEnergyStored() <= 0) break;
        }
    }

    private @Nullable BioEnergyMachineBlockEntity findGeneratorWithResidue() {
        if (level == null) return null;
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(worldPosition);
        visited.add(worldPosition);
        while (!queue.isEmpty() && visited.size() <= MAX_NETWORK_NODES) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!visited.add(next)) continue;
                BlockEntity entity = level.getBlockEntity(next);
                if (entity instanceof BioEnergyMachineBlockEntity machine
                        && machine.kind == Kind.GENERATOR && machine.residue > 0) return machine;
                if (PipeTransferUtil.isPipe(level.getBlockState(next))) queue.addLast(next);
            }
        }
        return null;
    }

    public static FuelTier classifyFuel(ItemStack stack) {
        if (stack.isEmpty()) return FuelTier.NONE;
        if (isBlockCrop(stack)) return FuelTier.BLOCK_CROP;
        if (stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) return FuelTier.SEED;
        if (isCrop(stack)) return FuelTier.CROP;
        return FuelTier.NONE;
    }

    private static boolean isBlockCrop(ItemStack stack) {
        return stack.is(Items.PUMPKIN) || stack.is(Items.MELON) || stack.is(Items.HAY_BLOCK)
                || stack.is(Items.DRIED_KELP_BLOCK) || stack.is(Items.NETHER_WART_BLOCK)
                || stack.is(ModItems.INDUSTRIAL_CARROT_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_POTATO_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_WHEAT_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_MELON_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_PUMPKIN_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_MELON.get())
                || stack.is(ModItems.INDUSTRIAL_PUMPKIN.get());
    }

    private static boolean isCrop(ItemStack stack) {
        if (stack.is(ModItems.INDUSTRIAL_CARROT.get()) || stack.is(ModItems.INDUSTRIAL_POTATO.get())
                || stack.is(ModItems.INDUSTRIAL_WHEAT.get())) return true;
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().is(BlockTags.SAPLINGS)) return true;
        return stack.is(Items.WHEAT) || stack.is(Items.CARROT) || stack.is(Items.POTATO)
                || stack.is(Items.BEETROOT) || stack.is(Items.MELON_SLICE)
                || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES)
                || stack.is(Items.COCOA_BEANS) || stack.is(Items.NETHER_WART)
                || stack.is(Items.SUGAR_CANE) || stack.is(Items.CACTUS) || stack.is(Items.BAMBOO)
                || stack.is(Items.KELP) || stack.is(Items.BROWN_MUSHROOM) || stack.is(Items.RED_MUSHROOM)
                || stack.is(Items.CHORUS_FRUIT) || stack.is(Items.APPLE)
                || stack.is(Items.AZALEA) || stack.is(Items.FLOWERING_AZALEA);
    }

    private boolean isItemValid(ItemStack stack) {
        return kind == Kind.GENERATOR ? classifyFuel(stack) != FuelTier.NONE
                : kind == Kind.INCINERATOR && stack.getBurnTime(null) > 0;
    }

    public int removeResidue(int amount) {
        int removed = Math.min(Math.max(0, amount), residue);
        if (removed > 0) { residue -= removed; setChanged(); }
        return removed;
    }
    private int displayResidue() {
        if (kind != Kind.INCINERATOR) return residue;
        BioEnergyMachineBlockEntity generator = findGeneratorWithResidue();
        return generator == null ? 0 : generator.residue;
    }
    public Kind getKind() { return kind; }
    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }
    public FuelTier currentTier() { return kind == Kind.GENERATOR ? classifyFuel(inventory.getStackInSlot(0)) : FuelTier.NONE; }
    public boolean toggleEnergyOutput(Direction direction) {
        energyOutputSides ^= 1 << direction.ordinal();
        setChanged();
        return isEnergyOutputEnabled(direction);
    }
    public void setAllEnergyOutputs(boolean enabled) {
        energyOutputSides = enabled ? ALL_SIDES : 0;
        if (kind == Kind.BATTERY) energyInputSides = 0;
        setChanged();
    }
    public EnergySideMode cycleEnergySide(Direction direction) {
        if (kind != Kind.BATTERY) {
            return toggleEnergyOutput(direction) ? EnergySideMode.OUTPUT : EnergySideMode.NONE;
        }
        EnergySideMode next = switch (getEnergySideMode(direction)) {
            case OUTPUT -> EnergySideMode.INPUT;
            case INPUT -> EnergySideMode.NONE;
            case NONE -> EnergySideMode.OUTPUT;
        };
        setEnergySideMode(direction, next);
        return next;
    }
    private void setEnergySideMode(Direction direction, EnergySideMode mode) {
        int bit = 1 << direction.ordinal();
        energyOutputSides &= ~bit;
        energyInputSides &= ~bit;
        if (mode == EnergySideMode.OUTPUT) energyOutputSides |= bit;
        else if (mode == EnergySideMode.INPUT) energyInputSides |= bit;
        setChanged();
    }
    public EnergySideMode getEnergySideMode(Direction direction) {
        int bit = 1 << direction.ordinal();
        if ((energyOutputSides & bit) != 0) return EnergySideMode.OUTPUT;
        if (kind == Kind.BATTERY && (energyInputSides & bit) != 0) return EnergySideMode.INPUT;
        return EnergySideMode.NONE;
    }
    public boolean isEnergyOutputEnabled(Direction direction) {
        return (energyOutputSides & (1 << direction.ordinal())) != 0;
    }
    public @Nullable IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        if (kind == Kind.INCINERATOR) return null;
        return side == null ? energy : sidedEnergy[side.ordinal()];
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putInt("Residue", residue);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putInt("EnergyOutputSides", energyOutputSides);
        tag.putInt("EnergyInputSides", energyInputSides);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
            int expected = kind == Kind.BATTERY ? 0 : kind == Kind.GENERATOR ? 1 + UPGRADE_SLOT_COUNT : 1;
            MachineInventoryHelper.ensureSize(inventory, expected);
        }
        energy.setStored(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        residue = Math.max(0, Math.min(RESIDUE_CAPACITY, tag.getInt("Residue")));
        burnTime = Math.max(0, tag.getInt("BurnTime"));
        burnTimeTotal = Math.max(0, tag.getInt("BurnTimeTotal"));
        energyOutputSides = tag.contains("EnergyOutputSides") ? tag.getInt("EnergyOutputSides") & ALL_SIDES : ALL_SIDES;
        energyInputSides = kind == Kind.BATTERY && tag.contains("EnergyInputSides")
                ? tag.getInt("EnergyInputSides") & ALL_SIDES & ~energyOutputSides : 0;
    }
    @Override public Component getDisplayName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BioEnergyMenu(id, inventory, this, worldPosition);
    }

    private final class TrackedEnergyStorage extends EnergyStorage {
        private TrackedEnergyStorage(int capacity, int receive, int extract) { super(capacity, receive, extract); }
        private void setStored(int amount) { energy = Math.max(0, Math.min(capacity, amount)); }
        private void receiveInternal(int amount) { energy = Math.min(capacity, energy + Math.max(0, amount)); }
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int received = super.receiveEnergy(amount, simulate);
            if (received > 0 && !simulate) setChanged();
            return received;
        }
        @Override public int extractEnergy(int amount, boolean simulate) {
            int extracted = super.extractEnergy(amount, simulate);
            if (extracted > 0 && !simulate) setChanged();
            return extracted;
        }
    }

    private final class DirectionalEnergyStorage implements IEnergyStorage {
        private final Direction side;

        private DirectionalEnergyStorage(Direction side) { this.side = side; }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            return canReceive() ? energy.receiveEnergy(maxReceive, simulate) : 0;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            return canExtract() ? energy.extractEnergy(maxExtract, simulate) : 0;
        }
        @Override public int getEnergyStored() { return energy.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return energy.getMaxEnergyStored(); }
        @Override public boolean canExtract() {
            return getEnergySideMode(side) == EnergySideMode.OUTPUT && energy.canExtract();
        }
        @Override public boolean canReceive() {
            return kind == Kind.BATTERY && getEnergySideMode(side) == EnergySideMode.INPUT && energy.canReceive();
        }
    }
}
