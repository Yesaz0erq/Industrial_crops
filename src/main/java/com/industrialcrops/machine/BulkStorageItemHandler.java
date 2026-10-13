package com.industrialcrops.machine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
/** Preserve oversized storage counts without the vanilla byte-sized Count field. */
public class BulkStorageItemHandler extends ItemStackHandler {
    public BulkStorageItemHandler(int slots) { super(slots); }
    @Override public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            CompoundTag item = stack.copyWithCount(1).save(new CompoundTag());
            item.putInt("Slot", slot);
            item.putInt("IndustrialCount", stack.getCount());
            items.add(item);
        }
        tag.putInt("Size", getSlots());
        tag.put("Items", items);
        return tag;
    }
    @Override public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        ListTag items = tag.getList("Items", 10);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getInt("Slot");
            if (slot >= 0 && slot < getSlots() && item.contains("IndustrialCount"))
                getStackInSlot(slot).setCount(Math.max(0, item.getInt("IndustrialCount")));
        }
    }
}
