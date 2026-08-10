package com.industrialcrops.screen;

import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class IncubatorMenu extends AbstractContainerMenu implements UpgradeableMenu {
    public static final int UPGRADE_X=-68,UPGRADE_Y=31,UPGRADE_SPACING=22;
    private static final int MACHINE_SLOT_COUNT = 5;
    private static final int PLAYER_INVENTORY_START = 5;
    private static final int PLAYER_INVENTORY_END = 32;
    private static final int PLAYER_HOTBAR_START = 32;
    private static final int PLAYER_HOTBAR_END = 41;

    private final IncubatorBlockEntity blockEntity;
    private final BlockPos pos;
    private int slimeType;
    private int slimeSize;
    private int progress;
    private boolean upgradeSlotsVisible;

    public IncubatorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, readBlockEntity(inventory, buffer));
    }

    private IncubatorMenu(int containerId, Inventory inventory, IncubatorBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity, blockEntity.getBlockPos());
    }

    public IncubatorMenu(
            int containerId,
            Inventory inventory,
            IncubatorBlockEntity blockEntity,
            BlockPos pos
    ) {
        super(ModMenus.INCUBATOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = pos;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return IncubatorBlockEntity.isRawOre(stack);
            }
        });
        for(int i=0;i<4;i++){int x=UPGRADE_X+i%2*22,y=UPGRADE_Y+i/2*22;addSlot(new SlotItemHandler(blockEntity.getInventory(),1+i,x,y){@Override public boolean mayPlace(ItemStack stack){return SpeedUpgradeHelper.isSpeedUpgrade(stack);}@Override public boolean isActive(){return upgradeSlotsVisible;}});}
        addPlayerInventory(inventory, 8, 84);
        addPlayerHotbar(inventory, 8, 142);

        addDataSlot(dataSlot(blockEntity::getSlimeType, value -> slimeType = value));
        addDataSlot(dataSlot(blockEntity::getSlimeSize, value -> slimeSize = value));
        addDataSlot(dataSlot(blockEntity::getProgress, value -> progress = value));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id == 0 && blockEntity.releaseSlime(player);
    }

    public boolean hasSlime() {
        return slimeType != IncubatorBlockEntity.SLIME_NONE;
    }

    public int getSlimeType() {
        return slimeType;
    }

    public int getSlimeSize() {
        return slimeSize;
    }

    public int getScaledProgress(int width) {
        return Mth.clamp(progress * width / IncubatorBlockEntity.MAX_PROGRESS, 0, width);
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

        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (SpeedUpgradeHelper.isSpeedUpgrade(stack)) {
            if (!moveItemStackTo(stack, 1, 5, false)) return ItemStack.EMPTY;
        } else if (IncubatorBlockEntity.isRawOre(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
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
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.INCUBATOR.get());
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

    private static IncubatorBlockEntity readBlockEntity(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof IncubatorBlockEntity slime_converter) {
            return slime_converter;
        }
        throw new IllegalStateException("Missing slime_converter block entity at " + pos);
    }
}
