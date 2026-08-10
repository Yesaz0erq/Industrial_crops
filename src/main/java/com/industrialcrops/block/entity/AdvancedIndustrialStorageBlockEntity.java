package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.item.IndustrialStorageComponentItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class AdvancedIndustrialStorageBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CELL_SLOT_COUNT = 6;
    public static final int SLOTS_PER_CELL = 54;
    public static final int MAX_CELL_MULTIPLIER = 16;
    public static final int STORAGE_SLOT_COUNT = CELL_SLOT_COUNT * SLOTS_PER_CELL * MAX_CELL_MULTIPLIER;

    private final ItemStackHandler cellInventory = new ItemStackHandler(CELL_SLOT_COUNT) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isStorageCell(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler storageInventory = new ItemStackHandler(STORAGE_SLOT_COUNT) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isStorageSlotUnlocked(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!isStorageSlotUnlocked(slot)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isStorageSlotUnlocked(slot)) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IItemHandler basic_pipeItemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return STORAGE_SLOT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return isValidStorageSlot(slot) ? storageInventory.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!isValidStorageSlot(slot) || !isStorageSlotUnlocked(slot)) {
                return stack;
            }
            return storageInventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isValidStorageSlot(slot) || !isStorageSlotUnlocked(slot)) {
                return ItemStack.EMPTY;
            }
            return storageInventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return isStorageSlotUnlocked(slot) ? Integer.MAX_VALUE : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isStorageSlotUnlocked(slot);
        }
    };

    public AdvancedIndustrialStorageBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), pos, state);
    }

    protected AdvancedIndustrialStorageBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static boolean isStorageCell(ItemStack stack) {
        return stack.getItem() instanceof IndustrialStorageComponentItem;
    }

    public static int getStorageCellMultiplier(ItemStack stack) {
        return stack.getItem() instanceof IndustrialStorageComponentItem component
                ? component.capacityMultiplier() : 0;
    }

    public ItemStackHandler getCellInventory() {
        return cellInventory;
    }

    public IItemHandler getPipeItemHandler() {
        return basic_pipeItemHandler;
    }

    public int getUnlockedStorageSlots() {
        int capacityUnits = 0;
        for (int slot = 0; slot < CELL_SLOT_COUNT; slot++) {
            capacityUnits += getStorageCellMultiplier(cellInventory.getStackInSlot(slot));
        }
        return Math.min(STORAGE_SLOT_COUNT, capacityUnits * SLOTS_PER_CELL);
    }

    public boolean isStorageSlotUnlocked(int slot) {
        return isValidStorageSlot(slot) && slot < getUnlockedStorageSlots();
    }

    public boolean canRemoveStorageCell(int cellSlot) {
        if (cellSlot < 0 || cellSlot >= CELL_SLOT_COUNT) return false;
        int removedSlots = getStorageCellMultiplier(cellInventory.getStackInSlot(cellSlot)) * SLOTS_PER_CELL;
        int unlockedAfterRemove = Math.max(0, getUnlockedStorageSlots() - removedSlots);
        for (int slot = unlockedAfterRemove; slot < STORAGE_SLOT_COUNT; slot++) {
            if (!storageInventory.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public ItemStack getStorageStack(int slot) {
        return isValidStorageSlot(slot) ? storageInventory.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    public ItemStack extractFromStorageSlot(int slot, int amount) {
        if (!isStorageSlotUnlocked(slot)) {
            return ItemStack.EMPTY;
        }
        return storageInventory.extractItem(slot, amount, false);
    }

    public int insertIntoStorageSlot(int slot, ItemStack stack) {
        if (!isStorageSlotUnlocked(slot) || stack.isEmpty()) {
            return 0;
        }
        int original = stack.getCount();
        ItemStack remainder = storageInventory.insertItem(slot, stack, false);
        return original - remainder.getCount();
    }

    public int insertIntoStorage(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int original = stack.getCount();
        ItemStack remaining = stack.copy();
        int unlocked = getUnlockedStorageSlots();
        for (int slot = 0; slot < unlocked && !remaining.isEmpty(); slot++) {
            remaining = storageInventory.insertItem(slot, remaining, false);
        }
        return original - remaining.getCount();
    }

    public ItemStack createDroppedStack(Block block) {
        ItemStack stack = new ItemStack(block.asItem());
        CompoundTag storageData = writeInventories(level == null ? null : level.registryAccess());
        if (!storageData.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(storageData));
        }
        return stack;
    }

    public void readStorageFromStack(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && level != null) {
            readInventories(customData.copyTag(), level.registryAccess());
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("StorageData", writeInventories(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StorageData")) {
            readInventories(tag.getCompound("StorageData"), registries);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.industrialcrops.screen.AdvancedIndustrialStorageMenu(containerId, inventory, this, worldPosition);
    }

    private CompoundTag writeInventories(@Nullable HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("Cells", cellInventory.serializeNBT(registries));
        tag.put("Storage", storageInventory.serializeNBT(registries));
        return tag;
    }

    private void readInventories(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("Cells")) {
            readFixedSizeInventory(cellInventory, CELL_SLOT_COUNT, tag.getCompound("Cells"), registries);
        }
        if (tag.contains("Storage")) {
            readFixedSizeInventory(storageInventory, STORAGE_SLOT_COUNT, tag.getCompound("Storage"), registries);
        }
    }

    private static void readFixedSizeInventory(ItemStackHandler target, int expectedSize, CompoundTag tag, HolderLookup.Provider registries) {
        ItemStackHandler temporary = new ItemStackHandler();
        temporary.deserializeNBT(registries, tag);
        target.setSize(expectedSize);
        int slots = Math.min(expectedSize, temporary.getSlots());
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = temporary.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                target.setStackInSlot(slot, stack);
            }
        }
    }

    private static boolean isValidStorageSlot(int slot) {
        return slot >= 0 && slot < STORAGE_SLOT_COUNT;
    }
}
