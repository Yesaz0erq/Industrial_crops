package com.industrialcrops.block;

import com.industrialcrops.block.entity.SatelliteRocketBlockEntity;
import com.industrialcrops.item.SatelliteRocketItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class SatelliteRocketBlock extends Block implements EntityBlock {
   public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);
   private static final VoxelShape ENGINE = Shapes.or(
      Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0), new VoxelShape[]{Block.box(7.5, 0.0, 1.0, 8.5, 7.0, 15.0), Block.box(1.0, 0.0, 7.5, 15.0, 7.0, 8.5)}
   );
   private static final VoxelShape BODY = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
   private static final VoxelShape NOSE = Block.box(5.0, 0.0, 5.0, 11.0, 14.0, 11.0);
   private final int tier;
   private final int height;

   public SatelliteRocketBlock(int tier, int height, Properties properties) {
      super(properties);
      this.tier = tier;
      this.height = height;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(PART, 0));
   }

   public int tier() {
      return this.tier;
   }

   public int height() {
      return this.height;
   }

   protected MapCodec<? extends Block> codec() {
      return simpleCodec(properties -> new SatelliteRocketBlock(this.tier, this.height, properties));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{PART});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      Level level = context.getLevel();
      if (pos.getY() + this.height - 1 >= level.getMaxBuildHeight()) {
         return null;
      } else {
         for (int i = 1; i < this.height; i++) {
            if (!level.getBlockState(pos.above(i)).canBeReplaced(context)) {
               return null;
            }
         }

         return this.defaultBlockState();
      }
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      for (int i = 1; i < this.height; i++) {
         level.setBlock(pos.above(i), (BlockState)this.defaultBlockState().setValue(PART, i), 3);
      }

      if (level.getBlockEntity(pos) instanceof SatelliteRocketBlockEntity rocket) {
         rocket.set(SatelliteRocketItem.durability(stack), SatelliteRocketItem.maxDurability(stack));
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return state.getValue(PART) == 0 ? new SatelliteRocketBlockEntity(pos, state) : null;
   }

   public static void removeWithoutDrops(Level level, BlockPos base) {
      BlockState engine = level.getBlockState(base);
      if (engine.getBlock() instanceof SatelliteRocketBlock rocket) {
         byte var7 = 50;

         for (int i = rocket.height - 1; i >= 0; i--) {
            BlockPos pos = base.above(i);
            if (level.getBlockState(pos).is(rocket)) {
               level.removeBlockEntity(pos);
               level.setBlock(pos, Blocks.AIR.defaultBlockState(), var7);
            }
         }

         for (int ix = 0; ix < rocket.height; ix++) {
            level.blockUpdated(base.above(ix), Blocks.AIR);
         }
      }
   }

   protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      int part = (Integer)state.getValue(PART);
      if (part >= this.height) {
         return Blocks.AIR.defaultBlockState();
      } else if (direction != Direction.DOWN || part <= 0 || neighbor.is(this) && (Integer)neighbor.getValue(PART) == part - 1) {
         return direction != Direction.UP || part >= this.height - 1 || neighbor.is(this) && neighbor.getValue(PART) == part + 1
            ? state
            : Blocks.AIR.defaultBlockState();
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      int part = (Integer)state.getValue(PART);
      if (part == 0) {
         return ENGINE;
      } else {
         return part == this.height - 1 ? NOSE : BODY;
      }
   }

   protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   public BlockState segment(int part) {
      return (BlockState)this.defaultBlockState().setValue(PART, Math.max(0, Math.min(this.height - 1, part)));
   }
}
