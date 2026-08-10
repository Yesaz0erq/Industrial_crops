package com.industrialcrops.block;
import com.industrialcrops.block.entity.MatterDigitizerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class MatterDigitizerBlock extends MatterMachineBlock {
    public static final MapCodec<MatterDigitizerBlock> CODEC = simpleCodec(MatterDigitizerBlock::new);
    public MatterDigitizerBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends MatterMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MatterDigitizerBlockEntity(pos, state); }
}
