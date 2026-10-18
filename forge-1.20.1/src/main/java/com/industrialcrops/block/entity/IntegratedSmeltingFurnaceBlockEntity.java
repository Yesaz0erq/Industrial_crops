package com.industrialcrops.block.entity;

import com.industrialcrops.block.IntegratedSmeltingFurnaceBlock;
import com.industrialcrops.machine.BulkStorageItemHandler;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.IntegratedSmeltingFurnaceMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Integrated Smelting Furnace (AP steel): a chest-sized inventory that smelts its own contents when
 * powered. Up to 9 stacks smelt in parallel; results stay inside, marked as finished so they are not
 * smelted again and so pipes can pull them out.
 *
 * <p>Storage tiers come from an Industrial Storage Component in the storage slot:
 * none = 3x9, I = 4x9, II = 5x9, III = 5x9 at 99 per slot, IV = 5x9 at 999 per slot.
 */
public final class IntegratedSmeltingFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int COLUMNS = 9;
    public static final int MAX_ROWS = 6;
    public static final int MAX_SLOTS = COLUMNS * MAX_ROWS;
    public static final int SPEED_SLOTS = 4;
    public static final int STORAGE_UPGRADE_SLOT = 4;
    public static final int PROCESS_TICKS = 100;
    public static final int LANES = 9;
    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int RECEIVE_RATE = 20_000;
    public static final int ENERGY_PER_LANE_TICK = 20;
    private static final int[] TIER_ROWS = {3, 4, 5, 6, 6};
    private static final int[] TIER_LIMIT = {64, 64, 64, 99, 999};

    private final TrackedEnergyStorage energy = new TrackedEnergyStorage();
    /** Smelt modes: once, twice, or keep re-smelting until the result cannot be smelted further. */
    public static final int MODE_ONCE = 0, MODE_TWICE = 1, MODE_FULL = 2;
    private static final int[] MODE_PASSES = {1, 2, 16};

    private final boolean[] finished = new boolean[MAX_SLOTS];
    /** How many times the items in each slot have already been smelted. */
    private final byte[] passes = new byte[MAX_SLOTS];
    private int mode = MODE_ONCE;
    private final Map<Item, Optional<ItemStack>> recipeCache = new HashMap<>();
    private int progress;
    private int lanes;

    private final BulkStorageItemHandler storage = new BulkStorageItemHandler(MAX_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return slot < activeSlots() ? slotLimit() : 0;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return stackLimit(slot, stack);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < activeSlots();
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (getStackInSlot(slot).isEmpty()) {
                finished[slot] = false;
                passes[slot] = 0;
            }
            setChanged();
        }
    };

    private final ItemStackHandler upgrades = new ItemStackHandler(SPEED_SLOTS + 1) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < SPEED_SLOTS ? SpeedUpgradeHelper.isSpeedUpgrade(stack) : storageTierOf(stack) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Pipes: insert into any open, unfinished slot; extract smelted results and anything that cannot be smelted. */
    private final IItemHandler automation = new IItemHandler() {
        @Override public int getSlots() { return MAX_SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) { return storage.getStackInSlot(slot); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot >= activeSlots() || finished[slot]) return stack;
            return storage.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= activeSlots()) return ItemStack.EMPTY;
            ItemStack stack = storage.getStackInSlot(slot);
            if (stack.isEmpty() || !finished[slot] && smeltResult(stack).isPresent()) return ItemStack.EMPTY;
            return storage.extractItem(slot, Math.min(amount, stack.getMaxStackSize()), simulate);
        }

        @Override public int getSlotLimit(int slot) { return storage.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot < activeSlots() && !finished[slot]; }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> energy.getEnergyStored() >>> 16;
                case 2 -> progress;
                case 3 -> lanes;
                case 4 -> mode;
                case 5, 6, 7, 8 -> finishedChunk(index - 5);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy.setStored((energy.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
                case 1 -> energy.setStored((energy.getEnergyStored() & 0xFFFF) | ((value & 0xFFFF) << 16));
                case 2 -> progress = value;
                case 3 -> lanes = value;
                case 4 -> mode = Math.max(0, Math.min(MODE_FULL, value));
                case 5, 6, 7, 8 -> setFinishedChunk(index - 5, value);
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    /** Finished flags travel to the client in 14-bit chunks (container data is sent as shorts). */
    private int finishedChunk(int chunk) {
        int value = 0;
        for (int bit = 0; bit < 14; bit++) {
            int slot = chunk * 14 + bit;
            if (slot < MAX_SLOTS && finished[slot]) value |= 1 << bit;
        }
        return value;
    }

    private void setFinishedChunk(int chunk, int value) {
        for (int bit = 0; bit < 14; bit++) {
            int slot = chunk * 14 + bit;
            if (slot < MAX_SLOTS) finished[slot] = (value & (1 << bit)) != 0;
        }
    }

    public IntegratedSmeltingFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INTEGRATED_SMELTING_FURNACE.get(), pos, state);
    }

    // ------------------------------------------------------------------ tiers

    public static int storageTierOf(ItemStack stack) {
        if (stack.is(ModItems.INDUSTRIAL_STORAGE_COMPONENT_1.get())) return 1;
        if (stack.is(ModItems.INDUSTRIAL_STORAGE_COMPONENT_2.get())) return 2;
        if (stack.is(ModItems.INDUSTRIAL_STORAGE_COMPONENT_3.get())) return 3;
        if (stack.is(ModItems.INDUSTRIAL_STORAGE_COMPONENT_4.get())) return 4;
        return 0;
    }

    public int tier() {
        return storageTierOf(upgrades.getStackInSlot(STORAGE_UPGRADE_SLOT));
    }

    public static int rowsForTier(int tier) {
        return TIER_ROWS[Math.max(0, Math.min(4, tier))];
    }

    public static int limitForTier(int tier) {
        return TIER_LIMIT[Math.max(0, Math.min(4, tier))];
    }

    public int activeSlots() {
        return rowsForTier(tier()) * COLUMNS;
    }

    public int slotLimit() {
        return limitForTier(tier());
    }

    private int stackLimit(int slot, ItemStack stack) {
        if (slot >= activeSlots()) return 0;
        return stackLimitFor(tier(), stack);
    }

    /** Per-slot cap for a stack at a tier: vanilla stack size up to 64, larger tiers lift it for stackable items. */
    public static int stackLimitFor(int tier, ItemStack stack) {
        int limit = limitForTier(tier);
        if (stack.getMaxStackSize() <= 1) return 1;
        return limit <= 64 ? Math.min(limit, stack.getMaxStackSize()) : limit;
    }

    /** True if the contents would still fit after switching to {@code newTier}. */
    public boolean canSwitchTier(int newTier) {
        int keep = rowsForTier(newTier) * COLUMNS;
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            ItemStack stack = storage.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (slot >= keep || stack.getCount() > stackLimitFor(newTier, stack)) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ smelting

    public static void tick(Level level, BlockPos pos, BlockState state, IntegratedSmeltingFurnaceBlockEntity furnace) {
        if (level.isClientSide()) return;
        furnace.serverTick(level, pos, state);
    }

    private void serverTick(Level level, BlockPos pos, BlockState state) {
        PoweredMachineSupport.pullEnergy(level, pos, energy, RECEIVE_RATE);
        if (level.getGameTime() % 1200 == 0) recipeCache.clear();

        List<Integer> work = pickLanes();
        boolean working = false;
        if (work.isEmpty()) {
            if (progress != 0 || lanes != 0) {
                progress = 0;
                lanes = 0;
                setChanged();
            }
        } else {
            lanes = work.size();
            int step = SpeedUpgradeHelper.progressStep(upgrades, 0, SPEED_SLOTS, PROCESS_TICKS);
            int advance = Math.min(step, PROCESS_TICKS - progress);
            int cost = ENERGY_PER_LANE_TICK * advance * work.size();
            if (energy.getEnergyStored() >= cost) {
                energy.consume(cost);
                progress += advance;
                working = true;
                if (progress >= PROCESS_TICKS) {
                    for (int slot : work) smeltOne(slot);
                    progress = 0;
                }
                setChanged();
            }
        }
        if (state.hasProperty(IntegratedSmeltingFurnaceBlock.LIT) && state.getValue(IntegratedSmeltingFurnaceBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(IntegratedSmeltingFurnaceBlock.LIT, working), 3);
        }
    }

    /** Up to 9 slots holding smeltable, unfinished items whose result has room. */
    private List<Integer> pickLanes() {
        List<Integer> work = new ArrayList<>();
        int active = activeSlots();
        for (int slot = 0; slot < active && work.size() < LANES; slot++) {
            ItemStack stack = storage.getStackInSlot(slot);
            if (stack.isEmpty() || finished[slot]) continue;
            Optional<ItemStack> result = smeltResult(stack);
            if (result.isPresent() && roomFor(result.get(), slot, stack.getCount() == 1)) work.add(slot);
        }
        return work;
    }

    public Optional<ItemStack> smeltResult(ItemStack input) {
        if (level == null || input.isEmpty()) return Optional.empty();
        return recipeCache.computeIfAbsent(input.getItem(), item -> {
            SimpleContainer recipeInput = new SimpleContainer(input.copyWithCount(1));
            SmeltingRecipe holder = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, recipeInput, level).orElse(null);
            if (holder == null) return Optional.empty();
            ItemStack result = holder.assemble(recipeInput, level.registryAccess());
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        }).map(ItemStack::copy);
    }

    /** Whether a result made from {@code sourceSlot} is done, given the smelt mode and whether it can be smelted again. */
    private boolean resultIsFinal(ItemStack result, int nextPasses) {
        return nextPasses >= MODE_PASSES[mode] || smeltResult(result).isEmpty();
    }

    private boolean accepts(int slot, ItemStack result, boolean isFinal, int nextPasses) {
        ItemStack stack = storage.getStackInSlot(slot);
        return finished[slot] == isFinal && (isFinal || passes[slot] == nextPasses)
                && ItemStack.isSameItemSameTags(stack, result)
                && stack.getCount() + result.getCount() <= stackLimitFor(tier(), stack);
    }

    private boolean roomFor(ItemStack result, int sourceSlot, boolean sourceEmpties) {
        int nextPasses = passes[sourceSlot] + 1;
        boolean isFinal = resultIsFinal(result, nextPasses);
        int active = activeSlots();
        for (int slot = 0; slot < active; slot++) {
            ItemStack stack = storage.getStackInSlot(slot);
            if (stack.isEmpty() || slot == sourceSlot && sourceEmpties) return true;
            if (accepts(slot, result, isFinal, nextPasses)) return true;
        }
        return false;
    }

    private void smeltOne(int slot) {
        ItemStack input = storage.getStackInSlot(slot);
        if (input.isEmpty() || finished[slot]) return;
        Optional<ItemStack> result = smeltResult(input);
        if (result.isEmpty() || !roomFor(result.get(), slot, input.getCount() == 1)) return;
        int nextPasses = passes[slot] + 1;
        boolean isFinal = resultIsFinal(result.get(), nextPasses);
        input.shrink(1);
        storage.setStackInSlot(slot, input.isEmpty() ? ItemStack.EMPTY : input);
        insertResult(result.get(), isFinal, nextPasses);
    }

    private void insertResult(ItemStack result, boolean isFinal, int nextPasses) {
        int active = activeSlots();
        for (int slot = 0; slot < active && !result.isEmpty(); slot++) {
            if (!accepts(slot, result, isFinal, nextPasses)) continue;
            ItemStack stack = storage.getStackInSlot(slot);
            stack.grow(result.getCount());
            result = ItemStack.EMPTY;
            storage.setStackInSlot(slot, stack);
        }
        for (int slot = 0; slot < active && !result.isEmpty(); slot++) {
            if (!storage.getStackInSlot(slot).isEmpty()) continue;
            storage.setStackInSlot(slot, result.copy());
            finished[slot] = isFinal;
            passes[slot] = (byte) Math.min(127, nextPasses);
            result = ItemStack.EMPTY;
        }
    }

    public int mode() { return mode; }

    /** Switch smelt mode; finished items that the new mode would smelt further go back into the queue, and vice versa. */
    public void setMode(int newMode) {
        newMode = Math.max(0, Math.min(MODE_FULL, newMode));
        if (newMode == mode) return;
        mode = newMode;
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            ItemStack stack = storage.getStackInSlot(slot);
            if (stack.isEmpty() || passes[slot] == 0) continue;
            boolean done = passes[slot] >= MODE_PASSES[mode] || smeltResult(stack).isEmpty();
            finished[slot] = done;
        }
        setChanged();
    }

    public boolean isFinished(int slot) {
        return slot >= 0 && slot < MAX_SLOTS && finished[slot];
    }

    // ------------------------------------------------------------------ access

    public BulkStorageItemHandler getStorage() { return storage; }
    public ItemStackHandler getUpgrades() { return upgrades; }
    public IItemHandler getAutomationHandler() { return automation; }
    public EnergyStorage getEnergyStorage(@Nullable Direction side) { return energy; }
    public ContainerData getData() { return data; }

    public List<ItemStack> collectDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            ItemStack stack = storage.getStackInSlot(slot);
            while (!stack.isEmpty()) {
                int take = Math.min(stack.getCount(), stack.getMaxStackSize());
                drops.add(stack.copyWithCount(take));
                stack = stack.copyWithCount(stack.getCount() - take);
            }
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (!upgrades.getStackInSlot(slot).isEmpty()) drops.add(upgrades.getStackInSlot(slot).copy());
        }
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Storage", storage.serializeNBT());
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        long mask = 0L;
        for (int slot = 0; slot < MAX_SLOTS; slot++) if (finished[slot]) mask |= 1L << slot;
        tag.putLong("Finished", mask);
        tag.putByteArray("Passes", passes.clone());
        tag.putInt("Mode", mode);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Upgrades")) upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        if (tag.contains("Storage")) {
            // Older saves were written with fewer slots; never let the stored size shrink the grid.
            CompoundTag stored = tag.getCompound("Storage").copy();
            stored.putInt("Size", MAX_SLOTS);
            storage.deserializeNBT(stored);
        }
        energy.setStored(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        long mask = tag.getLong("Finished");
        for (int slot = 0; slot < MAX_SLOTS; slot++) finished[slot] = (mask & (1L << slot)) != 0;
        byte[] storedPasses = tag.getByteArray("Passes");
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            passes[slot] = slot < storedPasses.length ? storedPasses[slot] : (byte) (finished[slot] ? 1 : 0);
        }
        mode = Math.max(0, Math.min(MODE_FULL, tag.getInt("Mode")));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.integrated_smelting_furnace");
    }

    /** Opens the furnace screen sized to the rows the current storage upgrade unlocks. */
    public void openMenu(net.minecraft.server.level.ServerPlayer player) {
        int rows = rowsForTier(tier());
        net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, this, buffer -> {
            buffer.writeBlockPos(worldPosition);
            buffer.writeVarInt(rows);
        });
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IntegratedSmeltingFurnaceMenu(containerId, playerInventory, this, worldPosition);
    }

    private static final class TrackedEnergyStorage extends EnergyStorage {
        private TrackedEnergyStorage() { super(ENERGY_CAPACITY, RECEIVE_RATE, 0); }
        private void setStored(int value) { energy = Math.max(0, Math.min(capacity, value)); }
        private void consume(int value) { energy = Math.max(0, energy - value); }
    }
}
