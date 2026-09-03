package com.industrialcrops.block;
import com.industrialcrops.block.entity.DigitizedItemCopierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class DigitizedItemCopierBlock extends MatterMachineBlock {
    public DigitizedItemCopierBlock(Properties properties) { super(properties); }
@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DigitizedItemCopierBlockEntity(pos, state); }
}
