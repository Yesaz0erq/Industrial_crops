package com.industrialcrops.block;

import com.industrialcrops.block.entity.MimicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public final class MimicBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public MimicBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
@Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public static BlockPos targetPosition(BlockPos mimicPos, Direction front) {
        return mimicPos.relative(front.getCounterClockWise());
    }

    public static boolean canMimic(Level level, BlockPos targetPos) {
        if (!level.hasChunkAt(targetPos)) {
            return false;
        }
        BlockState targetState = level.getBlockState(targetPos);
        return !targetState.isAir()
                && !targetState.is(com.industrialcrops.registry.CarroteBlocks.MIMIC_BLOCK.get());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof MimicBlockEntity mimic) {
            mimic.setOwner(placer.getUUID());
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MimicBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != com.industrialcrops.registry.CarroteBlockEntities.MIMIC_BLOCK.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, entity) ->
                MimicBlockEntity.serverTick(tickerLevel, pos, tickerState, (MimicBlockEntity) entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
}
