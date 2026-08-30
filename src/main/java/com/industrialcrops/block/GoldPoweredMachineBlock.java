package com.industrialcrops.block;

import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public abstract class GoldPoweredMachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected GoldPoweredMachineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (tickerLevel, pos, tickerState, entity) -> {
            if (entity instanceof ElectricFurnaceBlockEntity furnace) ElectricFurnaceBlockEntity.tick(tickerLevel, pos, tickerState, furnace);
            else if (entity instanceof DigitalMiniatureForestBlockEntity forest) DigitalMiniatureForestBlockEntity.tick(tickerLevel, pos, tickerState, forest);
            else if (entity instanceof GoldPlasmaExtractorBlockEntity extractor) GoldPlasmaExtractorBlockEntity.tick(tickerLevel, pos, tickerState, extractor);
        };
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof net.minecraft.world.MenuProvider provider) {
            player.openMenu(provider, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            ItemStackHandler inventory = null;
            if (level.getBlockEntity(pos) instanceof ElectricFurnaceBlockEntity furnace) inventory = furnace.getInventory();
            else if (level.getBlockEntity(pos) instanceof DigitalMiniatureForestBlockEntity forest) inventory = forest.getInventory();
            else if (level.getBlockEntity(pos) instanceof GoldPlasmaExtractorBlockEntity extractor) inventory = extractor.getInventory();
            if (inventory != null) for (int slot = 0; slot < inventory.getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
}
