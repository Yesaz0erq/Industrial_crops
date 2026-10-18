package com.industrialcrops.registry;

import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.ExpandableStorageBlockEntity;
import com.industrialcrops.block.entity.SuperCompressorBlockEntity;
import com.industrialcrops.block.entity.AnimalBreederBlockEntity;
import com.industrialcrops.block.entity.ApAgricultureBlockEntity;
import com.industrialcrops.block.entity.ApLivestockBlockEntity;
import com.industrialcrops.block.entity.AutomaticPlanterBlockEntity;
import com.industrialcrops.block.entity.BasicControlDeviceBlockEntity;
import com.industrialcrops.block.entity.BasicCropStorageArrayBlockEntity;
import com.industrialcrops.block.entity.BioEnergyGeneratorBlockEntity;
import com.industrialcrops.block.entity.CopperFluidStorageCabinetBlockEntity;
import com.industrialcrops.block.entity.CraftingProcessorBlockEntity;
import com.industrialcrops.block.entity.CropAnalysisDeviceBlockEntity;
import com.industrialcrops.block.entity.CropCompressorBlockEntity;
import com.industrialcrops.block.entity.CropGeneticsBlockEntity;
import com.industrialcrops.block.entity.CrystalLogisticsBlockEntity;
import com.industrialcrops.block.entity.CrystalSteelWorkbenchBlockEntity;
import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.block.entity.DigitizedItemCopierBlockEntity;
import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.block.entity.EnergyBatteryBlockEntity;
import com.industrialcrops.block.entity.EnergyCableBlockEntity;
import com.industrialcrops.block.entity.FluidPipeBlockEntity;
import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.block.entity.GourdModificationDeviceBlockEntity;
import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import com.industrialcrops.block.entity.LaunchSiteBlockEntity;
import com.industrialcrops.block.entity.MatterDigitizerBlockEntity;
import com.industrialcrops.block.entity.MatterReconstructorBlockEntity;
import com.industrialcrops.block.entity.MixerBlockEntity;
import com.industrialcrops.block.entity.PipeSorterBlockEntity;
import com.industrialcrops.block.entity.ProcessorProgrammerBlockEntity;
import com.industrialcrops.block.entity.ReinforcedControlDeviceBlockEntity;
import com.industrialcrops.block.entity.ReinforcedIndustrialStorageArrayBlockEntity;
import com.industrialcrops.block.entity.ResidueIncineratorBlockEntity;
import com.industrialcrops.block.entity.RootOreExtractorBlockEntity;
import com.industrialcrops.block.entity.SatelliteControlDeviceBlockEntity;
import com.industrialcrops.block.entity.SatelliteReceiverBlockEntity;
import com.industrialcrops.block.entity.SatelliteRocketBlockEntity;
import com.industrialcrops.block.entity.SlimeIncubatorBlockEntity;
import com.industrialcrops.block.entity.TerrainProcessorBlockEntity;
import com.industrialcrops.block.entity.TransportPipeBlockEntity;
import com.industrialcrops.block.entity.UltraHighPressureInvForgeBlockEntity;
import com.industrialcrops.block.entity.WirelessEnergyTransmitterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "industrialcrops");
   public static final RegistryObject<BlockEntityType<RootOreExtractorBlockEntity>> ROOT_ORE_EXTRACTOR = BLOCK_ENTITIES.register(
      "basic_crop_conversion_device", () -> Builder.of(RootOreExtractorBlockEntity::new, new Block[]{(Block)ModBlocks.ROOT_ORE_EXTRACTOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<CropCompressorBlockEntity>> CROP_COMPRESSOR = BLOCK_ENTITIES.register(
      "crop_compressor", () -> Builder.of(CropCompressorBlockEntity::new, new Block[]{(Block)ModBlocks.CROP_COMPRESSOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<CropAnalysisDeviceBlockEntity>> CROP_ANALYSIS_DEVICE = BLOCK_ENTITIES.register(
      "crop_analysis_device", () -> Builder.of(CropAnalysisDeviceBlockEntity::new, new Block[]{(Block)ModBlocks.CROP_ANALYSIS_DEVICE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<GourdModificationDeviceBlockEntity>> MELON_MODIFICATION_DEVICE = BLOCK_ENTITIES.register(
      "melon_modification_device",
      () -> Builder.of(GourdModificationDeviceBlockEntity::new, new Block[]{(Block)ModBlocks.MELON_MODIFICATION_DEVICE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<MixerBlockEntity>> MIXER = BLOCK_ENTITIES.register(
      "mixer", () -> Builder.of(MixerBlockEntity::new, new Block[]{(Block)ModBlocks.MIXER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<BasicCropStorageArrayBlockEntity>> INDUSTRIAL_CROP_STORAGE_ARRAY = BLOCK_ENTITIES.register(
      "basic_storage_component",
      () -> Builder.of(BasicCropStorageArrayBlockEntity::new, new Block[]{(Block)ModBlocks.INDUSTRIAL_CROP_STORAGE_ARRAY.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ReinforcedIndustrialStorageArrayBlockEntity>> REINFORCED_INDUSTRIAL_STORAGE_ARRAY = BLOCK_ENTITIES.register(
      "reinforced_storage_component",
      () -> Builder.of(ReinforcedIndustrialStorageArrayBlockEntity::new, new Block[]{(Block)ModBlocks.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<AdvancedIndustrialStorageBlockEntity>> ADVANCED_INDUSTRIAL_STORAGE_DEVICE = BLOCK_ENTITIES.register(
      "advanced_industrial_storage_device",
      () -> Builder.of(AdvancedIndustrialStorageBlockEntity::new, new Block[]{(Block)ModBlocks.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ReinforcedControlDeviceBlockEntity>> REINFORCED_CONTROL_DEVICE = BLOCK_ENTITIES.register(
      "reinforced_control_device",
      () -> Builder.of(ReinforcedControlDeviceBlockEntity::new, new Block[]{(Block)ModBlocks.REINFORCED_CONTROL_DEVICE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<BasicControlDeviceBlockEntity>> BASIC_CONTROL_DEVICE = BLOCK_ENTITIES.register(
      "basic_control_device", () -> Builder.of(BasicControlDeviceBlockEntity::new, new Block[]{(Block)ModBlocks.CARROT_CONTROL_DEVICE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TransportPipeBlockEntity>> TRANSPORT_PIPE = BLOCK_ENTITIES.register(
      "transport_pipe",
      () -> Builder.of(
               TransportPipeBlockEntity::new,
               new Block[]{
                  (Block)ModBlocks.PIPE.get(),
                  (Block)ModBlocks.INPUT_PIPE.get(),
                  (Block)ModBlocks.OUTPUT_PIPE.get(),
                  (Block)ModBlocks.REINFORCED_PIPE.get(),
                  (Block)ModBlocks.REINFORCED_INPUT_PIPE.get(),
                  (Block)ModBlocks.REINFORCED_OUTPUT_PIPE.get(),
                  (Block)ModBlocks.ADVANCED_PIPE.get(),
                  (Block)ModBlocks.ADVANCED_INPUT_PIPE.get(),
                  (Block)ModBlocks.ADVANCED_OUTPUT_PIPE.get()
               }
            )
            .build(null)
   );
   public static final RegistryObject<BlockEntityType<PipeSorterBlockEntity>> PIPE_SORTER = BLOCK_ENTITIES.register(
      "pipe_sorter", () -> Builder.of(PipeSorterBlockEntity::new, new Block[]{(Block)ModBlocks.PIPE_SORTER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = BLOCK_ENTITIES.register(
      "gold_fluid_pipe", () -> Builder.of(FluidPipeBlockEntity::new, new Block[]{(Block)ModBlocks.GOLD_FLUID_PIPE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<GoldPlasmaExtractorBlockEntity>> GOLD_PLASMA_EXTRACTOR = BLOCK_ENTITIES.register(
      "extraction_device", () -> Builder.of(GoldPlasmaExtractorBlockEntity::new, new Block[]{(Block)ModBlocks.GOLD_PLASMA_EXTRACTOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<CrystalSteelWorkbenchBlockEntity>> CRYSTAL_STEEL_WORKBENCH = BLOCK_ENTITIES.register(
      "crystal_steel_workbench",
      () -> Builder.of(CrystalSteelWorkbenchBlockEntity::new, new Block[]{(Block)ModBlocks.CRYSTAL_STEEL_WORKBENCH.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<CopperFluidStorageCabinetBlockEntity>> COPPER_FLUID_STORAGE_CABINET = BLOCK_ENTITIES.register(
      "copper_fluid_storage_cabinet",
      () -> Builder.of(CopperFluidStorageCabinetBlockEntity::new, new Block[]{(Block)ModBlocks.COPPER_FLUID_STORAGE_CABINET.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<IncubatorBlockEntity>> INCUBATOR = BLOCK_ENTITIES.register(
      "slime_conversion_device", () -> Builder.of(IncubatorBlockEntity::new, new Block[]{(Block)ModBlocks.INCUBATOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<SlimeIncubatorBlockEntity>> SLIME_INCUBATOR = BLOCK_ENTITIES.register(
      "slime_cultivation_device", () -> Builder.of(SlimeIncubatorBlockEntity::new, new Block[]{(Block)ModBlocks.SLIME_INCUBATOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<GoldenLaunchSiloBlockEntity>> GOLDEN_LAUNCH_SILO = BLOCK_ENTITIES.register(
      "explosive_potato_launcher", () -> Builder.of(GoldenLaunchSiloBlockEntity::new, new Block[]{(Block)ModBlocks.GOLDEN_LAUNCH_SILO.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ProcessorProgrammerBlockEntity>> PROCESSOR_PROGRAMMER = BLOCK_ENTITIES.register(
      "component_programming_device",
      () -> Builder.of(ProcessorProgrammerBlockEntity::new, new Block[]{(Block)ModBlocks.PROCESSOR_PROGRAMMER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<AutomaticPlanterBlockEntity>> AUTOMATIC_PLANTER = BLOCK_ENTITIES.register(
      "automatic_sowing_device", () -> Builder.of(AutomaticPlanterBlockEntity::new, new Block[]{(Block)ModBlocks.AUTOMATIC_PLANTER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<MatterDigitizerBlockEntity>> MATTER_DIGITIZER = BLOCK_ENTITIES.register(
      "matter_digitization_device", () -> Builder.of(MatterDigitizerBlockEntity::new, new Block[]{(Block)ModBlocks.MATTER_DIGITIZER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<DigitizedItemCopierBlockEntity>> DIGITIZED_ITEM_COPIER = BLOCK_ENTITIES.register(
      "data_copying_device", () -> Builder.of(DigitizedItemCopierBlockEntity::new, new Block[]{(Block)ModBlocks.DIGITIZED_ITEM_COPIER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<MatterReconstructorBlockEntity>> MATTER_RECONSTRUCTOR = BLOCK_ENTITIES.register(
      "matter_reconstruction_device",
      () -> Builder.of(MatterReconstructorBlockEntity::new, new Block[]{(Block)ModBlocks.MATTER_RECONSTRUCTOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ItemNetworkTerminalBlockEntity>> ITEM_NETWORK_TERMINAL = BLOCK_ENTITIES.register(
      "item_network_management_terminal",
      () -> Builder.of(ItemNetworkTerminalBlockEntity::new, new Block[]{(Block)ModBlocks.ITEM_NETWORK_TERMINAL.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<BioEnergyGeneratorBlockEntity>> BIO_ENERGY_GENERATOR = BLOCK_ENTITIES.register(
      "bio_energy_generation_device",
      () -> Builder.of(BioEnergyGeneratorBlockEntity::new, new Block[]{(Block)ModBlocks.BIO_ENERGY_GENERATOR.get(), (Block)ModBlocks.BIO_ENERGY_REACTOR.get()})
            .build(null)
   );
   public static final RegistryObject<BlockEntityType<EnergyBatteryBlockEntity>> ENERGY_BATTERY = BLOCK_ENTITIES.register(
      "gold_energy_storage_cabinet", () -> Builder.of(EnergyBatteryBlockEntity::new, new Block[]{(Block)ModBlocks.ENERGY_BATTERY.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ResidueIncineratorBlockEntity>> RESIDUE_INCINERATOR = BLOCK_ENTITIES.register(
      "incinerator", () -> Builder.of(ResidueIncineratorBlockEntity::new, new Block[]{(Block)ModBlocks.RESIDUE_INCINERATOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<EnergyCableBlockEntity>> ENERGY_CABLE = BLOCK_ENTITIES.register(
      "energy_cable",
      () -> Builder.of(EnergyCableBlockEntity::new, new Block[]{(Block)ModBlocks.BASIC_ENERGY_CABLE.get(), (Block)ModBlocks.ADVANCED_ENERGY_CABLE.get()})
            .build(null)
   );
   public static final RegistryObject<BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE = BLOCK_ENTITIES.register(
      "electric_furnace", () -> Builder.of(ElectricFurnaceBlockEntity::new, new Block[]{(Block)ModBlocks.ELECTRIC_FURNACE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ExpandableStorageBlockEntity>> EXPANDABLE_STORAGE_DEVICE = BLOCK_ENTITIES.register(
      "expandable_storage_device",
      () -> Builder.of(ExpandableStorageBlockEntity::new, new Block[]{(Block)ModBlocks.EXPANDABLE_STORAGE_DEVICE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<com.industrialcrops.block.entity.IntegratedSmeltingFurnaceBlockEntity>> INTEGRATED_SMELTING_FURNACE = BLOCK_ENTITIES.register(
      "integrated_smelting_furnace",
      () -> Builder.of(com.industrialcrops.block.entity.IntegratedSmeltingFurnaceBlockEntity::new, new Block[]{(Block)ModBlocks.INTEGRATED_SMELTING_FURNACE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<SuperCompressorBlockEntity>> SUPER_COMPRESSOR = BLOCK_ENTITIES.register(
      "super_compressor",
      () -> Builder.of(SuperCompressorBlockEntity::new, new Block[]{(Block)ModBlocks.SUPER_COMPRESSOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<com.industrialcrops.block.entity.SuperCropCultivatorBlockEntity>> SUPER_CROP_CULTIVATOR = BLOCK_ENTITIES.register(
      "super_crop_cultivator",
      () -> Builder.of(com.industrialcrops.block.entity.SuperCropCultivatorBlockEntity::new, new Block[]{(Block)ModBlocks.SUPER_CROP_CULTIVATOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<UltraHighPressureInvForgeBlockEntity>> ULTRA_HIGH_PRESSURE_INV_FORGE = BLOCK_ENTITIES.register(
      "ultra_high_pressure_inv_forge",
      () -> Builder.of(UltraHighPressureInvForgeBlockEntity::new, new Block[]{(Block)ModBlocks.ULTRA_HIGH_PRESSURE_INV_FORGE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<DigitalMiniatureForestBlockEntity>> DIGITAL_MINIATURE_FOREST = BLOCK_ENTITIES.register(
      "digital_miniature_forest",
      () -> Builder.of(DigitalMiniatureForestBlockEntity::new, new Block[]{(Block)ModBlocks.DIGITAL_MINIATURE_FOREST.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<CropGeneticsBlockEntity>> CROP_GENETICS = BLOCK_ENTITIES.register(
      "crop_genetics",
      () -> Builder.of(
               CropGeneticsBlockEntity::new,
               new Block[]{
                  (Block)ModBlocks.INDUSTRIAL_CARROT_CROP.get(),
                  (Block)ModBlocks.INDUSTRIAL_POTATO_CROP.get(),
                  (Block)ModBlocks.INDUSTRIAL_WHEAT_CROP.get(),
                  (Block)ModBlocks.INDUSTRIAL_MELON_CROP.get(),
                  (Block)ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get(),
                  (Block)ModBlocks.PRISM_POD_CROP.get(),
                  (Block)ModBlocks.EMBERCOIL_CROP.get(),
                  (Block)ModBlocks.STARBLOOM_CROP.get(),
                  (Block)ModBlocks.NEONBULB_CROP.get(),
                  (Block)ModBlocks.FLUXSTALK_CROP.get(),
                  (Block)ModBlocks.INDUSTRIAL_MELON_BLOCK.get(),
                  (Block)ModBlocks.INDUSTRIAL_PUMPKIN_BLOCK.get()
               }
            )
            .build(null)
   );
   public static final RegistryObject<BlockEntityType<WirelessEnergyTransmitterBlockEntity>> WIRELESS_ENERGY_TRANSMITTER = BLOCK_ENTITIES.register(
      "wireless_energy_transfer_device",
      () -> Builder.of(WirelessEnergyTransmitterBlockEntity::new, new Block[]{(Block)ModBlocks.WIRELESS_ENERGY_TRANSMITTER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<SatelliteReceiverBlockEntity>> SATELLITE_RECEIVER = BLOCK_ENTITIES.register(
      "satellite_receiver", () -> Builder.of(SatelliteReceiverBlockEntity::new, new Block[]{(Block)ModBlocks.SATELLITE_RECEIVER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<CrystalLogisticsBlockEntity>> CRYSTAL_LOGISTICS = BLOCK_ENTITIES.register(
      "wireless_logistics_transfer_device",
      () -> Builder.of(CrystalLogisticsBlockEntity::new, new Block[]{(Block)ModBlocks.CRYSTAL_LOGISTICS.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<CraftingProcessorBlockEntity>> CRAFTING_PROCESSOR = BLOCK_ENTITIES.register(
      "crafting_processing_device", () -> Builder.of(CraftingProcessorBlockEntity::new, new Block[]{(Block)ModBlocks.CRAFTING_PROCESSOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TerrainProcessorBlockEntity>> TERRAIN_PROCESSOR = BLOCK_ENTITIES.register(
      "terrain_processing_device", () -> Builder.of(TerrainProcessorBlockEntity::new, new Block[]{(Block)ModBlocks.TERRAIN_PROCESSOR.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<LaunchSiteBlockEntity>> LAUNCH_SITE = BLOCK_ENTITIES.register(
      "rocket_launch_site", () -> Builder.of(LaunchSiteBlockEntity::new, new Block[]{(Block)ModBlocks.LAUNCH_SITE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<AnimalBreederBlockEntity>> ANIMAL_BREEDER = BLOCK_ENTITIES.register(
      "automatic_animal_breeder", () -> Builder.of(AnimalBreederBlockEntity::new, new Block[]{(Block)ModBlocks.ANIMAL_BREEDER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<SatelliteRocketBlockEntity>> SATELLITE_ROCKET = BLOCK_ENTITIES.register(
      "satellite_rocket",
      () -> Builder.of(
               SatelliteRocketBlockEntity::new,
               new Block[]{
                  (Block)ModBlocks.SMALL_SATELLITE_ROCKET.get(), (Block)ModBlocks.MEDIUM_SATELLITE_ROCKET.get(), (Block)ModBlocks.LARGE_SATELLITE_ROCKET.get()
               }
            )
            .build(null)
   );
   public static final RegistryObject<BlockEntityType<SatelliteControlDeviceBlockEntity>> SATELLITE_CONTROL_DEVICE = BLOCK_ENTITIES.register(
      "satellite_control_device",
      () -> Builder.of(SatelliteControlDeviceBlockEntity::new, new Block[]{(Block)ModBlocks.SATELLITE_CONTROL_DEVICE.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ApAgricultureBlockEntity>> AP_AGRICULTURE = BLOCK_ENTITIES.register(
      "ap_agricultural_complex", () -> Builder.of(ApAgricultureBlockEntity::new, new Block[]{(Block)ModBlocks.AP_AGRICULTURE_CONTROLLER.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<ApLivestockBlockEntity>> AP_LIVESTOCK = BLOCK_ENTITIES.register(
      "ap_livestock_ranch", () -> Builder.of(ApLivestockBlockEntity::new, new Block[]{(Block)ModBlocks.AP_LIVESTOCK_CONTROLLER.get()}).build(null)
   );

   private ModBlockEntities() {
   }
}
