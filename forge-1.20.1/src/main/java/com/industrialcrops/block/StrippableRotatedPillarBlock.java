package com.industrialcrops.block;

import java.util.function.Supplier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public final class StrippableRotatedPillarBlock extends RotatedPillarBlock {
    private final Supplier<? extends RotatedPillarBlock> strippedBlock;

    public StrippableRotatedPillarBlock(Properties properties,
            Supplier<? extends RotatedPillarBlock> strippedBlock) {
        super(properties);
        this.strippedBlock = strippedBlock;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context,
            ToolAction toolAction, boolean simulate) {
        if (toolAction != ToolActions.AXE_STRIP) {
            return super.getToolModifiedState(state, context, toolAction, simulate);
        }
        return strippedBlock.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
    }
}
