package com.industrialcrops.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public final class PoweredMachineSupport {
    /**
     * Gold-tier machine power, sized against crop energy (1 crop = 50 FE, 10K crop = 500,000 FE):
     * every gold machine buffers half a 10K crop and accepts a full Basic Energy Cable.
     * Per-operation costs sit at 1.25x the matching AP machine; AP buys throughput and efficiency.
     */
    public static final int GOLD_CAPACITY = 250_000;
    public static final int GOLD_INPUT = 10_000;

    private PoweredMachineSupport() {
    }

    public static void pullEnergy(Level level, BlockPos pos, IEnergyStorage target, int rate) {
        if (target.getEnergyStored() >= target.getMaxEnergyStored()) return;
        for (Direction direction : Direction.values()) {
            BlockPos sourcePos = pos.relative(direction);
            IEnergyStorage source = com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ENERGY, sourcePos, direction.getOpposite());
            if (source == null || source.extractEnergy(1, true) <= 0) {
                source = com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ENERGY, sourcePos, null);
            }
            if (source == null) continue;
            int wanted = Math.min(rate, target.getMaxEnergyStored() - target.getEnergyStored());
            int available = source.extractEnergy(wanted, true);
            int accepted = target.receiveEnergy(available, true);
            if (accepted > 0) target.receiveEnergy(source.extractEnergy(accepted, false), false);
            if (target.getEnergyStored() >= target.getMaxEnergyStored()) return;
        }
    }
}
