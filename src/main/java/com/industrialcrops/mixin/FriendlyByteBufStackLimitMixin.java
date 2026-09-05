package com.industrialcrops.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Carry oversized counts in the share tag while retaining the 1.20.1 packet layout. */
@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufStackLimitMixin {
    @Unique private static final String INDUSTRIALCROPS_COUNT = "industrialcrops:network_count";

    @Redirect(method = "writeItemStack", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", remap = true))
    private int industrialcrops$boundedWireCount(ItemStack stack) {
        return Math.min(Byte.MAX_VALUE, stack.getCount());
    }

    @Redirect(method = "writeItemStack", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeNbt(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/network/FriendlyByteBuf;", remap = true))
    private FriendlyByteBuf industrialcrops$writeFullCount(FriendlyByteBuf buffer, CompoundTag tag, ItemStack stack, boolean limited) {
        if (stack.getCount() > Byte.MAX_VALUE) {
            tag = tag == null ? new CompoundTag() : tag.copy();
            tag.putInt(INDUSTRIALCROPS_COUNT, stack.getCount());
        }
        return buffer.writeNbt(tag);
    }

    @Redirect(method = "readItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;readShareTag(Lnet/minecraft/nbt/CompoundTag;)V", remap = false))
    private void industrialcrops$readFullCount(ItemStack stack, CompoundTag tag) {
        if (tag != null && tag.contains(INDUSTRIALCROPS_COUNT, Tag.TAG_INT)) {
            stack.setCount(tag.getInt(INDUSTRIALCROPS_COUNT));
            tag.remove(INDUSTRIALCROPS_COUNT);
            if (tag.isEmpty()) tag = null;
        }
        stack.readShareTag(tag);
    }
}
