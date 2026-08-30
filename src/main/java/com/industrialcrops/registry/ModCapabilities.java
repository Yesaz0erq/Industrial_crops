package com.industrialcrops.registry;

import com.industrialcrops.block.entity.BasicCropStorageArrayBlockEntity;
import com.industrialcrops.block.entity.ReinforcedIndustrialStorageArrayBlockEntity;
import com.industrialcrops.block.entity.RootOreExtractorBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ROOT_ORE_EXTRACTOR.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CROP_COMPRESSOR.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CROP_ANALYSIS_DEVICE.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.GOURD_MODIFICATION_DEVICE.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SLIME_INCUBATOR.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.INCUBATOR.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.INDUSTRIAL_CROP_STORAGE_ARRAY.get(),
                (blockEntity, side) -> blockEntity.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(),
                (blockEntity, side) -> blockEntity.getPipeItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.REINFORCED_CONTROL_DEVICE.get(),
                (blockEntity, side) -> blockEntity.getPipeItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.GOLDEN_LAUNCH_SILO.get(),
                (blockEntity, side) -> blockEntity.getAmmunitionInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PROCESSOR_PROGRAMMER.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.AUTOMATIC_PLANTER.get(),
                (blockEntity, side) -> blockEntity.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.AUTOMATIC_PLANTER.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage(side)
        );
        registerMatter(event, ModBlockEntities.MATTER_DIGITIZER.get());
        registerMatter(event, ModBlockEntities.DIGITIZED_ITEM_COPIER.get());
        registerMatter(event, ModBlockEntities.MATTER_RECONSTRUCTOR.get());
        registerBioEnergy(event, ModBlockEntities.BIO_ENERGY_GENERATOR.get());
        registerBioEnergy(event, ModBlockEntities.ENERGY_BATTERY.get());
        registerBioEnergy(event, ModBlockEntities.RESIDUE_INCINERATOR.get());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ENERGY_CABLE.get(),
                (entity, side) -> entity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ELECTRIC_FURNACE.get(),
                (entity, side) -> entity.getInventory());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ELECTRIC_FURNACE.get(),
                (entity, side) -> entity.getEnergyStorage(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(),
                (entity, side) -> entity.getInventory());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(),
                (entity, side) -> entity.getEnergyStorage(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MIXER.get(),
                (entity, side) -> entity.getInventory());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.PIPE_SORTER.get(),
                (entity, side) -> entity.getInputHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(),
                (entity, side) -> entity.getInventory());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(),
                (entity, side) -> entity.getEnergyStorage(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(),
                (entity, side) -> entity.getOutputHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.COPPER_FLUID_STORAGE_CABINET.get(),
                (entity, side) -> entity.getTank());
    }

    private static <T extends com.industrialcrops.block.entity.MatterMachineBlockEntity> void registerMatter(
            RegisterCapabilitiesEvent event, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (entity, side) -> entity.getInventory());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (entity, side) -> entity.getEnergyStorage(side));
    }

    private static <T extends com.industrialcrops.block.entity.BioEnergyMachineBlockEntity> void registerBioEnergy(
            RegisterCapabilitiesEvent event, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (entity, side) -> entity.getInventory());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (entity, side) -> entity.getEnergyStorage(side));
    }

}
