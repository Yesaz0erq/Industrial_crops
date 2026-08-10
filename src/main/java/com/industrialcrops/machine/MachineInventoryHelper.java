package com.industrialcrops.machine;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MachineInventoryHelper {
    private MachineInventoryHelper() { }
    public static void ensureSize(ItemStackHandler inventory, int expected) {
        if (inventory.getSlots() == expected) return;
        ItemStack[] old = new ItemStack[inventory.getSlots()];
        for (int i=0;i<old.length;i++) old[i]=inventory.getStackInSlot(i).copy();
        inventory.setSize(expected);
        for (int i=0;i<Math.min(old.length,expected);i++) inventory.setStackInSlot(i,old[i]);
    }
}
