package com.industrialcrops.item;

import com.industrialcrops.crop.CropGenetics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;

public final class BaggedCropItem extends ItemNameBlockItem {
    public BaggedCropItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return CropGenetics.createInitialStack(this);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide()) {
            CropGenetics.initializeInventoryStack(stack, level.random);
        }
    }

}
