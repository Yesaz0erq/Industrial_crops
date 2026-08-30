package com.industrialcrops.block;

import java.util.function.Supplier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public final class StrippableRotatedPillarBlock extends RotatedPillarBlock {
    private final Supplier<? extends RotatedPillarBlock> strippedBlock;

    public StrippableRotatedPillarBlock(Properties properties,
            Supplier<? extends RotatedPillarBlock> strippedBlock) {
        super(properties);
        this.strippedBlock = strippedBlock;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context,
            ItemAbility itemAbility, boolean simulate) {
        if (itemAbility != ItemAbilities.AXE_STRIP) {
            return super.getToolModifiedState(state, context, itemAbility, simulate);
        }
        return strippedBlock.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
    }
}
