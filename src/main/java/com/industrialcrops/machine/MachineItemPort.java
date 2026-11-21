package com.industrialcrops.machine;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/** Exposes the working slots of a machine without granting direct mutation of its menu inventory. */
public final class MachineItemPort implements IItemHandler {
    private final IItemHandler inventory;
    private final int inputs;
    private final int firstOutput;
    private final int slots;

    private MachineItemPort(IItemHandler inventory, int inputs, int firstOutput, int slots) {
        this.inventory = inventory;
        this.inputs = inputs;
        this.firstOutput = firstOutput;
        this.slots = slots;
    }

    public static MachineItemPort processing(IItemHandler inventory, int inputs, int outputs) {
        return new MachineItemPort(inventory, inputs, inputs, inputs + outputs);
    }

    public static MachineItemPort storage(IItemHandler inventory, int slots) {
        return new MachineItemPort(inventory, slots, 0, slots);
    }

    @Override public int getSlots() { return Math.min(slots, inventory.getSlots()); }

    private boolean visible(int slot) { return slot >= 0 && slot < getSlots(); }

    @Override public ItemStack getStackInSlot(int slot) {
        return visible(slot) ? inventory.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override public int getSlotLimit(int slot) {
        return visible(slot) ? inventory.getSlotLimit(slot) : 0;
    }

    @Override public boolean isItemValid(int slot, ItemStack stack) {
        return visible(slot) && slot < inputs && inventory.isItemValid(slot, stack);
    }

    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return visible(slot) && slot >= firstOutput && amount > 0
            ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }
}
