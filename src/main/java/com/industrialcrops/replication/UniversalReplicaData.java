package com.industrialcrops.replication;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/** Persistent mirrored-state data and the short-lived virtual-world context. */
public final class UniversalReplicaData {
    private static final String MARKER = "IndustrialCropsUniversalReplica";
    private static final String MIRRORED_STATE = "IndustrialCropsMirroredState";
    private static final String FE_VALIDATED = "IndustrialCropsFeValidated";
    private static final ThreadLocal<Deque<Frame>> ACTIVE = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<AbstractContainerMenu, MenuFrame> REPLICA_MENUS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private UniversalReplicaData() {
    }

    public static void setMirroredState(BlockEntity entity, BlockState state) {
        CompoundTag data = entity.getPersistentData();
        data.putBoolean(MARKER, true);
        data.put(MIRRORED_STATE, NbtUtils.writeBlockState(state));
        setEntityStateWhenValid(entity, state);
        entity.setChanged();
    }

    public static void markFeValidated(BlockEntity entity) {
        entity.getPersistentData().putBoolean(FE_VALIDATED, true);
        entity.setChanged();
    }

    public static Optional<BlockState> getMirroredState(Level level, BlockEntity entity) {
        return getMirroredState(level, entity.getPersistentData());
    }

    public static BlockState getMirroredStateFromTag(Level level, CompoundTag entityTag) {
        CompoundTag data = entityTag.getCompound("NeoForgeData");
        return getMirroredState(level, data).orElseThrow(
                () -> new IllegalArgumentException("Missing mirrored block state in universal replica payload"));
    }

    private static Optional<BlockState> getMirroredState(Level level, CompoundTag data) {
        if (!data.getBoolean(MARKER)
                || !data.getBoolean(FE_VALIDATED)
                || !data.contains(MIRRORED_STATE, CompoundTag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(NbtUtils.readBlockState(
                level.holderLookup(Registries.BLOCK),
                data.getCompound(MIRRORED_STATE)
        ));
    }

    public static boolean isReplica(BlockEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(MARKER) && data.getBoolean(FE_VALIDATED);
    }

    public static boolean hasUnvalidatedReplicaMarker(BlockEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(MARKER) && !data.getBoolean(FE_VALIDATED);
    }

    public static void registerMenu(AbstractContainerMenu menu, Level level, BlockPos pos,
            BlockEntity entity) {
        if (menu != null && getMirroredState(level, entity).isPresent()) {
            REPLICA_MENUS.put(menu, new MenuFrame(level, pos.immutable(), entity));
        }
    }

    public static void registerActiveMenu(AbstractContainerMenu menu) {
        Deque<Frame> frames = ACTIVE.get();
        if (menu == null || frames.isEmpty()) {
            return;
        }
        Frame frame = frames.peek();
        REPLICA_MENUS.put(menu, new MenuFrame(frame.level, frame.pos, frame.entity));
    }

    public static boolean checkMenuValidity(AbstractContainerMenu menu, Player player) {
        MenuFrame frame = REPLICA_MENUS.get(menu);
        if (frame == null || frame.entity.isRemoved()
                || frame.level.getBlockEntity(frame.pos) != frame.entity) {
            REPLICA_MENUS.remove(menu);
            return menu.stillValid(player);
        }
        boolean valid = player.level() == frame.level
                && frame.level.getBlockState(frame.pos).is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())
                && player.distanceToSqr(
                        frame.pos.getX() + 0.5D,
                        frame.pos.getY() + 0.5D,
                        frame.pos.getZ() + 0.5D) <= 64.0D
                && getMirroredState(frame.level, frame.entity).isPresent();
        if (!valid) {
            REPLICA_MENUS.remove(menu);
        }
        return valid;
    }

    public static void broadcastMenuChanges(AbstractContainerMenu menu) {
        MenuFrame frame = REPLICA_MENUS.get(menu);
        if (frame == null || frame.entity.isRemoved()
                || frame.level.getBlockEntity(frame.pos) != frame.entity) {
            REPLICA_MENUS.remove(menu);
            menu.broadcastChanges();
            return;
        }
        getMirroredState(frame.level, frame.entity).ifPresentOrElse(
                state -> run(frame.level, frame.pos, frame.entity, state, menu::broadcastChanges),
                () -> REPLICA_MENUS.remove(menu)
        );
    }

    public static <T> T call(Level level, BlockPos pos, BlockEntity entity, BlockState mirroredState,
            Supplier<T> action) {
        Frame frame = new Frame(level, pos.immutable(), entity, mirroredState);
        ACTIVE.get().push(frame);
        setEntityStateWhenValid(entity, mirroredState);
        try {
            return action.get();
        } finally {
            ACTIVE.get().pop();
            setMirroredState(entity, frame.mirroredState);
            if (ACTIVE.get().isEmpty()) {
                ACTIVE.remove();
            }
        }
    }

    public static void run(Level level, BlockPos pos, BlockEntity entity, BlockState mirroredState,
            Runnable action) {
        call(level, pos, entity, mirroredState, () -> {
            action.run();
            return null;
        });
    }

    public static Optional<BlockState> virtualState(Level level, BlockPos pos) {
        Frame frame = current(level, pos);
        return frame == null ? Optional.empty() : Optional.of(frame.mirroredState);
    }

    public static boolean captureStateChange(Level level, BlockPos pos, BlockState newState) {
        Frame frame = current(level, pos);
        if (frame == null) {
            return false;
        }
        // Keep the outer device in the world. State changes of the emulated
        // machine (for example furnace "lit") are retained as virtual state.
        if (newState.is(frame.mirroredState.getBlock())) {
            frame.mirroredState = newState;
            setEntityStateWhenValid(frame.entity, newState);
            frame.entity.setChanged();
        }
        return true;
    }

    private static Frame current(Level level, BlockPos pos) {
        Deque<Frame> frames = ACTIVE.get();
        if (frames.isEmpty()) {
            return null;
        }
        Frame frame = frames.peek();
        return frame.level == level && frame.pos.equals(pos) ? frame : null;
    }

    private static void setEntityStateWhenValid(BlockEntity entity, BlockState state) {
        // A functional block without a block entity (for example a lever or
        // crafting table) uses our placeholder BE only as storage. Giving that
        // placeholder the mirrored state fails BlockEntity validation and was
        // the cause of the reported server crash.
        if (entity.getType().isValid(state)) {
            entity.setBlockState(state);
        }
    }

    private static final class Frame {
        private final Level level;
        private final BlockPos pos;
        private final BlockEntity entity;
        private BlockState mirroredState;

        private Frame(Level level, BlockPos pos, BlockEntity entity, BlockState mirroredState) {
            this.level = level;
            this.pos = pos;
            this.entity = entity;
            this.mirroredState = mirroredState;
        }
    }

    private record MenuFrame(Level level, BlockPos pos, BlockEntity entity) {
    }
}
