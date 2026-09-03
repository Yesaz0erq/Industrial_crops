package com.industrialcrops.block;

import com.industrialcrops.block.entity.CarroteSteelForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class CarroteSteelForgeBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    private static final DustParticleOptions CARROTE_STEEL_SPARK = new DustParticleOptions(
            new Vector3f(0.86F, 0.055F, 0.025F), 0.75F);
    private static final DustParticleOptions CARROTE_STEEL_EMBER = new DustParticleOptions(
            new Vector3f(0.42F, 0.012F, 0.018F), 1.05F);
    private static final VoxelShape SHAPE = Shapes.block();

    public CarroteSteelForgeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }
@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CarroteSteelForgeBlockEntity(pos, state);
    }
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return (tickerLevel, tickerPos, tickerState, entity) -> {
            if (entity instanceof CarroteSteelForgeBlockEntity forge) {
                CarroteSteelForgeBlockEntity.tick(tickerLevel, tickerPos, tickerState, forge);
            }
        };
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CarroteSteelForgeBlockEntity forge) {
            if (CarroteSteelForgeBlockEntity.isAcceptedInput(stack)) {
                int offered = stack.getCount();
                ItemStack remainder = forge.insertInput(stack.copy(), false);
                int inserted = offered - remainder.getCount();
                if (inserted > 0) {
                    if (!player.getAbilities().instabuild) stack.shrink(inserted);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.35F, 0.85F);
                }
            } else {
                ItemStack output = forge.extractOutput(64, false);
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) player.drop(output, false);
                    level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.7F, 1.2F);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        double centerX = pos.getX() + 0.5D;
        double surfaceY = pos.getY() + 0.94D;
        double centerZ = pos.getZ() + 0.5D;
        int emberCount = 2 + random.nextInt(2);
        for (int i = 0; i < emberCount; i++) {
            double x = centerX + (random.nextDouble() - 0.5D) * 0.52D;
            double z = centerZ + (random.nextDouble() - 0.5D) * 0.52D;
            level.addParticle(random.nextBoolean() ? CARROTE_STEEL_SPARK : CARROTE_STEEL_EMBER,
                    x, surfaceY + random.nextDouble() * 0.08D, z,
                    (centerX - x) * 0.30D, 0.025D + random.nextDouble() * 0.045D,
                    (centerZ - z) * 0.30D);
        }
        if (random.nextInt(2) == 0) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.12D + random.nextDouble() * 0.22D;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            level.addParticle(ParticleTypes.FLAME, x, surfaceY + 0.035D, z,
                    -Math.sin(angle) * 0.018D, 0.018D, Math.cos(angle) * 0.018D);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE, centerX + (random.nextDouble() - 0.5D) * 0.35D,
                    surfaceY + 0.08D, centerZ + (random.nextDouble() - 0.5D) * 0.35D,
                    0.0D, 0.025D, 0.0D);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.LAVA, centerX + (random.nextDouble() - 0.5D) * 0.38D,
                    surfaceY + 0.06D, centerZ + (random.nextDouble() - 0.5D) * 0.38D,
                    0.0D, 0.03D, 0.0D);
        }
        if (random.nextInt(35) == 0) {
            level.playLocalSound(centerX, surfaceY, centerZ, SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS, 0.22F, 0.75F + random.nextFloat() * 0.15F, false);
        }
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CarroteSteelForgeBlockEntity forge) {
            for (int slot = 0; slot < forge.getInventory().getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), forge.getInventory().getStackInSlot(slot));
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
}
