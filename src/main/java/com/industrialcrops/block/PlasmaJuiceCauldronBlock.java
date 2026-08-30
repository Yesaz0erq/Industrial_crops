package com.industrialcrops.block;

import com.industrialcrops.mechanic.PlasmaTransmutation;
import com.industrialcrops.registry.ModCauldronInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class PlasmaJuiceCauldronBlock extends AbstractCauldronBlock {
    public PlasmaJuiceCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, ModCauldronInteractions.PLASMA_JUICE);
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return 0.9375D;
    }

    @Override
    public boolean isFull(BlockState state) {
        return true;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity itemEntity && isEntityInsideContent(state, pos, itemEntity)) {
            PlasmaTransmutation.tryConvertDiamond(level, itemEntity);
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return 3;
    }
}
