package com.industrialcrops.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Item with a permanent enchantment-style shimmer. */
public final class ShimmeringItem extends Item {
    public ShimmeringItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
