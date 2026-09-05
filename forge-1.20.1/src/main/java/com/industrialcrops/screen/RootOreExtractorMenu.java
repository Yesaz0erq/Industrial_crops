package com.industrialcrops.screen;

import com.industrialcrops.block.entity.RootOreExtractorBlockEntity;
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

public final class RootOreExtractorMenu extends AbstractContainerMenu implements UpgradeableMenu {
    private static final int INPUT_SLOT = RootOreExtractorBlockEntity.INPUT_SLOT;
    private static final int CATALYST_SLOT = RootOreExtractorBlockEntity.CATALYST_SLOT;
    private static final int BAG_SLOT = RootOreExtractorBlockEntity.BAG_SLOT;
    private static final int OUTPUT_SLOT = RootOreExtractorBlockEntity.OUTPUT_SLOT;
    public static final int UPGRADE_X=-68,UPGRADE_Y=28,UPGRADE_SPACING=22;
    private static final int MACHINE_SLOT_COUNT = 8;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final RootOreExtractorBlockEntity blockEntity;
    private final BlockPos pos;
    private int progress;
    private int hasCatalyst;
    private boolean upgradeSlotsVisible;

    public RootOreExtractorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer));
    }

    private RootOreExtractorMenu(int containerId, Inventory playerInventory, RootOreExtractorBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    public RootOreExtractorMenu(int containerId, Inventory playerInventory, RootOreExtractorBlockEntity blockEntity, BlockPos pos) {
        super(ModMenus.ROOT_ORE_EXTRACTOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = pos;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), INPUT_SLOT, 39, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return RootOreExtractorBlockEntity.isAcceptedInput(stack);
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getInventory(), CATALYST_SLOT, 51, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return RootOreExtractorBlockEntity.isCatalyst(stack);
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getInventory(), BAG_SLOT, 63, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return RootOreExtractorBlockEntity.isEmptyBag(stack);
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getInventory(), OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for(int i=0;i<4;i++){int x=UPGRADE_X+i%2*22,y=UPGRADE_Y+i/2*22;addSlot(new SlotItemHandler(blockEntity.getInventory(),RootOreExtractorBlockEntity.UPGRADE_SLOT_START+i,x,y){@Override public boolean mayPlace(ItemStack stack){return SpeedUpgradeHelper.isSpeedUpgrade(stack);}@Override public boolean isActive(){return upgradeSlotsVisible;}});}

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
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.hasCatalyst() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                hasCatalyst = value;
            }
        });
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isProcessing() {
        return getProgress() > 0;
    }

    public boolean hasCatalyst() {
        return hasCatalyst > 0;
    }
    public void setUpgradeSlotsVisible(boolean visible){upgradeSlotsVisible=visible;}

    public int getScaledProgress(int pixels) {
        return Mth.clamp(getProgress() * pixels / RootOreExtractorBlockEntity.MAX_PROGRESS, 0, pixels);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        moved = original.copy();

        if (slotIndex == OUTPUT_SLOT) {
            if (!moveItemStackTo(original, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(original, moved);
        } else if (slotIndex == INPUT_SLOT || slotIndex == CATALYST_SLOT || slotIndex == BAG_SLOT) {
            if (!moveItemStackTo(original, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SpeedUpgradeHelper.isSpeedUpgrade(original)) {
            if (!moveItemStackTo(original, RootOreExtractorBlockEntity.UPGRADE_SLOT_START,
                    RootOreExtractorBlockEntity.UPGRADE_SLOT_START + RootOreExtractorBlockEntity.UPGRADE_SLOT_COUNT,
                    false)) return ItemStack.EMPTY;
        } else if (RootOreExtractorBlockEntity.isAcceptedInput(original)) {
            if (!moveItemStackTo(original, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (RootOreExtractorBlockEntity.isCatalyst(original)) {
            if (!moveItemStackTo(original, CATALYST_SLOT, CATALYST_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (RootOreExtractorBlockEntity.isEmptyBag(original)) {
            if (!moveItemStackTo(original, BAG_SLOT, BAG_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(original, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_HOTBAR_END) {
            if (!moveItemStackTo(original, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (original.getCount() == moved.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, original);
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.ROOT_ORE_EXTRACTOR.get());
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

    private static RootOreExtractorBlockEntity readBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RootOreExtractorBlockEntity extractor) {
            return extractor;
        }
        throw new IllegalStateException("Missing root ore extractor block entity at " + pos);
    }
}
