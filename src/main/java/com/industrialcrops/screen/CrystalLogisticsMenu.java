package com.industrialcrops.screen;

import com.industrialcrops.block.entity.CrystalLogisticsBlockEntity;
import com.industrialcrops.registry.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class CrystalLogisticsMenu extends AbstractContainerMenu {
    private final CrystalLogisticsBlockEntity machine;
    public CrystalLogisticsMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, (CrystalLogisticsBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }
    public CrystalLogisticsMenu(int id, Inventory inventory, CrystalLogisticsBlockEntity machine) {
        super(ModMenus.CRYSTAL_LOGISTICS.get(), id); this.machine = machine;
        for (int slot=0; slot<9; slot++) addSlot(new SlotItemHandler(machine.inventory(), slot, CrystalLogisticsLayout.GRID_X+CrystalLogisticsLayout.SLOT_STEP*slot, CrystalLogisticsLayout.BUFFER_Y));
        addSlot(new SlotItemHandler(machine.upgrade(), 0, CrystalLogisticsLayout.UPGRADE_X, CrystalLogisticsLayout.UPGRADE_Y));
        for (int row=0; row<3; row++) for (int col=0; col<9; col++) addSlot(new Slot(inventory, col+row*9+9, CrystalLogisticsLayout.GRID_X+col*CrystalLogisticsLayout.SLOT_STEP, CrystalLogisticsLayout.INVENTORY_Y+row*CrystalLogisticsLayout.SLOT_STEP));
        for (int col=0; col<9; col++) addSlot(new Slot(inventory, col, CrystalLogisticsLayout.GRID_X+col*CrystalLogisticsLayout.SLOT_STEP, CrystalLogisticsLayout.HOTBAR_Y));
    }
    public CrystalLogisticsBlockEntity machine() { return machine; }
    public void configure(Player player, String channel, boolean output) {
        if (!player.level().isClientSide && stillValid(player)) machine.configure(channel, output);
    }
    @Override public boolean stillValid(Player player) {
        return !machine.isRemoved() && player.level() == machine.getLevel() && stillValid(ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()), player, ModBlocks.CRYSTAL_LOGISTICS.get());
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), copy = stack.copy();
        if (index < 10) { if (!moveItemStackTo(stack, 10, 46, true)) return ItemStack.EMPTY; }
        else if (machine.upgrade().isItemValid(0, stack)) { if (!moveItemStackTo(stack, 9, 10, false)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack, 0, 9, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack); return copy;
    }
}
