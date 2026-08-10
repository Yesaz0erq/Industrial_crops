package com.industrialcrops.block.entity;

import com.industrialcrops.block.TransportPipeBlock;
import com.industrialcrops.screen.MatterMachineMenu;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import com.industrialcrops.registry.ModEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public abstract class MatterMachineBlockEntity extends BlockEntity implements MenuProvider {
    public enum Kind { DIGITIZER, COPIER, RECONSTRUCTOR }

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int DIGITIZER_MAIN_SLOT_COUNT = 27;
    public static final int UPGRADE_SLOT_COUNT = 4;
    public static final int DIGITIZER_SLOT_COUNT = DIGITIZER_MAIN_SLOT_COUNT + UPGRADE_SLOT_COUNT;
    public static final int ENERGY_CAPACITY = 10_000;
    public static final int DEFAULT_PROCESS_TICKS = 160;
    private static final int MAX_RECEIVE = 10_000;
    private static final int ALL_SIDES = (1 << Direction.values().length) - 1;

    private final Kind kind;
    private int progress;
    private boolean operationRequested;
    private int selectedNetworkIndex = -1;
    private int energyInputSides = ALL_SIDES;
    // Several menu/data queries can ask for the terminal during one server tick.
    // Memoizing only for that tick avoids repeated BFS scans without delaying
    // topology changes across ticks.
    private long terminalLookupTick = Long.MIN_VALUE;
    private @Nullable BlockPos cachedTerminalPos;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE);
    private final ItemStackHandler inventory;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> energy.getEnergyStored() >>> 16;
                case 2 -> progress;
                case 3 -> DEFAULT_PROCESS_TICKS;
                case 4 -> kind.ordinal();
                case 5 -> energyInputSides;
                case 6 -> hasConnectedTerminal() ? 1 : 0;
                case 7 -> canStartOperation() ? 1 : 0;
                case 8 -> operationRequested ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy.setStored((energy.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
                case 1 -> energy.setStored((energy.getEnergyStored() & 0x0000FFFF) | ((value & 0xFFFF) << 16));
                case 2 -> progress = value;
                case 5 -> energyInputSides = value & ALL_SIDES;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    protected MatterMachineBlockEntity(BlockEntityType<?> type, Kind kind, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.kind = kind;
        this.inventory = new ItemStackHandler(kind == Kind.DIGITIZER ? DIGITIZER_SLOT_COUNT : 2 + UPGRADE_SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return MatterMachineBlockEntity.this.isItemValid(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MatterMachineBlockEntity machine) {
        if (!level.isClientSide()) machine.serverTick();
    }

    private void serverTick() {
        if (!operationRequested) {
            pullEnergyFromNeighbors();
            return;
        }

        ItemNetworkTerminalBlockEntity terminal = findConnectedTerminal();
        if (terminal == null || !canContinueOperation(terminal)) {
            resetOperation();
            return;
        }

        int perTick = energyPerTick();
        if (!consumeEnergyPreferExternal(perTick)) return;
        progress += SpeedUpgradeHelper.progressStep(inventory, getUpgradeSlotStart(), UPGRADE_SLOT_COUNT,
                DEFAULT_PROCESS_TICKS);
        if (progress >= DEFAULT_PROCESS_TICKS) {
            if (kind == Kind.DIGITIZER) uploadDigitizerContents(terminal);
            else {
                terminal.completeOperation(this, selectedNetworkIndex);
                if (terminal.count(selectedNetworkIndex) <= 0) selectedNetworkIndex = -1;
            }
            resetOperation();
        }
        setChanged();
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        int upgradeStart = getUpgradeSlotStart();
        if (slot >= upgradeStart && slot < upgradeStart + UPGRADE_SLOT_COUNT) return isMachineUpgrade(stack);
        if (kind != Kind.DIGITIZER || slot < 0 || slot >= DIGITIZER_MAIN_SLOT_COUNT) return false;
        return stack.is(ModItems.INDUSTRIAL_CARROT.get())
                || stack.is(com.industrialcrops.registry.CarroteItems.CARROTE_STEEL_INGOT.get())
                || stack.is(com.industrialcrops.registry.CarroteItems.STABLE_MATTER_INGOT.get())
                || net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft");
    }

    public int getUpgradeSlotStart() { return kind == Kind.DIGITIZER ? DIGITIZER_MAIN_SLOT_COUNT : 2; }

    public static boolean isMachineUpgrade(ItemStack stack) {
        return stack.is(ModItems.POWER_COMPONENT.get())
                || stack.is(ModItems.RAPID_FIRE_COMPONENT.get())
                || stack.is(ModItems.GOLD_UPGRADE_KIT.get())
                || SpeedUpgradeHelper.isSpeedUpgrade(stack);
    }

    public boolean requestOperation() {
        if (!canStartOperation()) return false;
        // Copying creates one additional data entry and deliberately drains a
        // fully charged copier up front. The other two machines pay per tick.
        if (kind == Kind.COPIER && !consumeEnergyPreferExternal(ENERGY_CAPACITY)) return false;
        operationRequested = true;
        progress = 0;
        setChanged();
        return true;
    }

    /** Selects a terminal entry and starts the matching operation as one server-side action. */
    public boolean requestNetworkOperation(int absoluteIndex) {
        return selectNetworkEntry(absoluteIndex) && requestOperation();
    }

    public boolean performNetworkOperation(Player player) {
        if (kind == Kind.DIGITIZER && hasIndustrialCarrot()) return triggerCarroteFault(player);
        return requestOperation();
    }

    public boolean canStartOperation() {
        if (operationRequested) return false;
        if (kind == Kind.DIGITIZER && hasIndustrialCarrot()) return true;
        if (findConnectedTerminal() == null) return false;
        if (kind == Kind.DIGITIZER) return hasDigitizerInput() && availableEnergy(energyPerTick()) >= energyPerTick();
        ItemNetworkTerminalBlockEntity terminal = findConnectedTerminal();
        return terminal != null && terminal.canOperateWith(this, selectedNetworkIndex)
                && (kind == Kind.COPIER
                    ? availableEnergy(ENERGY_CAPACITY) >= ENERGY_CAPACITY
                    : availableEnergy(energyPerTick()) >= energyPerTick());
    }

    private boolean hasIndustrialCarrot() {
        if (kind != Kind.DIGITIZER) return false;
        for (int slot = 0; slot < DIGITIZER_MAIN_SLOT_COUNT; slot++) {
            if (inventory.getStackInSlot(slot).is(ModItems.INDUSTRIAL_CARROT.get())) return true;
        }
        return false;
    }

    private boolean triggerCarroteFault(Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !hasIndustrialCarrot()) return false;
        int carrots = 0;
        for (int slot = 0; slot < DIGITIZER_MAIN_SLOT_COUNT; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.is(ModItems.INDUSTRIAL_CARROT.get())) continue;
            carrots += inventory.extractItem(slot, stack.getCount(), false).getCount();
        }
        if (carrots <= 0) return false;

        player.closeContainer();
        player.setAbsorptionAmount(0.0F);
        player.setHealth(1.0F);
        player.addEffect(new MobEffectInstance(ModEffects.GLITCH, 100, 0, false, false, true));
        serverLevel.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 0.72F);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, 12, 0.35D, 0.35D, 0.35D, 0.02D);

        BlockPos dropPos = worldPosition.immutable();
        serverLevel.destroyBlock(dropPos, false, player);
        while (carrots > 0) {
            int amount = Math.min(64, carrots);
            Containers.dropItemStack(serverLevel, dropPos.getX() + 0.5D, dropPos.getY() + 0.5D,
                    dropPos.getZ() + 0.5D, new ItemStack(com.industrialcrops.registry.CarroteItems.CARROTE.get(), amount));
            carrots -= amount;
        }
        return true;
    }

    private boolean canContinueOperation(ItemNetworkTerminalBlockEntity terminal) {
        return kind == Kind.DIGITIZER ? hasDigitizerInput() : terminal.canOperateWith(this, selectedNetworkIndex);
    }

    private boolean hasDigitizerInput() {
        for (int slot = 0; slot < DIGITIZER_MAIN_SLOT_COUNT; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    private void uploadDigitizerContents(ItemNetworkTerminalBlockEntity terminal) {
        for (int slot = 0; slot < DIGITIZER_MAIN_SLOT_COUNT; slot++) {
            ItemStack stored = inventory.getStackInSlot(slot);
            if (stored.isEmpty()) continue;
            ItemStack extracted = inventory.extractItem(slot, stored.getCount(), false);
            if (extracted.is(com.industrialcrops.registry.CarroteItems.CARROTE_STEEL_INGOT.get())) {
                extracted = new ItemStack(com.industrialcrops.registry.CarroteItems.STABLE_MATTER_INGOT.get(), extracted.getCount());
            }
            terminal.upload(extracted);
        }
    }

    private void resetOperation() {
        if (progress != 0 || operationRequested) {
            progress = 0;
            operationRequested = false;
            setChanged();
        }
    }

    private int energyPerTick() {
        return switch (kind) {
            case DIGITIZER, RECONSTRUCTOR -> 5;
            case COPIER -> 0;
        };
    }

    private void pullEnergyFromNeighbors() {
        if (level == null || energy.getEnergyStored() >= energy.getMaxEnergyStored()) return;
        for (Direction direction : Direction.values()) {
            if (!isEnergyInputEnabled(direction)) continue;
            int wanted = Math.min(MAX_RECEIVE, energy.getMaxEnergyStored() - energy.getEnergyStored());
            BlockPos neighborPos = worldPosition.relative(direction);
            int transferred = pullFromCapability(neighborPos, direction.getOpposite(), wanted);
            if (transferred <= 0) {
                for (Direction candidateSide : Direction.values()) {
                    if (candidateSide == direction.getOpposite()) continue;
                    transferred = pullFromCapability(neighborPos, candidateSide, wanted);
                    if (transferred > 0) break;
                }
            }
            if (transferred <= 0) pullFromCapability(neighborPos, null, wanted);
            if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) break;
        }
    }

    private int pullFromCapability(BlockPos neighborPos, @Nullable Direction side, int wanted) {
        IEnergyStorage neighbor = level == null ? null : level.getCapability(
                Capabilities.EnergyStorage.BLOCK, neighborPos, side);
        if (neighbor == null || wanted <= 0) return 0;
        int available = neighbor.extractEnergy(wanted, true);
        int accepted = energy.receiveEnergy(available, true);
        if (accepted <= 0) return 0;
        int extracted = neighbor.extractEnergy(accepted, false);
        return energy.receiveEnergy(extracted, false);
    }

    /** Uses directly connected FE first and only spends the machine buffer for a shortfall. */
    private boolean consumeEnergyPreferExternal(int amount) {
        if (amount <= 0) return true;
        if (availableEnergy(amount) < amount) return false;
        int remaining = amount;
        for (Direction direction : Direction.values()) {
            if (!isEnergyInputEnabled(direction) || remaining <= 0) continue;
            IEnergyStorage source = findExternalSource(direction, remaining);
            if (source != null) {
                remaining -= source.extractEnergy(remaining, false);
            }
        }
        if (remaining > 0) energy.consume(remaining);
        setChanged();
        return true;
    }

    private int availableEnergy(int limit) {
        int available = Math.min(limit, energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (!isEnergyInputEnabled(direction) || available >= limit) continue;
            IEnergyStorage source = findExternalSource(direction, limit - available);
            if (source != null) {
                available += source.extractEnergy(limit - available, true);
            }
        }
        return Math.min(limit, available);
    }

    /**
     * Finds an extractable FE capability on a neighboring block. Some mods
     * expose energy only on their configured face (or only through the
     * directionless capability), so checking only the geometrically opposite
     * face rejects otherwise valid Mekanism cables and energy cubes.
     */
    private @Nullable IEnergyStorage findExternalSource(Direction neighborDirection, int wanted) {
        if (level == null || wanted <= 0) return null;
        BlockPos neighborPos = worldPosition.relative(neighborDirection);
        Direction preferred = neighborDirection.getOpposite();
        IEnergyStorage source = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, preferred);
        if (canSupply(source, wanted)) return source;
        for (Direction candidate : Direction.values()) {
            if (candidate == preferred) continue;
            source = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, candidate);
            if (canSupply(source, wanted)) return source;
        }
        source = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, null);
        return canSupply(source, wanted) ? source : null;
    }

    private static boolean canSupply(@Nullable IEnergyStorage storage, int wanted) {
        // The simulated extraction is the authoritative interoperability check;
        // a few third-party wrappers do not report canExtract consistently.
        return storage != null && storage.extractEnergy(wanted, true) > 0;
    }

    public boolean toggleEnergyInput(Direction direction) {
        int bit = 1 << direction.ordinal();
        energyInputSides ^= bit;
        setChanged();
        return isEnergyInputEnabled(direction);
    }

    public void setAllEnergyInputs(boolean enabled) {
        energyInputSides = enabled ? ALL_SIDES : 0;
        setChanged();
    }

    public boolean isEnergyInputEnabled(Direction direction) {
        return (energyInputSides & (1 << direction.ordinal())) != 0;
    }

    public int getEnergyInputSides() {
        return energyInputSides;
    }

    public boolean hasConnectedTerminal() {
        return findConnectedTerminal() != null;
    }

    public boolean selectNetworkEntry(int absoluteIndex) {
        ItemNetworkTerminalBlockEntity terminal = findConnectedTerminal();
        if (kind == Kind.DIGITIZER || terminal == null || terminal.count(absoluteIndex) <= 0) return false;
        selectedNetworkIndex = absoluteIndex;
        setChanged();
        return true;
    }

    public int getSelectedNetworkIndex() { return selectedNetworkIndex; }

    public @Nullable ItemNetworkTerminalBlockEntity findConnectedTerminal() {
        if (level == null) return null;
        long gameTime = level.getGameTime();
        if (terminalLookupTick == gameTime) {
            if (cachedTerminalPos == null) return null;
            BlockEntity cached = level.getBlockEntity(cachedTerminalPos);
            return cached instanceof ItemNetworkTerminalBlockEntity terminal ? terminal : null;
        }
        terminalLookupTick = gameTime;
        cachedTerminalPos = null;
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(worldPosition);
        visited.add(worldPosition);
        while (!queue.isEmpty() && visited.size() <= 512) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!level.hasChunkAt(next) || visited.contains(next)) continue;
                BlockEntity entity = level.getBlockEntity(next);
                if (entity instanceof ItemNetworkTerminalBlockEntity terminal) {
                    cachedTerminalPos = next.immutable();
                    return terminal;
                }
                if (level.getBlockState(next).getBlock() instanceof TransportPipeBlock) {
                    visited.add(next);
                    queue.addLast(next);
                }
            }
        }
        return null;
    }

    public ItemStackHandler getInventory() { return inventory; }
    public MachineEnergyStorage getEnergyStorage() { return energy; }
    public @Nullable MachineEnergyStorage getEnergyStorage(@Nullable Direction side) {
        return side == null || isEnergyInputEnabled(side) ? energy : null;
    }
    public ContainerData getData() { return data; }
    public Kind getKind() { return kind; }
    public boolean isOperationRequested() { return operationRequested; }

    public boolean canAcceptReconstructed(ItemStack stack) {
        if (kind != Kind.RECONSTRUCTOR || stack.isEmpty()) return false;
        if (stack.getCount() > stack.getMaxStackSize()) return false;
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, stack)
                && output.getCount() + stack.getCount() <= output.getMaxStackSize();
    }

    public int reconstructedCapacityFor(ItemStack template) {
        if (kind != Kind.RECONSTRUCTOR || template.isEmpty()) return 0;
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return template.getMaxStackSize();
        if (!ItemStack.isSameItemSameComponents(output, template)) return 0;
        return Math.max(0, output.getMaxStackSize() - output.getCount());
    }

    public boolean insertReconstructed(ItemStack stack) {
        if (!canAcceptReconstructed(stack)) return false;
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) inventory.setStackInSlot(OUTPUT_SLOT, stack.copy());
        else {
            output.grow(stack.getCount());
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putBoolean("OperationRequested", operationRequested);
        tag.putInt("SelectedNetworkIndex", selectedNetworkIndex);
        tag.putInt("EnergyInputSides", energyInputSides);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
            int expectedSlots = kind == Kind.DIGITIZER ? DIGITIZER_SLOT_COUNT : 2 + UPGRADE_SLOT_COUNT;
            if (inventory.getSlots() != expectedSlots) {
                ItemStack[] oldStacks = new ItemStack[inventory.getSlots()];
                for (int slot = 0; slot < oldStacks.length; slot++) oldStacks[slot] = inventory.getStackInSlot(slot).copy();
                inventory.setSize(expectedSlots);
                for (int slot = 0; slot < Math.min(oldStacks.length, expectedSlots); slot++) {
                    inventory.setStackInSlot(slot, oldStacks[slot]);
                }
            }
            migrateFormerDigitizerSideSlots();
        }
        energy.setStored(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        operationRequested = tag.getBoolean("OperationRequested");
        selectedNetworkIndex = tag.contains("SelectedNetworkIndex") ? tag.getInt("SelectedNetworkIndex") : -1;
        energyInputSides = tag.contains("EnergyInputSides") ? tag.getInt("EnergyInputSides") & ALL_SIDES : ALL_SIDES;
    }

    private void migrateFormerDigitizerSideSlots() {
        if (kind != Kind.DIGITIZER) return;
        for (int oldSlot = DIGITIZER_MAIN_SLOT_COUNT; oldSlot < DIGITIZER_MAIN_SLOT_COUNT + 3; oldSlot++) {
            ItemStack stack = inventory.getStackInSlot(oldSlot);
            if (stack.isEmpty() || isMachineUpgrade(stack)) continue;
            ItemStack remaining = stack.copy();
            inventory.setStackInSlot(oldSlot, ItemStack.EMPTY);
            for (int target = 0; target < DIGITIZER_MAIN_SLOT_COUNT && !remaining.isEmpty(); target++) {
                remaining = inventory.insertItem(target, remaining, false);
            }
            if (!remaining.isEmpty()) inventory.setStackInSlot(oldSlot, remaining);
        }
    }

    @Override public Component getDisplayName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MatterMachineMenu(id, inv, this, worldPosition);
    }

    public final class MachineEnergyStorage extends EnergyStorage {
        private MachineEnergyStorage(int capacity, int maxReceive) { super(capacity, maxReceive, 0); }
        private void setStored(int value) { energy = Math.max(0, Math.min(capacity, value)); }
        private void consume(int amount) { energy = Math.max(0, energy - amount); }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) MatterMachineBlockEntity.this.setChanged();
            return received;
        }
    }
}
