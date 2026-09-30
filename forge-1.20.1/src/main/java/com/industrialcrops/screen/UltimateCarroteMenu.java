package com.industrialcrops.screen;

import com.industrialcrops.curios.CarroteCuriosItems;
import com.industrialcrops.curios.UltimateCarroteStorage;
import com.industrialcrops.registry.CarroteCuriosMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class UltimateCarroteMenu extends AbstractContainerMenu {
    public static final int PAGE_SLOTS = 54;
    private final Inventory inventory;
    private final int bagSlot;
    private final ItemStack bag;
    private final BagContainer contents;
    private List<ItemStack> stored = new ArrayList<>();
    private int totalRows = 2;
    private int layoutRows = -1;
    private int scrollRow;
    private int visibleRowLimit = 6;
    private boolean loading;

    public UltimateCarroteMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readVarInt());
    }

    public UltimateCarroteMenu(int id, Inventory inventory, int bagSlot) {
        super(CarroteCuriosMenus.ULTIMATE.get(), id);
        this.inventory = inventory;
        this.bagSlot = bagSlot;
        this.bag = bagSlot >= 0 && bagSlot < inventory.getContainerSize() ? inventory.getItem(bagSlot) : ItemStack.EMPTY;
        this.contents = new BagContainer();
        reload();
        for (int i = 0; i < PAGE_SLOTS; i++) addSlot(bagSlot(i));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(playerSlot(col + row * 9 + 9, col, row));
        }
        for (int col = 0; col < 9; col++) addSlot(playerSlot(col, col, 3));
        addDataSlot(new DataSlot() {
            @Override public int get() { return totalRows; }
            @Override public void set(int value) { totalRows = Math.max(2, value); layoutRows = -1; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return scrollRow; }
            @Override public void set(int value) { scrollRow = Math.max(0, value); layoutRows = -1; }
        });
        layout();
    }

    public int rows() { return totalRows; }
    public int visibleRows() { return Math.min(visibleRowLimit, rows()); }
    public int scrollRow() { return scrollRow; }
    public int inventoryY() { return 32 + visibleRows() * 18; }
    public int bagSlotCount() { return PAGE_SLOTS; }

    public void visibleRowLimit(int limit) {
        visibleRowLimit = Math.max(2, Math.min(6, limit));
        layoutRows = -1;
        layout();
    }

    /** Slot indices stay fixed over the wire while their on-screen layout grows. */
    public void layout() {
        int visible = visibleRows();
        if (layoutRows == visible) return;
        layoutRows = visible;
        for (int i = 0; i < PAGE_SLOTS; i++) replaceSlot(i, bagSlot(i));
        for (int i = 0; i < 27; i++) replaceSlot(PAGE_SLOTS + i, playerSlot(i + 9, i % 9, i / 9));
        for (int i = 0; i < 9; i++) replaceSlot(PAGE_SLOTS + 27 + i, playerSlot(i, i, 3));
    }

    private void replaceSlot(int index, Slot slot) {
        slot.index = index;
        slots.set(index, slot);
    }

    private Slot bagSlot(int index) {
        return new Slot(contents, index, 8 + index % 9 * 18, 18 + index / 9 * 18) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) {
                return scrollRow + index / 9 < rows() && UltimateCarroteStorage.accepts(stack);
            }
            @Override public boolean isActive() { return index / 9 < visibleRows() && scrollRow + index / 9 < rows(); }
        };
    }

    private Slot playerSlot(int index, int column, int row) {
        int y = inventoryY() + (row == 3 ? 58 : row * 18);
        return new Slot(inventory, index, 8 + column * 18, y) {
            @Override public boolean mayPickup(Player player) { return index != bagSlot; }
            @Override public boolean mayPlace(ItemStack stack) { return index != bagSlot; }
        };
    }

    @Override public boolean stillValid(Player player) {
        if (!player.isAlive() || bagSlot < 0 || bagSlot >= inventory.getContainerSize()) return false;
        ItemStack current = inventory.getItem(bagSlot);
        return current.is(CarroteCuriosItems.ULTIMATE.get()) && (player.level().isClientSide || current == bag);
    }

    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player)) return;
        if (type == ClickType.SWAP && button == bagSlot) return;
        if (!player.level().isClientSide) reload();
        super.clicked(slot, button, type, player);
        if (!player.level().isClientSide) compact();
        layout();
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < PAGE_SLOTS) {
            if (!moveItemStackTo(stack, PAGE_SLOTS, PAGE_SLOTS + 36, true)) return ItemStack.EMPTY;
        } else {
            if (!UltimateCarroteStorage.accepts(stack)) return ItemStack.EMPTY;
            if (player.level().isClientSide) return ItemStack.EMPTY;
            stored.add(stack.split(1));
            persist();
            loadPage();
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player)) return false;
        if (button >= -6 && button <= -2) {
            visibleRowLimit(-button);
            scrollRow = Math.min(scrollRow, Math.max(0, rows() - visibleRows()));
            reload();
            broadcastChanges();
            return true;
        }
        if (button < 0) return false;
        reload();
        scrollRow = Math.min(button, Math.max(0, rows() - visibleRows()));
        loadPage();
        broadcastChanges();
        return true;
    }

    @Override public void broadcastChanges() {
        if (!inventory.player.level().isClientSide && stillValid(inventory.player)) reload();
        layout();
        super.broadcastChanges();
    }

    private void reload() {
        stored = new ArrayList<>(UltimateCarroteStorage.contents(bag, inventory.player.level().registryAccess()));
        totalRows = UltimateCarroteStorage.rows(stored);
        scrollRow = Math.min(scrollRow, Math.max(0, totalRows - visibleRows()));
        loadPage();
    }

    private void loadPage() {
        loading = true;
        int start = scrollRow * 9;
        for (int i = 0; i < PAGE_SLOTS; i++) contents.setItem(i,
                start + i < stored.size() ? stored.get(start + i).copy() : ItemStack.EMPTY);
        loading = false;
    }

    private void compact() {
        stored.removeIf(ItemStack::isEmpty);
        persist();
        scrollRow = Math.min(scrollRow, Math.max(0, totalRows - visibleRows()));
        loadPage();
    }

    private void persist() {
        UltimateCarroteStorage.save(bag, stored, inventory.player.level().registryAccess());
        totalRows = UltimateCarroteStorage.rows(stored);
        inventory.setChanged();
    }

    private final class BagContainer extends SimpleContainer {
        private BagContainer() { super(PAGE_SLOTS); }
        @Override public int getMaxStackSize() { return 1; }
        @Override public void setChanged() {
            super.setChanged();
            if (!loading && !inventory.player.level().isClientSide && stillValid(inventory.player)) {
                int start = scrollRow * 9;
                for (int i = 0; i < PAGE_SLOTS; i++) {
                    int index = start + i;
                    ItemStack stack = getItem(i);
                    if (stack.isEmpty() && index >= stored.size()) continue;
                    while (stored.size() <= index) stored.add(ItemStack.EMPTY);
                    stored.set(index, stack.copy());
                }
                persist();
            }
        }
    }
}
