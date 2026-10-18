package com.industrialcrops.screen;

import com.industrialcrops.block.entity.IntegratedSmeltingFurnaceBlockEntity;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public final class IntegratedSmeltingFurnaceMenu extends AbstractContainerMenu implements UpgradeableMenu {
    public static final int GRID_X = 8, GRID_Y = 24;
    /** Storage-upgrade slot, in the side panel to the right of the window (Incinerator-style). */
    public static final int STORAGE_UPGRADE_X = 188, STORAGE_UPGRADE_Y = 82;
    public static final int UPGRADE_X = -58, UPGRADE_Y = 24;
    private static final int HIDDEN = -10000;

    /** Top of the player inventory for a grid of {@code rows}. */
    public static int inventoryY(int rows) { return GRID_Y + rows * 18 + 18; }
    public static int hotbarY(int rows) { return inventoryY(rows) + 58; }
    public static int imageHeight(int rows) { return hotbarY(rows) + 24; }

    private static final int STORAGE_END = IntegratedSmeltingFurnaceBlockEntity.MAX_SLOTS;
    private static final int SPEED_START = STORAGE_END;
    private static final int SPEED_END = SPEED_START + IntegratedSmeltingFurnaceBlockEntity.SPEED_SLOTS;
    private static final int STORAGE_UPGRADE_INDEX = SPEED_END;
    private static final int PLAYER_START = STORAGE_UPGRADE_INDEX + 1;
    private static final int PLAYER_INVENTORY_END = PLAYER_START + 27;
    private static final int PLAYER_END = PLAYER_INVENTORY_END + 9;

    private final IntegratedSmeltingFurnaceBlockEntity furnace;
    private final BlockPos pos;
    private final ContainerData data;
    private final Player player;
    /** Rows this window was laid out for; slot positions are fixed, so a tier change reopens the window. */
    private final int rows;
    private boolean upgradeSlotsVisible;
    private boolean reopening;

    public IntegratedSmeltingFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer), buffer.readVarInt());
    }

    private IntegratedSmeltingFurnaceMenu(int containerId, Inventory playerInventory, IntegratedSmeltingFurnaceBlockEntity furnace, int rows) {
        this(containerId, playerInventory, furnace, furnace.getBlockPos(), rows);
    }

    public IntegratedSmeltingFurnaceMenu(int containerId, Inventory playerInventory, IntegratedSmeltingFurnaceBlockEntity furnace, BlockPos pos) {
        this(containerId, playerInventory, furnace, pos, IntegratedSmeltingFurnaceBlockEntity.rowsForTier(furnace.tier()));
    }

    private IntegratedSmeltingFurnaceMenu(int containerId, Inventory playerInventory, IntegratedSmeltingFurnaceBlockEntity furnace, BlockPos pos, int rows) {
        super(ModMenus.INTEGRATED_SMELTING_FURNACE.get(), containerId);
        this.furnace = furnace;
        this.pos = pos;
        this.data = furnace.getData();
        this.player = playerInventory.player;
        this.rows = Math.max(1, Math.min(IntegratedSmeltingFurnaceBlockEntity.MAX_ROWS, rows));

        for (int row = 0; row < IntegratedSmeltingFurnaceBlockEntity.MAX_ROWS; row++) {
            for (int column = 0; column < IntegratedSmeltingFurnaceBlockEntity.COLUMNS; column++) {
                int index = row * IntegratedSmeltingFurnaceBlockEntity.COLUMNS + column;
                boolean shown = row < this.rows;
                addSlot(new StorageSlot(index, shown ? GRID_X + column * 18 : HIDDEN, shown ? GRID_Y + row * 18 : HIDDEN));
            }
        }
        for (int i = 0; i < IntegratedSmeltingFurnaceBlockEntity.SPEED_SLOTS; i++) {
            addSlot(new SlotItemHandler(furnace.getUpgrades(), i, UPGRADE_X + i % 2 * 18, UPGRADE_Y + i / 2 * 18) {
                @Override public boolean mayPlace(ItemStack stack) { return SpeedUpgradeHelper.isSpeedUpgrade(stack); }
                @Override public boolean isActive() { return upgradeSlotsVisible; }
            });
        }
        addSlot(new SlotItemHandler(furnace.getUpgrades(), IntegratedSmeltingFurnaceBlockEntity.STORAGE_UPGRADE_SLOT, STORAGE_UPGRADE_X, STORAGE_UPGRADE_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                int tier = IntegratedSmeltingFurnaceBlockEntity.storageTierOf(stack);
                return tier > 0 && furnace.canSwitchTier(tier);
            }

            @Override
            public boolean mayPickup(Player player) {
                // Removing the upgrade must not strand items in rows or stacks the base furnace cannot hold.
                return furnace.canSwitchTier(0);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, inventoryY(this.rows) + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, hotbarY(this.rows)));
        }
        addDataSlots(data);
    }

    /** A furnace storage slot: hidden beyond the current tier, may hold up to 99 or 999 items. */
    private final class StorageSlot extends SlotItemHandler {
        private StorageSlot(int index, int x, int y) {
            super(furnace.getStorage(), index, x, y);
        }

        @Override public boolean isActive() { return getSlotIndex() < Math.min(rows * IntegratedSmeltingFurnaceBlockEntity.COLUMNS, furnace.activeSlots()); }
        @Override public boolean mayPlace(ItemStack stack) { return isActive(); }
        @Override public int getMaxStackSize() { return furnace.slotLimit(); }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return IntegratedSmeltingFurnaceBlockEntity.stackLimitFor(furnace.tier(), stack);
        }

        @Override
        public ItemStack remove(int amount) {
            return super.remove(Math.min(amount, getItem().getMaxStackSize()));
        }
    }

    public IntegratedSmeltingFurnaceBlockEntity furnace() { return furnace; }
    public int energy() { return (data.get(0) & 0xFFFF) | (data.get(1) << 16); }
    public int progress() { return data.get(2); }
    public int lanes() { return data.get(3); }
    public int mode() { return data.get(4); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < IntegratedSmeltingFurnaceBlockEntity.MODE_ONCE || id > IntegratedSmeltingFurnaceBlockEntity.MODE_FULL) return false;
        furnace.setMode(id);
        return true;
    }
    public int tier() { return furnace.tier(); }
    public int activeRows() { return IntegratedSmeltingFurnaceBlockEntity.rowsForTier(tier()); }
    /** Rows this window shows. */
    public int rows() { return rows; }

    /** When the storage upgrade changes the row count, reopen the window at the new size, keeping the cursor item. */
    @Override
    public void broadcastChanges() {
        if (!reopening && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && serverPlayer.containerMenu == this && activeRows() != rows) {
            reopening = true;
            ItemStack carried = getCarried();
            setCarried(ItemStack.EMPTY);
            furnace.openMenu(serverPlayer);
            serverPlayer.containerMenu.setCarried(carried);
            serverPlayer.containerMenu.broadcastChanges();
            return;
        }
        super.broadcastChanges();
    }
    public int slotLimit() { return IntegratedSmeltingFurnaceBlockEntity.limitForTier(tier()); }

    @Override
    public void setUpgradeSlotsVisible(boolean visible) {
        upgradeSlotsVisible = visible;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < PLAYER_START) {
            if (index < STORAGE_END && stack.getCount() > stack.getMaxStackSize()) {
                // Bulk slot: hand out one normal stack at a time.
                ItemStack part = stack.copyWithCount(stack.getMaxStackSize());
                ItemStack before = part.copy();
                if (!moveItemStackTo(part, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
                int moved = before.getCount() - part.getCount();
                stack.shrink(moved);
                slot.set(stack);
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else if (SpeedUpgradeHelper.isSpeedUpgrade(stack)) {
            if (!moveItemStackTo(stack, SPEED_START, SPEED_END, false) && !moveItemStackTo(stack, 0, furnace.activeSlots(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (IntegratedSmeltingFurnaceBlockEntity.storageTierOf(stack) > 0
                && !slots.get(STORAGE_UPGRADE_INDEX).hasItem() && slots.get(STORAGE_UPGRADE_INDEX).mayPlace(stack)) {
            if (!moveItemStackTo(stack, STORAGE_UPGRADE_INDEX, STORAGE_UPGRADE_INDEX + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, furnace.activeSlots(), false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.INTEGRATED_SMELTING_FURNACE.get());
    }

    private static IntegratedSmeltingFurnaceBlockEntity readBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof IntegratedSmeltingFurnaceBlockEntity furnace) return furnace;
        throw new IllegalStateException("Missing integrated smelting furnace block entity at " + pos);
    }
}
