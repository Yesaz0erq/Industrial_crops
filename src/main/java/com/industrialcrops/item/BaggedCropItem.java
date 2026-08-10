package com.industrialcrops.item;

import com.industrialcrops.crop.CropGenetics;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
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

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        int previousCount = stack.getCount();
        InteractionResult result = super.useOn(context);
        Player player = context.getPlayer();
        if (!context.getLevel().isClientSide()
                && player != null
                && !player.getAbilities().instabuild
                && result.consumesAction()
                && stack.getCount() < previousCount) {
            BagItemHelper.giveEmptyBag(player);
        }
        return result;
    }
}
