package com.industrialcrops.item;

import com.industrialcrops.effect.OmniEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;

/** Omni Carrot: food that grants every beneficial vanilla effect for 5 minutes. */
public final class OmniCarrotItem extends ItemNameBlockItem {
    public static final int DURATION = 20 * 60 * 5;

    public OmniCarrotItem(Block crop, Properties properties) {
        super(crop, properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            OmniEffects.apply(entity, DURATION);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
