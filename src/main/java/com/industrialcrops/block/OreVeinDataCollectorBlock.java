package com.industrialcrops.block;

import com.industrialcrops.block.entity.OreVeinDataCollectorBlockEntity;
import com.industrialcrops.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** AP-steel survey machine that turns slimeballs into Flux Ore Gel. */
public final class OreVeinDataCollectorBlock extends BaseEntityBlock {
   public static final MapCodec<OreVeinDataCollectorBlock> CODEC = simpleCodec(OreVeinDataCollectorBlock::new);
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   private static final DustParticleOptions SCAN = new DustParticleOptions(new Vector3f(0.40F, 0.95F, 0.90F), 0.7F);

   public OreVeinDataCollectorBlock(Properties properties) {
      super(properties);
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Override
   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new OreVeinDataCollectorBlockEntity(pos, state);
   }

   @Override
   public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null
         : createTickerHelper(type, ModBlockEntities.ORE_VEIN_DATA_COLLECTOR.get(), OreVeinDataCollectorBlockEntity::tick);
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
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof OreVeinDataCollectorBlockEntity collector) {
         collector.openMenu(serverPlayer);
      }
      return InteractionResult.sidedSuccess(level.isClientSide());
   }

   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof OreVeinDataCollectorBlockEntity collector) {
         for (ItemStack stack : collector.collectDrops()) {
            if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
         }
      }
      super.onRemove(state, level, pos, newState, moved);
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (!state.getValue(LIT)) return;
      // A thin scan curtain falls from the dish on top into the ground below.
      double angle = (level.getGameTime() % 40L) / 40.0D * Math.PI * 2.0D;
      double x = pos.getX() + 0.5D + Math.cos(angle) * 0.42D;
      double z = pos.getZ() + 0.5D + Math.sin(angle) * 0.42D;
      level.addParticle(SCAN, x, pos.getY() + 1.05D, z, 0.0D, -0.04D, 0.0D);
      if (random.nextInt(4) == 0) {
         level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.6D,
            pos.getZ() + 0.5D, (random.nextDouble() - 0.5D) * 0.6D, -0.9D, (random.nextDouble() - 0.5D) * 0.6D);
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
      builder.add(FACING, LIT);
   }
}
