package com.industrialcrops.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Sort a visible storage page without sending bulk stacks through the cursor. */
public final class ControllerStorageSorting {
    public static final int BUTTON_SORT = 90;
    public static final int BUTTON_RESET_LOCKS = 91;
    public static final int BUTTON_LOCK_BASE = 10_000;

    private ControllerStorageSorting() {}

    public static boolean updateLocks(AbstractContainerMenu menu, int id) {
        if (menu.slots.isEmpty() || !(menu.slots.get(0).container instanceof ControllerStorageContainer storage)) return false;
        if (id == BUTTON_RESET_LOCKS) {
            storage.clearSortingLocks();
            return true;
        }
        if (id >= BUTTON_LOCK_BASE && id < BUTTON_LOCK_BASE + storage.getContainerSize()) {
            storage.lockForSorting(id - BUTTON_LOCK_BASE);
            return true;
        }
        return false;
    }

    public static boolean sort(AbstractContainerMenu menu, Player player) {
        if (!menu.getCarried().isEmpty() || !menu.stillValid(player)) return false;
        List<Slot> targets = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (!(slot instanceof ControllerStorageSlot) || !slot.mayPickup(player)) continue;
            if (((ControllerStorageContainer) slot.container).isSortingLocked(slot.getSlotIndex())) continue;
            targets.add(slot);
            ItemStack remaining = slot.getItem().copy();
            if (remaining.isEmpty()) continue;
            for (ItemStack existing : stacks) {
                if (!ItemStack.isSameItemSameTags(existing, remaining)) continue;
                int moved = Math.min(Integer.MAX_VALUE - existing.getCount(), remaining.getCount());
                existing.grow(moved);
                remaining.shrink(moved);
                if (remaining.isEmpty()) break;
            }
            if (!remaining.isEmpty()) stacks.add(remaining);
        }
        stacks.sort(Comparator.comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .thenComparing(stack -> stack.getHoverName().getString())
                .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed()));
        for (int i = 0; i < targets.size(); i++) {
            targets.get(i).set(i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
        }
        menu.broadcastChanges();
        return true;
    }
}
