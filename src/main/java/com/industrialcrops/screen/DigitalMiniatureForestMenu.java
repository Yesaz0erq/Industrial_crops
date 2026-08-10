package com.industrialcrops.screen;

import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
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

public final class DigitalMiniatureForestMenu extends AbstractContainerMenu {
    public static final int UPGRADE_X=-68,UPGRADE_Y=31,UPGRADE_SPACING=22;
    private final BlockPos pos; private final ContainerData data; private boolean upgradeSlotsVisible;
    public DigitalMiniatureForestMenu(int id,Inventory inv,RegistryFriendlyByteBuf buffer){this(id,inv,read(inv,buffer));}
    private DigitalMiniatureForestMenu(int id,Inventory inv,DigitalMiniatureForestBlockEntity forest){this(id,inv,forest,forest.getBlockPos());}
    public DigitalMiniatureForestMenu(int id,Inventory inv,DigitalMiniatureForestBlockEntity forest,BlockPos pos){
        super(ModMenus.DIGITAL_MINIATURE_FOREST.get(),id);this.pos=pos;this.data=forest.getData();
        for(int i=0;i<4;i++){int x=UPGRADE_X+i%2*UPGRADE_SPACING,y=UPGRADE_Y+i/2*UPGRADE_SPACING;addSlot(new SlotItemHandler(forest.getInventory(),i,x,y){
            @Override public boolean mayPlace(ItemStack stack){return SpeedUpgradeHelper.isSpeedUpgrade(stack);}
            @Override public boolean isActive(){return upgradeSlotsVisible;}
        });}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,8+col*18,84+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,142));addDataSlots(data);
    }
    public void setUpgradeSlotsVisible(boolean v){upgradeSlotsVisible=v;}
    public int energy(){return(data.get(0)&0xFFFF)|((data.get(1)&0xFFFF)<<16);} public int progress(){return data.get(2);} public int maxProgress(){return data.get(3);}
    public int treeId(){return data.get(4);} public int speedTier(){return data.get(5);} public boolean connected(){return data.get(6)>0;}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;Slot s=slots.get(index);if(!s.hasItem())return ItemStack.EMPTY;ItemStack st=s.getItem(),copy=st.copy();
        if(index<4){if(!moveItemStackTo(st,4,slots.size(),true))return ItemStack.EMPTY;}else if(SpeedUpgradeHelper.isSpeedUpgrade(st)){if(!moveItemStackTo(st,0,4,false))return ItemStack.EMPTY;}else return ItemStack.EMPTY;
        if(st.isEmpty())s.set(ItemStack.EMPTY);else s.setChanged();return st.getCount()==copy.getCount()?ItemStack.EMPTY:copy;}
    @Override public boolean stillValid(Player p){return stillValid(ContainerLevelAccess.create(p.level(),pos),p,ModBlocks.DIGITAL_MINIATURE_FOREST.get());}
    private static DigitalMiniatureForestBlockEntity read(Inventory inv,RegistryFriendlyByteBuf b){BlockEntity be=inv.player.level().getBlockEntity(b.readBlockPos());if(be instanceof DigitalMiniatureForestBlockEntity f)return f;throw new IllegalStateException("Missing digital miniature forest");}
}
