package com.industrialcrops.screen;

import com.industrialcrops.block.entity.CropAnalysisDeviceBlockEntity;
import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public final class CropAnalysisDeviceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 1;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final CropAnalysisDeviceBlockEntity blockEntity;
    private final BlockPos pos;

    public CropAnalysisDeviceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer));
    }

    private CropAnalysisDeviceMenu(
            int containerId,
            Inventory playerInventory,
            CropAnalysisDeviceBlockEntity blockEntity
    ) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    public CropAnalysisDeviceMenu(
            int containerId,
            Inventory playerInventory,
            CropAnalysisDeviceBlockEntity blockEntity,
            BlockPos pos
    ) {
        super(ModMenus.CROP_ANALYSIS_DEVICE.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = pos;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), CropAnalysisDeviceBlockEntity.INPUT_SLOT, 25, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CropGenetics.isGeneticCrop(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addPlayerInventory(playerInventory, 8, 84);
        addPlayerHotbar(playerInventory, 8, 142);
    }

    public ItemStack getAnalyzedStack() {
        return blockEntity.getInventory().getStackInSlot(CropAnalysisDeviceBlockEntity.INPUT_SLOT);
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
        } else if (CropGenetics.isGeneticCrop(stack)) {
            if (!moveItemStackTo(stack, 0, MACHINE_SLOT_COUNT, false)) {
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
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.CROP_ANALYSIS_DEVICE.get());
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

    private static CropAnalysisDeviceBlockEntity readBlockEntity(
            Inventory inventory,
            FriendlyByteBuf buffer
    ) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof CropAnalysisDeviceBlockEntity analyzer) {
            return analyzer;
        }
        throw new IllegalStateException("Missing crop analysis device block entity at " + pos);
    }
}
