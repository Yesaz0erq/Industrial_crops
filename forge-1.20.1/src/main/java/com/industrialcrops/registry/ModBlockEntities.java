package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.BasicCropStorageArrayBlockEntity;
import com.industrialcrops.block.entity.CropCompressorBlockEntity;
import com.industrialcrops.block.entity.CropAnalysisDeviceBlockEntity;
import com.industrialcrops.block.entity.GourdModificationDeviceBlockEntity;
import com.industrialcrops.block.entity.MixerBlockEntity;
import com.industrialcrops.block.entity.ReinforcedIndustrialStorageArrayBlockEntity;
import com.industrialcrops.block.entity.ReinforcedControlDeviceBlockEntity;
import com.industrialcrops.block.entity.RootOreExtractorBlockEntity;
import com.industrialcrops.block.entity.ProcessorProgrammerBlockEntity;
import com.industrialcrops.block.entity.SlimeIncubatorBlockEntity;
import com.industrialcrops.block.entity.TransportPipeBlockEntity;
import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.block.entity.MatterDigitizerBlockEntity;
import com.industrialcrops.block.entity.DigitizedItemCopierBlockEntity;
import com.industrialcrops.block.entity.MatterReconstructorBlockEntity;
import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import com.industrialcrops.block.entity.BioEnergyGeneratorBlockEntity;
import com.industrialcrops.block.entity.EnergyBatteryBlockEntity;
import com.industrialcrops.block.entity.ResidueIncineratorBlockEntity;
import com.industrialcrops.block.entity.EnergyCableBlockEntity;
import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.block.entity.CropGeneticsBlockEntity;
import com.industrialcrops.block.entity.AutomaticPlanterBlockEntity;
import com.industrialcrops.block.entity.PipeSorterBlockEntity;
import com.industrialcrops.block.entity.FluidPipeBlockEntity;
import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import com.industrialcrops.block.entity.CopperFluidStorageCabinetBlockEntity;
import com.industrialcrops.block.entity.BasicControlDeviceBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IndustrialCrops.MOD_ID);

    public static final RegistryObject<BlockEntityType<RootOreExtractorBlockEntity>> ROOT_ORE_EXTRACTOR =
            BLOCK_ENTITIES.register("basic_crop_conversion_device", () -> BlockEntityType.Builder
                    .of(RootOreExtractorBlockEntity::new, ModBlocks.ROOT_ORE_EXTRACTOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CropCompressorBlockEntity>> CROP_COMPRESSOR =
            BLOCK_ENTITIES.register("crop_compressor", () -> BlockEntityType.Builder
                    .of(CropCompressorBlockEntity::new, ModBlocks.CROP_COMPRESSOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CropAnalysisDeviceBlockEntity>> CROP_ANALYSIS_DEVICE =
            BLOCK_ENTITIES.register("crop_analysis_device", () -> BlockEntityType.Builder
                    .of(CropAnalysisDeviceBlockEntity::new, ModBlocks.CROP_ANALYSIS_DEVICE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<GourdModificationDeviceBlockEntity>> GOURD_MODIFICATION_DEVICE =
            BLOCK_ENTITIES.register("gourd_modification_device", () -> BlockEntityType.Builder
                    .of(GourdModificationDeviceBlockEntity::new, ModBlocks.GOURD_MODIFICATION_DEVICE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MixerBlockEntity>> MIXER =
            BLOCK_ENTITIES.register("mixer", () -> BlockEntityType.Builder
                    .of(MixerBlockEntity::new, ModBlocks.MIXER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<BasicCropStorageArrayBlockEntity>> INDUSTRIAL_CROP_STORAGE_ARRAY =
            BLOCK_ENTITIES.register("basic_storage_component", () -> BlockEntityType.Builder
                    .of(BasicCropStorageArrayBlockEntity::new, ModBlocks.INDUSTRIAL_CROP_STORAGE_ARRAY.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ReinforcedIndustrialStorageArrayBlockEntity>> REINFORCED_INDUSTRIAL_STORAGE_ARRAY =
            BLOCK_ENTITIES.register("reinforced_storage_component", () -> BlockEntityType.Builder
                    .of(ReinforcedIndustrialStorageArrayBlockEntity::new, ModBlocks.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<AdvancedIndustrialStorageBlockEntity>> ADVANCED_INDUSTRIAL_STORAGE_DEVICE =
            BLOCK_ENTITIES.register("advanced_industrial_storage_device", () -> BlockEntityType.Builder
                    .of(AdvancedIndustrialStorageBlockEntity::new, ModBlocks.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get())
                    .build(null));
    public static final RegistryObject<BlockEntityType<ReinforcedControlDeviceBlockEntity>> REINFORCED_CONTROL_DEVICE =
            BLOCK_ENTITIES.register("reinforced_control_device", () -> BlockEntityType.Builder
                    .of(ReinforcedControlDeviceBlockEntity::new, ModBlocks.REINFORCED_CONTROL_DEVICE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BasicControlDeviceBlockEntity>> BASIC_CONTROL_DEVICE =
            BLOCK_ENTITIES.register("basic_control_device", () -> BlockEntityType.Builder
                    .of(BasicControlDeviceBlockEntity::new, ModBlocks.CARROT_CONTROL_DEVICE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TransportPipeBlockEntity>> TRANSPORT_PIPE =
            BLOCK_ENTITIES.register("transport_pipe", () -> BlockEntityType.Builder
                    .of(
                            TransportPipeBlockEntity::new,
                            ModBlocks.PIPE.get(),
                            ModBlocks.INPUT_PIPE.get(),
                            ModBlocks.OUTPUT_PIPE.get(),
                            ModBlocks.REINFORCED_PIPE.get(),
                            ModBlocks.REINFORCED_INPUT_PIPE.get(),
                            ModBlocks.REINFORCED_OUTPUT_PIPE.get(),
                            ModBlocks.ADVANCED_PIPE.get(),
                            ModBlocks.ADVANCED_INPUT_PIPE.get(),
                            ModBlocks.ADVANCED_OUTPUT_PIPE.get()
                    )
                    .build(null));
    public static final RegistryObject<BlockEntityType<PipeSorterBlockEntity>> PIPE_SORTER =
            BLOCK_ENTITIES.register("pipe_sorter", () -> BlockEntityType.Builder
                    .of(PipeSorterBlockEntity::new, ModBlocks.PIPE_SORTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE =
            BLOCK_ENTITIES.register("gold_fluid_pipe", () -> BlockEntityType.Builder
                    .of(FluidPipeBlockEntity::new, ModBlocks.GOLD_FLUID_PIPE.get()).build(null));
    public static final RegistryObject<BlockEntityType<GoldPlasmaExtractorBlockEntity>> GOLD_PLASMA_EXTRACTOR =
            BLOCK_ENTITIES.register("gold_plasma_extractor", () -> BlockEntityType.Builder
                    .of(GoldPlasmaExtractorBlockEntity::new, ModBlocks.GOLD_PLASMA_EXTRACTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<CopperFluidStorageCabinetBlockEntity>> COPPER_FLUID_STORAGE_CABINET =
            BLOCK_ENTITIES.register("copper_fluid_storage_cabinet", () -> BlockEntityType.Builder
                    .of(CopperFluidStorageCabinetBlockEntity::new, ModBlocks.COPPER_FLUID_STORAGE_CABINET.get()).build(null));

    public static final RegistryObject<BlockEntityType<IncubatorBlockEntity>> INCUBATOR =
            BLOCK_ENTITIES.register("slime_converter", () -> BlockEntityType.Builder
                    .of(IncubatorBlockEntity::new, ModBlocks.INCUBATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<SlimeIncubatorBlockEntity>> SLIME_INCUBATOR =
            BLOCK_ENTITIES.register("slime_incubator", () -> BlockEntityType.Builder
                    .of(SlimeIncubatorBlockEntity::new, ModBlocks.SLIME_INCUBATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<GoldenLaunchSiloBlockEntity>> GOLDEN_LAUNCH_SILO =
            BLOCK_ENTITIES.register("explosive_potato_launcher", () -> BlockEntityType.Builder
                    .of(GoldenLaunchSiloBlockEntity::new, ModBlocks.GOLDEN_LAUNCH_SILO.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ProcessorProgrammerBlockEntity>> PROCESSOR_PROGRAMMER =
            BLOCK_ENTITIES.register("processor_programming_device", () -> BlockEntityType.Builder
                    .of(ProcessorProgrammerBlockEntity::new, ModBlocks.PROCESSOR_PROGRAMMER.get())
                    .build(null));
    public static final RegistryObject<BlockEntityType<AutomaticPlanterBlockEntity>> AUTOMATIC_PLANTER =
            BLOCK_ENTITIES.register("automatic_planter", () -> BlockEntityType.Builder
                    .of(AutomaticPlanterBlockEntity::new, ModBlocks.AUTOMATIC_PLANTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MatterDigitizerBlockEntity>> MATTER_DIGITIZER =
            BLOCK_ENTITIES.register("matter_digitization_device", () -> BlockEntityType.Builder
                    .of(MatterDigitizerBlockEntity::new, ModBlocks.MATTER_DIGITIZER.get()).build(null));
    public static final RegistryObject<BlockEntityType<DigitizedItemCopierBlockEntity>> DIGITIZED_ITEM_COPIER =
            BLOCK_ENTITIES.register("digitized_item_copier", () -> BlockEntityType.Builder
                    .of(DigitizedItemCopierBlockEntity::new, ModBlocks.DIGITIZED_ITEM_COPIER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterReconstructorBlockEntity>> MATTER_RECONSTRUCTOR =
            BLOCK_ENTITIES.register("matter_reconstruction_device", () -> BlockEntityType.Builder
                    .of(MatterReconstructorBlockEntity::new, ModBlocks.MATTER_RECONSTRUCTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<ItemNetworkTerminalBlockEntity>> ITEM_NETWORK_TERMINAL =
            BLOCK_ENTITIES.register("item_network_management_terminal", () -> BlockEntityType.Builder
                    .of(ItemNetworkTerminalBlockEntity::new, ModBlocks.ITEM_NETWORK_TERMINAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<BioEnergyGeneratorBlockEntity>> BIO_ENERGY_GENERATOR =
            BLOCK_ENTITIES.register("bio_energy_generator", () -> BlockEntityType.Builder
                    .of(BioEnergyGeneratorBlockEntity::new, ModBlocks.BIO_ENERGY_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyBatteryBlockEntity>> ENERGY_BATTERY =
            BLOCK_ENTITIES.register("energy_battery", () -> BlockEntityType.Builder
                    .of(EnergyBatteryBlockEntity::new, ModBlocks.ENERGY_BATTERY.get()).build(null));
    public static final RegistryObject<BlockEntityType<ResidueIncineratorBlockEntity>> RESIDUE_INCINERATOR =
            BLOCK_ENTITIES.register("residue_incinerator", () -> BlockEntityType.Builder
                    .of(ResidueIncineratorBlockEntity::new, ModBlocks.RESIDUE_INCINERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyCableBlockEntity>> ENERGY_CABLE =
            BLOCK_ENTITIES.register("energy_cable", () -> BlockEntityType.Builder
                    .of(EnergyCableBlockEntity::new, ModBlocks.BASIC_ENERGY_CABLE.get(), ModBlocks.ADVANCED_ENERGY_CABLE.get())
                    .build(null));
    public static final RegistryObject<BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITIES.register("electric_furnace", () -> BlockEntityType.Builder
                    .of(ElectricFurnaceBlockEntity::new, ModBlocks.ELECTRIC_FURNACE.get()).build(null));
    public static final RegistryObject<BlockEntityType<DigitalMiniatureForestBlockEntity>> DIGITAL_MINIATURE_FOREST =
            BLOCK_ENTITIES.register("digital_miniature_forest", () -> BlockEntityType.Builder
                    .of(DigitalMiniatureForestBlockEntity::new, ModBlocks.DIGITAL_MINIATURE_FOREST.get()).build(null));
    public static final RegistryObject<BlockEntityType<CropGeneticsBlockEntity>> CROP_GENETICS =
            BLOCK_ENTITIES.register("crop_genetics", () -> BlockEntityType.Builder
                    .of(CropGeneticsBlockEntity::new,
                            ModBlocks.INDUSTRIAL_CARROT_CROP.get(),
                            ModBlocks.INDUSTRIAL_POTATO_CROP.get(),
                            ModBlocks.INDUSTRIAL_WHEAT_CROP.get(),
                            ModBlocks.INDUSTRIAL_MELON_CROP.get(),
                            ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get(),
                            ModBlocks.PRISM_POD_CROP.get(),
                            ModBlocks.EMBERCOIL_CROP.get(),
                            ModBlocks.STARBLOOM_CROP.get(),
                            ModBlocks.NEONBULB_CROP.get(),
                            ModBlocks.FLUXSTALK_CROP.get(),
                            ModBlocks.INDUSTRIAL_MELON_BLOCK.get(),
                            ModBlocks.INDUSTRIAL_PUMPKIN_BLOCK.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
