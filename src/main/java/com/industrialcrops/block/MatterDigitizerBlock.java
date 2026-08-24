package com.industrialcrops.block;
import com.industrialcrops.block.entity.MatterDigitizerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class MatterDigitizerBlock extends MatterMachineBlock {
    public MatterDigitizerBlock(Properties properties) { super(properties); }
@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MatterDigitizerBlockEntity(pos, state); }
}
