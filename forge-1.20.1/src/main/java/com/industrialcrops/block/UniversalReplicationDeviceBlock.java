package com.industrialcrops.block;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.UniversalReplicationDeviceBlockEntity;
import com.industrialcrops.network.payload.UniversalReplicaSyncPayload;
import com.industrialcrops.replication.UniversalReplicaData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.Collections;
import java.util.IdentityHashMap;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * A permanent Carrote-steel shell that runs an independent copy of the block
 * on its right. The world block, model and loot never become the copied block.
 */
public final class UniversalReplicationDeviceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public UniversalReplicationDeviceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
@Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UniversalReplicationDeviceBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos sourcePos = sourcePosition(pos, state.getValue(FACING));
        BlockState sourceState = level.getBlockState(sourcePos);
        boolean copied = initializeReplica(serverLevel, pos, sourcePos);
        if (placer instanceof Player player) {
            player.displayClientMessage(Component.translatable(copied
                    ? "message.industrialcrops.universal_replication_device.copied"
                    : "message.industrialcrops.universal_replication_device.failed",
                    sourceState.getBlock().getName()), true);
        }
    }

    @Override
    public InteractionResult use(BlockState shellState, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) {
            return InteractionResult.PASS;
        }
        if (UniversalReplicaData.getMirroredState(level, entity).isEmpty()
                && level instanceof ServerLevel serverLevel) {
            boolean copied = initializeReplica(serverLevel, pos,
                    sourcePosition(pos, shellState.getValue(FACING)));
            player.displayClientMessage(Component.translatable(copied
                    ? "message.industrialcrops.universal_replication_device.retry_success"
                    : "message.industrialcrops.universal_replication_device.failed"), true);
            return InteractionResult.SUCCESS;
        }
        var previousMenu = player.containerMenu;
        InteractionResult result = UniversalReplicaData.getMirroredState(level, entity)
                .map(mirrored -> UniversalReplicaData.call(level, pos, entity, mirrored,
                        () -> mirrored.use(level, player, hand, hit)))
                .orElse(InteractionResult.PASS);
        if (!level.isClientSide() && player.containerMenu != previousMenu) {
            UniversalReplicaData.registerMenu(player.containerMenu, level, pos, entity);
        }
        return result;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return (tickerLevel, pos, ignoredShellState, entity) ->
                UniversalReplicaData.getMirroredState(tickerLevel, entity).ifPresent(mirrored -> {
                    if (mirrored.is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())) {
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    BlockEntityTicker<T> targetTicker = mirrored.getTicker(tickerLevel, type);
                    if (targetTicker != null) {
                        UniversalReplicaData.run(tickerLevel, pos, entity, mirrored,
                                () -> targetTicker.tick(tickerLevel, pos, mirrored, entity));
                    }
                });
    }

    @Override
    public void neighborChanged(BlockState shellState, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) {
            return;
        }
        UniversalReplicaData.getMirroredState(level, entity).ifPresent(mirrored ->
                UniversalReplicaData.run(level, pos, entity, mirrored,
                        () -> mirrored.getBlock().neighborChanged(mirrored, level, pos, neighborBlock,
                                neighborPos, movedByPiston)));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        // The mirrored state is stored in the block entity, which this
        // state-only query cannot access. Returning true allows the delegated
        // signal methods below to decide the actual strength.
        return true;
    }

    @Override
    public int getSignal(BlockState shellState, BlockGetter getter, BlockPos pos, Direction direction) {
        if (!(getter instanceof Level level)) {
            return 0;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) {
            return 0;
        }
        return UniversalReplicaData.getMirroredState(level, entity)
                .map(mirrored -> UniversalReplicaData.call(level, pos, entity, mirrored,
                        () -> mirrored.getSignal(getter, pos, direction)))
                .orElse(0);
    }

    @Override
    public int getDirectSignal(BlockState shellState, BlockGetter getter, BlockPos pos,
            Direction direction) {
        if (!(getter instanceof Level level)) {
            return 0;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) {
            return 0;
        }
        return UniversalReplicaData.getMirroredState(level, entity)
                .map(mirrored -> UniversalReplicaData.call(level, pos, entity, mirrored,
                        () -> mirrored.getDirectSignal(getter, pos, direction)))
                .orElse(0);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState shellState, Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) {
            return 0;
        }
        return UniversalReplicaData.getMirroredState(level, entity)
                .map(mirrored -> UniversalReplicaData.call(level, pos, entity, mirrored,
                        () -> mirrored.getAnalogOutputSignal(level, pos)))
                .orElse(0);
    }

    public static BlockPos sourcePosition(BlockPos devicePos, Direction front) {
        return devicePos.relative(front.getCounterClockWise());
    }

    public static boolean canCopy(Level level, BlockPos sourcePos) {
        if (!level.hasChunkAt(sourcePos)) {
            return false;
        }
        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.isAir() || sourceState.is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())) {
            return false;
        }
        return hasFeCapability(level, sourcePos);
    }

    private static boolean hasFeCapability(Level level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            if (getEnergyCapabilitySafely(level, pos, side) != null) {
                return true;
            }
        }
        return getEnergyCapabilitySafely(level, pos, null) != null;
    }

    private static @Nullable IEnergyStorage getEnergyCapabilitySafely(
            Level level, BlockPos pos, @Nullable Direction side) {
        try {
            return com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ENERGY, pos, side);
        } catch (Throwable throwable) {
            IndustrialCrops.LOGGER.warn("FE capability probe failed for {} at {} from side {}",
                    level.getBlockState(pos), pos, side, throwable);
            return null;
        }
    }

    private static boolean initializeReplica(ServerLevel level, BlockPos destination, BlockPos sourcePos) {
        if (!canCopy(level, sourcePos)) {
            return false;
        }
        BlockState sourceState = level.getBlockState(sourcePos);
        BlockEntity sourceEntity = level.getBlockEntity(sourcePos);
        try {
            BlockEntity replicaEntity = level.getBlockEntity(destination);
            if (sourceEntity != null) {
                CompoundTag snapshot = sourceEntity.saveWithFullMetadata();
                snapshot.putInt("x", destination.getX());
                snapshot.putInt("y", destination.getY());
                snapshot.putInt("z", destination.getZ());
                replicaEntity = BlockEntity.loadStatic(destination, sourceState, snapshot);
                if (replicaEntity == null) {
                    return false;
                }
                level.removeBlockEntity(destination);
                level.setBlockEntity(replicaEntity);
            }
            if (replicaEntity == null) {
                return false;
            }
            UniversalReplicaData.setMirroredState(replicaEntity, sourceState);
            UniversalReplicaData.markFeValidated(replicaEntity);
            clearCopiedContents(level, destination, replicaEntity);
            UniversalReplicaSyncPayload.sendToTracking(level, replicaEntity);
            return true;
        } catch (Throwable throwable) {
            IndustrialCrops.LOGGER.error("Failed to initialize universal replica of {} from {} at {}",
                    sourceState, sourcePos, destination, throwable);
            restoreShellBlockEntity(level, destination);
            return false;
        }
    }

    public static void restoreShellBlockEntity(ServerLevel level, BlockPos pos) {
        BlockState shellState = level.getBlockState(pos);
        if (!shellState.is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())) {
            return;
        }
        level.removeBlockEntity(pos);
        level.setBlockEntity(new UniversalReplicationDeviceBlockEntity(pos, shellState));
        level.sendBlockUpdated(pos, shellState, shellState, Block.UPDATE_CLIENTS);
    }

    /**
     * Preserve copied energy and configuration, but never duplicate exposed
     * item inventories or fluid tanks. Capability-based clearing covers
     * compatible third-party NeoForge machines as well.
     */
    private static void clearCopiedContents(ServerLevel level, BlockPos pos, BlockEntity replicaEntity) {
        if (replicaEntity instanceof Container container) {
            container.clearContent();
            replicaEntity.setChanged();
        }

        Set<IItemHandler> itemHandlers = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<IFluidHandler> fluidHandlers = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Direction side : Direction.values()) {
            clearItemHandler(com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ITEM_HANDLER, pos, side), itemHandlers);
            clearFluidHandler(com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.FLUID_HANDLER, pos, side), fluidHandlers);
        }
        clearItemHandler(com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ITEM_HANDLER, pos, null), itemHandlers);
        clearFluidHandler(com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.FLUID_HANDLER, pos, null), fluidHandlers);
        replicaEntity.setChanged();
    }

    private static void clearItemHandler(@Nullable IItemHandler handler, Set<IItemHandler> visited) {
        if (handler == null || !visited.add(handler)) {
            return;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            int attempts = 0;
            while (!handler.getStackInSlot(slot).isEmpty() && attempts++ < 128) {
                int before = handler.getStackInSlot(slot).getCount();
                ItemStack extracted = handler.extractItem(slot, Integer.MAX_VALUE, false);
                int after = handler.getStackInSlot(slot).getCount();
                if (extracted.isEmpty() || after >= before) {
                    break;
                }
            }
        }
    }

    private static void clearFluidHandler(@Nullable IFluidHandler handler, Set<IFluidHandler> visited) {
        if (handler == null || !visited.add(handler)) {
            return;
        }
        int attempts = 0;
        while (attempts++ < 128
                && !handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE).isEmpty()) {
            // Continue until every exposed tank is empty.
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
