package com.industrialcrops.screen;

import com.industrialcrops.block.entity.BasicCropStorageArrayBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ReinforcedControlDeviceMenu extends AbstractContainerMenu {
    private static final int STORAGE_SLOT_COUNT = BasicCropStorageArrayBlockEntity.SLOT_COUNT;
    private static final int PROPERTY_CONNECTED = 0;
    private static final int PROPERTY_PAGE = 1;
    private static final int PROPERTY_TOTAL_PAGES = 2;
    public static final int BUTTON_PREVIOUS_PAGE = 0;
    public static final int BUTTON_NEXT_PAGE = 1;

    private final BlockPos controllerPos;
    private final List<BasicCropStorageArrayBlockEntity> drives;
    private final boolean remoteAccess;
    private int page;
    private int connected;
    private int syncedPage;
    private int totalPages = 1;
    private final int[] syncedCounts = new int[STORAGE_SLOT_COUNT];

    public ReinforcedControlDeviceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, buffer.readBlockPos(), buffer.readableBytes() > 0 && buffer.readBoolean());
    }

    public ReinforcedControlDeviceMenu(int containerId, Inventory playerInventory, BlockPos controllerPos) {
        this(containerId, playerInventory, controllerPos, false);
    }

    public ReinforcedControlDeviceMenu(int containerId, Inventory playerInventory, BlockPos controllerPos, boolean remoteAccess) {
        super(ModMenus.REINFORCED_CONTROL_DEVICE.get(), containerId);
        this.controllerPos = controllerPos;
        this.remoteAccess = remoteAccess;
        this.drives = playerInventory.player.level().isClientSide()
                ? List.of()
                : BasicCropStorageArrayBlockEntity.findAllAttached(playerInventory.player.level(), controllerPos);

        addDataSlots();
        addStorageSlots();
        addPlayerInventory(playerInventory, 8, 140);
        addPlayerHotbar(playerInventory, 8, 198);
    }

    public boolean isConnected() {
        return connected > 0;
    }

    public int getPage() {
        return syncedPage;
    }

    public int getTotalPages() {
        return Math.max(1, totalPages);
    }

    public int getStoredCount(int slot) {
        return slot >= 0 && slot < syncedCounts.length ? syncedCounts[slot] : 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (drives.isEmpty()) {
            return false;
        }

        if (id == BUTTON_PREVIOUS_PAGE && page > 0) {
            page--;
            broadcastChanges();
            return true;
        }

        if (id == BUTTON_NEXT_PAGE && page < drives.size() - 1) {
            page++;
            broadcastChanges();
            return true;
        }

        return false;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if (slotIndex >= 0 && slotIndex < STORAGE_SLOT_COUNT) {
            if (clickType == ClickType.PICKUP) {
                handleStorageClick(slotIndex, button);
                return;
            }
            // Minecraft uses the mouse button as part of a shift-click packet.
            // Accept both buttons so Shift+right-click is a fast withdrawal too.
            if (clickType == ClickType.QUICK_MOVE && (button == 0 || button == 1)) {
                quickMoveStorageToPlayer(slotIndex);
            }
            return;
        }

        super.clicked(slotIndex, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        BasicCropStorageArrayBlockEntity drive = getCurrentDrive();
        if (drive == null || slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        if (slotIndex < STORAGE_SLOT_COUNT) {
            return quickMoveStorageToPlayer(slotIndex);
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        int inserted = drive.insertStack(original.copy());
        if (inserted <= 0) {
            return ItemStack.EMPTY;
        }

        original.shrink(inserted);
        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        broadcastChanges();
        return copy;
    }

    private ItemStack quickMoveStorageToPlayer(int slotIndex) {
        BasicCropStorageArrayBlockEntity drive = getCurrentDrive();
        if (drive == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stored = drive.getStackInSlot(slotIndex);
        if (stored.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int requested = Math.min(stored.getCount(), stored.getMaxStackSize());
        ItemStack moving = stored.copyWithCount(requested);
        ItemStack original = moving.copy();
        moveItemStackTo(moving, STORAGE_SLOT_COUNT, slots.size(), true);

        int moved = requested - moving.getCount();
        if (moved <= 0) {
            return ItemStack.EMPTY;
        }

        drive.extractFromSlot(slotIndex, moved);
        broadcastChanges();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (remoteAccess) {
            return player.level().hasChunkAt(controllerPos)
                    && player.level().getBlockState(controllerPos).is(ModBlocks.CARROT_CONTROL_DEVICE.get());
        }
        return stillValid(ContainerLevelAccess.create(player.level(), controllerPos), player, ModBlocks.CARROT_CONTROL_DEVICE.get());
    }

    private void addDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getCurrentDrive() == null ? 0 : 1;
            }

            @Override
            public void set(int value) {
                connected = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return page;
            }

            @Override
            public void set(int value) {
                syncedPage = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return Math.max(1, drives.size());
            }

            @Override
            public void set(int value) {
                totalPages = Math.max(1, value);
            }
        });
        for (int index = 0; index < STORAGE_SLOT_COUNT; index++) {
            final int slot = index;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    BasicCropStorageArrayBlockEntity drive = getCurrentDrive();
                    // 10,000 is the only sentinel the client needs to render "9999+" and stays
                    // safely inside the menu-data synchronization range.
                    return drive == null ? 0 : Math.min(10_000, drive.getStackInSlot(slot).getCount());
                }

                @Override
                public void set(int value) {
                    syncedCounts[slot] = Math.max(0, value);
                }
            });
        }
    }

    private void addStorageSlots() {
        SimpleContainer placeholder = new SimpleContainer(STORAGE_SLOT_COUNT);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = col + row * 9;
                addSlot(new StorageSlot(placeholder, slot, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    private void handleStorageClick(int slotIndex, int button) {
        BasicCropStorageArrayBlockEntity drive = getCurrentDrive();
        if (drive == null) {
            return;
        }

        ItemStack cursor = getCarried();
        ItemStack slotStack = drive.getStackInSlot(slotIndex);

        if (cursor.isEmpty()) {
            if (slotStack.isEmpty()) {
                return;
            }

            int amount = button == 1 ? 1 : Math.min(slotStack.getMaxStackSize(), slotStack.getCount());
            setCarried(drive.extractFromSlot(slotIndex, amount));
            broadcastChanges();
            return;
        }

        int amount = button == 1 ? 1 : cursor.getCount();
        int inserted = drive.insertIntoSlot(slotIndex, cursor.copyWithCount(amount));
        if (inserted <= 0) {
            return;
        }

        cursor.shrink(inserted);
        setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
        broadcastChanges();
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

    private int clampPage(int requestedPage) {
        if (drives.isEmpty()) {
            return 0;
        }
        return Math.max(0, Math.min(requestedPage, drives.size() - 1));
    }

    private @Nullable BasicCropStorageArrayBlockEntity getCurrentDrive() {
        return drives.isEmpty() ? null : drives.get(clampPage(page));
    }

    private final class StorageSlot extends Slot {
        private StorageSlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public ItemStack getItem() {
            BasicCropStorageArrayBlockEntity drive = getCurrentDrive();
            return drive == null ? super.getItem() : drive.getStackInSlot(getSlotIndex());
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}

