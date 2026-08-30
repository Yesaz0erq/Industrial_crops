package com.industrialcrops.screen;

import com.industrialcrops.block.entity.PipeSorterBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class PipeSorterMenu extends AbstractContainerMenu {
    public static final int FILTER_SLOT_COUNT = PipeSorterBlockEntity.FILTER_SLOT_COUNT;
    public static final int BUTTON_WHITELIST = 0;
    public static final int BUTTON_BLACKLIST = 1;
    private static final int PLAYER_INVENTORY_START = FILTER_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final PipeSorterBlockEntity blockEntity;
    private final BlockPos pos;
    private boolean blacklist;

    public PipeSorterMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer));
    }

    private PipeSorterMenu(int containerId, Inventory playerInventory, PipeSorterBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    public PipeSorterMenu(int containerId, Inventory playerInventory, PipeSorterBlockEntity blockEntity, BlockPos pos) {
        super(ModMenus.PIPE_SORTER.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = pos;
        this.blacklist = blockEntity.isBlacklist();

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;
                addSlot(new SlotItemHandler(blockEntity.getFilters(), slot,
                        8 + column * 18, 18 + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                    @Override public boolean mayPickup(Player player) { return false; }
                });
            }
        }
        addPlayerInventory(playerInventory, 8, 88);
        addPlayerHotbar(playerInventory, 8, 146);
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.isBlacklist() ? 1 : 0; }
            @Override public void set(int value) { blacklist = value != 0; }
        });
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public ItemStack filter(int slot) {
        return slot >= 0 && slot < FILTER_SLOT_COUNT
                ? blockEntity.getFilters().getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!stillValid(player)) return false;
        if (id == BUTTON_WHITELIST) {
            blockEntity.setBlacklist(false);
            broadcastChanges();
            return true;
        }
        if (id == BUTTON_BLACKLIST) {
            blockEntity.setBlacklist(true);
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if (slotIndex >= 0 && slotIndex < FILTER_SLOT_COUNT && clickType == ClickType.PICKUP) {
            ItemStack cursor = getCarried();
            blockEntity.setFilter(slotIndex, button == 0 && !cursor.isEmpty() ? cursor : ItemStack.EMPTY);
            broadcastChanges();
            return;
        }
        super.clicked(slotIndex, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < PLAYER_INVENTORY_START || slotIndex >= PLAYER_HOTBAR_END) return ItemStack.EMPTY;
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.PIPE_SORTER.get());
    }

    private void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory, int x, int y) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * 18, y));
        }
    }

    private static PipeSorterBlockEntity readBlockEntity(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PipeSorterBlockEntity sorter) return sorter;
        throw new IllegalStateException("Missing pipe sorter block entity at " + pos);
    }
}
