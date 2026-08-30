package com.industrialcrops.basic_pipe;

import com.industrialcrops.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/** Small, directional fluid network used by the gold fluid pipe. */
public final class FluidPipeTransferUtil {
    private static final int TRANSFER_RATE = 1_000;
    private static final long TICK_INTERVAL = 4L;

    private FluidPipeTransferUtil() {
    }

    public static boolean isPipe(BlockState state) {
        return state.is(ModBlocks.GOLD_FLUID_PIPE.get());
    }

    public static boolean canConnectEndpoint(LevelAccessor level, BlockPos pos, @Nullable Direction side) {
        if (!(level instanceof Level realLevel)) return false;
        return realLevel.getCapability(Capabilities.FluidHandler.BLOCK, pos, side) != null;
    }

    public static void tick(Level level, BlockPos startPos) {
        if (level.isClientSide() || level.getGameTime() % TICK_INTERVAL != Math.floorMod(startPos.asLong(), TICK_INTERVAL)) return;
        List<Endpoint> endpoints = collectEndpoints(level, startPos);
        for (Endpoint source : endpoints) {
            if (source.handler().getTanks() == 0 || source.handler().getFluidInTank(0).isEmpty()) continue;
            for (Endpoint target : endpoints) {
                if (source.pos().equals(target.pos()) || source.handler() == target.handler()) continue;
                if (!FluidUtil.tryFluidTransfer(target.handler(), source.handler(), TRANSFER_RATE, true).isEmpty()) return;
            }
        }
    }

    private static List<Endpoint> collectEndpoints(Level level, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Set<EndpointKey> keys = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPos.immutable());
        visited.add(startPos.immutable());
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction).immutable();
                BlockState nextState = level.getBlockState(next);
                if (isPipe(nextState)) {
                    if (visited.add(next)) queue.addLast(next);
                    continue;
                }
                keys.add(new EndpointKey(next, direction.getOpposite()));
            }
        }
        List<Endpoint> result = new ArrayList<>();
        for (EndpointKey key : keys) {
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, key.pos(), key.side());
            if (handler != null) result.add(new Endpoint(key.pos(), handler));
        }
        return result;
    }

    private record EndpointKey(BlockPos pos, Direction side) { }
    private record Endpoint(BlockPos pos, IFluidHandler handler) { }
}
