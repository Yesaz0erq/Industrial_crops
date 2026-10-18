package com.industrialcrops.block.entity;

import com.industrialcrops.item.ExpandableStorageComponentItem;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Expandable Storage Device.
 *
 * <p>Modelled on Beyond Dimensions' unified storage: each item type occupies exactly one storage slot
 * and the device enforces a per-type capacity instead of a per-stack one. Installing Expandable
 * storage components (the 10K Storage Component) both unlocks item-type slots and raises the
 * per-type capacity to the best installed component (9,999 for the 10K component).
 *
 * <p>The block reuses the Advanced Industrial Storage screen (search, sorting, 3x3 crafting, pipes,
 * remote access) but has no fluid tank.
 */
public class ExpandableStorageBlockEntity extends AdvancedIndustrialStorageBlockEntity {
    public ExpandableStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXPANDABLE_STORAGE_DEVICE.get(), pos, state);
    }

    @Override
    public boolean acceptsStorageCell(ItemStack stack) {
        return stack.getItem() instanceof ExpandableStorageComponentItem;
    }

    @Override
    protected int storageCellMultiplier(ItemStack stack) {
        return stack.getItem() instanceof ExpandableStorageComponentItem component ? component.itemTypes() : 0;
    }

    @Override
    public int getUnlockedStorageSlots() {
        return unlockedTypesWithout(-1);
    }

    @Override
    public int getStorageSlotLimit() {
        return perTypeLimitWithout(-1);
    }

    @Override
    public ItemStack getCellTabIcon() {
        return new ItemStack(ModItems.STORAGE_COMPONENT_10K.get());
    }

    /**
     * One slot per item type: a stack may go into a slot that already holds the same item, or into an
     * empty slot only when no other unlocked slot holds that item yet.
     */
    @Override
    public boolean canInsertIntoSlot(int slot, ItemStack stack) {
        if (stack.isEmpty()) return true;
        ItemStack current = getStorageStack(slot);
        if (!current.isEmpty()) {
            return ItemStack.isSameItemSameTags(current, stack);
        }
        int existing = findSlotOfType(stack);
        return existing < 0 || existing == slot;
    }

    // ---- item type -> slot index (rebuilt lazily after any change) ----

    private final Map<Integer, List<Integer>> typeIndex = new HashMap<>();
    private boolean typeIndexDirty = true;

    @Override
    public void setChanged() {
        typeIndexDirty = true;
        super.setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        typeIndexDirty = true;
    }

    /** Slot that already holds this item type, or -1. */
    public int findSlotOfType(ItemStack stack) {
        if (typeIndexDirty) {
            typeIndex.clear();
            int unlocked = getUnlockedStorageSlots();
            for (int slot = 0; slot < unlocked; slot++) {
                ItemStack stored = getStorageStack(slot);
                if (!stored.isEmpty()) {
                    typeIndex.computeIfAbsent(java.util.Objects.hash(stored.getItem(), stored.getTag()), key -> new ArrayList<>()).add(slot);
                }
            }
            typeIndexDirty = false;
        }
        List<Integer> candidates = typeIndex.get(java.util.Objects.hash(stack.getItem(), stack.getTag()));
        if (candidates != null) {
            for (int slot : candidates) {
                if (ItemStack.isSameItemSameTags(getStorageStack(slot), stack)) return slot;
            }
        }
        return -1;
    }

    /**
     * A component can only be taken out if the remaining components still cover every stored item
     * type and every stored amount.
     */
    @Override
    public boolean canRemoveStorageCell(int cellSlot) {
        if (cellSlot < 0 || cellSlot >= CELL_SLOT_COUNT) return false;
        if (getCellInventory().getStackInSlot(cellSlot).isEmpty()) return true;
        int typesAfter = unlockedTypesWithout(cellSlot);
        int limitAfter = perTypeLimitWithout(cellSlot);
        for (int slot = 0; slot < STORAGE_SLOT_COUNT; slot++) {
            ItemStack stored = getStorageStack(slot);
            if (stored.isEmpty()) continue;
            if (slot >= typesAfter || stored.getCount() > limitAfter) return false;
        }
        return true;
    }

    private int unlockedTypesWithout(int skippedCell) {
        int types = 0;
        for (int cell = 0; cell < CELL_SLOT_COUNT; cell++) {
            if (cell == skippedCell) continue;
            types += storageCellMultiplier(getCellInventory().getStackInSlot(cell));
        }
        return Math.min(STORAGE_SLOT_COUNT, types);
    }

    private int perTypeLimitWithout(int skippedCell) {
        int limit = 0;
        for (int cell = 0; cell < CELL_SLOT_COUNT; cell++) {
            if (cell == skippedCell) continue;
            if (getCellInventory().getStackInSlot(cell).getItem() instanceof ExpandableStorageComponentItem component) {
                limit = Math.max(limit, component.perTypeLimit());
            }
        }
        return limit;
    }
}
