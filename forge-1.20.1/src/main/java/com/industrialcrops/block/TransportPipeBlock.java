package com.industrialcrops.block;

import com.industrialcrops.block.entity.TransportPipeBlockEntity;
import com.industrialcrops.basic_pipe.PipeTransferUtil;
import com.industrialcrops.registry.ModBlockEntities;
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

public class TransportPipeBlock extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape CORE_SHAPE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape NORTH_SHAPE = Block.box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(11, 5, 5, 16, 11, 11);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape UP_SHAPE = Block.box(5, 11, 5, 11, 16, 11);
    private static final VoxelShape DOWN_SHAPE = Block.box(5, 0, 5, 11, 5, 11);
    private static final VoxelShape[] SHAPES = createShapes();

    public TransportPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }
@Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransportPipeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.TRANSPORT_PIPE.get() && PipeTransferUtil.isOutputPipe(state)
                ? (tickerLevel, tickerPos, tickerState, blockEntity) -> TransportPipeBlockEntity.tick(tickerLevel, tickerPos, tickerState, (TransportPipeBlockEntity) blockEntity)
                : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnectionState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(getProperty(direction), PipeTransferUtil.isPipe(neighborState) || PipeTransferUtil.canConnectEndpoint(level, neighborPos, direction.getOpposite()));
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getPipeShape(state);
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getPipeShape(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    private static BlockState withConnectionState(BlockState state, Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            state = state.setValue(getProperty(direction), PipeTransferUtil.isPipe(neighborState) || PipeTransferUtil.canConnectEndpoint(level, neighborPos, direction.getOpposite()));
        }
        return state;
    }

    private static BooleanProperty getProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    private static VoxelShape getPipeShape(BlockState state) {
        int index = 0;
        if (state.getValue(NORTH)) index |= 1;
        if (state.getValue(SOUTH)) index |= 1 << 1;
        if (state.getValue(EAST)) index |= 1 << 2;
        if (state.getValue(WEST)) index |= 1 << 3;
        if (state.getValue(UP)) index |= 1 << 4;
        if (state.getValue(DOWN)) index |= 1 << 5;
        return SHAPES[index];
    }

    private static VoxelShape[] createShapes() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int index = 0; index < shapes.length; index++) {
            VoxelShape shape = CORE_SHAPE;
            if ((index & 1) != 0) shape = Shapes.or(shape, NORTH_SHAPE);
            if ((index & (1 << 1)) != 0) shape = Shapes.or(shape, SOUTH_SHAPE);
            if ((index & (1 << 2)) != 0) shape = Shapes.or(shape, EAST_SHAPE);
            if ((index & (1 << 3)) != 0) shape = Shapes.or(shape, WEST_SHAPE);
            if ((index & (1 << 4)) != 0) shape = Shapes.or(shape, UP_SHAPE);
            if ((index & (1 << 5)) != 0) shape = Shapes.or(shape, DOWN_SHAPE);
            shapes[index] = shape.optimize();
        }
        return shapes;
    }
}
