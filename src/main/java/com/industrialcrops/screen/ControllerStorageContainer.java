package com.industrialcrops.screen;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/** A writable view of one controller page, also exposed to inventory sorting mods. */
public final class ControllerStorageContainer extends SimpleContainer {
    private final BooleanSupplier client;
    private final IntFunction<ItemStack> reader;
    private final BiConsumer<Integer, ItemStack> writer;
    private final IntPredicate available;
    private final Runnable changed;
    private final java.util.List<ItemStack> clientStacks;
    private final java.util.Set<Integer> sortingLocks = new java.util.HashSet<>();
    private java.util.function.IntSupplier slotLimit = () -> Integer.MAX_VALUE;
    private java.util.function.BiPredicate<Integer, ItemStack> placementRule = (slot, stack) -> true;

    public ControllerStorageContainer(int size, BooleanSupplier client, IntFunction<ItemStack> reader,
                                      BiConsumer<Integer, ItemStack> writer, IntPredicate available, Runnable changed) {
        super(size);
        this.client = client;
        this.reader = reader;
        this.writer = writer;
        this.available = available;
        this.changed = changed;
        this.clientStacks = new java.util.ArrayList<>(java.util.Collections.nCopies(size, ItemStack.EMPTY));
    }

    /** Per-slot item cap of the backing storage (99 / 99 / 999 / 9,999 depending on the device). */
    public ControllerStorageContainer withSlotLimit(java.util.function.IntSupplier limit) {
        this.slotLimit = limit;
        return this;
    }

    /** Server-side placement rule, e.g. "one slot per item type" for the expandable storage device. */
    public ControllerStorageContainer withPlacementRule(java.util.function.BiPredicate<Integer, ItemStack> rule) {
        this.placementRule = rule;
        return this;
    }

    public int getSlotLimit() {
        return Math.max(1, slotLimit.getAsInt());
    }

    public boolean isAvailable(int slot) {
        return slot >= 0 && slot < getContainerSize() && available.test(slot);
    }

    public void clearSortingLocks() { sortingLocks.clear(); }
    public void lockForSorting(int slot) { if (slot >= 0 && slot < getContainerSize()) sortingLocks.add(slot); }
    public boolean isSortingLocked(int slot) { return sortingLocks.contains(slot); }

    @Override public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
        return client.getAsBoolean() ? clientStacks.get(slot) : reader.apply(slot);
    }

    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getContainerSize()) return;
        if (client.getAsBoolean()) clientStacks.set(slot, stack.copy());
        else if (isAvailable(slot)) writer.accept(slot, stack.copy());
        setChanged();
    }

    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack current = getItem(slot);
        if (!isAvailable(slot) || current.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        int count = Math.min(amount, current.getCount());
        ItemStack result = current.copyWithCount(count);
        ItemStack remainder = current.copy();
        remainder.shrink(count);
        setItem(slot, remainder);
        return result;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) { return removeItem(slot, Integer.MAX_VALUE); }
    @Override public int getMaxStackSize() { return getSlotLimit(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        // The client cannot see the full storage, so the placement rule is enforced by the server only.
        return isAvailable(slot) && (client.getAsBoolean() || placementRule.test(slot, stack));
    }
    @Override public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) if (!getItem(i).isEmpty()) return false;
        return true;
    }
    @Override public void clearContent() {
        for (int i = 0; i < getContainerSize(); i++) setItem(i, ItemStack.EMPTY);
    }
    @Override public void setChanged() {
        if (client != null && !client.getAsBoolean()) changed.run();
        super.setChanged();
    }
}
