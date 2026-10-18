package com.industrialcrops.item;

import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** An edible planting item that keeps its cultivated carrot name and permanent shimmer. */
public final class ShimmeringCarrotBlockItem extends ItemNameBlockItem {
    public ShimmeringCarrotBlockItem(Block crop, Properties properties) {
        super(crop, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
