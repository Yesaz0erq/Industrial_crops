package com.industrialcrops.block;

import com.industrialcrops.block.entity.EnergyCableBlockEntity;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

public final class EnergyCableBlock extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape[] SHAPES = createShapes();

    public EnergyCableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }
@Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCableBlockEntity(pos, state);
    }
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return type == ModBlockEntities.ENERGY_CABLE.get()
                ? (l, p, s, be) -> EnergyCableBlockEntity.tick(l, p, s, (EnergyCableBlockEntity) be) : null;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectedState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(property(direction), canConnect(level, neighborPos, direction.getOpposite()));
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int index = 0;
        for (Direction direction : Direction.values()) if (state.getValue(property(direction))) index |= 1 << direction.ordinal();
        return SHAPES[index];
    }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    private static BlockState connectedState(BlockState state, Level level, BlockPos pos) {
        for (Direction direction : Direction.values())
            state = state.setValue(property(direction), canConnect(level, pos.relative(direction), direction.getOpposite()));
        return state;
    }
    private static boolean canConnect(LevelAccessor level, BlockPos pos, Direction side) {
        if (level.getBlockState(pos).getBlock() instanceof EnergyCableBlock) return true;
        return level instanceof Level actual
                && com.industrialcrops.util.ForgeCapabilityUtil.find(actual, ForgeCapabilities.ENERGY, pos, side) != null;
    }
    private static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH; case SOUTH -> SOUTH; case EAST -> EAST;
            case WEST -> WEST; case UP -> UP; case DOWN -> DOWN;
        };
    }
    private static VoxelShape[] createShapes() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int i = 0; i < 64; i++) {
            VoxelShape shape = CORE;
            if ((i & 1 << Direction.DOWN.ordinal()) != 0) shape = Shapes.or(shape, Block.box(5, 0, 5, 11, 5, 11));
            if ((i & 1 << Direction.UP.ordinal()) != 0) shape = Shapes.or(shape, Block.box(5, 11, 5, 11, 16, 11));
            if ((i & 1 << Direction.NORTH.ordinal()) != 0) shape = Shapes.or(shape, Block.box(5, 5, 0, 11, 11, 5));
            if ((i & 1 << Direction.SOUTH.ordinal()) != 0) shape = Shapes.or(shape, Block.box(5, 5, 11, 11, 11, 16));
            if ((i & 1 << Direction.WEST.ordinal()) != 0) shape = Shapes.or(shape, Block.box(0, 5, 5, 5, 11, 11));
            if ((i & 1 << Direction.EAST.ordinal()) != 0) shape = Shapes.or(shape, Block.box(11, 5, 5, 16, 11, 11));
            shapes[i] = shape.optimize();
        }
        return shapes;
    }
}
