package com.industrialcrops.screen;

import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
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

/** Synchronizes ammunition, four upgrade slots, coordinate mode and target state. */
public final class GoldenLaunchSiloMenu extends AbstractContainerMenu {
    public static final int ROCKET_X = 18;
    public static final int ROCKET_Y = 36;
    public static final int UPGRADE_X = -68;
    public static final int UPGRADE_Y = 28;
    public static final int UPGRADE_SPACING = 22;
    public static final int PLAYER_INVENTORY_Y = 120;
    public static final int PLAYER_HOTBAR_Y = 178;

    private static final int MACHINE_SLOT_COUNT = 1 + GoldenLaunchSiloBlockEntity.UPGRADE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final BlockPos siloPos;
    private int coordinateX;
    private int coordinateY;
    private int coordinateZ;
    private int relativeMode;
    private int ignoreYMode;
    private int targetConfigured;
    private int cooldown;
    private int status;
    private int powerUpgradeCount;
    private int requiredPotatoes = 1;
    private boolean upgradeSlotsVisible;

    public GoldenLaunchSiloMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, readBlockEntity(inventory, buffer));
    }

    private GoldenLaunchSiloMenu(int containerId, Inventory inventory, GoldenLaunchSiloBlockEntity silo) {
        this(containerId, inventory, silo, silo.getBlockPos());
    }

    public GoldenLaunchSiloMenu(int containerId, Inventory inventory, GoldenLaunchSiloBlockEntity silo, BlockPos siloPos) {
        super(ModMenus.GOLDEN_LAUNCH_SILO.get(), containerId);
        this.siloPos = siloPos;

        addSlot(new SlotItemHandler(silo.getInventory(), GoldenLaunchSiloBlockEntity.ROCKET_SLOT, ROCKET_X, ROCKET_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GoldenLaunchSiloBlockEntity.isRocket(stack);
            }
        });
        for (int index = 0; index < GoldenLaunchSiloBlockEntity.UPGRADE_SLOT_COUNT; index++) {
            int inventorySlot = GoldenLaunchSiloBlockEntity.UPGRADE_SLOT_START + index;
            int x = UPGRADE_X + index % 2 * UPGRADE_SPACING;
            int y = UPGRADE_Y + index / 2 * UPGRADE_SPACING;
            addSlot(new SlotItemHandler(silo.getInventory(), inventorySlot, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return GoldenLaunchSiloBlockEntity.isUpgrade(stack) && super.mayPlace(stack);
                }

                @Override
                public boolean isActive() {
                    return upgradeSlotsVisible;
                }
            });
        }
        addPlayerInventory(inventory, 8, PLAYER_INVENTORY_Y);
        addPlayerHotbar(inventory, 8, PLAYER_HOTBAR_Y);

        addDataSlot(dataSlot(silo::getCoordinateX, value -> coordinateX = value));
        addDataSlot(dataSlot(silo::getCoordinateY, value -> coordinateY = value));
        addDataSlot(dataSlot(silo::getCoordinateZ, value -> coordinateZ = value));
        addDataSlot(dataSlot(() -> silo.isRelativeMode() ? 1 : 0, value -> relativeMode = value));
        addDataSlot(dataSlot(() -> silo.isIgnoreYMode() ? 1 : 0, value -> ignoreYMode = value));
        addDataSlot(dataSlot(() -> silo.hasTarget() ? 1 : 0, value -> targetConfigured = value));
        addDataSlot(dataSlot(silo::getCooldown, value -> cooldown = value));
        addDataSlot(dataSlot(silo::getLaunchStatus, value -> status = value));
        addDataSlot(dataSlot(silo::getPowerUpgradeCount, value -> powerUpgradeCount = value));
        addDataSlot(dataSlot(silo::getRequiredPotatoes, value -> requiredPotatoes = value));
    }

    public BlockPos getSiloPos() {
        return siloPos;
    }

    public int getTargetX() {
        return coordinateX;
    }

    public int getTargetY() {
        return coordinateY;
    }

    public int getTargetZ() {
        return coordinateZ;
    }

    public boolean isRelativeMode() {
        return relativeMode > 0;
    }

    public boolean isIgnoreYMode() {
        return ignoreYMode > 0;
    }

    public boolean hasTarget() {
        return targetConfigured > 0;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getStatus() {
        return status;
    }

    public int getPowerUpgradeCount() {
        return powerUpgradeCount;
    }

    public int getRequiredPotatoes() {
        return requiredPotatoes;
    }

    public void setUpgradeSlotsVisible(boolean visible) {
        upgradeSlotsVisible = visible;
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
        } else if (GoldenLaunchSiloBlockEntity.isRocket(stack)) {
            if (!moveItemStackTo(stack, GoldenLaunchSiloBlockEntity.ROCKET_SLOT, GoldenLaunchSiloBlockEntity.ROCKET_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (GoldenLaunchSiloBlockEntity.isUpgrade(stack)) {
            if (!moveItemStackTo(
                    stack,
                    GoldenLaunchSiloBlockEntity.UPGRADE_SLOT_START,
                    GoldenLaunchSiloBlockEntity.UPGRADE_SLOT_START + GoldenLaunchSiloBlockEntity.UPGRADE_SLOT_COUNT,
                    false
            )) {
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
        return stack.getCount() == copy.getCount() ? ItemStack.EMPTY : copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), siloPos), player, ModBlocks.GOLDEN_LAUNCH_SILO.get());
    }

    private static DataSlot dataSlot(java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
        return new DataSlot() {
            @Override
            public int get() {
                return getter.getAsInt();
            }

            @Override
            public void set(int value) {
                setter.accept(value);
            }
        };
    }

    private static GoldenLaunchSiloBlockEntity readBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof GoldenLaunchSiloBlockEntity silo) {
            return silo;
        }
        throw new IllegalStateException("Missing golden launch silo block entity at " + pos);
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
}
