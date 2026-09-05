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
import com.industrialcrops.block.PipeSorterBlock;
import com.industrialcrops.block.PlasmaJuiceBlock;
import com.industrialcrops.block.PlasmaJuiceCauldronBlock;
import com.industrialcrops.block.EnergyCableBlock;
import com.industrialcrops.block.MatterDigitizerBlock;
import com.industrialcrops.block.DigitizedItemCopierBlock;
import com.industrialcrops.block.MatterReconstructorBlock;
import com.industrialcrops.block.ItemNetworkTerminalBlock;
import com.industrialcrops.block.BioEnergyGeneratorBlock;
import com.industrialcrops.block.EnergyBatteryBlock;
import com.industrialcrops.block.ResidueIncineratorBlock;
import com.industrialcrops.block.ElectricFurnaceBlock;
import com.industrialcrops.block.DigitalMiniatureForestBlock;
import com.industrialcrops.block.AutomaticPlanterBlock;
import com.industrialcrops.block.CopperFluidStorageCabinetBlock;
import com.industrialcrops.block.EnergyBushBlock;
import com.industrialcrops.block.FluidPipeBlock;
import com.industrialcrops.block.GoldPlasmaExtractorBlock;
import com.industrialcrops.block.CometSoilBlock;
import com.industrialcrops.block.CometSaplingBlock;
import com.industrialcrops.block.StrippableRotatedPillarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
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
    public static final DeferredBlock<Block> CRYSTAL_STEEL_DEVICE_CASING = BLOCKS.register("crystal_steel_device_casing",
            () -> new Block(machineProperties().mapColor(MapColor.COLOR_PURPLE)));
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
    public static final DeferredBlock<Block> PIPE_SORTER = BLOCKS.register("pipe_sorter",
            () -> new PipeSorterBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> GOLD_FLUID_PIPE = BLOCKS.register("gold_fluid_pipe",
            () -> new FluidPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD).strength(1.0F).sound(SoundType.COPPER)));
    public static final DeferredBlock<Block> GOLD_PLASMA_EXTRACTOR = BLOCKS.register("gold_plasma_extractor",
            () -> new GoldPlasmaExtractorBlock(machineProperties().mapColor(MapColor.GOLD)));
    public static final DeferredBlock<Block> COPPER_FLUID_STORAGE_CABINET = BLOCKS.register("copper_fluid_storage_cabinet",
            () -> new CopperFluidStorageCabinetBlock(machineProperties().mapColor(MapColor.COLOR_ORANGE).noOcclusion()));

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
    public static final DeferredBlock<Block> PRISM_POD_CROP = BLOCKS.register("prism_pod_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_PRISM_POD_SEEDS,
                    ModItems.PRISM_POD, 2, 2));
    public static final DeferredBlock<Block> EMBERCOIL_CROP = BLOCKS.register("embercoil_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_EMBERCOIL_SEEDS,
                    ModItems.EMBERCOIL, 2, 3));
    public static final DeferredBlock<Block> STARBLOOM_CROP = BLOCKS.register("starbloom_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_STARBLOOM_SEEDS,
                    ModItems.STARBLOOM, 1, 2));
    public static final DeferredBlock<Block> NEONBULB_CROP = BLOCKS.register("neonbulb_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_NEONBULB_SEEDS,
                    ModItems.NEONBULB, 2, 3));
    public static final DeferredBlock<Block> FLUXSTALK_CROP = BLOCKS.register("fluxstalk_crop",
            () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_FLUXSTALK_SEEDS,
                    ModItems.FLUXSTALK, 1, 2));
    public static final DeferredBlock<Block> ENERGY_BUSH = BLOCKS.register("energy_bush",
            () -> new EnergyBushBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE).noCollission().randomTicks().instabreak()
                    .sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<net.minecraft.world.level.block.LiquidBlock> CONCENTRATED_PLASMA_JUICE = BLOCKS.register(
            "concentrated_plasma_juice", () -> new PlasmaJuiceBlock(
                    ModFluids.CONCENTRATED_PLASMA_JUICE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .lightLevel(state -> 2)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)));
    public static final DeferredBlock<PlasmaJuiceCauldronBlock> PLASMA_JUICE_CAULDRON = BLOCKS.register(
            "plasma_juice_cauldron", () -> new PlasmaJuiceCauldronBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)));

    public static final DeferredBlock<Block> COMET_SOIL = BLOCKS.register("comet_soil",
            () -> new CometSoilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.55F)
                    .sound(SoundType.ROOTED_DIRT)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_COMET_LOG = BLOCKS.register("stripped_comet_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_WARPED_STEM)
                    .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_COMET_WOOD = BLOCKS.register("stripped_comet_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_WARPED_HYPHAE)
                    .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<RotatedPillarBlock> COMET_LOG = BLOCKS.register("comet_log",
            () -> new StrippableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_STEM)
                    .mapColor(MapColor.COLOR_PURPLE), STRIPPED_COMET_LOG));
    public static final DeferredBlock<RotatedPillarBlock> COMET_WOOD = BLOCKS.register("comet_wood",
            () -> new StrippableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_HYPHAE)
                    .mapColor(MapColor.COLOR_PURPLE), STRIPPED_COMET_WOOD));
    public static final DeferredBlock<Block> COMET_PLANKS = BLOCKS.register("comet_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_PLANKS)
                    .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<StairBlock> COMET_STAIRS = BLOCKS.register("comet_stairs",
            () -> new StairBlock(COMET_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_STAIRS)));
    public static final DeferredBlock<SlabBlock> COMET_SLAB = BLOCKS.register("comet_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_SLAB)));
    public static final DeferredBlock<FenceBlock> COMET_FENCE = BLOCKS.register("comet_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_FENCE)));
    public static final DeferredBlock<FenceGateBlock> COMET_FENCE_GATE = BLOCKS.register("comet_fence_gate",
            () -> new FenceGateBlock(ModWoodTypes.COMET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> COMET_DOOR = BLOCKS.register("comet_door",
            () -> new DoorBlock(ModWoodTypes.COMET_SET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> COMET_TRAPDOOR = BLOCKS.register("comet_trapdoor",
            () -> new TrapDoorBlock(ModWoodTypes.COMET_SET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_TRAPDOOR)));
    public static final DeferredBlock<PressurePlateBlock> COMET_PRESSURE_PLATE = BLOCKS.register("comet_pressure_plate",
            () -> new PressurePlateBlock(ModWoodTypes.COMET_SET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> COMET_BUTTON = BLOCKS.register("comet_button",
            () -> new ButtonBlock(ModWoodTypes.COMET_SET, 30,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_BUTTON)));
    public static final DeferredBlock<LeavesBlock> COMET_LEAVES = BLOCKS.register("comet_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<SaplingBlock> COMET_SAPLING = BLOCKS.register("comet_sapling",
            () -> new CometSaplingBlock(ModTreeGrowers.COMET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)
                            .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<StandingSignBlock> COMET_SIGN = BLOCKS.register("comet_sign",
            () -> new StandingSignBlock(ModWoodTypes.COMET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_SIGN)));
    public static final DeferredBlock<WallSignBlock> COMET_WALL_SIGN = BLOCKS.register("comet_wall_sign",
            () -> new WallSignBlock(ModWoodTypes.COMET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_WALL_SIGN)
                            .lootFrom(COMET_SIGN)));
    public static final DeferredBlock<CeilingHangingSignBlock> COMET_HANGING_SIGN = BLOCKS.register("comet_hanging_sign",
            () -> new CeilingHangingSignBlock(ModWoodTypes.COMET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_HANGING_SIGN)));
    public static final DeferredBlock<WallHangingSignBlock> COMET_WALL_HANGING_SIGN = BLOCKS.register("comet_wall_hanging_sign",
            () -> new WallHangingSignBlock(ModWoodTypes.COMET,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WARPED_WALL_HANGING_SIGN)
                            .lootFrom(COMET_HANGING_SIGN)));
    public static final DeferredBlock<FlowerPotBlock> POTTED_COMET_SAPLING = BLOCKS.register("potted_comet_sapling",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, COMET_SAPLING,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_OAK_SAPLING)));

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
