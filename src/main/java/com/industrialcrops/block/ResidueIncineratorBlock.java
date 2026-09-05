package com.industrialcrops.block;

import com.industrialcrops.block.entity.ResidueIncineratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ResidueIncineratorBlock extends BioEnergyMachineBlock {
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;
    public ResidueIncineratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ResidueIncineratorBlockEntity(pos, state); }
}
