package com.industrialcrops.compat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
/** Forge 1.20.1 equivalent of the custom-data storage payload. */
public final class LegacyItemData {
    private LegacyItemData() {}
    public static CustomData get(ItemStack stack) { return stack.hasTag() ? CustomData.of(stack.getTag()) : null; }
    public static void set(ItemStack stack, CustomData data) { stack.setTag(data.copyTag()); }
    public static void update(ItemStack stack, java.util.function.Consumer<CompoundTag> change) {
        CompoundTag tag = stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        change.accept(tag); stack.setTag(tag);
    }
    public record CustomData(CompoundTag tag) {
        public static CustomData of(CompoundTag tag) { return new CustomData(tag.copy()); }
        public CompoundTag copyTag() { return tag.copy(); }
    }
}
