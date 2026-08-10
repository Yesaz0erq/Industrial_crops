package com.industrialcrops.item;

import com.industrialcrops.crop.CropGenetics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class GeneticCropBlockItem extends ItemNameBlockItem {
    public GeneticCropBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return CropGenetics.createInitialStack(this);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return GeneticCropItem.CROP_STACK_LIMIT;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide()) {
            CropGenetics.initializeInventoryStack(stack, level.random);
        }
    }
}
