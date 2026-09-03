package com.industrialcrops.block.entity;

import com.industrialcrops.machine.DimensionUpgradeHelper;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public final class BasicControlDeviceBlockEntity extends BlockEntity {
    private boolean ticketRequested;
    private final ItemStackHandler upgrade = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return DimensionUpgradeHelper.isDimensionUpgrade(stack); }
        @Override protected void onContentsChanged(int slot) { setChanged(); refreshTicket(); }
    };

    public BasicControlDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_CONTROL_DEVICE.get(), pos, state);
    }

    public ItemStack getDimensionUpgrade() { return upgrade.getStackInSlot(0); }
    public boolean installDimensionUpgrade(ItemStack stack) {
        if (!getDimensionUpgrade().isEmpty() || !DimensionUpgradeHelper.isDimensionUpgrade(stack)) return false;
        ItemStack installed = stack.copy();
        installed.setCount(1);
        upgrade.setStackInSlot(0, installed);
        return true;
    }
    public ItemStack removeDimensionUpgrade() { return upgrade.extractItem(0, 1, false); }
    public void releaseTicket() {
        if (ticketRequested || !getDimensionUpgrade().isEmpty()) DimensionUpgradeHelper.forceOwnerChunk(this, false);
        ticketRequested = false;
    }
    @Override public void onLoad() { super.onLoad(); refreshTicket(); }
    private void refreshTicket() {
        boolean shouldForce = !getDimensionUpgrade().isEmpty();
        if (level != null && shouldForce != ticketRequested) {
            DimensionUpgradeHelper.forceOwnerChunk(this, shouldForce);
            ticketRequested = shouldForce;
        }
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("DimensionUpgrade", upgrade.serializeNBT());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("DimensionUpgrade")) upgrade.deserializeNBT(tag.getCompound("DimensionUpgrade"));
    }
}
