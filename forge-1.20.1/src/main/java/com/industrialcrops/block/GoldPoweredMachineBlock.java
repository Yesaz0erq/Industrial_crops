package com.industrialcrops.block;
import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import com.industrialcrops.block.entity.UltraHighPressureInvForgeBlockEntity;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
public abstract class GoldPoweredMachineBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   protected GoldPoweredMachineBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }
   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }
   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return (tickerLevel, pos, tickerState, entity) -> {
         if (entity instanceof ElectricFurnaceBlockEntity furnace) {
            ElectricFurnaceBlockEntity.tick(tickerLevel, pos, tickerState, furnace);
         } else if (entity instanceof UltraHighPressureInvForgeBlockEntity furnace) {
            UltraHighPressureInvForgeBlockEntity.tick(tickerLevel, pos, tickerState, furnace);
         } else if (entity instanceof DigitalMiniatureForestBlockEntity forest) {
            DigitalMiniatureForestBlockEntity.tick(tickerLevel, pos, tickerState, forest);
         } else if (entity instanceof GoldPlasmaExtractorBlockEntity extractor) {
            GoldPlasmaExtractorBlockEntity.tick(tickerLevel, pos, tickerState, extractor);
         }
      };
   }
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider provider) {
         net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer)player, provider, buffer -> buffer.writeBlockPos(pos));
      }
      return InteractionResult.sidedSuccess(level.isClientSide());
   }
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock())) {
         ItemStackHandler inventory = null;
         if (level.getBlockEntity(pos) instanceof ElectricFurnaceBlockEntity furnace) {
            inventory = furnace.getInventory();
         } else if (level.getBlockEntity(pos) instanceof UltraHighPressureInvForgeBlockEntity furnace) {
            inventory = furnace.getInventory();
         } else if (level.getBlockEntity(pos) instanceof DigitalMiniatureForestBlockEntity forest) {
            inventory = forest.getInventory();
         } else if (level.getBlockEntity(pos) instanceof GoldPlasmaExtractorBlockEntity extractor) {
            inventory = extractor.getInventory();
         }
         if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
               Containers.dropItemStack(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), inventory.getStackInSlot(slot));
            }
         }
      }
      super.onRemove(state, level, pos, newState, moved);
   }
   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }
}
