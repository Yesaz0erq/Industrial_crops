package com.industrialcrops.util;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Small bridge from 1.21 data components to the 1.20.1 ItemStack tag API. */
public final class ItemStackNbt {
    private ItemStackNbt() {}

    public static void update(ItemStack stack, Consumer<CompoundTag> updater) {
        updater.accept(stack.getOrCreateTag());
    }

    public static CompoundTag copyTag(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
    }
}
