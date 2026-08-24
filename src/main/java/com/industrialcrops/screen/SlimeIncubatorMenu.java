package com.industrialcrops.screen;

import com.industrialcrops.block.entity.SlimeIncubatorBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public final class SlimeIncubatorMenu extends AbstractContainerMenu implements UpgradeableMenu {
    private static final int INPUT_SLOT = SlimeIncubatorBlockEntity.INPUT_SLOT;
    private static final int OUTPUT_SLOT = SlimeIncubatorBlockEntity.OUTPUT_SLOT;
    public static final int UPGRADE_X=-68,UPGRADE_Y=28,UPGRADE_SPACING=22;
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final SlimeIncubatorBlockEntity blockEntity;
    private final BlockPos pos;
    private int progress;
    private int slimeType;
    private boolean upgradeSlotsVisible;

    public SlimeIncubatorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, readBlockEntity(inventory, buffer));
    }

    private SlimeIncubatorMenu(int containerId, Inventory inventory, SlimeIncubatorBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity, blockEntity.getBlockPos());
    }

    public SlimeIncubatorMenu(int containerId, Inventory inventory, SlimeIncubatorBlockEntity blockEntity, BlockPos pos) {
        super(ModMenus.SLIME_INCUBATOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = pos;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), INPUT_SLOT, 44, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SlimeIncubatorBlockEntity.isAcceptedSlimeInput(stack);
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getInventory(), OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for(int i=0;i<4;i++){int x=UPGRADE_X+i%2*22,y=UPGRADE_Y+i/2*22;addSlot(new SlotItemHandler(blockEntity.getInventory(),2+i,x,y){@Override public boolean mayPlace(ItemStack stack){return SpeedUpgradeHelper.isSpeedUpgrade(stack);}@Override public boolean isActive(){return upgradeSlotsVisible;}});}

        addPlayerInventory(inventory, 8, 84);
        addPlayerHotbar(inventory, 8, 142);

        addDataSlot(dataSlot(blockEntity::getProgress, value -> progress = value));
        addDataSlot(dataSlot(blockEntity::getActiveSlimeType, value -> slimeType = value));
    }

    public int getScaledProgress(int width) {
        return Mth.clamp(progress * width / SlimeIncubatorBlockEntity.MAX_PROGRESS, 0, width);
    }

    public int getSlimeType() {
        return slimeType;
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
        } else if (SlimeIncubatorBlockEntity.isAcceptedSlimeInput(stack)) {
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
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.SLIME_INCUBATOR.get());
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

    private static SlimeIncubatorBlockEntity readBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SlimeIncubatorBlockEntity slime_converter) {
            return slime_converter;
        }
        throw new IllegalStateException("Missing slime slime_converter block entity at " + pos);
    }
}
