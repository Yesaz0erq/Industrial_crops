package com.industrialcrops.block;

import com.industrialcrops.block.entity.SuperCropCultivatorBlockEntity;
import com.industrialcrops.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
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
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

/** Super Crop Cultivator: grows new plant varieties from Super-quality crops. */
public final class SuperCropCultivatorBlock extends BaseEntityBlock {
    public static final MapCodec<SuperCropCultivatorBlock> CODEC = simpleCodec(SuperCropCultivatorBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final DustParticleOptions NEBULA_DUST = new DustParticleOptions(new Vector3f(0.55F, 0.32F, 0.95F), 0.9F);

    public SuperCropCultivatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuperCropCultivatorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.SUPER_CROP_CULTIVATOR.get()
                ? (tickerLevel, tickerPos, tickerState, blockEntity) ->
                        SuperCropCultivatorBlockEntity.tick(tickerLevel, tickerPos, tickerState, (SuperCropCultivatorBlockEntity) blockEntity)
                : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SuperCropCultivatorBlockEntity cultivator) {
            player.openMenu(cultivator, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SuperCropCultivatorBlockEntity cultivator) {
            for (ItemStack stack : cultivator.collectDrops()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Glass dome sparkles: nebula dust while growing, the odd spore when idle. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.0D;
        double z = pos.getZ() + 0.5D;
        if (state.getValue(ACTIVE)) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(NEBULA_DUST, x + (random.nextDouble() - 0.5D) * 0.6D, y + random.nextDouble() * 0.2D,
                        z + (random.nextDouble() - 0.5D) * 0.6D, 0.0D, 0.02D, 0.0D);
            }
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.END_ROD, x + (random.nextDouble() - 0.5D) * 0.4D, y + 0.05D,
                        z + (random.nextDouble() - 0.5D) * 0.4D, 0.0D, 0.03D, 0.0D);
            }
            if (random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.PORTAL, x, y - 0.3D, z, (random.nextDouble() - 0.5D), 0.3D, (random.nextDouble() - 0.5D));
            }
        } else if (random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x + (random.nextDouble() - 0.5D) * 0.5D, y, z + (random.nextDouble() - 0.5D) * 0.5D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }
}
