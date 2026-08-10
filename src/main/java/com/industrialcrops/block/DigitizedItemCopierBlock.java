package com.industrialcrops.block;
import com.industrialcrops.block.entity.DigitizedItemCopierBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class DigitizedItemCopierBlock extends MatterMachineBlock {
    public static final MapCodec<DigitizedItemCopierBlock> CODEC = simpleCodec(DigitizedItemCopierBlock::new);
    public DigitizedItemCopierBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends MatterMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DigitizedItemCopierBlockEntity(pos, state); }
}
