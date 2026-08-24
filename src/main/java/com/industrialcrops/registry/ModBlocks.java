package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.AdvancedIndustrialStorageBlock;
import com.industrialcrops.block.AdvancedManipulatorBlock;
import com.industrialcrops.block.BasicCropStorageArrayBlock;
import com.industrialcrops.block.BasicControlDeviceBlock;
import com.industrialcrops.block.BasicManipulatorBlock;
import com.industrialcrops.block.CropCompressorBlock;
import com.industrialcrops.block.CropAnalysisDeviceBlock;
import com.industrialcrops.block.GourdModificationDeviceBlock;
import com.industrialcrops.block.MixerBlock;
import com.industrialcrops.block.FertileFarmlandBlock;
import com.industrialcrops.block.IndustrialCropBlock;
import com.industrialcrops.block.IndustrialGourdCropBlock;
import com.industrialcrops.block.IncubatorBlock;
import com.industrialcrops.block.GoldenLaunchSiloBlock;
import com.industrialcrops.block.ReinforcedIndustrialStorageArrayBlock;
import com.industrialcrops.block.ProcessorProgrammerBlock;
import com.industrialcrops.block.ReinforcedControlDeviceBlock;
import com.industrialcrops.block.RootOreExtractorBlock;
import com.industrialcrops.block.SlimeIncubatorBlock;
import com.industrialcrops.block.TransportPipeBlock;
import com.industrialcrops.block.EnergyCableBlock;
import com.industrialcrops.block.MatterDigitizerBlock;
import com.industrialcrops.block.DigitizedItemCopierBlock;
import com.industrialcrops.block.MatterReconstructorBlock;
import com.industrialcrops.block.ItemNetworkTerminalBlock;
import com.industrialcrops.block.BioEnergyMachineBlock;
import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.block.BioEnergyGeneratorBlock;
import com.industrialcrops.block.EnergyBatteryBlock;
import com.industrialcrops.block.ResidueIncineratorBlock;
import com.industrialcrops.block.ElectricFurnaceBlock;
import com.industrialcrops.block.DigitalMiniatureForestBlock;
import com.industrialcrops.block.AutomaticPlanterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.industrialcrops.block.IndustrialFruitBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IndustrialCrops.MOD_ID);

    public static final DeferredBlock<Block> INDUSTRIAL_CARROT_BLOCK = registerCompressBlock("industrial_carrot_block", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<Block> INDUSTRIAL_POTATO_BLOCK = registerCompressBlock("industrial_potato_block", MapColor.COLOR_YELLOW);
    public static final DeferredBlock<Block> INDUSTRIAL_WHEAT_BLOCK = registerCompressBlock("industrial_wheat_block", MapColor.COLOR_YELLOW);
    public static final DeferredBlock<Block> INDUSTRIAL_MELON_BLOCK = BLOCKS.register("industrial_melon_block",
            () -> new IndustrialFruitBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(1.0F)
                    .sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> INDUSTRIAL_PUMPKIN_BLOCK = BLOCKS.register("industrial_pumpkin_block",
            () -> new IndustrialFruitBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(1.0F)
                    .sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> FUSION_MELON = BLOCKS.register("fusion_melon",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(2.0F)
                    .lightLevel(state -> 7)
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> COPPER_DEVICE_CASING = registerMachineBlock("copper_device_casing");
    public static final DeferredBlock<Block> IRON_DEVICE_CASING = registerMachineBlock("iron_device_casing");
    public static final DeferredBlock<Block> PROCESSOR_GOLD_DEVICE_CASING = registerMachineBlock("processor_integrated_gold_device_casing");
    public static final DeferredBlock<Block> ROOT_ORE_EXTRACTOR = BLOCKS.register("basic_crop_conversion_device",
            () -> new RootOreExtractorBlock(machineProperties()));
    public static final DeferredBlock<Block> CROP_COMPRESSOR = BLOCKS.register("crop_compressor",
            () -> new CropCompressorBlock(machineProperties().noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<Block> CROP_ANALYSIS_DEVICE = BLOCKS.register("crop_analysis_device",
            () -> new CropAnalysisDeviceBlock(machineProperties()));
    public static final DeferredBlock<Block> GOURD_MODIFICATION_DEVICE = BLOCKS.register("gourd_modification_device",
            () -> new GourdModificationDeviceBlock(machineProperties()));
    public static final DeferredBlock<Block> MIXER = BLOCKS.register("mixer",
            () -> new MixerBlock(machineProperties().mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER)));
    public static final DeferredBlock<Block> FERTILE_FARMLAND = BLOCKS.register("fertile_farmland",
            () -> new FertileFarmlandBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND)
                    .mapColor(MapColor.DIRT)));
    public static final DeferredBlock<Block> INDUSTRIAL_CROP_STORAGE_ARRAY = BLOCKS.register("basic_storage_component",
            () -> new BasicCropStorageArrayBlock(machineProperties()));
    public static final DeferredBlock<Block> CARROT_CONTROL_DEVICE = BLOCKS.register("basic_control_device",
            () -> new BasicControlDeviceBlock(machineProperties()));
    public static final DeferredBlock<Block> BASIC_MANIPULATOR = BLOCKS.register("basic_manipulation_device",
            () -> new BasicManipulatorBlock(machineProperties()));
    public static final DeferredBlock<Block> ADVANCED_MANIPULATOR = BLOCKS.register("advanced_manipulation_device",
            () -> new AdvancedManipulatorBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> PROCESSOR_PROGRAMMER = BLOCKS.register("processor_programming_device",
            () -> new ProcessorProgrammerBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> AUTOMATIC_PLANTER = BLOCKS.register("automatic_planter",
            () -> new AutomaticPlanterBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> MATTER_DIGITIZER = BLOCKS.register("matter_digitization_device",
            () -> new MatterDigitizerBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> DIGITIZED_ITEM_COPIER = BLOCKS.register("digitized_item_copier",
            () -> new DigitizedItemCopierBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> MATTER_RECONSTRUCTOR = BLOCKS.register("matter_reconstruction_device",
            () -> new MatterReconstructorBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> ITEM_NETWORK_TERMINAL = BLOCKS.register("item_network_management_terminal",
            () -> new ItemNetworkTerminalBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> BIO_ENERGY_GENERATOR = BLOCKS.register("bio_energy_generator",
            () -> new BioEnergyGeneratorBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> ENERGY_BATTERY = BLOCKS.register("energy_battery",
            () -> new EnergyBatteryBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> RESIDUE_INCINERATOR = BLOCKS.register("residue_incinerator",
            () -> new ResidueIncineratorBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> ELECTRIC_FURNACE = BLOCKS.register("electric_furnace",
            () -> new ElectricFurnaceBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> DIGITAL_MINIATURE_FOREST = BLOCKS.register("digital_miniature_forest",
            () -> new DigitalMiniatureForestBlock(machineProperties()
                    .mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .sound(SoundType.GLASS)));
    public static final DeferredBlock<Block> BASIC_ENERGY_CABLE = registerEnergyCable("basic_energy_cable", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<Block> ADVANCED_ENERGY_CABLE = registerEnergyCable("advanced_energy_cable", MapColor.GOLD);
    public static final DeferredBlock<Block> REINFORCED_INDUSTRIAL_STORAGE_ARRAY = BLOCKS.register("reinforced_storage_component",
            () -> new ReinforcedIndustrialStorageArrayBlock(machineProperties()));
    public static final DeferredBlock<Block> REINFORCED_CONTROL_DEVICE = BLOCKS.register("reinforced_control_device",
            () -> new ReinforcedControlDeviceBlock(machineProperties()));
    public static final DeferredBlock<Block> ADVANCED_INDUSTRIAL_STORAGE_DEVICE = BLOCKS.register("advanced_industrial_storage_device",
            () -> new AdvancedIndustrialStorageBlock(machineProperties()));
    public static final DeferredBlock<Block> INCUBATOR = BLOCKS.register("slime_converter",
            () -> new IncubatorBlock(machineProperties()
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<Block> SLIME_INCUBATOR = BLOCKS.register("slime_incubator",
            () -> new SlimeIncubatorBlock(machineProperties()));
    public static final DeferredBlock<Block> GOLDEN_LAUNCH_SILO = BLOCKS.register("explosive_potato_launcher",
            () -> new GoldenLaunchSiloBlock(machineProperties().mapColor(MapColor.GOLD)));

    public static final DeferredBlock<Block> PIPE = registerPipeBlock("basic_pipe");
    public static final DeferredBlock<Block> INPUT_PIPE = registerPipeBlock("basic_input_pipe");
    public static final DeferredBlock<Block> OUTPUT_PIPE = registerPipeBlock("basic_output_pipe");
    public static final DeferredBlock<Block> REINFORCED_PIPE = registerPipeBlock("reinforced_pipe");
    public static final DeferredBlock<Block> REINFORCED_INPUT_PIPE = registerPipeBlock("reinforced_input_pipe");
    public static final DeferredBlock<Block> REINFORCED_OUTPUT_PIPE = registerPipeBlock("reinforced_output_pipe");
    public static final DeferredBlock<Block> ADVANCED_PIPE = registerPipeBlock("advanced_pipe");
    public static final DeferredBlock<Block> ADVANCED_INPUT_PIPE = registerPipeBlock("advanced_input_pipe");
    public static final DeferredBlock<Block> ADVANCED_OUTPUT_PIPE = registerPipeBlock("advanced_output_pipe");

    public static final DeferredBlock<Block> INDUSTRIAL_CARROT_CROP = BLOCKS.register("industrial_carrot_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_CARROT,
                    ModItems.INDUSTRIAL_CARROT, 4, 2, true));
    public static final DeferredBlock<Block> INDUSTRIAL_POTATO_CROP = BLOCKS.register("industrial_potato_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_POTATO,
                    ModItems.INDUSTRIAL_POTATO, 4, 2, true));
    public static final DeferredBlock<Block> INDUSTRIAL_WHEAT_CROP = BLOCKS.register("industrial_wheat_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_WHEAT_SEEDS,
                    ModItems.INDUSTRIAL_WHEAT, 4, 2));
    public static final DeferredBlock<Block> INDUSTRIAL_MELON_CROP = BLOCKS.register("industrial_melon_crop",
            () -> new IndustrialGourdCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_MELON_SEEDS, INDUSTRIAL_MELON_BLOCK));
    public static final DeferredBlock<Block> INDUSTRIAL_PUMPKIN_CROP = BLOCKS.register("industrial_pumpkin_crop",
            () -> new IndustrialGourdCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_PUMPKIN_SEEDS, INDUSTRIAL_PUMPKIN_BLOCK));

    private ModBlocks() {
    }

    private static DeferredBlock<Block> registerCompressBlock(String id, MapColor color) {
        return BLOCKS.register(id, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(0.8F)
                .sound(SoundType.WART_BLOCK)));
    }

    private static DeferredBlock<Block> registerMachineBlock(String id) {
        return BLOCKS.register(id, () -> new Block(machineProperties()));
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .sound(SoundType.METAL);
    }

    private static DeferredBlock<Block> registerPipeBlock(String id) {
        return BLOCKS.register(id, () -> new TransportPipeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0F)
                .sound(SoundType.COPPER)));
    }

    private static DeferredBlock<Block> registerEnergyCable(String id, MapColor color) {
        return BLOCKS.register(id, () -> new EnergyCableBlock(BlockBehaviour.Properties.of()
                .mapColor(color).strength(1.0F).sound(SoundType.COPPER)));
    }

    private static BlockBehaviour.Properties cropProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY);
    }
}
