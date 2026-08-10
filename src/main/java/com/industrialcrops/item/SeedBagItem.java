package com.industrialcrops.item;

import com.industrialcrops.block.IndustrialCropBlock;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class SeedBagItem extends Item {
    public SeedBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof IndustrialCropBlock crop)
                || !crop.isMaxAge(state)
                || crop.wereSeedsExtracted(state)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (!level.isClientSide() && player != null) {
            boolean resetsGrowth = crop.resetsAfterBagHarvest();
            ItemStack seed = crop.createSeedStack(level, context.getClickedPos(),
                    resetsGrowth ? 1 : 3 + level.random.nextInt(4));
            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            if (!player.getInventory().add(seed)) {
                player.drop(seed, false);
            }
            level.setBlock(context.getClickedPos(),
                    resetsGrowth ? crop.resetAfterBagHarvest() : crop.markSeedsExtracted(state), 3);
            if (resetsGrowth) {
                crop.resetGeneticsForRegrowth(level, context.getClickedPos());
            }
            level.playSound(null, context.getClickedPos(), SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS, 0.6F, 1.1F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.industrialcrops.empty_bag"));
    }
}
