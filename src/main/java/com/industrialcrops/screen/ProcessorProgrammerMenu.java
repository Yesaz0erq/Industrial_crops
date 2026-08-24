package com.industrialcrops.screen;

import com.industrialcrops.block.entity.ProcessorProgrammerBlockEntity;
import com.industrialcrops.recipe.ProcessorProgrammingRecipes;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.machine.SpeedUpgradeHelper;
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

public final class ProcessorProgrammerMenu extends AbstractContainerMenu implements UpgradeableMenu {
    public static final int UPGRADE_X=-68,UPGRADE_Y=28,UPGRADE_SPACING=22;
    private static final int PLAYER_START = ProcessorProgrammerBlockEntity.SLOT_COUNT + 4;
    private static final int PLAYER_END = PLAYER_START + 36;
    private final BlockPos pos;
    private int progress;
    private boolean upgradeSlotsVisible;

    public ProcessorProgrammerMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, readBlockEntity(inventory, buffer));
    }

    private ProcessorProgrammerMenu(int containerId, Inventory inventory, ProcessorProgrammerBlockEntity programmer) {
        this(containerId, inventory, programmer, programmer.getBlockPos());
    }

    public ProcessorProgrammerMenu(
            int containerId,
            Inventory playerInventory,
            ProcessorProgrammerBlockEntity programmer,
            BlockPos pos
    ) {
        super(ModMenus.PROCESSOR_PROGRAMMER.get(), containerId);
        this.pos = pos;
        for (int slot = 0; slot < ProcessorProgrammerBlockEntity.INPUT_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(programmer.getInventory(), slot, 17 + slot * 18, 35));
        }
        addSlot(new SlotItemHandler(programmer.getInventory(), ProcessorProgrammerBlockEntity.OUTPUT_SLOT, 143, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for(int i=0;i<4;i++){int x=UPGRADE_X+i%2*22,y=UPGRADE_Y+i/2*22;addSlot(new SlotItemHandler(programmer.getInventory(),ProcessorProgrammerBlockEntity.UPGRADE_SLOT_START+i,x,y){@Override public boolean mayPlace(ItemStack stack){return SpeedUpgradeHelper.isSpeedUpgrade(stack);}@Override public boolean isActive(){return upgradeSlotsVisible;}});}
        addPlayerInventory(playerInventory, 8, 84);
        addPlayerHotbar(playerInventory, 8, 142);
        addDataSlot(new DataSlot() {
            @Override public int get() { return programmer.getProgress(); }
            @Override public void set(int value) { progress = value; }
        });
    }

    public int getScaledProgress(int width) {
        return Math.max(0, Math.min(width, progress * width / ProcessorProgrammerBlockEntity.MAX_PROGRESS));
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
        if (slotIndex < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (ProcessorProgrammingRecipes.isIngredient(stack)) {
            if (!moveItemStackTo(stack, 0, ProcessorProgrammerBlockEntity.INPUT_SLOT_COUNT, false)
                    && (!SpeedUpgradeHelper.isSpeedUpgrade(stack)
                    || !moveItemStackTo(stack, ProcessorProgrammerBlockEntity.UPGRADE_SLOT_START, PLAYER_START, false))) {
                return ItemStack.EMPTY;
            }
        } else if (SpeedUpgradeHelper.isSpeedUpgrade(stack)) {
            if (!moveItemStackTo(stack, ProcessorProgrammerBlockEntity.UPGRADE_SLOT_START, PLAYER_START, false)) return ItemStack.EMPTY;
        } else {
            int inventoryEnd = PLAYER_START + 27;
            if (slotIndex < inventoryEnd) {
                if (!moveItemStackTo(stack, inventoryEnd, PLAYER_END, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, PLAYER_START, inventoryEnd, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.PROCESSOR_PROGRAMMER.get());
    }

    private void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
    }

    private void addPlayerHotbar(Inventory inventory, int x, int y) {
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, x + column * 18, y));
    }

    private static ProcessorProgrammerBlockEntity readBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ProcessorProgrammerBlockEntity programmer) return programmer;
        throw new IllegalStateException("Missing processor programmer block entity at " + pos);
    }
}
