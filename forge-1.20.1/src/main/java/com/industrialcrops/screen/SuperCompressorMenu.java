package com.industrialcrops.screen;

import com.industrialcrops.block.entity.SuperCompressorBlockEntity;
import com.industrialcrops.machine.SpeedUpgradeHelper;
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

public final class SuperCompressorMenu extends AbstractContainerMenu implements UpgradeableMenu {
    public static final int INPUT_X = 26, INPUT_Y = 35;
    public static final int OUTPUT_X = 134, OUTPUT_Y = 35;
    public static final int UPGRADE_X = -58, UPGRADE_Y = 24;
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final BlockPos pos;
    private int progress;
    private int storedPotatoes;
    private boolean upgradeSlotsVisible;

    public SuperCompressorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer));
    }

    private SuperCompressorMenu(int containerId, Inventory playerInventory, SuperCompressorBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    public SuperCompressorMenu(int containerId, Inventory playerInventory, SuperCompressorBlockEntity blockEntity, BlockPos pos) {
        super(ModMenus.SUPER_COMPRESSOR.get(), containerId);
        this.pos = pos;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), SuperCompressorBlockEntity.INPUT_SLOT, INPUT_X, INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SuperCompressorBlockEntity.potatoValue(stack) > 0;
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getInventory(), SuperCompressorBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int i = 0; i < SuperCompressorBlockEntity.UPGRADE_SLOT_COUNT; i++) {
            int x = UPGRADE_X + i % 2 * 18;
            int y = UPGRADE_Y + i / 2 * 18;
            addSlot(new SlotItemHandler(blockEntity.getInventory(), SuperCompressorBlockEntity.UPGRADE_SLOT_START + i, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return SpeedUpgradeHelper.isSpeedUpgrade(stack);
                }

                @Override
                public boolean isActive() {
                    return upgradeSlotsVisible;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int value) { progress = value; }
        });
        // Buffer is at most 20,000, which fits in the 16-bit value a DataSlot sends.
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getStoredPotatoes(); }
            @Override public void set(int value) { storedPotatoes = value & 0xFFFF; }
        });
    }

    public int getStoredPotatoes() {
        return storedPotatoes;
    }

    public int getScaledProgress(int width) {
        return Math.max(0, Math.min(width, progress * width / SuperCompressorBlockEntity.MAX_PROGRESS));
    }

    /** Fill of the "next batch" gauge, 0..width. */
    public int getScaledBuffer(int width) {
        int current = Math.min(storedPotatoes, SuperCompressorBlockEntity.REQUIRED_POTATOES);
        return Math.max(0, Math.min(width, (int) ((long) current * width / SuperCompressorBlockEntity.REQUIRED_POTATOES)));
    }

    @Override
    public void setUpgradeSlotsVisible(boolean visible) {
        upgradeSlotsVisible = visible;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, slotIndex == SuperCompressorBlockEntity.OUTPUT_SLOT)) {
                return ItemStack.EMPTY;
            }
            if (slotIndex == SuperCompressorBlockEntity.OUTPUT_SLOT) slot.onQuickCraft(stack, copy);
        } else if (SpeedUpgradeHelper.isSpeedUpgrade(stack)) {
            if (!moveItemStackTo(stack, SuperCompressorBlockEntity.UPGRADE_SLOT_START,
                    SuperCompressorBlockEntity.UPGRADE_SLOT_START + SuperCompressorBlockEntity.UPGRADE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SuperCompressorBlockEntity.potatoValue(stack) > 0) {
            if (!moveItemStackTo(stack, SuperCompressorBlockEntity.INPUT_SLOT, SuperCompressorBlockEntity.INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) return ItemStack.EMPTY;
        } else if (slotIndex < PLAYER_HOTBAR_END) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) return ItemStack.EMPTY;
        } else {
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
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.SUPER_COMPRESSOR.get());
    }

    private static SuperCompressorBlockEntity readBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SuperCompressorBlockEntity compressor) return compressor;
        throw new IllegalStateException("Missing super compressor block entity at " + pos);
    }
}
