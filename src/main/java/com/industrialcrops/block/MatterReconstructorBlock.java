package com.industrialcrops.block;
import com.industrialcrops.block.entity.MatterReconstructorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class MatterReconstructorBlock extends MatterMachineBlock {
    public static final MapCodec<MatterReconstructorBlock> CODEC = simpleCodec(MatterReconstructorBlock::new);
    public MatterReconstructorBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends MatterMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MatterReconstructorBlockEntity(pos, state); }
}
