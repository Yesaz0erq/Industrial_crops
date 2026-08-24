package com.industrialcrops.block;
import com.industrialcrops.block.entity.MatterReconstructorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class MatterReconstructorBlock extends MatterMachineBlock {
    public MatterReconstructorBlock(Properties properties) { super(properties); }
@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MatterReconstructorBlockEntity(pos, state); }
}
