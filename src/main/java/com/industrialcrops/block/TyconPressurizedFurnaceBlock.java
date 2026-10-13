package com.industrialcrops.block;

import com.industrialcrops.block.entity.TyconPressurizedFurnaceBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class TyconPressurizedFurnaceBlock extends GoldPoweredMachineBlock {
   public static final MapCodec<TyconPressurizedFurnaceBlock> CODEC = simpleCodec(TyconPressurizedFurnaceBlock::new);
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   public TyconPressurizedFurnaceBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LIT, false));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{LIT});
   }

   protected MapCodec<? extends GoldPoweredMachineBlock> codec() {
      return CODEC;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TyconPressurizedFurnaceBlockEntity(pos, state);
   }
}
