package com.industrialcrops.item;

import com.industrialcrops.block.AdvancedManipulatorBlock;
import com.industrialcrops.block.BasicManipulatorBlock;
import com.industrialcrops.registry.ModBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvancedManipulatorUpgradeItem extends Item {
    public AdvancedManipulatorUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!state.is(ModBlocks.BASIC_MANIPULATOR.get())) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            BlockState upgraded = ModBlocks.ADVANCED_MANIPULATOR.get().defaultBlockState()
                    .setValue(AdvancedManipulatorBlock.FACING, state.getValue(BasicManipulatorBlock.FACING));
            context.getLevel().setBlock(context.getClickedPos(), upgraded, 3);
            context.getLevel().playSound(
                    null,
                    context.getClickedPos(),
                    SoundEvents.SMITHING_TABLE_USE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
