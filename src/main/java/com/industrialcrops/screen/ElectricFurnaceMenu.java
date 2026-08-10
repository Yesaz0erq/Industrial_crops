package com.industrialcrops.screen;

import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ElectricFurnaceMenu extends AbstractContainerMenu {
    public static final int UPGRADE_X = -68, UPGRADE_Y = 31, UPGRADE_SPACING = 22;
    private static final int MACHINE_SLOTS = 10;
    private final ElectricFurnaceBlockEntity furnace;
    private final BlockPos pos;
    private final ContainerData data;
    private boolean upgradeSlotsVisible;

    public ElectricFurnaceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, read(inventory, buffer));
    }
    private ElectricFurnaceMenu(int id, Inventory inventory, ElectricFurnaceBlockEntity furnace) {
        this(id, inventory, furnace, furnace.getBlockPos());
    }
    public ElectricFurnaceMenu(int id, Inventory playerInventory, ElectricFurnaceBlockEntity furnace, BlockPos pos) {
        super(ModMenus.ELECTRIC_FURNACE.get(), id); this.furnace = furnace; this.pos = pos; this.data = furnace.getData();
        for (int channel = 0; channel < 3; channel++) {
            addSlot(new SlotItemHandler(furnace.getInventory(), channel, 29, 24 + channel * 24));
            addSlot(new SlotItemHandler(furnace.getInventory(), 3 + channel, 129, 24 + channel * 24) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }
        for (int index = 0; index < 4; index++) {
            int x = UPGRADE_X + index % 2 * UPGRADE_SPACING, y = UPGRADE_Y + index / 2 * UPGRADE_SPACING;
            addSlot(new SlotItemHandler(furnace.getInventory(), 6 + index, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return SpeedUpgradeHelper.isSpeedUpgrade(stack); }
                @Override public boolean isActive() { return upgradeSlotsVisible; }
            });
        }
        for (int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(playerInventory,col+row*9+9,8+col*18,110+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(playerInventory,col,8+col*18,168));
        addDataSlots(data);
    }
    public void setUpgradeSlotsVisible(boolean visible) { upgradeSlotsVisible = visible; }
    public int energy() { return (data.get(0)&0xFFFF)|((data.get(1)&0xFFFF)<<16); }
    public int progress(int channel) { return data.get(2+channel); }
    public int maxProgress() { return data.get(5); }
    public int speedTier() { return data.get(6); }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size())return ItemStack.EMPTY; Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index<MACHINE_SLOTS){if(!moveItemStackTo(stack,MACHINE_SLOTS,slots.size(),true))return ItemStack.EMPTY;}
        else if(SpeedUpgradeHelper.isSpeedUpgrade(stack)){if(!moveItemStackTo(stack,6,10,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,6,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();return stack.getCount()==copy.getCount()?ItemStack.EMPTY:copy;
    }
    @Override public boolean stillValid(Player player){return stillValid(ContainerLevelAccess.create(player.level(),pos),player,ModBlocks.ELECTRIC_FURNACE.get());}
    private static ElectricFurnaceBlockEntity read(Inventory inv,RegistryFriendlyByteBuf buffer){BlockEntity be=inv.player.level().getBlockEntity(buffer.readBlockPos());if(be instanceof ElectricFurnaceBlockEntity f)return f;throw new IllegalStateException("Missing electric furnace");}
}
