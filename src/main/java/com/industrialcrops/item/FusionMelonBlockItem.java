package com.industrialcrops.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Placeable fusion gourd that keeps the crop-family 999 item stack limit. */
public final class FusionMelonBlockItem extends BlockItem {
    public FusionMelonBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return GeneticCropItem.CROP_STACK_LIMIT;
    }
}
