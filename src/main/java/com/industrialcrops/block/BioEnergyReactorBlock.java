package com.industrialcrops.block;

import com.mojang.serialization.MapCodec;
import com.industrialcrops.block.entity.BioEnergyGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Crystal-steel reactor upgrade of the bio-energy generation device. */
public final class BioEnergyReactorBlock extends BioEnergyMachineBlock {
    public static final MapCodec<BioEnergyReactorBlock> CODEC = simpleCodec(BioEnergyReactorBlock::new);

    public BioEnergyReactorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BioEnergyMachineBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BioEnergyGeneratorBlockEntity(pos, state);
    }
}
