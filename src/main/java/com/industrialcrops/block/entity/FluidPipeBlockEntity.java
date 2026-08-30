package com.industrialcrops.block.entity;

import com.industrialcrops.basic_pipe.FluidPipeTransferUtil;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FluidPipeBlockEntity extends BlockEntity {
    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidPipeBlockEntity entity) {
        FluidPipeTransferUtil.tick(level, pos);
    }
}
