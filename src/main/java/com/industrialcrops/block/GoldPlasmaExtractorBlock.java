package com.industrialcrops.block;

import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;

public final class GoldPlasmaExtractorBlock extends GoldPoweredMachineBlock {
    public GoldPlasmaExtractorBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GoldPlasmaExtractorBlockEntity(pos, state); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(com.industrialcrops.registry.ModItems.PLASMA_BERRY.get())) {
            return super.use(state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GoldPlasmaExtractorBlockEntity extractor) {
            int inserted = extractor.insertInput(stack.copy(), false);
            if (inserted > 0 && !player.getAbilities().instabuild) stack.shrink(inserted);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
