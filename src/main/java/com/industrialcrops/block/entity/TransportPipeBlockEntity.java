package com.industrialcrops.block.entity;

import com.industrialcrops.basic_pipe.PipeTransferUtil;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TransportPipeBlockEntity extends BlockEntity {
    public TransportPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSPORT_PIPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransportPipeBlockEntity blockEntity) {
        if (!level.isClientSide() && PipeTransferUtil.isOutputPipe(state)) {
            PipeTransferUtil.tickOutputPipe(level, pos, state);
        }
    }
}
