package com.industrialcrops.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class PoweredMachineSupport {
    private PoweredMachineSupport() {
    }

    public static void pullEnergy(Level level, BlockPos pos, IEnergyStorage target, int rate) {
        if (target.getEnergyStored() >= target.getMaxEnergyStored()) return;
        for (Direction direction : Direction.values()) {
            BlockPos sourcePos = pos.relative(direction);
            IEnergyStorage source = level.getCapability(Capabilities.EnergyStorage.BLOCK, sourcePos, direction.getOpposite());
            if (source == null || source.extractEnergy(1, true) <= 0) {
                source = level.getCapability(Capabilities.EnergyStorage.BLOCK, sourcePos, null);
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
