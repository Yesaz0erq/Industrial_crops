package com.industrialcrops.block;

import com.industrialcrops.block.entity.ApAgricultureBlockEntity;
import com.industrialcrops.block.entity.ApControllerBlockEntity;
import com.industrialcrops.block.entity.ApLivestockBlockEntity;
import com.industrialcrops.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ApControllerBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
   private static final MapCodec<ApControllerBlock> AGRI_CODEC = simpleCodec(var0 -> new ApControllerBlock(ApControllerBlock.Kind.AGRICULTURE, var0));
   private static final MapCodec<ApControllerBlock> LIVESTOCK_CODEC = simpleCodec(var0 -> new ApControllerBlock(ApControllerBlock.Kind.LIVESTOCK, var0));
   private final ApControllerBlock.Kind kind;

   public ApControllerBlock(ApControllerBlock.Kind var1, Properties var2) {
      super(var2);
      this.kind = var1;
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false));
   }

   public ApControllerBlock.Kind kind() {
      return this.kind;
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return this.kind == ApControllerBlock.Kind.AGRICULTURE ? AGRI_CODEC : LIVESTOCK_CODEC;
   }

   protected RenderShape getRenderShape(BlockState var1) {
      return RenderShape.MODEL;
   }

   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return (BlockEntity)(this.kind == ApControllerBlock.Kind.AGRICULTURE ? new ApAgricultureBlockEntity(var1, var2) : new ApLivestockBlockEntity(var1, var2));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext var1) {
      return (BlockState)this.defaultBlockState().setValue(FACING, var1.getHorizontalDirection().getOpposite());
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level var1, BlockState var2, BlockEntityType<T> var3) {
      if (var1.isClientSide()) {
         return null;
      } else {
         return this.kind == ApControllerBlock.Kind.AGRICULTURE
            ? createTickerHelper(var3, (BlockEntityType)ModBlockEntities.AP_AGRICULTURE.get(), ApAgricultureBlockEntity::tick)
            : createTickerHelper(var3, (BlockEntityType)ModBlockEntities.AP_LIVESTOCK.get(), ApLivestockBlockEntity::tick);
      }
   }

   protected InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5) {
      if (!var2.isClientSide() && var2.getBlockEntity(var3) instanceof MenuProvider var6) {
         var4.openMenu(var6, var1x -> var1x.writeBlockPos(var3));
      }

      return InteractionResult.sidedSuccess(var2.isClientSide());
   }

   protected void onRemove(BlockState var1, Level var2, BlockPos var3, BlockState var4, boolean var5) {
      if (!var1.is(var4.getBlock())) {
         BlockEntity var6 = var2.getBlockEntity(var3);
         if (var6 instanceof ApControllerBlockEntity var7) {
            var7.releaseChunks();
            ItemStackHandler var8 = var7.getUpgradeSlots();

            for (int var9 = 0; var9 < var8.getSlots(); var9++) {
               Containers.dropItemStack(var2, (double)var3.getX(), (double)var3.getY(), (double)var3.getZ(), var8.getStackInSlot(var9));
            }
         }

         ItemStackHandler var10 = var6 instanceof ApAgricultureBlockEntity var13
            ? var13.getInventory()
            : (var6 instanceof ApLivestockBlockEntity var11 ? var11.getInventory() : null);
         if (var10 != null) {
            for (int var12 = 0; var12 < var10.getSlots(); var12++) {
               Containers.dropItemStack(var2, (double)var3.getX(), (double)var3.getY(), (double)var3.getZ(), var10.getStackInSlot(var12));
            }
         }
      }

      super.onRemove(var1, var2, var3, var4, var5);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> var1) {
      var1.add(new Property[]{FACING, ACTIVE});
   }

   protected BlockState rotate(BlockState var1, Rotation var2) {
      return (BlockState)var1.setValue(FACING, var2.rotate((Direction)var1.getValue(FACING)));
   }

   protected BlockState mirror(BlockState var1, Mirror var2) {
      return var1.rotate(var2.getRotation((Direction)var1.getValue(FACING)));
   }

   public static enum Kind {
      AGRICULTURE,
      LIVESTOCK;
   }
}
