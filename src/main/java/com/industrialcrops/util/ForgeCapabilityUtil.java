package com.industrialcrops.util;

import com.industrialcrops.block.entity.AutomaticPlanterBlockEntity;
import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.block.entity.EnergyCableBlockEntity;
import com.industrialcrops.block.entity.MatterMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public final class ForgeCapabilityUtil {
    private ForgeCapabilityUtil() {}

    @SuppressWarnings("unchecked")
    public static @Nullable <T> T find(Level level, Capability<T> capability, BlockPos pos,
            @Nullable Direction side) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null) {
            return null;
        }

        T exposed = entity.getCapability(capability, side).orElse(null);
        if (exposed != null) {
            return exposed;
        }

        // The migrated machines expose NeoForge-style getters. Keep a direct
        // fallback here so client previews and server placement validation do
        // not depend on the capability bridge mixin having run first.
        if (capability == ForgeCapabilities.ENERGY) {
            return (T) localEnergyStorage(entity, side);
        }
        return null;
    }

    private static @Nullable IEnergyStorage localEnergyStorage(BlockEntity entity,
            @Nullable Direction side) {
        if (entity instanceof AutomaticPlanterBlockEntity value) return value.getEnergyStorage(side);
        if (entity instanceof BioEnergyMachineBlockEntity value) return value.getEnergyStorage(side);
        if (entity instanceof DigitalMiniatureForestBlockEntity value) return value.getEnergyStorage(side);
        if (entity instanceof ElectricFurnaceBlockEntity value) return value.getEnergyStorage(side);
        if (entity instanceof EnergyCableBlockEntity value) return value.getEnergyStorage();
        if (entity instanceof MatterMachineBlockEntity value) return value.getEnergyStorage(side);
        return null;
    }
}
