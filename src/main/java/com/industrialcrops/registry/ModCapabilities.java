package com.industrialcrops.registry;

import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.block.entity.MatterMachineBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;

public final class ModCapabilities {
   private ModCapabilities() {
   }

   public static void register(RegisterCapabilitiesEvent var0) {
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.ANIMAL_BREEDER.get(), (var0x, var1) -> var0x.itemCapability());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.ANIMAL_BREEDER.get(), (var0x, var1) -> var0x.energy());
      var0.registerBlockEntity(FluidHandler.BLOCK, ModBlockEntities.ANIMAL_BREEDER.get(), (var0x, var1) -> var0x.tank());
      var0.registerBlockEntity(
         EnergyStorage.BLOCK, ModBlockEntities.WIRELESS_ENERGY_TRANSMITTER.get(), (var0x, var1) -> var0x.getEnergyStorage(var1)
      );
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.LAUNCH_SITE.get(), (var0x, var1) -> var0x.energy());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.SATELLITE_CONTROL_DEVICE.get(), (var0x, var1) -> var0x.energy());
      var0.registerBlockEntity(FluidHandler.BLOCK, ModBlockEntities.LAUNCH_SITE.get(), (var0x, var1) -> var0x.tank());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.SATELLITE_RECEIVER.get(), (var0x, var1) -> var0x.getEnergyStorage(var1));
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.ROOT_ORE_EXTRACTOR.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.CROP_COMPRESSOR.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.CROP_ANALYSIS_DEVICE.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.MELON_MODIFICATION_DEVICE.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.SLIME_INCUBATOR.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.INCUBATOR.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(
         ItemHandler.BLOCK, ModBlockEntities.INDUSTRIAL_CROP_STORAGE_ARRAY.get(), (var0x, var1) -> var0x.getItemHandler()
      );
      var0.registerBlockEntity(
         ItemHandler.BLOCK, ModBlockEntities.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get(), (var0x, var1) -> var0x.getInventory()
      );
      var0.registerBlockEntity(
         ItemHandler.BLOCK, ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), (var0x, var1) -> var0x.getPipeItemHandler()
      );
      var0.registerBlockEntity(
         ItemHandler.BLOCK, ModBlockEntities.REINFORCED_CONTROL_DEVICE.get(), (var0x, var1) -> var0x.getPipeItemHandler()
      );
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.GOLDEN_LAUNCH_SILO.get(), (var0x, var1) -> var0x.getAmmunitionInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.PROCESSOR_PROGRAMMER.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.AUTOMATIC_PLANTER.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.AUTOMATIC_PLANTER.get(), (var0x, var1) -> var0x.getEnergyStorage(var1));
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.TERRAIN_PROCESSOR.get(), (var0x, var1) -> var0x.itemCapability());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.CRAFTING_PROCESSOR.get(), (var0x, var1) -> var0x.outputCapability());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.CRYSTAL_LOGISTICS.get(), (var0x, var1) -> var0x.itemCapability());
      var0.registerBlockEntity(FluidHandler.BLOCK, ModBlockEntities.CRYSTAL_LOGISTICS.get(), (var0x, var1) -> var0x.fluidCapability());
      registerMatter(var0, ModBlockEntities.MATTER_DIGITIZER.get());
      registerMatter(var0, ModBlockEntities.DIGITIZED_ITEM_COPIER.get());
      registerMatter(var0, ModBlockEntities.MATTER_RECONSTRUCTOR.get());
      registerBioEnergy(var0, ModBlockEntities.BIO_ENERGY_GENERATOR.get());
      registerBioEnergy(var0, ModBlockEntities.ENERGY_BATTERY.get());
      registerBioEnergy(var0, ModBlockEntities.RESIDUE_INCINERATOR.get());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.ENERGY_CABLE.get(), (var0x, var1) -> var0x.getEnergyStorage());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.ELECTRIC_FURNACE.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.ELECTRIC_FURNACE.get(), (var0x, var1) -> var0x.getEnergyStorage(var1));
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.AP_AGRICULTURE.get(), (var0x, var1) -> var0x.automation());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.AP_AGRICULTURE.get(), (var0x, var1) -> var0x.getEnergyStorage(var1));
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.AP_LIVESTOCK.get(), (var0x, var1) -> var0x.automation());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.AP_LIVESTOCK.get(), (var0x, var1) -> var0x.getEnergyStorage(var1));
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.ULTRA_HIGH_PRESSURE_INV_FORGE.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.EXPANDABLE_STORAGE_DEVICE.get(), (var0x, var1) -> var0x.getPipeItemHandler());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.SUPER_COMPRESSOR.get(), (var0x, var1) -> var0x.getAutomationHandler());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.INTEGRATED_SMELTING_FURNACE.get(), (var0x, var1) -> var0x.getAutomationHandler());
      var0.registerBlockEntity(EnergyStorage.BLOCK, ModBlockEntities.INTEGRATED_SMELTING_FURNACE.get(), (var0x, var1) -> var0x.getEnergyStorage(var1));
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.SUPER_CROP_CULTIVATOR.get(), (var0x, var1) -> var0x.getAutomationHandler());
      var0.registerBlockEntity(
         EnergyStorage.BLOCK, ModBlockEntities.ULTRA_HIGH_PRESSURE_INV_FORGE.get(), (var0x, var1) -> var0x.getEnergyStorage(var1)
      );
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(
         EnergyStorage.BLOCK, ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(), (var0x, var1) -> var0x.getEnergyStorage(var1)
      );
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.MIXER.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.PIPE_SORTER.get(), (var0x, var1) -> var0x.getInputHandler());
      var0.registerBlockEntity(ItemHandler.BLOCK, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(), (var0x, var1) -> var0x.getInventory());
      var0.registerBlockEntity(
         EnergyStorage.BLOCK, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(), (var0x, var1) -> var0x.getEnergyStorage(var1)
      );
      var0.registerBlockEntity(FluidHandler.BLOCK, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(), (var0x, var1) -> var0x.getOutputHandler());
      var0.registerBlockEntity(FluidHandler.BLOCK, ModBlockEntities.CRYSTAL_STEEL_WORKBENCH.get(), (var0x, var1) -> var0x.getTank());
      var0.registerBlockEntity(FluidHandler.BLOCK, ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), (var0x, var1) -> var0x.getTank());
      var0.registerBlockEntity(FluidHandler.BLOCK, ModBlockEntities.COPPER_FLUID_STORAGE_CABINET.get(), (var0x, var1) -> var0x.getTank());
   }

   private static <T extends MatterMachineBlockEntity> void registerMatter(RegisterCapabilitiesEvent var0, BlockEntityType<T> var1) {
      var0.registerBlockEntity(ItemHandler.BLOCK, var1, (var0x, var1x) -> var0x.getInventory());
      var0.registerBlockEntity(EnergyStorage.BLOCK, var1, (var0x, var1x) -> var0x.getEnergyStorage(var1x));
   }

   private static <T extends BioEnergyMachineBlockEntity> void registerBioEnergy(RegisterCapabilitiesEvent var0, BlockEntityType<T> var1) {
      var0.registerBlockEntity(ItemHandler.BLOCK, var1, (var0x, var1x) -> var0x.getInventory());
      var0.registerBlockEntity(EnergyStorage.BLOCK, var1, (var0x, var1x) -> var0x.getEnergyStorage(var1x));
   }
}
