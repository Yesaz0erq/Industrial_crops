package com.industrialcrops.screen;

import com.industrialcrops.block.entity.StructureBuilderBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public final class StructureBuilderMenu extends AbstractContainerMenu {
    public static final int MACHINE_SLOTS=StructureBuilderBlockEntity.SLOTS;
    private final StructureBuilderBlockEntity machine;
    private final int[] data=new int[7+StructureBuilderBlockEntity.MATERIAL_ROWS*2];
    public StructureBuilderMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, (StructureBuilderBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }
    public StructureBuilderMenu(int id, Inventory inventory, StructureBuilderBlockEntity machine) {
        super(ModMenus.STRUCTURE_BUILDER.get(),id);
        this.machine=machine;
        for (int slot=0; slot<StructureBuilderBlockEntity.MATERIAL_SLOTS; slot++) {
            final int slotPage=slot/27;
            addSlot(new SlotItemHandler(machine.getInventory(),slot,12+slot%9*18,78+(slot%27)/9*18) {
                @Override public boolean isActive() { return page() == slotPage; }
                @Override public boolean mayPlace(ItemStack stack) { return !running(); }
                @Override public boolean mayPickup(Player player) { return !running(); }
            });
        }
        for (int i=0; i<4; i++) addSlot(new SlotItemHandler(machine.getInventory(),108+i,194+i*22,178) {
            @Override public boolean mayPlace(ItemStack stack) { return !running() && com.industrialcrops.machine.SpeedUpgradeHelper.isSpeedUpgrade(stack); }
            @Override public boolean mayPickup(Player player) { return !running(); }
        });
        for (int row=0; row<3; row++) for (int col=0; col<9; col++)
            addSlot(new Slot(inventory,col+row*9+9,12+col*18,151+row*18));
        for (int col=0; col<9; col++) addSlot(new Slot(inventory,col,12+col*18,209));
        for (int i=0; i<data.length; i++) {
            final int index=i;
            addDataSlot(new DataSlot() {
                @Override public int get() { return machine.value(index); }
                @Override public void set(int value) { data[index]=value; }
            });
            data[i]=machine.value(i);
        }
    }
    private int field(int index) { return machine.getLevel() != null && !machine.getLevel().isClientSide() ? machine.value(index) : data[index]; }
    public int speed() { return field(6+StructureBuilderBlockEntity.MATERIAL_ROWS*2); }
    public int type() { return field(0); }
    public int tier() { return field(1); }
    public int page() { return field(2); }
    public int status() { return field(3); }
    public boolean running() { return status()==StructureBuilderBlockEntity.RUNNING; }
    public int progress() { return field(4); }
    public int total() { return field(5); }
    public ItemStack material(int row) { return field(6+row*2)==0 ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.byId(field(6+row*2))); }
    public int needed(int row) { return field(7+row*2); }
    public int stock(ItemStack material) {
        int count=0;
        for (int i=0; i<StructureBuilderBlockEntity.MATERIAL_SLOTS; i++) if (slots.get(i).getItem().is(material.getItem())) count+=slots.get(i).getItem().getCount();
        return count;
    }
    @Override public boolean clickMenuButton(Player player,int id) {
        return stillValid(player) && machine.button(player,id);
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if (running() || index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(), copy=stack.copy();
        if (index<MACHINE_SLOTS) {
            if (!moveItemStackTo(stack,MACHINE_SLOTS,slots.size(),true)) return ItemStack.EMPTY;
        } else {
            if (com.industrialcrops.machine.SpeedUpgradeHelper.isSpeedUpgrade(stack)) moveItemStackTo(stack,108,112,false);
            if (!stack.isEmpty() && !moveItemStackTo(stack,0,108,false) && stack.getCount()==copy.getCount()) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount()==copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player,stack);
        return copy;
    }
    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(),machine.getBlockPos()),player,ModBlocks.STRUCTURE_BUILDER.get());
    }
}
