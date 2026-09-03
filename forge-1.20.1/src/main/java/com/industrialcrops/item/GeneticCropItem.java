package com.industrialcrops.item;

import com.industrialcrops.crop.CropGenetics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GeneticCropItem extends Item {
    public static final int CROP_STACK_LIMIT = 999;

    public GeneticCropItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return CropGenetics.createInitialStack(this);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return CROP_STACK_LIMIT;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide()) {
            CropGenetics.initializeInventoryStack(stack, level.random);
        }
    }
}
