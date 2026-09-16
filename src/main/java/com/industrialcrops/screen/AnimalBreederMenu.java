package com.industrialcrops.screen;

import com.industrialcrops.block.entity.AnimalBreederBlockEntity;
import com.industrialcrops.registry.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AnimalBreederMenu extends AbstractContainerMenu {
    private final AnimalBreederBlockEntity machine;
    public AnimalBreederMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,(AnimalBreederBlockEntity)inv.player.level().getBlockEntity(buf.readBlockPos()));}
    public AnimalBreederMenu(int id,Inventory inv,AnimalBreederBlockEntity machine){
        super(ModMenus.ANIMAL_BREEDER.get(),id);this.machine=machine;addDataSlots(machine.data());
        for(int i=0;i<9;i++)addSlot(new SlotItemHandler(machine.inventory(),i,30+i*19,152));
        for(int i=0;i<9;i++)addSlot(new SlotItemHandler(machine.inventory(),i+9,30+i*19,192){@Override public boolean mayPlace(ItemStack s){return false;}});
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,30+c*19,234+r*19));
        for(int c=0;c<9;c++)addSlot(new Slot(inv,c,30+c*19,296));
    }
    public AnimalBreederBlockEntity machine(){return machine;}
    @Override public boolean stillValid(Player p){return machine!=null&&!machine.isRemoved()&&p.level()==machine.getLevel()
            &&stillValid(ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),p,ModBlocks.ANIMAL_BREEDER.get());}
    public void configure(Player p,int limit,boolean wide,boolean enabled,boolean harvest,boolean slaughter){
        if(!p.level().isClientSide&&stillValid(p)&&limit>=2&&limit<=4096)machine.configure(limit,wide,enabled,harvest,slaughter);
    }
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var copy=stack.copy();
        if(index<18){if(!moveItemStackTo(stack,18,54,true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,9,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;
    }
}
