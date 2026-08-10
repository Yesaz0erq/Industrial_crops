package com.industrialcrops.screen;

import com.industrialcrops.block.entity.BasicCropStorageArrayBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BasicControlDeviceMenu extends AbstractContainerMenu {
    public static final int STORE_ALL_CARROTS = 0;
    public static final int STORE_ALL_POTATOES = 1;
    public static final int TAKE_ONE_CARROT = 2;
    public static final int TAKE_STACK_CARROTS = 3;
    public static final int TAKE_ONE_POTATO = 4;
    public static final int TAKE_STACK_POTATOES = 5;

    private static final int PROPERTY_CARROTS = 0;
    private static final int PROPERTY_POTATOES = 1;
    private static final int PROPERTY_CONNECTED = 2;

    private final Inventory playerInventory;
    private final BlockPos controllerPos;
    private final @Nullable BasicCropStorageArrayBlockEntity drive;
    private final boolean remoteAccess;
    private int carrots;
    private int potatoes;
    private int connected;

    public BasicControlDeviceMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, buffer.readBlockPos(), buffer.readableBytes() > 0 && buffer.readBoolean());
    }

    public BasicControlDeviceMenu(int containerId, Inventory playerInventory, BlockPos controllerPos) {
        this(containerId, playerInventory, controllerPos, false);
    }

    public BasicControlDeviceMenu(int containerId, Inventory playerInventory, BlockPos controllerPos, boolean remoteAccess) {
        super(ModMenus.BASIC_CONTROL_DEVICE.get(), containerId);
        this.playerInventory = playerInventory;
        this.controllerPos = controllerPos;
        this.remoteAccess = remoteAccess;
        this.drive = playerInventory.player.level().isClientSide()
                ? null
                : BasicCropStorageArrayBlockEntity.findAttached(playerInventory.player.level(), controllerPos);

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return drive == null ? 0 : drive.getStoredIndustrialCarrots();
            }

            @Override
            public void set(int value) {
                carrots = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return drive == null ? 0 : drive.getStoredIndustrialPotatoes();
            }

            @Override
            public void set(int value) {
                potatoes = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return drive == null ? 0 : 1;
            }

            @Override
            public void set(int value) {
                connected = value;
            }
        });

        addPlayerInventory(playerInventory, 8, 84);
        addPlayerHotbar(playerInventory, 8, 142);
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public int getIndustrialCarrotCount() {
        return carrots;
    }

    public int getIndustrialPotatoCount() {
        return potatoes;
    }

    public boolean isConnected() {
        return connected > 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (drive == null) {
            return false;
        }

        boolean handled = switch (id) {
            case STORE_ALL_CARROTS -> drive.storeFromInventory(player.getInventory(), ModItems.INDUSTRIAL_CARROT.get()) > 0;
            case STORE_ALL_POTATOES -> drive.storeFromInventory(player.getInventory(), ModItems.INDUSTRIAL_POTATO.get()) > 0;
            case TAKE_ONE_CARROT -> drive.extractToInventory(player.getInventory(), ModItems.INDUSTRIAL_CARROT.get(), 1) > 0;
            case TAKE_STACK_CARROTS -> drive.extractToInventory(player.getInventory(), ModItems.INDUSTRIAL_CARROT.get(), 64) > 0;
            case TAKE_ONE_POTATO -> drive.extractToInventory(player.getInventory(), ModItems.INDUSTRIAL_POTATO.get(), 1) > 0;
            case TAKE_STACK_POTATOES -> drive.extractToInventory(player.getInventory(), ModItems.INDUSTRIAL_POTATO.get(), 64) > 0;
            default -> false;
        };
        if (handled) {
            broadcastChanges();
        }
        return handled;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (drive == null || slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotIndex);
        ItemStack original = slot.getItem();
        if (original.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item item = getSupportedStorageItem(original);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack moved = original.copy();
        int inserted = drive.insert(item, original.getCount());
        if (inserted <= 0) {
            return ItemStack.EMPTY;
        }

        original.shrink(inserted);
        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        player.getInventory().setChanged();
        broadcastChanges();
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        if (remoteAccess) {
            return player.level().hasChunkAt(controllerPos)
                    && player.level().getBlockState(controllerPos).is(ModBlocks.CARROT_CONTROL_DEVICE.get());
        }
        return stillValid(ContainerLevelAccess.create(player.level(), controllerPos), player, ModBlocks.CARROT_CONTROL_DEVICE.get());
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

    private static @Nullable Item getSupportedStorageItem(ItemStack stack) {
        if (stack.is(ModItems.INDUSTRIAL_CARROT.get())) {
            return ModItems.INDUSTRIAL_CARROT.get();
        }
        if (stack.is(ModItems.INDUSTRIAL_POTATO.get())) {
            return ModItems.INDUSTRIAL_POTATO.get();
        }
        if (stack.is(ModItems.INDUSTRIAL_WHEAT.get())) {
            return ModItems.INDUSTRIAL_WHEAT.get();
        }
        if (stack.is(ModItems.INDUSTRIAL_MELON.get())) {
            return ModItems.INDUSTRIAL_MELON.get();
        }
        if (stack.is(ModItems.INDUSTRIAL_PUMPKIN.get())) {
            return ModItems.INDUSTRIAL_PUMPKIN.get();
        }
        return null;
    }
}
