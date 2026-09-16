package com.industrialcrops.block.entity;

import com.industrialcrops.item.RemoteAccessDeviceItem;
import com.industrialcrops.machine.*;
import com.industrialcrops.registry.*;
import com.industrialcrops.screen.CraftingProcessorMenu;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.*;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public final class CraftingProcessorBlockEntity extends BlockEntity implements MenuProvider {
    // 0 is a real, non-consumed sample; 1 is the remote link; 2..10 hold products and remainders.
    private final ItemStackHandler items=new ItemStackHandler(11) {
        @Override public int getSlotLimit(int slot) { return slot<2?1:64; }
        @Override public boolean isItemValid(int slot,ItemStack stack) { return slot==0 || slot==1 && stack.is(ModItems.REMOTE_ACCESS_DEVICE.get()); }
        @Override protected void onContentsChanged(int slot) { setChanged(); if(slot<2) status="select"; sync(); }
    };
    private ItemStack ghost=ItemStack.EMPTY;
    private String status="select", binding="";
    private int operations, requested=1;
    private long lastRequest=-20;
    public CraftingProcessorBlockEntity(BlockPos pos,BlockState state) { super(ModBlockEntities.CRAFTING_PROCESSOR.get(),pos,state); }
    public ItemStackHandler inventory() { return items; }
    public ItemStack target() { return items.getStackInSlot(0).isEmpty()?ghost:items.getStackInSlot(0); }
    public String status() { return status; }
    public String binding() { return binding; }
    public int operations() { return operations; }
    public int requested() { return requested; }
    public void setRequested(int n) { requested=net.minecraft.util.Mth.clamp(n,1,64); status="check"; sync(); }
    public void selectTarget(ItemStack stack) { ghost=stack.copyWithCount(1); status="check"; setChanged(); sync(); }
    public BlockEntity resolveSource() {
        if (!(level instanceof ServerLevel server)) return null;
        ItemStack remote=items.getStackInSlot(1);
        if (!remote.isEmpty()) {
            var link=RemoteAccessDeviceItem.readBinding(remote);
            if(link==null) { status="unbound"; return null; }
            var targetLevel=server.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,link.dimension()));
            if(targetLevel==null || !targetLevel.hasChunkAt(link.pos())) { status="unloaded"; return null; }
            var be=targetLevel.getBlockEntity(link.pos());
            if(!CraftingInventorySource.supported(be)) { status="unbound"; return null; }
            ItemStack upgrade=DimensionUpgradeHelper.installedUpgrade(targetLevel,link.pos());
            if(targetLevel!=level ? !DimensionUpgradeHelper.isInfinite(upgrade) : upgrade.isEmpty() && worldPosition.distSqr(link.pos())>128*128) {
                status="range"; return null;
            }
            binding=be.getBlockState().getBlock().getDescriptionId()+"|"+link.pos().toShortString(); return be;
        }
        BlockEntity nearest=null; double distance=65;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-8,-8,-8),worldPosition.offset(8,8,8))) {
            double d=pos.distSqr(worldPosition);
            if(d>64 || d>=distance || !level.hasChunkAt(pos)) continue;
            BlockEntity be=level.getBlockEntity(pos);
            if(CraftingInventorySource.supported(be)) { nearest=be; distance=d; }
        }
        if(nearest!=null) binding=nearest.getBlockState().getBlock().getDescriptionId()+"|"+nearest.getBlockPos().toShortString();
        else { binding=""; status="unbound"; }
        return nearest;
    }
    public void process(boolean execute) {
        if(level==null || level.isClientSide || level.getGameTime()-lastRequest<5) return;
        lastRequest=level.getGameTime(); operations=0; binding="";
        if(target().isEmpty()) { status="select"; sync(); return; }
        BlockEntity be=resolveSource();
        if(be==null) { sync(); return; }
        var source=new CraftingInventorySource(be);
        var planner=new RecursiveCraftingPlanner(level,source.snapshot());
        var plan=planner.plan(target().copyWithCount(1),requested);
        if(plan==null) { status=planner.limited()?"limit":"missing"; sync(); return; }
        // Stage all products in a private handler before touching the bound inventory.
        ItemStackHandler staged=new ItemStackHandler(9);
        for(int i=0;i<9;i++) staged.setStackInSlot(i,items.getStackInSlot(i+2).copy());
        for(ItemStack output:plan.outputs()) if(!ItemHandlerHelper.insertItemStacked(staged,output.copy(),false).isEmpty()) {
            status="full"; sync(); return;
        }
        operations=plan.operations(); status="ready";
        if(execute) {
            if(!source.commit(plan.withdrawn())) status="changed";
            else {
                for(int i=0;i<9;i++) items.setStackInSlot(i+2,staged.getStackInSlot(i));
                status="done"; setChanged();
            }
        }
        sync();
    }
    public IItemHandler outputCapability() {
        return new IItemHandler() {
            public int getSlots(){return 9;}
            public ItemStack getStackInSlot(int slot){return items.getStackInSlot(slot+2);}
            public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}
            public ItemStack extractItem(int slot,int amount,boolean simulate){return items.extractItem(slot+2,amount,simulate);}
            public int getSlotLimit(int slot){return 64;}
            public boolean isItemValid(int slot,ItemStack stack){return false;}
        };
    }
    public void dropContents() { if(level!=null) for(int i=0;i<11;i++) net.minecraft.world.Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,items.getStackInSlot(i).copy()); }
    private void sync() { if(level!=null&&!level.isClientSide) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Items",items.serializeNBT());
        if(!ghost.isEmpty()) tag.put("Target",ghost.save(new CompoundTag())); tag.putInt("Requested",requested);
        tag.putString("Status",status); tag.putString("Binding",binding); tag.putInt("Operations",operations);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); items.deserializeNBT(tag.getCompound("Items"));
        ghost=ItemStack.of(tag.getCompound("Target")); requested=net.minecraft.util.Mth.clamp(tag.getInt("Requested"),1,64);
        status=tag.getString("Status"); binding=tag.getString("Binding"); operations=tag.getInt("Operations");
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.crafting_processing_device"); }
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player) { resolveSource(); sync(); return new CraftingProcessorMenu(id,inventory,this); }

    private LazyOptional<net.minecraftforge.items.IItemHandler> forgeCapability0 = LazyOptional.of(this::outputCapability);
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @org.jetbrains.annotations.Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return forgeCapability0.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps();
        forgeCapability0.invalidate();
    }
    @Override public void reviveCaps() {
        super.reviveCaps();
        forgeCapability0 = LazyOptional.of(this::outputCapability);
    }
}
