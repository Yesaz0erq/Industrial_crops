package com.industrialcrops.screen;

import com.industrialcrops.block.entity.CraftingProcessorBlockEntity;
import com.industrialcrops.registry.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class CraftingProcessorMenu extends AbstractContainerMenu {
    private final CraftingProcessorBlockEntity machine;
    public CraftingProcessorMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer) {
        this(id,inventory,(CraftingProcessorBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }
    public CraftingProcessorMenu(int id,Inventory inventory,CraftingProcessorBlockEntity machine) {
        super(ModMenus.CRAFTING_PROCESSOR.get(),id); this.machine=machine;
        addSlot(new SlotItemHandler(machine.inventory(),0,19,42));
        addSlot(new SlotItemHandler(machine.inventory(),1,191,42));
        for(int i=0;i<9;i++) addSlot(new SlotItemHandler(machine.inventory(),i+2,27+18*i,123) {
            @Override public boolean mayPlace(ItemStack stack){return false;}
        });
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(inventory,col+row*9+9,27+col*18,161+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inventory,col,27+col*18,219));
    }
    public CraftingProcessorBlockEntity machine(){return machine;}
    @Override public boolean stillValid(Player player) {
        return machine!=null && !machine.isRemoved() && player.level()==machine.getLevel() &&
                stillValid(ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),player,ModBlocks.CRAFTING_PROCESSOR.get());
    }
    @Override public boolean clickMenuButton(Player player,int id) {
        if(player.level().isClientSide || !stillValid(player)) return false;
        switch(id) {
            case 0 -> machine.process(false);
            case 1 -> machine.process(true);
            case 2 -> machine.setRequested(machine.requested()-1);
            case 3 -> machine.setRequested(machine.requested()+1);
            case 4 -> machine.setRequested(1);
            case 5 -> machine.setRequested(64);
            default -> {return false;}
        }
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index); if(!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index<11) {if(!moveItemStackTo(stack,11,47,true)) return ItemStack.EMPTY;}
        else if(stack.is(ModItems.REMOTE_ACCESS_DEVICE.get())) {if(!moveItemStackTo(stack,1,2,false)) return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,1,false)) return ItemStack.EMPTY;
        if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player,stack); return copy;
    }
}
