package com.industrialcrops.screen;

import com.industrialcrops.block.entity.MaterialHardeningDeviceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public final class MaterialHardeningDeviceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 2;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final MaterialHardeningDeviceBlockEntity device;
    private final BlockPos pos;
    private int progress;

    public MaterialHardeningDeviceMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, read(inventory, buffer));
    }

    private MaterialHardeningDeviceMenu(int id, Inventory inventory, MaterialHardeningDeviceBlockEntity device) {
        this(id, inventory, device, device.getBlockPos());
    }

    public MaterialHardeningDeviceMenu(int id, Inventory playerInventory, MaterialHardeningDeviceBlockEntity device, BlockPos pos) {
        super(com.industrialcrops.registry.CarroteMenus.MATERIAL_HARDENING_DEVICE.get(), id);
        this.device = device;
        this.pos = pos;
        addSlot(new SlotItemHandler(device.getInventory(), MaterialHardeningDeviceBlockEntity.INPUT_SLOT, 56, 35) {
            @Override public boolean mayPlace(ItemStack stack) {
                return MaterialHardeningDeviceBlockEntity.accepts(stack);
            }
        });
        addSlot(new SlotItemHandler(device.getInventory(), MaterialHardeningDeviceBlockEntity.OUTPUT_SLOT, 116, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addPlayerInventory(playerInventory, 8, 84);
        addPlayerHotbar(playerInventory, 8, 142);
        addDataSlot(new DataSlot() {
            @Override public int get() { return device.getData().get(0); }
            @Override public void set(int value) { progress = value; }
        });
    }

    public int getScaledProgress(int width) {
        return Math.max(0, Math.min(width, progress * width / MaterialHardeningDeviceBlockEntity.PROCESS_TICKS));
    }

    @Override public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (slotIndex == MaterialHardeningDeviceBlockEntity.OUTPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (MaterialHardeningDeviceBlockEntity.accepts(stack)) {
            if (!moveItemStackTo(stack, MaterialHardeningDeviceBlockEntity.INPUT_SLOT,
                    MaterialHardeningDeviceBlockEntity.INPUT_SLOT + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return stack.getCount() == copy.getCount() ? ItemStack.EMPTY : copy;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, com.industrialcrops.registry.CarroteBlocks.MATERIAL_HARDENING_DEVICE.get());
    }

    private void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
        }
    }

    private void addPlayerHotbar(Inventory inventory, int x, int y) {
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, x + column * 18, y));
    }

    private static MaterialHardeningDeviceBlockEntity read(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof MaterialHardeningDeviceBlockEntity device) return device;
        throw new IllegalStateException("Missing material hardening device at " + pos);
    }
}
