package com.industrialcrops.screen;

import com.industrialcrops.block.entity.GourdModificationDeviceBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class GourdModificationDeviceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 3;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final BlockPos pos;
    private int progress;

    public GourdModificationDeviceMenu(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buffer
    ) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer));
    }

    private GourdModificationDeviceMenu(
            int containerId,
            Inventory playerInventory,
            GourdModificationDeviceBlockEntity blockEntity
    ) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    public GourdModificationDeviceMenu(
            int containerId,
            Inventory playerInventory,
            GourdModificationDeviceBlockEntity blockEntity,
            BlockPos pos
    ) {
        super(ModMenus.GOURD_MODIFICATION_DEVICE.get(), containerId);
        this.pos = pos;

        addSlot(new SlotItemHandler(
                blockEntity.getInventory(),
                GourdModificationDeviceBlockEntity.MELON_INPUT_SLOT,
                42,
                35
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GourdModificationDeviceBlockEntity.isSuperMelon(stack);
            }
        });
        addSlot(new SlotItemHandler(
                blockEntity.getInventory(),
                GourdModificationDeviceBlockEntity.PUMPKIN_INPUT_SLOT,
                64,
                35
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GourdModificationDeviceBlockEntity.isSuperPumpkin(stack);
            }
        });
        addSlot(new SlotItemHandler(
                blockEntity.getInventory(),
                GourdModificationDeviceBlockEntity.OUTPUT_SLOT,
                124,
                35
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory, 8, 84);
        addPlayerHotbar(playerInventory, 8, 142);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProgress();
            }

            @Override
            public void set(int value) {
                progress = value;
            }
        });
    }

    public int getScaledProgress(int width) {
        return Math.max(0, Math.min(
                width,
                progress * width / GourdModificationDeviceBlockEntity.MAX_PROGRESS
        ));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (GourdModificationDeviceBlockEntity.isSuperMelon(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (GourdModificationDeviceBlockEntity.isSuperPumpkin(stack)) {
            if (!moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                ContainerLevelAccess.create(player.level(), pos),
                player,
                ModBlocks.GOURD_MODIFICATION_DEVICE.get()
        );
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

    private static GourdModificationDeviceBlockEntity readBlockEntity(
            Inventory inventory,
            RegistryFriendlyByteBuf buffer
    ) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof GourdModificationDeviceBlockEntity device) {
            return device;
        }
        throw new IllegalStateException("Missing gourd modification device block entity at " + pos);
    }
}
