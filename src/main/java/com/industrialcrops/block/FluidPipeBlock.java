package com.industrialcrops.block;

import com.industrialcrops.basic_pipe.FluidPipeTransferUtil;
import com.industrialcrops.block.entity.FluidPipeBlockEntity;
import com.industrialcrops.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class FluidPipeBlock extends BaseEntityBlock {
    public static final MapCodec<FluidPipeBlock> CODEC = simpleCodec(FluidPipeBlock::new);
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape NORTH_SHAPE = Block.box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(11, 5, 5, 16, 11, 11);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape UP_SHAPE = Block.box(5, 11, 5, 11, 16, 11);
    private static final VoxelShape DOWN_SHAPE = Block.box(5, 0, 5, 11, 11, 11);
    private static final VoxelShape[] SHAPES = createShapes();

    public FluidPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FluidPipeBlockEntity(pos, state); }
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.FLUID_PIPE.get()
                ? (l, p, s, e) -> FluidPipeBlockEntity.tick(l, p, s, (FluidPipeBlockEntity) e) : null;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                               LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(property(direction), FluidPipeTransferUtil.isPipe(neighborState)
                || FluidPipeTransferUtil.canConnectEndpoint(level, neighborPos, direction.getOpposite()));
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return shape(state); }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return shape(state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN); }

    private static BlockState withConnections(BlockState state, Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            state = state.setValue(property(direction), FluidPipeTransferUtil.isPipe(level.getBlockState(neighbor))
                    || FluidPipeTransferUtil.canConnectEndpoint(level, neighbor, direction.getOpposite()));
        }
        return state;
    }
    private static BooleanProperty property(Direction direction) {
        return switch (direction) { case NORTH -> NORTH; case SOUTH -> SOUTH; case EAST -> EAST; case WEST -> WEST; case UP -> UP; case DOWN -> DOWN; };
    }
    private static VoxelShape shape(BlockState state) {
        int index = (state.getValue(NORTH) ? 1 : 0) | (state.getValue(SOUTH) ? 2 : 0)
                | (state.getValue(EAST) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0)
                | (state.getValue(UP) ? 16 : 0) | (state.getValue(DOWN) ? 32 : 0);
        return SHAPES[index];
    }
    private static VoxelShape[] createShapes() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int i = 0; i < 64; i++) {
            VoxelShape result = CORE;
            if ((i & 1) != 0) result = Shapes.or(result, NORTH_SHAPE);
            if ((i & 2) != 0) result = Shapes.or(result, SOUTH_SHAPE);
            if ((i & 4) != 0) result = Shapes.or(result, EAST_SHAPE);
            if ((i & 8) != 0) result = Shapes.or(result, WEST_SHAPE);
            if ((i & 16) != 0) result = Shapes.or(result, UP_SHAPE);
            if ((i & 32) != 0) result = Shapes.or(result, DOWN_SHAPE);
            shapes[i] = result.optimize();
        }
        return shapes;
    }
}
