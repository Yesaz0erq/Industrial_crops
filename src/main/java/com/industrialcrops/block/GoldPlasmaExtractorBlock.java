package com.industrialcrops.block;

import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;

public final class GoldPlasmaExtractorBlock extends GoldPoweredMachineBlock {
    public static final MapCodec<GoldPlasmaExtractorBlock> CODEC = simpleCodec(GoldPlasmaExtractorBlock::new);

    public GoldPlasmaExtractorBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends GoldPoweredMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GoldPlasmaExtractorBlockEntity(pos, state); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                        Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(com.industrialcrops.registry.ModItems.PLASMA_BERRY.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GoldPlasmaExtractorBlockEntity extractor) {
            int inserted = extractor.insertInput(stack.copy(), false);
            if (inserted > 0 && !player.getAbilities().instabuild) stack.shrink(inserted);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
