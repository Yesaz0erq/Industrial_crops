package com.industrialcrops.launcher;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Shared rising-edge redstone trigger contract for launch devices.
 * Implementing devices fire once for each new redstone pulse and never repeat
 * merely because a signal remains powered.
 */
public interface RedstoneTriggeredLauncher {
    boolean wasRedstonePowered();

    void setRedstonePowered(boolean powered);

    boolean launch();

    default void checkRedstoneTrigger(Level level, BlockPos pos) {
        boolean powered = level.hasNeighborSignal(pos);
        boolean previouslyPowered = wasRedstonePowered();
        if (powered != previouslyPowered) {
            setRedstonePowered(powered);
        }
        if (powered && !previouslyPowered) {
            launch();
        }
    }
}
