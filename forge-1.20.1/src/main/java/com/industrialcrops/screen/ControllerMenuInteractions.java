package com.industrialcrops.screen;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class ControllerMenuInteractions {
    private ControllerMenuInteractions() {}

    static boolean handle(AbstractContainerMenu menu, int index, int button, ClickType type, Player player) {
        Slot slot = index >= 0 && index < menu.slots.size() ? menu.slots.get(index) : null;
        if (type == ClickType.PICKUP_ALL && !menu.getCarried().isEmpty()) {
            ItemStack carried = menu.getCarried();
            for (int i = 0; i < menu.slots.size() && carried.getCount() < carried.getMaxStackSize(); i++) {
                Slot candidate = menu.slots.get(button == 1 ? menu.slots.size() - 1 - i : i);
                if (candidate.isActive() && candidate.mayPickup(player)
                        && menu.canTakeItemForPickAll(carried, candidate)
                        && ItemStack.isSameItemSameTags(candidate.getItem(), carried)) {
                    ItemStack taken = candidate.remove(carried.getMaxStackSize() - carried.getCount());
                    carried.grow(taken.getCount());
                    if (!taken.isEmpty()) candidate.onTake(player, taken);
                }
            }
            menu.broadcastChanges();
            return true;
        }
        if (!(slot instanceof ControllerStorageSlot)) return false;
        if (!slot.mayPickup(player)) return true;
        if (type == ClickType.QUICK_MOVE) {
            if (button == 0 || button == 1) menu.quickMoveStack(player, index);
            return true;
        }
        ItemStack stored = slot.getItem();
        if (stored.isEmpty() || stored.getCount() <= stored.getMaxStackSize()) return false;
        if (type == ClickType.SWAP) {
            if ((button >= 0 && button < 9) || button == 40) {
                ItemStack hotbar = player.getInventory().getItem(button);
                if (hotbar.isEmpty()) {
                    ItemStack taken = slot.remove(stored.getMaxStackSize());
                    player.getInventory().setItem(button, taken);
                    slot.onTake(player, taken);
                } else if (ItemStack.isSameItemSameTags(stored, hotbar)) {
                    int amount = Math.min(hotbar.getCount(), Integer.MAX_VALUE - stored.getCount());
                    stored.grow(amount);
                    hotbar.shrink(amount);
                    slot.setChanged();
                    player.getInventory().setChanged();
                }
            }
            menu.broadcastChanges();
            return true;
        }
        // A different item cannot swap an entire bulk stack onto a normal cursor.
        return type == ClickType.PICKUP && !menu.getCarried().isEmpty()
                && !ItemStack.isSameItemSameTags(stored, menu.getCarried());
    }
}
