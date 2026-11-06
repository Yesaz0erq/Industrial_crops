package com.industrialcrops.block.entity;

import com.industrialcrops.basic_pipe.PipeTransferUtil;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.screen.BioEnergyMenu;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.machine.BulkStorageItemHandler;
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
    /**
     * Biomass fuel. Total energy scales with the crops an item represents (one crop = 50 FE), so a 10K
     * compressed crop is worth exactly 10,000 crops. Denser fuel also burns at a higher FE/t and leaves
     * proportionally less residue. Rates are per lane at base speed in the Generation Device.
     */
    public enum FuelTier {
        NONE(0, 1, 0),
        SEED(25, 5, 1),
        CROP(50, 5, 1),
        BLOCK_CROP(3_200, 80, 32),
        COMPRESSED_10K(500_000, 1_000, 500);
        final int energy;
        final int ticks;
        final int residue;
        FuelTier(int energy, int ticks, int residue) { this.energy = energy; this.ticks = ticks; this.residue = residue; }
        public int energy() { return energy; }
        public int residue() { return residue; }
        public int ticks() { return ticks; }
        /** FE per tick while one item of this fuel burns at base speed. */
        public int rate() { return energy / ticks; }
    }

    /** Energy of one 10K compressed crop in the Generation Device: the unit AP cables are rated in. */
    public static final int TEN_K_ENERGY = 500_000;
    public static final int RESIDUE_CAPACITY = 10_000;
    public static final int GENERATOR_CAPACITY = 1_000_000;
    public static final int REACTOR_CAPACITY = 10_000_000;
    public static final int BATTERY_CAPACITY = 20_000_000;
    public static final int GENERATOR_TRANSFER = 50_000;
    public static final int REACTOR_TRANSFER = 500_000;
    public static final int BATTERY_TRANSFER = 100_000;
    /** The reactor turns the same biomass into three times the energy and half the residue. */
    public static final int REACTOR_EFFICIENCY = 3;
    /** Fuel lanes burning in parallel, each holding up to 999 items. */
    public static final int REACTOR_LANES = 6;
    public static final int REACTOR_SLOT_LIMIT = 999;
    /** Residue an incinerator destroys per tick of burning fuel. */
    public static final int INCINERATOR_RATE = 10;
    private static final int ALL_SIDES = (1 << Direction.values().length) - 1;
    private static final int MAX_NETWORK_NODES = 512;
    public static final int GENERATOR_UPGRADE_START = 1;
    public static final int UPGRADE_SLOT_COUNT = 4;
    public static final int DATA_COUNT = 16 + REACTOR_LANES;

    private final Kind kind;
    private final boolean reactor;
    private final TrackedEnergyStorage energy;
    private final ItemStackHandler inventory;
    private final int[] laneLeft;
    private final int[] laneTotal;
    private final int[] laneRate;
    private int currentRate;
    private int residue;
    private int burnTime;
    private int burnTimeTotal;
    private int energyOutputSides = ALL_SIDES;
    private int energyInputSides;
    private final IEnergyStorage[] sidedEnergy = new IEnergyStorage[Direction.values().length];
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            if (index >= 16) {
                int lane = index - 16;
                return lane < laneLeft.length && laneTotal[lane] > 0
                        ? (laneTotal[lane] - laneLeft[lane]) * 1000 / laneTotal[lane] : 0;
            }
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> energy.getEnergyStored() >>> 16;
                case 2 -> energy.getMaxEnergyStored() & 0xFFFF;
                case 3 -> energy.getMaxEnergyStored() >>> 16;
                case 4 -> laneTotal.length > 0 ? laneTotal[0] - laneLeft[0] : 0;
                case 5 -> laneTotal.length > 0 ? laneTotal[0] : 0;
                case 6 -> kind == Kind.INCINERATOR && level != null && !level.isClientSide()
                        ? displayResidue() : residue;
                case 7 -> RESIDUE_CAPACITY;
                case 8 -> burnTime;
                case 9 -> burnTimeTotal;
                case 10 -> kind.ordinal();
                case 11 -> currentRate & 0xFFFF;
                case 12 -> energyOutputSides;
                case 13 -> energyInputSides;
                case 14 -> currentRate >>> 16;
                case 15 -> reactor ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> energy.setStored((energy.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
                case 1 -> energy.setStored((energy.getEnergyStored() & 0x0000FFFF) | ((value & 0xFFFF) << 16));
                case 6 -> residue = value;
                case 8 -> burnTime = value;
                case 9 -> burnTimeTotal = value;
                case 12 -> energyOutputSides = value & ALL_SIDES;
                case 13 -> energyInputSides = value & ALL_SIDES;
                default -> { }
            }
        }
        @Override public int getCount() { return DATA_COUNT; }
    };

    protected BioEnergyMachineBlockEntity(BlockEntityType<?> type, Kind kind, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.kind = kind;
        this.reactor = kind == Kind.GENERATOR && state.is(ModBlocks.BIO_ENERGY_REACTOR.get());
        int capacity = kind == Kind.BATTERY ? BATTERY_CAPACITY
                : kind == Kind.GENERATOR ? (reactor ? REACTOR_CAPACITY : GENERATOR_CAPACITY) : 0;
        int rate = kind == Kind.BATTERY ? BATTERY_TRANSFER : reactor ? REACTOR_TRANSFER : GENERATOR_TRANSFER;
        int receive = kind == Kind.BATTERY ? rate : 0;
        int extract = kind == Kind.INCINERATOR ? 0 : rate;
        energy = new TrackedEnergyStorage(capacity, receive, extract);
        for (Direction direction : Direction.values()) {
            sidedEnergy[direction.ordinal()] = new DirectionalEnergyStorage(direction);
        }
        int lanes = kind == Kind.GENERATOR ? (reactor ? REACTOR_LANES : 1) : 0;
        laneLeft = new int[lanes];
        laneTotal = new int[lanes];
        laneRate = new int[lanes];
        inventory = new BulkStorageItemHandler(slotCount()) {
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return kind == Kind.GENERATOR && slot >= upgradeStart()
                        ? SpeedUpgradeHelper.isSpeedUpgrade(stack)
                        : BioEnergyMachineBlockEntity.this.isItemValid(stack);
            }
            @Override public int getSlotLimit(int slot) {
                return reactor && slot < REACTOR_LANES ? REACTOR_SLOT_LIMIT : super.getSlotLimit(slot);
            }
            @Override protected int getStackLimit(int slot, ItemStack stack) {
                if (reactor && slot < REACTOR_LANES && stack.getMaxStackSize() > 1) return REACTOR_SLOT_LIMIT;
                return super.getStackLimit(slot, stack);
            }
            @Override protected void onContentsChanged(int slot) { setChanged(); }
        };
    }

    private int slotCount() {
        return switch (kind) {
            case BATTERY -> 0;
            case INCINERATOR -> 1;
            case GENERATOR -> fuelSlots() + UPGRADE_SLOT_COUNT;
        };
    }

    /** Fuel slots: one for the generation device, six parallel lanes for the reactor. */
    public int fuelSlots() {
        return kind == Kind.GENERATOR ? (reactor ? REACTOR_LANES : 1) : (kind == Kind.INCINERATOR ? 1 : 0);
    }

    public int upgradeStart() {
        return fuelSlots();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BioEnergyMachineBlockEntity machine) {
        if (level.isClientSide()) return;
        switch (machine.kind) {
            case GENERATOR -> machine.tickGenerator();
            case BATTERY -> machine.pushEnergyToNeighbors();
            case INCINERATOR -> {
                boolean working = machine.tickIncinerator();
                var lit = com.industrialcrops.block.ResidueIncineratorBlock.LIT;
                if (state.getValue(lit) != working) level.setBlock(pos, state.setValue(lit, working), 3);
            }
        }
    }

    /** Speed components burn fuel 2/4/8/16 times faster; the FE per item stays the same. */
    private int speedMultiplier() {
        return switch (SpeedUpgradeHelper.tier(inventory, upgradeStart(), UPGRADE_SLOT_COUNT)) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            case 4 -> 16;
            default -> 1;
        };
    }

    private int residueFor(FuelTier tier) {
        return reactor ? (tier.residue + 1) / 2 : tier.residue;
    }

    private void tickGenerator() {
        pushEnergyToNeighbors();
        int multiplier = speedMultiplier();
        int produced = 0;
        boolean changed = false;
        for (int lane = 0; lane < laneLeft.length; lane++) {
            if (laneLeft[lane] <= 0) {
                laneTotal[lane] = 0;
                FuelTier tier = classifyFuel(inventory.getStackInSlot(lane));
                if (tier == FuelTier.NONE) continue;
                int waste = residueFor(tier);
                if (residue + waste > RESIDUE_CAPACITY) continue;
                inventory.extractItem(lane, 1, false);
                residue += waste;
                laneLeft[lane] = tier.ticks;
                laneTotal[lane] = tier.ticks;
                laneRate[lane] = tier.rate() * (reactor ? REACTOR_EFFICIENCY : 1);
                changed = true;
            }
            int steps = Math.min(multiplier, laneLeft[lane]);
            long gain = (long) laneRate[lane] * steps;
            // Never waste fuel: a lane pauses while the buffer cannot take this tick's output.
            if (gain > energy.getMaxEnergyStored() - energy.getEnergyStored()) continue;
            energy.receiveInternal((int) gain);
            laneLeft[lane] -= steps;
            produced += (int) gain;
            changed = true;
        }
        if (currentRate != produced) {
            currentRate = produced;
            changed = true;
        }
        if (changed) setChanged();
    }

    private boolean tickIncinerator() {
        BioEnergyMachineBlockEntity generator = findGeneratorWithResidue();
        if (generator == null) return false;
        if (burnTime <= 0 && !consumeFuel()) return false;
        if (generator.removeResidue(INCINERATOR_RATE) > 0) {
            burnTime--;
            setChanged();
            return true;
        }
        return false;
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
            int offered = Math.min(energy.maxExtractRate(), energy.getEnergyStored());
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
        if (stack.is(ModItems.COMPRESSED_POTATO_10K.get()) || stack.is(ModItems.COMPRESSED_CARROT_10K.get())
                || stack.is(ModItems.COMPRESSED_WHEAT_10K.get())) return FuelTier.COMPRESSED_10K;
        if (isBlockCrop(stack)) return FuelTier.BLOCK_CROP;
        if (stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) || isModSeed(stack)) return FuelTier.SEED;
        if (isCrop(stack)) return FuelTier.CROP;
        return FuelTier.NONE;
    }

    private static boolean isBlockCrop(ItemStack stack) {
        return stack.is(Items.PUMPKIN) || stack.is(Items.MELON) || stack.is(Items.HAY_BLOCK)
                || stack.is(Items.DRIED_KELP_BLOCK) || stack.is(Items.NETHER_WART_BLOCK)
                || stack.is(ModItems.INDUSTRIAL_CARROT_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_POTATO_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_WHEAT_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_MELON.get())
                || stack.is(ModItems.INDUSTRIAL_PUMPKIN.get());
    }

    private static boolean isCrop(ItemStack stack) {
        if (stack.is(ModItems.INDUSTRIAL_CARROT.get()) || stack.is(ModItems.INDUSTRIAL_POTATO.get())
                || stack.is(ModItems.INDUSTRIAL_WHEAT.get()) || stack.is(ModItems.PRISM_POD.get())
                || stack.is(ModItems.EMBERCOIL.get()) || stack.is(ModItems.STARBLOOM.get())
                || stack.is(ModItems.NEONBULB.get()) || stack.is(ModItems.FLUXSTALK.get())) return true;
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

    private static boolean isModSeed(ItemStack stack) {
        return stack.is(ModItems.PRISM_POD_SEEDS.get()) || stack.is(ModItems.EMBERCOIL_SEEDS.get())
                || stack.is(ModItems.STARBLOOM_SEEDS.get()) || stack.is(ModItems.NEONBULB_SEEDS.get())
                || stack.is(ModItems.FLUXSTALK_SEEDS.get());
    }

    /** Energy one item yields here: the fuel value, tripled in the reactor. */
    public int generationEnergy(FuelTier tier) {
        return reactor ? Math.multiplyExact(tier.energy, REACTOR_EFFICIENCY) : tier.energy;
    }

    public boolean isReactor() {
        return reactor;
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
    /** Transfers runtime state when the gold generator is upgraded in place. */
    public void copyUpgradeStateTo(BioEnergyMachineBlockEntity target) {
        // Fuel moves lane-to-lane; speed components move to the target's own upgrade slots.
        for (int slot = 0; slot < fuelSlots() && slot < target.fuelSlots(); slot++) {
            target.inventory.setStackInSlot(slot, inventory.getStackInSlot(slot).copy());
        }
        for (int slot = 0; slot < UPGRADE_SLOT_COUNT; slot++) {
            int from = upgradeStart() + slot, to = target.upgradeStart() + slot;
            if (from < inventory.getSlots() && to < target.inventory.getSlots()) {
                target.inventory.setStackInSlot(to, inventory.getStackInSlot(from).copy());
            }
        }
        target.energy.setStored(energy.getEnergyStored());
        for (int lane = 0; lane < laneLeft.length && lane < target.laneLeft.length; lane++) {
            target.laneLeft[lane] = laneLeft[lane];
            target.laneTotal[lane] = laneTotal[lane];
            target.laneRate[lane] = laneLeft[lane] > 0 ? laneRate[lane] * (target.reactor && !reactor ? REACTOR_EFFICIENCY : 1) : 0;
        }
        target.residue = residue;
        target.burnTime = burnTime;
        target.burnTimeTotal = burnTimeTotal;
        target.energyOutputSides = energyOutputSides;
        target.energyInputSides = energyInputSides;
        target.setChanged();
    }
    public ContainerData getData() { return data; }
    public FuelTier currentTier() { return kind == Kind.GENERATOR ? classifyFuel(inventory.getStackInSlot(0)) : FuelTier.NONE; }
    public int currentRate() { return currentRate; }
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
        tag.putIntArray("LaneLeft", laneLeft);
        tag.putIntArray("LaneTotal", laneTotal);
        tag.putIntArray("LaneRate", laneRate);
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
            if (reactor && inventory.getSlots() == 1 + UPGRADE_SLOT_COUNT) {
                // Reactors saved before the six-lane layout: fuel stays in lane 0, components move up.
                ItemStack[] old = new ItemStack[inventory.getSlots()];
                for (int slot = 0; slot < old.length; slot++) old[slot] = inventory.getStackInSlot(slot).copy();
                inventory.setSize(slotCount());
                inventory.setStackInSlot(0, old[0]);
                for (int slot = 0; slot < UPGRADE_SLOT_COUNT; slot++) inventory.setStackInSlot(upgradeStart() + slot, old[1 + slot]);
            }
            MachineInventoryHelper.ensureSize(inventory, slotCount());
        }
        energy.setStored(tag.getInt("Energy"));
        int[] left = tag.getIntArray("LaneLeft"), total = tag.getIntArray("LaneTotal"), rate = tag.getIntArray("LaneRate");
        for (int lane = 0; lane < laneLeft.length; lane++) {
            laneLeft[lane] = lane < left.length ? Math.max(0, left[lane]) : 0;
            laneTotal[lane] = lane < total.length ? Math.max(0, total[lane]) : 0;
            laneRate[lane] = lane < rate.length ? Math.max(0, rate[lane]) : 0;
        }
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
        private int maxExtractRate() { return Math.max(maxExtract, 1); }
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
