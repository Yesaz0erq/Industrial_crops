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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.industrialcrops.block.entity.BioEnergyGeneratorBlockEntity;
import com.industrialcrops.block.entity.EnergyBatteryBlockEntity;
import com.industrialcrops.block.entity.ResidueIncineratorBlockEntity;
import com.industrialcrops.block.entity.EnergyCableBlockEntity;
import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.block.entity.CropGeneticsBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IndustrialCrops.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RootOreExtractorBlockEntity>> ROOT_ORE_EXTRACTOR =
            BLOCK_ENTITIES.register("basic_crop_conversion_device", () -> BlockEntityType.Builder
                    .of(RootOreExtractorBlockEntity::new, ModBlocks.ROOT_ORE_EXTRACTOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CropCompressorBlockEntity>> CROP_COMPRESSOR =
            BLOCK_ENTITIES.register("crop_compressor", () -> BlockEntityType.Builder
                    .of(CropCompressorBlockEntity::new, ModBlocks.CROP_COMPRESSOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CropAnalysisDeviceBlockEntity>> CROP_ANALYSIS_DEVICE =
            BLOCK_ENTITIES.register("crop_analysis_device", () -> BlockEntityType.Builder
                    .of(CropAnalysisDeviceBlockEntity::new, ModBlocks.CROP_ANALYSIS_DEVICE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GourdModificationDeviceBlockEntity>> GOURD_MODIFICATION_DEVICE =
            BLOCK_ENTITIES.register("gourd_modification_device", () -> BlockEntityType.Builder
                    .of(GourdModificationDeviceBlockEntity::new, ModBlocks.GOURD_MODIFICATION_DEVICE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MixerBlockEntity>> MIXER =
            BLOCK_ENTITIES.register("mixer", () -> BlockEntityType.Builder
                    .of(MixerBlockEntity::new, ModBlocks.MIXER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicCropStorageArrayBlockEntity>> INDUSTRIAL_CROP_STORAGE_ARRAY =
            BLOCK_ENTITIES.register("basic_storage_component", () -> BlockEntityType.Builder
                    .of(BasicCropStorageArrayBlockEntity::new, ModBlocks.INDUSTRIAL_CROP_STORAGE_ARRAY.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReinforcedIndustrialStorageArrayBlockEntity>> REINFORCED_INDUSTRIAL_STORAGE_ARRAY =
            BLOCK_ENTITIES.register("reinforced_storage_component", () -> BlockEntityType.Builder
                    .of(ReinforcedIndustrialStorageArrayBlockEntity::new, ModBlocks.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedIndustrialStorageBlockEntity>> ADVANCED_INDUSTRIAL_STORAGE_DEVICE =
            BLOCK_ENTITIES.register("advanced_industrial_storage_device", () -> BlockEntityType.Builder
                    .of(AdvancedIndustrialStorageBlockEntity::new, ModBlocks.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReinforcedControlDeviceBlockEntity>> REINFORCED_CONTROL_DEVICE =
            BLOCK_ENTITIES.register("reinforced_control_device", () -> BlockEntityType.Builder
                    .of(ReinforcedControlDeviceBlockEntity::new, ModBlocks.REINFORCED_CONTROL_DEVICE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransportPipeBlockEntity>> TRANSPORT_PIPE =
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IncubatorBlockEntity>> INCUBATOR =
            BLOCK_ENTITIES.register("slime_converter", () -> BlockEntityType.Builder
                    .of(IncubatorBlockEntity::new, ModBlocks.INCUBATOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SlimeIncubatorBlockEntity>> SLIME_INCUBATOR =
            BLOCK_ENTITIES.register("slime_incubator", () -> BlockEntityType.Builder
                    .of(SlimeIncubatorBlockEntity::new, ModBlocks.SLIME_INCUBATOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GoldenLaunchSiloBlockEntity>> GOLDEN_LAUNCH_SILO =
            BLOCK_ENTITIES.register("explosive_potato_launcher", () -> BlockEntityType.Builder
                    .of(GoldenLaunchSiloBlockEntity::new, ModBlocks.GOLDEN_LAUNCH_SILO.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessorProgrammerBlockEntity>> PROCESSOR_PROGRAMMER =
            BLOCK_ENTITIES.register("processor_programming_device", () -> BlockEntityType.Builder
                    .of(ProcessorProgrammerBlockEntity::new, ModBlocks.PROCESSOR_PROGRAMMER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MatterDigitizerBlockEntity>> MATTER_DIGITIZER =
            BLOCK_ENTITIES.register("matter_digitization_device", () -> BlockEntityType.Builder
                    .of(MatterDigitizerBlockEntity::new, ModBlocks.MATTER_DIGITIZER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DigitizedItemCopierBlockEntity>> DIGITIZED_ITEM_COPIER =
            BLOCK_ENTITIES.register("digitized_item_copier", () -> BlockEntityType.Builder
                    .of(DigitizedItemCopierBlockEntity::new, ModBlocks.DIGITIZED_ITEM_COPIER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MatterReconstructorBlockEntity>> MATTER_RECONSTRUCTOR =
            BLOCK_ENTITIES.register("matter_reconstruction_device", () -> BlockEntityType.Builder
                    .of(MatterReconstructorBlockEntity::new, ModBlocks.MATTER_RECONSTRUCTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemNetworkTerminalBlockEntity>> ITEM_NETWORK_TERMINAL =
            BLOCK_ENTITIES.register("item_network_management_terminal", () -> BlockEntityType.Builder
                    .of(ItemNetworkTerminalBlockEntity::new, ModBlocks.ITEM_NETWORK_TERMINAL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BioEnergyGeneratorBlockEntity>> BIO_ENERGY_GENERATOR =
            BLOCK_ENTITIES.register("bio_energy_generator", () -> BlockEntityType.Builder
                    .of(BioEnergyGeneratorBlockEntity::new, ModBlocks.BIO_ENERGY_GENERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBatteryBlockEntity>> ENERGY_BATTERY =
            BLOCK_ENTITIES.register("energy_battery", () -> BlockEntityType.Builder
                    .of(EnergyBatteryBlockEntity::new, ModBlocks.ENERGY_BATTERY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResidueIncineratorBlockEntity>> RESIDUE_INCINERATOR =
            BLOCK_ENTITIES.register("residue_incinerator", () -> BlockEntityType.Builder
                    .of(ResidueIncineratorBlockEntity::new, ModBlocks.RESIDUE_INCINERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyCableBlockEntity>> ENERGY_CABLE =
            BLOCK_ENTITIES.register("energy_cable", () -> BlockEntityType.Builder
                    .of(EnergyCableBlockEntity::new, ModBlocks.BASIC_ENERGY_CABLE.get(), ModBlocks.ADVANCED_ENERGY_CABLE.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITIES.register("electric_furnace", () -> BlockEntityType.Builder
                    .of(ElectricFurnaceBlockEntity::new, ModBlocks.ELECTRIC_FURNACE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DigitalMiniatureForestBlockEntity>> DIGITAL_MINIATURE_FOREST =
            BLOCK_ENTITIES.register("digital_miniature_forest", () -> BlockEntityType.Builder
                    .of(DigitalMiniatureForestBlockEntity::new, ModBlocks.DIGITAL_MINIATURE_FOREST.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CropGeneticsBlockEntity>> CROP_GENETICS =
            BLOCK_ENTITIES.register("crop_genetics", () -> BlockEntityType.Builder
                    .of(CropGeneticsBlockEntity::new,
                            ModBlocks.INDUSTRIAL_CARROT_CROP.get(),
                            ModBlocks.INDUSTRIAL_POTATO_CROP.get(),
                            ModBlocks.INDUSTRIAL_WHEAT_CROP.get(),
                            ModBlocks.INDUSTRIAL_MELON_CROP.get(),
                            ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get(),
                            ModBlocks.INDUSTRIAL_MELON_BLOCK.get(),
                            ModBlocks.INDUSTRIAL_PUMPKIN_BLOCK.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
