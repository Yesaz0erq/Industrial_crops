package com.industrialcrops.machine;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Store quantity separately from the normal ItemStack codec's count limit. */
public class BulkStorageItemHandler extends ItemStackHandler {
    public BulkStorageItemHandler(int slots) { super(slots); }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            CompoundTag item = (CompoundTag) stack.copyWithCount(1).save(registries);
            item.putInt("Slot", slot);
            item.putInt("IndustrialCount", stack.getCount());
            items.add(item);
        }
        tag.putInt("Size", getSlots());
        tag.put("Items", items);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        super.deserializeNBT(registries, tag);
        ListTag items = tag.getList("Items", 10);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getInt("Slot");
            if (slot >= 0 && slot < getSlots() && item.contains("IndustrialCount")) {
                getStackInSlot(slot).setCount(Math.max(0, item.getInt("IndustrialCount")));
            }
        }
    }
}
