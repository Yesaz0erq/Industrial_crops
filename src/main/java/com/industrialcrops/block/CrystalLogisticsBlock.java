package com.industrialcrops.block;

import com.industrialcrops.block.entity.CrystalLogisticsBlockEntity;
import com.industrialcrops.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.EnumMap;

public final class CrystalLogisticsBlock extends BaseEntityBlock {
    public static final MapCodec<CrystalLogisticsBlock> CODEC = simpleCodec(CrystalLogisticsBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final BooleanProperty OUTPUT = BooleanProperty.create("output");
    private static final EnumMap<Direction, VoxelShape> SHAPES = createShapes();
    public CrystalLogisticsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.FLOOR).setValue(OUTPUT, false));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CrystalLogisticsBlockEntity(pos, state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPES.get(mountDirection(state));
    }
    public static Direction mountDirection(BlockState state) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> Direction.UP;
            case CEILING -> Direction.DOWN;
            case WALL -> state.getValue(FACING);
        };
    }
    private static EnumMap<Direction, VoxelShape> createShapes() {
        var shapes = new EnumMap<Direction, VoxelShape>(Direction.class);
        var upright = Shapes.or(Block.box(0,0,0,16,2,16), Block.box(1,2,1,15,3,15),
                Block.box(4,3,4,12,3.5,12), Block.box(3.5,3.5,3.5,12.5,12.5,12.5));
        for (Direction direction : Direction.values()) {
            VoxelShape shape = Shapes.empty();
            for (var box : upright.toAabbs()) {
                shape = Shapes.or(shape, switch (direction) {
                    case UP -> Shapes.create(box);
                    case DOWN -> Shapes.box(box.minX,1-box.maxY,1-box.maxZ,box.maxX,1-box.minY,1-box.minZ);
                    case NORTH -> Shapes.box(box.minX,box.minZ,1-box.maxY,box.maxX,box.maxZ,1-box.minY);
                    case SOUTH -> Shapes.box(box.minX,1-box.maxZ,box.minY,box.maxX,1-box.minZ,box.maxY);
                    case EAST -> Shapes.box(box.minY,1-box.maxX,box.minZ,box.maxY,1-box.minX,box.maxZ);
                    case WEST -> Shapes.box(1-box.maxY,box.minX,box.minZ,1-box.minY,box.maxX,box.maxZ);
                });
            }
            shapes.put(direction, shape.optimize());
        }
        return shapes;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        AttachFace face = clicked == Direction.UP ? AttachFace.FLOOR : clicked == Direction.DOWN ? AttachFace.CEILING : AttachFace.WALL;
        Direction facing = face == AttachFace.WALL ? clicked : context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACE, face).setValue(FACING, facing);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, FACE, OUTPUT); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CRYSTAL_LOGISTICS.get(), CrystalLogisticsBlockEntity::tick);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof CrystalLogisticsBlockEntity machine) serverPlayer.openMenu(machine, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof CrystalLogisticsBlockEntity machine
                && net.neoforged.neoforge.fluids.FluidUtil.interactWithFluidHandler(player, hand, machine.fluidCapability()))
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof CrystalLogisticsBlockEntity machine) machine.dropContents();
        super.onRemove(state, level, pos, next, moving);
    }
}
