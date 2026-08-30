package com.industrialcrops.basic_pipe;

import com.industrialcrops.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class PipeTransferUtil {
    private static final int BASIC_TRANSFER_RATE = 4;
    private static final int REINFORCED_TRANSFER_RATE = 8;
    private static final int ADVANCED_TRANSFER_RATE = 16;
    private static final long TICK_INTERVAL = 8L;

    private PipeTransferUtil() {
    }

    public static void tickOutputPipe(Level level, BlockPos basic_pipePos, BlockState basic_pipeState) {
        long phase = Math.floorMod(basic_pipePos.asLong(), TICK_INTERVAL);
        if (level.getGameTime() % TICK_INTERVAL != phase) {
            return;
        }

        int transferRate = getTransferRate(basic_pipeState);
        List<HandlerEndpoint> targets = collectInputEndpoints(level, basic_pipePos);
        if (targets.isEmpty()) {
            return;
        }

        for (HandlerEndpoint source : getAdjacentEndpoints(level, basic_pipePos)) {
            List<Candidate> candidates = getExtractableCandidates(source.handler(), transferRate);
            for (Candidate candidate : candidates) {
                for (HandlerEndpoint target : targets) {
                    if (target.pos().equals(source.pos())) {
                        continue;
                    }

                    int accepted = getInsertableAmount(target.handler(), candidate.stack());
                    if (accepted <= 0) {
                        continue;
                    }

                    ItemStack extracted = source.handler().extractItem(
                            candidate.slot(),
                            Math.min(accepted, candidate.stack().getCount()),
                            false
                    );
                    if (extracted.isEmpty()) {
                        continue;
                    }

                    int inserted = insert(target.handler(), extracted);
                    if (inserted > 0) {
                        return;
                    }
                }
            }
        }
    }

    public static boolean isPipe(BlockState state) {
        return isBasicPipe(state) || isReinforcedPipe(state) || isAdvancedPipe(state)
                || state.is(ModBlocks.PIPE_SORTER.get());
    }

    public static boolean isInputPipe(BlockState state) {
        return state.is(ModBlocks.INPUT_PIPE.get())
                || state.is(ModBlocks.REINFORCED_INPUT_PIPE.get())
                || state.is(ModBlocks.ADVANCED_INPUT_PIPE.get());
    }

    public static boolean isOutputPipe(BlockState state) {
        return state.is(ModBlocks.OUTPUT_PIPE.get())
                || state.is(ModBlocks.REINFORCED_OUTPUT_PIPE.get())
                || state.is(ModBlocks.ADVANCED_OUTPUT_PIPE.get());
    }

    public static boolean canConnectEndpoint(LevelAccessor level, BlockPos pos, @Nullable Direction side) {
        if (!(level instanceof Level realLevel)) {
            return false;
        }
        return realLevel.getCapability(Capabilities.ItemHandler.BLOCK, pos, side) != null;
    }

    private static List<HandlerEndpoint> collectInputEndpoints(Level level, BlockPos startPos) {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos immutableStart = startPos.immutable();
        queue.add(immutableStart);
        visited.add(immutableStart);

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction).immutable();
                if (!visited.add(next)) {
                    continue;
                }

                BlockState nextState = level.getBlockState(next);
                if (nextState.is(ModBlocks.PIPE_SORTER.get())) {
                    endpoints.add(new Endpoint(next, direction.getOpposite()));
                    continue;
                }
                if (!isPipe(nextState)) {
                    continue;
                }

                queue.add(next);
                if (isInputPipe(nextState)) {
                    collectAdjacentEndpointKeys(level, next, endpoints);
                }
            }
        }

        List<HandlerEndpoint> handlers = new ArrayList<>(endpoints.size());
        for (Endpoint endpoint : endpoints) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, endpoint.pos(), endpoint.side());
            if (handler != null) {
                handlers.add(new HandlerEndpoint(endpoint.pos(), endpoint.side(), handler));
            }
        }
        return handlers;
    }

    private static List<HandlerEndpoint> getAdjacentEndpoints(Level level, BlockPos basic_pipePos) {
        List<HandlerEndpoint> endpoints = new ArrayList<>(Direction.values().length);
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = basic_pipePos.relative(direction);
            if (isPipe(level.getBlockState(neighborPos))) {
                continue;
            }
            Direction side = direction.getOpposite();
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, side);
            if (handler != null) {
                endpoints.add(new HandlerEndpoint(neighborPos.immutable(), side, handler));
            }
        }
        return endpoints;
    }

    private static void collectAdjacentEndpointKeys(Level level, BlockPos basic_pipePos, Set<Endpoint> endpoints) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = basic_pipePos.relative(direction);
            if (!isPipe(level.getBlockState(neighborPos))) {
                endpoints.add(new Endpoint(neighborPos.immutable(), direction.getOpposite()));
            }
        }
    }

    private static List<Candidate> getExtractableCandidates(IItemHandler handler, int maxAmount) {
        List<Candidate> candidates = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack extracted = handler.extractItem(slot, maxAmount, true);
            if (!extracted.isEmpty()) {
                candidates.add(new Candidate(slot, extracted));
            }
        }
        return candidates;
    }

    private static int getInsertableAmount(IItemHandler handler, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int original = stack.getCount();
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, true);
        }
        return original - remaining.getCount();
    }

    private static int insert(IItemHandler handler, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int original = stack.getCount();
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return original - remaining.getCount();
    }

    private static boolean isBasicPipe(BlockState state) {
        return state.is(ModBlocks.PIPE.get()) || state.is(ModBlocks.INPUT_PIPE.get()) || state.is(ModBlocks.OUTPUT_PIPE.get());
    }

    private static boolean isReinforcedPipe(BlockState state) {
        return state.is(ModBlocks.REINFORCED_PIPE.get()) || state.is(ModBlocks.REINFORCED_INPUT_PIPE.get()) || state.is(ModBlocks.REINFORCED_OUTPUT_PIPE.get());
    }

    private static boolean isAdvancedPipe(BlockState state) {
        return state.is(ModBlocks.ADVANCED_PIPE.get())
                || state.is(ModBlocks.ADVANCED_INPUT_PIPE.get())
                || state.is(ModBlocks.ADVANCED_OUTPUT_PIPE.get());
    }

    private static int getTransferRate(BlockState state) {
        if (isAdvancedPipe(state)) {
            return ADVANCED_TRANSFER_RATE;
        }
        return isReinforcedPipe(state) ? REINFORCED_TRANSFER_RATE : BASIC_TRANSFER_RATE;
    }

    private record Endpoint(BlockPos pos, @Nullable Direction side) {
    }

    private record HandlerEndpoint(BlockPos pos, @Nullable Direction side, IItemHandler handler) {
    }

    private record Candidate(int slot, ItemStack stack) {
    }
}
