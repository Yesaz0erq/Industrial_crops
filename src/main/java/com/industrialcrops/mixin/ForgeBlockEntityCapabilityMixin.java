package com.industrialcrops.mixin;

import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.AutomaticPlanterBlockEntity;
import com.industrialcrops.block.entity.BasicCropStorageArrayBlockEntity;
import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.block.entity.CarroteSteelForgeBlockEntity;
import com.industrialcrops.block.entity.CropAnalysisDeviceBlockEntity;
import com.industrialcrops.block.entity.CropCompressorBlockEntity;
import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.block.entity.EnergyCableBlockEntity;
import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.block.entity.GourdModificationDeviceBlockEntity;
import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.block.entity.MaterialHardeningDeviceBlockEntity;
import com.industrialcrops.block.entity.MatterMachineBlockEntity;
import com.industrialcrops.block.entity.MixerBlockEntity;
import com.industrialcrops.block.entity.ProcessorProgrammerBlockEntity;
import com.industrialcrops.block.entity.ReinforcedIndustrialStorageArrayBlockEntity;
import com.industrialcrops.block.entity.RootOreExtractorBlockEntity;
import com.industrialcrops.block.entity.SlimeIncubatorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bridges the migrated NeoForge-style getters to Forge 1.20.1 capabilities. */
@Mixin(value = CapabilityProvider.class, remap = false)
public abstract class ForgeBlockEntityCapabilityMixin {
    @Inject(method = "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private <T> void industrialcrops$exposeMachineCapability(Capability<T> capability,
            @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (!((Object) this instanceof BlockEntity entity)) {
            return;
        }

        Object exposed = capability == ForgeCapabilities.ITEM_HANDLER
                ? industrialcrops$itemHandler(entity)
                : capability == ForgeCapabilities.ENERGY
                        ? industrialcrops$energyStorage(entity, side)
                        : null;
        if (exposed != null) {
            Object resolved = exposed;
            cir.setReturnValue(LazyOptional.of(() -> resolved).cast());
        }
    }

    private static @Nullable Object industrialcrops$itemHandler(BlockEntity entity) {
        if (entity instanceof AdvancedIndustrialStorageBlockEntity value) return value.getPipeItemHandler();
        if (entity instanceof BasicCropStorageArrayBlockEntity value) return value.getItemHandler();
        if (entity instanceof CarroteSteelForgeBlockEntity value) return value.getAutomationHandler();
        if (entity instanceof GoldenLaunchSiloBlockEntity value) return value.getAmmunitionInventory();
        if (entity instanceof AutomaticPlanterBlockEntity value) return value.getInventory();
        if (entity instanceof BioEnergyMachineBlockEntity value) return value.getInventory();
        if (entity instanceof CropAnalysisDeviceBlockEntity value) return value.getInventory();
        if (entity instanceof CropCompressorBlockEntity value) return value.getInventory();
        if (entity instanceof DigitalMiniatureForestBlockEntity value) return value.getInventory();
        if (entity instanceof ElectricFurnaceBlockEntity value) return value.getInventory();
        if (entity instanceof GourdModificationDeviceBlockEntity value) return value.getInventory();
        if (entity instanceof IncubatorBlockEntity value) return value.getInventory();
        if (entity instanceof MaterialHardeningDeviceBlockEntity value) return value.getInventory();
        if (entity instanceof MatterMachineBlockEntity value) return value.getInventory();
        if (entity instanceof MixerBlockEntity value) return value.getInventory();
        if (entity instanceof ProcessorProgrammerBlockEntity value) return value.getInventory();
        if (entity instanceof ReinforcedIndustrialStorageArrayBlockEntity value) return value.getInventory();
        if (entity instanceof RootOreExtractorBlockEntity value) return value.getInventory();
        if (entity instanceof SlimeIncubatorBlockEntity value) return value.getInventory();
        return null;
    }

    private static @Nullable Object industrialcrops$energyStorage(BlockEntity entity,
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
