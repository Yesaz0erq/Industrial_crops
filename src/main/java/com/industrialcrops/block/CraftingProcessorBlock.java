package com.industrialcrops.block;

import com.industrialcrops.block.entity.CraftingProcessorBlockEntity;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class CraftingProcessorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public CraftingProcessorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CraftingProcessorBlockEntity(pos, state); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof CraftingProcessorBlockEntity machine) net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer)player, machine, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof CraftingProcessorBlockEntity machine) machine.dropContents();
        super.onRemove(state, level, pos, next, moving);
    }
}
