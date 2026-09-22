package com.industrialcrops.screen;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Storage may hold bulk quantities; a cursor or hotbar receives at most a normal stack. */
public final class ControllerStorageSlot extends Slot {
    public ControllerStorageSlot(ControllerStorageContainer container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(getSlotIndex(), stack); }
    @Override public boolean mayPickup(Player player) { return ((ControllerStorageContainer) container).isAvailable(getSlotIndex()); }
    @Override public int getMaxStackSize() { return Integer.MAX_VALUE; }
    @Override public int getMaxStackSize(ItemStack stack) { return Integer.MAX_VALUE; }
    @Override public ItemStack remove(int amount) {
        return super.remove(Math.min(amount, getItem().getMaxStackSize()));
    }
}
