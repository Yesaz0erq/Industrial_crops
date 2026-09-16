package com.industrialcrops.screen;

import com.industrialcrops.block.entity.TerrainProcessorBlockEntity;
import com.industrialcrops.registry.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public final class TerrainProcessorMenu extends AbstractContainerMenu implements UpgradeableMenu {
    private boolean upgradeSlotsVisible=true;
    public void setUpgradeSlotsVisible(boolean visible){upgradeSlotsVisible=visible;}
    private final TerrainProcessorBlockEntity machine;
    public TerrainProcessorMenu(int id,Inventory inventory,FriendlyByteBuf buffer) {
        this(id,inventory,(TerrainProcessorBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }
    public TerrainProcessorMenu(int id,Inventory inventory,TerrainProcessorBlockEntity machine) {
        super(ModMenus.TERRAIN_PROCESSOR.get(),id);this.machine=machine;
        addSlot(new SlotItemHandler(machine.inventory(),0,19,77));
        addSlot(new SlotItemHandler(machine.inventory(),1,191,77));
        for(int i=0;i<9;i++)addSlot(new SlotItemHandler(machine.inventory(),i+2,27+i*19,176) {
            @Override public boolean mayPlace(ItemStack stack){return com.industrialcrops.block.entity.TerrainProcessorBlockEntity.isBuildingMaterial(stack);}
        });
        for(int i=0;i<4;i++)addSlot(new SlotItemHandler(machine.upgrades(),i,-58+i%2*18,24+i/2*18) {
            @Override public boolean isActive(){return upgradeSlotsVisible;}
        });
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,27+col*19,214+row*19));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,27+col*19,274));
    }
    public TerrainProcessorBlockEntity machine(){return machine;}
    @Override public boolean stillValid(Player player) {
        return machine!=null&&!machine.isRemoved()&&player.level()==machine.getLevel()
                &&stillValid(ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),player,ModBlocks.TERRAIN_PROCESSOR.get());
    }
    public void configure(Player player,int cx,int cz,int y1,int y2,boolean fill,boolean replaceOnly,int action) {
        if(!player.level().isClientSide&&stillValid(player))machine.configure(player,cx,cz,y1,y2,fill,replaceOnly,action);
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size())return ItemStack.EMPTY;
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index<15){if(!moveItemStackTo(stack,15,51,true))return ItemStack.EMPTY;}
        else if(TerrainProcessorBlockEntity.isUpgrade(stack)){
            if(!moveItemStackTo(stack,11,15,false))return ItemStack.EMPTY;
        }else if(TerrainProcessorBlockEntity.isBuildingMaterial(stack)){
            if(!moveItemStackTo(stack,1,2,false))return ItemStack.EMPTY;
        }else return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        slot.onTake(player,stack);return copy;
    }
}
