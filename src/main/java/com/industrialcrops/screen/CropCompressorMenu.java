package com.industrialcrops.screen;

import com.industrialcrops.block.entity.CropCompressorBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.machine.SpeedUpgradeHelper;
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

public final class CropCompressorMenu extends AbstractContainerMenu implements UpgradeableMenu {
    private static final int INPUT_SLOT = CropCompressorBlockEntity.INPUT_SLOT;
    private static final int OUTPUT_SLOT = CropCompressorBlockEntity.OUTPUT_SLOT;
    public static final int UPGRADE_X=-68,UPGRADE_Y=28,UPGRADE_SPACING=22;
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final CropCompressorBlockEntity blockEntity;
    private final BlockPos pos;
    private int progress;
    private boolean upgradeSlotsVisible;

    public CropCompressorMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer));
    }

    private CropCompressorMenu(int containerId, Inventory playerInventory, CropCompressorBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    public CropCompressorMenu(int containerId, Inventory playerInventory, CropCompressorBlockEntity blockEntity, BlockPos pos) {
        super(ModMenus.CROP_COMPRESSOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = pos;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), INPUT_SLOT, 56, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CropCompressorBlockEntity.isCompressible(stack);
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getInventory(), OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for(int i=0;i<4;i++){int x=UPGRADE_X+i%2*22,y=UPGRADE_Y+i/2*22;addSlot(new SlotItemHandler(blockEntity.getInventory(),2+i,x,y){@Override public boolean mayPlace(ItemStack stack){return SpeedUpgradeHelper.isSpeedUpgrade(stack);}@Override public boolean isActive(){return upgradeSlotsVisible;}});}

        addPlayerInventory(playerInventory, 8, 84);
        addPlayerHotbar(playerInventory, 8, 142);
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int value) { progress = value; }
        });
    }

    public int getScaledProgress(int width) {
        return Math.max(0, Math.min(width, progress * width / CropCompressorBlockEntity.MAX_PROGRESS));
    }
    public void setUpgradeSlotsVisible(boolean visible){upgradeSlotsVisible=visible;}

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex == OUTPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (slotIndex == INPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SpeedUpgradeHelper.isSpeedUpgrade(stack)) {
            if (!moveItemStackTo(stack, 2, 6, false)) return ItemStack.EMPTY;
        } else if (CropCompressorBlockEntity.isCompressible(stack)) {
            if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_HOTBAR_END) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.CROP_COMPRESSOR.get());
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

    private static CropCompressorBlockEntity readBlockEntity(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof CropCompressorBlockEntity compressor) {
            return compressor;
        }
        throw new IllegalStateException("Missing crop compressor block entity at " + pos);
    }
}
