package com.industrialcrops.registry;

import com.industrialcrops.block.AdvancedIndustrialStorageBlock;
import com.industrialcrops.block.AdvancedManipulatorBlock;
import com.industrialcrops.block.AnimalBreederBlock;
import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.block.AutomaticPlanterBlock;
import com.industrialcrops.block.BasicControlDeviceBlock;
import com.industrialcrops.block.BasicCropStorageArrayBlock;
import com.industrialcrops.block.BasicManipulatorBlock;
import com.industrialcrops.block.BioEnergyGeneratorBlock;
import com.industrialcrops.block.BioEnergyReactorBlock;
import com.industrialcrops.block.CometSaplingBlock;
import com.industrialcrops.block.CometSoilBlock;
import com.industrialcrops.block.CopperFluidStorageCabinetBlock;
import com.industrialcrops.block.CraftingProcessorBlock;
import com.industrialcrops.block.CropAnalysisDeviceBlock;
import com.industrialcrops.block.CropCompressorBlock;
import com.industrialcrops.block.CrystalLogisticsBlock;
import com.industrialcrops.block.CrystalSteelWorkbenchBlock;
import com.industrialcrops.block.DigitalMiniatureForestBlock;
import com.industrialcrops.block.DigitizedItemCopierBlock;
import com.industrialcrops.block.ElectricFurnaceBlock;
import com.industrialcrops.block.EnergyBatteryBlock;
import com.industrialcrops.block.EnergyBushBlock;
import com.industrialcrops.block.EnergyCableBlock;
import com.industrialcrops.block.FertileFarmlandBlock;
import com.industrialcrops.block.FluidPipeBlock;
import com.industrialcrops.block.GoldPlasmaExtractorBlock;
import com.industrialcrops.block.GoldenLaunchSiloBlock;
import com.industrialcrops.block.GourdModificationDeviceBlock;
import com.industrialcrops.block.IncubatorBlock;
import com.industrialcrops.block.IndustrialCropBlock;
import com.industrialcrops.block.IndustrialFruitBlock;
import com.industrialcrops.block.IndustrialGourdCropBlock;
import com.industrialcrops.block.ItemNetworkTerminalBlock;
import com.industrialcrops.block.LaunchSiteBlock;
import com.industrialcrops.block.MatterDigitizerBlock;
import com.industrialcrops.block.MatterReconstructorBlock;
import com.industrialcrops.block.MixerBlock;
import com.industrialcrops.block.PipeSorterBlock;
import com.industrialcrops.block.PlasmaJuiceBlock;
import com.industrialcrops.block.PlasmaJuiceCauldronBlock;
import com.industrialcrops.block.ProcessorProgrammerBlock;
import com.industrialcrops.block.ReinforcedControlDeviceBlock;
import com.industrialcrops.block.ReinforcedIndustrialStorageArrayBlock;
import com.industrialcrops.block.ResidueIncineratorBlock;
import com.industrialcrops.block.RootOreExtractorBlock;
import com.industrialcrops.block.SatelliteControlDeviceBlock;
import com.industrialcrops.block.SatelliteReceiverBlock;
import com.industrialcrops.block.SatelliteRocketBlock;
import com.industrialcrops.block.SlimeIncubatorBlock;
import com.industrialcrops.block.StrippableRotatedPillarBlock;
import com.industrialcrops.block.TerrainProcessorBlock;
import com.industrialcrops.block.TransportPipeBlock;
import com.industrialcrops.block.TyconPressurizedFurnaceBlock;
import com.industrialcrops.block.WirelessEnergyTransmitterBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
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
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("industrialcrops");
   public static final DeferredBlock<Block> INDUSTRIAL_CARROT_BLOCK = registerCompressBlock("industrial_carrot_block", MapColor.COLOR_ORANGE);
   public static final DeferredBlock<Block> INDUSTRIAL_POTATO_BLOCK = registerCompressBlock("industrial_potato_block", MapColor.COLOR_YELLOW);
   public static final DeferredBlock<Block> INDUSTRIAL_WHEAT_BLOCK = registerCompressBlock("industrial_wheat_block", MapColor.COLOR_YELLOW);
   public static final DeferredBlock<Block> INDUSTRIAL_MELON_BLOCK = BLOCKS.register(
      "industrial_melon_block", () -> new IndustrialFruitBlock(Properties.of().mapColor(MapColor.COLOR_GREEN).strength(1.0F).sound(SoundType.WOOD))
   );
   public static final DeferredBlock<Block> INDUSTRIAL_PUMPKIN_BLOCK = BLOCKS.register(
      "industrial_pumpkin_block", () -> new IndustrialFruitBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0F).sound(SoundType.WOOD))
   );
   public static final DeferredBlock<Block> FUSION_MELON = BLOCKS.register(
      "fusion_melon", () -> new Block(Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(2.0F).lightLevel(var0 -> 7).sound(SoundType.WOOD))
   );
   public static final DeferredBlock<Block> COPPER_DEVICE_CASING = registerMachineBlock("copper_device_casing");
   public static final DeferredBlock<Block> IRON_DEVICE_CASING = registerMachineBlock("iron_device_casing");
   public static final DeferredBlock<Block> PROCESSOR_GOLD_DEVICE_CASING = registerMachineBlock("component_substrate_integrated_gold_device_casing");
   public static final DeferredBlock<Block> CRYSTAL_STEEL_WORKBENCH = BLOCKS.register(
      "crystal_steel_workbench", () -> new CrystalSteelWorkbenchBlock(machineProperties().mapColor(MapColor.COLOR_PURPLE))
   );
   public static final DeferredBlock<Block> CRYSTAL_STEEL_DEVICE_CASING = BLOCKS.register(
      "crystal_steel_casing", () -> new Block(machineProperties().mapColor(MapColor.COLOR_PURPLE))
   );
   public static final DeferredBlock<Block> ROOT_ORE_EXTRACTOR = BLOCKS.register(
      "basic_crop_conversion_device", () -> new RootOreExtractorBlock(machineProperties())
   );
   public static final DeferredBlock<Block> CROP_COMPRESSOR = BLOCKS.register(
      "crop_compressor", () -> new CropCompressorBlock(machineProperties().noOcclusion().pushReaction(PushReaction.BLOCK))
   );
   public static final DeferredBlock<Block> CROP_ANALYSIS_DEVICE = BLOCKS.register(
      "crop_analysis_device", () -> new CropAnalysisDeviceBlock(machineProperties())
   );
   public static final DeferredBlock<Block> GOURD_MODIFICATION_DEVICE = BLOCKS.register(
      "gourd_modification_device", () -> new GourdModificationDeviceBlock(machineProperties())
   );
   public static final DeferredBlock<Block> MIXER = BLOCKS.register(
      "mixer", () -> new MixerBlock(machineProperties().mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER))
   );
   public static final DeferredBlock<Block> FERTILE_FARMLAND = BLOCKS.register(
      "fertile_soil", () -> new FertileFarmlandBlock(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.FARMLAND).mapColor(MapColor.DIRT))
   );
   public static final DeferredBlock<Block> INDUSTRIAL_CROP_STORAGE_ARRAY = BLOCKS.register(
      "basic_storage_component", () -> new BasicCropStorageArrayBlock(machineProperties())
   );
   public static final DeferredBlock<Block> CARROT_CONTROL_DEVICE = BLOCKS.register(
      "basic_control_device", () -> new BasicControlDeviceBlock(machineProperties())
   );
   public static final DeferredBlock<Block> BASIC_MANIPULATOR = BLOCKS.register(
      "basic_manipulation_device", () -> new BasicManipulatorBlock(machineProperties())
   );
   public static final DeferredBlock<Block> ADVANCED_MANIPULATOR = BLOCKS.register(
      "advanced_manipulation_device", () -> new AdvancedManipulatorBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> PROCESSOR_PROGRAMMER = BLOCKS.register(
      "component_programming_device", () -> new ProcessorProgrammerBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> AUTOMATIC_PLANTER = BLOCKS.register(
      "automatic_sowing_device", () -> new AutomaticPlanterBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> MATTER_DIGITIZER = BLOCKS.register(
      "matter_digitization_device", () -> new MatterDigitizerBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> DIGITIZED_ITEM_COPIER = BLOCKS.register(
      "data_copying_device", () -> new DigitizedItemCopierBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> MATTER_RECONSTRUCTOR = BLOCKS.register(
      "matter_reconstruction_device", () -> new MatterReconstructorBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> ITEM_NETWORK_TERMINAL = BLOCKS.register(
      "item_network_management_terminal", () -> new ItemNetworkTerminalBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> BIO_ENERGY_GENERATOR = BLOCKS.register(
      "bio_energy_generation_device", () -> new BioEnergyGeneratorBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> BIO_ENERGY_REACTOR = BLOCKS.register(
      "bio_energy_reaction_device", () -> new BioEnergyReactorBlock(machineProperties().mapColor(MapColor.COLOR_PURPLE))
   );
   public static final DeferredBlock<Block> ENERGY_BATTERY = BLOCKS.register(
      "gold_energy_storage_cabinet", () -> new EnergyBatteryBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> RESIDUE_INCINERATOR = BLOCKS.register(
      "incinerator", () -> new ResidueIncineratorBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> ELECTRIC_FURNACE = BLOCKS.register(
      "electric_furnace", () -> new ElectricFurnaceBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> TYCON_PRESSURIZED_FURNACE = BLOCKS.register(
      "tycon_pressurized_furnace", () -> new TyconPressurizedFurnaceBlock(machineProperties().mapColor(MapColor.COLOR_ORANGE))
   );
   public static final DeferredBlock<Block> DIGITAL_MINIATURE_FOREST = BLOCKS.register(
      "digital_miniature_forest", () -> new DigitalMiniatureForestBlock(machineProperties().mapColor(MapColor.GOLD).noOcclusion().sound(SoundType.GLASS))
   );
   public static final DeferredBlock<Block> BASIC_ENERGY_CABLE = registerEnergyCable("basic_energy_cable", MapColor.COLOR_ORANGE);
   public static final DeferredBlock<Block> ADVANCED_ENERGY_CABLE = registerEnergyCable("advanced_energy_cable", MapColor.GOLD);
   public static final DeferredBlock<Block> REINFORCED_INDUSTRIAL_STORAGE_ARRAY = BLOCKS.register(
      "reinforced_storage_component", () -> new ReinforcedIndustrialStorageArrayBlock(machineProperties())
   );
   public static final DeferredBlock<Block> REINFORCED_CONTROL_DEVICE = BLOCKS.register(
      "reinforced_control_device", () -> new ReinforcedControlDeviceBlock(machineProperties())
   );
   public static final DeferredBlock<Block> ADVANCED_INDUSTRIAL_STORAGE_DEVICE = BLOCKS.register(
      "advanced_industrial_storage_device", () -> new AdvancedIndustrialStorageBlock(machineProperties())
   );
   public static final DeferredBlock<Block> INCUBATOR = BLOCKS.register(
      "slime_conversion_device", () -> new IncubatorBlock(machineProperties().noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK))
   );
   public static final DeferredBlock<Block> SLIME_INCUBATOR = BLOCKS.register("slime_cultivation_device", () -> new SlimeIncubatorBlock(machineProperties()));
   public static final DeferredBlock<Block> GOLDEN_LAUNCH_SILO = BLOCKS.register(
      "explosive_potato_launcher", () -> new GoldenLaunchSiloBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> PIPE = registerPipeBlock("basic_pipe");
   public static final DeferredBlock<Block> INPUT_PIPE = registerPipeBlock("basic_input_pipe");
   public static final DeferredBlock<Block> OUTPUT_PIPE = registerPipeBlock("basic_output_pipe");
   public static final DeferredBlock<Block> REINFORCED_PIPE = registerPipeBlock("reinforced_pipe");
   public static final DeferredBlock<Block> REINFORCED_INPUT_PIPE = registerPipeBlock("reinforced_input_pipe");
   public static final DeferredBlock<Block> REINFORCED_OUTPUT_PIPE = registerPipeBlock("reinforced_output_pipe");
   public static final DeferredBlock<Block> ADVANCED_PIPE = registerPipeBlock("advanced_pipe");
   public static final DeferredBlock<Block> ADVANCED_INPUT_PIPE = registerPipeBlock("advanced_input_pipe");
   public static final DeferredBlock<Block> ADVANCED_OUTPUT_PIPE = registerPipeBlock("advanced_output_pipe");
   public static final DeferredBlock<Block> PIPE_SORTER = BLOCKS.register("pipe_sorter", () -> new PipeSorterBlock(machineProperties().mapColor(MapColor.GOLD)));
   public static final DeferredBlock<Block> GOLD_FLUID_PIPE = BLOCKS.register(
      "gold_fluid_pipe", () -> new FluidPipeBlock(Properties.of().mapColor(MapColor.GOLD).strength(1.0F).sound(SoundType.COPPER))
   );
   public static final DeferredBlock<Block> GOLD_PLASMA_EXTRACTOR = BLOCKS.register(
      "extraction_device", () -> new GoldPlasmaExtractorBlock(machineProperties().mapColor(MapColor.GOLD))
   );
   public static final DeferredBlock<Block> COPPER_FLUID_STORAGE_CABINET = BLOCKS.register(
      "copper_fluid_storage_cabinet", () -> new CopperFluidStorageCabinetBlock(machineProperties().mapColor(MapColor.COLOR_ORANGE).noOcclusion())
   );
   public static final DeferredBlock<Block> INDUSTRIAL_CARROT_CROP = BLOCKS.register(
      "industrial_carrot_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_CARROT, ModItems.INDUSTRIAL_CARROT, 4, 2, true)
   );
   public static final DeferredBlock<Block> INDUSTRIAL_POTATO_CROP = BLOCKS.register(
      "industrial_potato_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_POTATO, ModItems.INDUSTRIAL_POTATO, 4, 2, true)
   );
   public static final DeferredBlock<Block> INDUSTRIAL_WHEAT_CROP = BLOCKS.register(
      "industrial_wheat_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_WHEAT_SEEDS, ModItems.INDUSTRIAL_WHEAT, 4, 2)
   );
   public static final DeferredBlock<Block> INDUSTRIAL_MELON_CROP = BLOCKS.register(
      "industrial_melon_crop", () -> new IndustrialGourdCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_MELON_SEEDS, INDUSTRIAL_MELON_BLOCK)
   );
   public static final DeferredBlock<Block> INDUSTRIAL_PUMPKIN_CROP = BLOCKS.register(
      "industrial_pumpkin_crop", () -> new IndustrialGourdCropBlock(cropProperties(), ModItems.BAGGED_INDUSTRIAL_PUMPKIN_SEEDS, INDUSTRIAL_PUMPKIN_BLOCK)
   );
   public static final DeferredBlock<Block> PRISM_POD_CROP = BLOCKS.register(
      "prism_pod_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_PRISM_POD_SEEDS, ModItems.PRISM_POD, 2, 2)
   );
   public static final DeferredBlock<Block> EMBERCOIL_CROP = BLOCKS.register(
      "embercoil_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_EMBERCOIL_SEEDS, ModItems.EMBERCOIL, 2, 3)
   );
   public static final DeferredBlock<Block> STARBLOOM_CROP = BLOCKS.register(
      "starbloom_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_STARBLOOM_SEEDS, ModItems.STARBLOOM, 1, 2)
   );
   public static final DeferredBlock<Block> NEONBULB_CROP = BLOCKS.register(
      "neonbulb_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_NEONBULB_SEEDS, ModItems.NEONBULB, 2, 3)
   );
   public static final DeferredBlock<Block> FLUXSTALK_CROP = BLOCKS.register(
      "fluxstalk_crop", () -> new IndustrialCropBlock(cropProperties(), ModItems.BAGGED_FLUXSTALK_SEEDS, ModItems.FLUXSTALK, 1, 2)
   );
   public static final DeferredBlock<Block> ENERGY_BUSH = BLOCKS.register(
      "energy_bush",
      () -> new EnergyBushBlock(
            Properties.of()
               .mapColor(MapColor.COLOR_BLUE)
               .noCollission()
               .randomTicks()
               .instabreak()
               .sound(SoundType.SWEET_BERRY_BUSH)
               .pushReaction(PushReaction.DESTROY)
         )
   );
   public static final DeferredBlock<LiquidBlock> CONCENTRATED_PLASMA_JUICE = BLOCKS.register(
      "concentrated_plasma_juice",
      () -> new PlasmaJuiceBlock(
            (FlowingFluid)ModFluids.CONCENTRATED_PLASMA_JUICE.get(),
            Properties.of()
               .mapColor(MapColor.COLOR_BLUE)
               .replaceable()
               .noCollission()
               .strength(100.0F)
               .lightLevel(var0 -> 2)
               .pushReaction(PushReaction.DESTROY)
               .noLootTable()
               .liquid()
               .sound(SoundType.EMPTY)
         )
   );
   public static final DeferredBlock<PlasmaJuiceCauldronBlock> PLASMA_JUICE_CAULDRON = BLOCKS.register(
      "plasma_juice_cauldron", () -> new PlasmaJuiceCauldronBlock(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.CAULDRON))
   );
   public static final DeferredBlock<Block> COMET_SOIL = BLOCKS.register(
      "comet_soil",
      () -> new CometSoilBlock(
            Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.DIRT).mapColor(MapColor.COLOR_PURPLE).strength(0.55F).sound(SoundType.ROOTED_DIRT)
         )
   );
   public static final DeferredBlock<RotatedPillarBlock> STRIPPED_COMET_LOG = BLOCKS.register(
      "stripped_comet_log",
      () -> new RotatedPillarBlock(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STRIPPED_WARPED_STEM).mapColor(MapColor.COLOR_PURPLE))
   );
   public static final DeferredBlock<RotatedPillarBlock> STRIPPED_COMET_WOOD = BLOCKS.register(
      "stripped_comet_wood",
      () -> new RotatedPillarBlock(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STRIPPED_WARPED_HYPHAE).mapColor(MapColor.COLOR_PURPLE))
   );
   public static final DeferredBlock<RotatedPillarBlock> COMET_LOG = BLOCKS.register(
      "comet_log",
      () -> new StrippableRotatedPillarBlock(
            Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_STEM).mapColor(MapColor.COLOR_PURPLE), STRIPPED_COMET_LOG
         )
   );
   public static final DeferredBlock<RotatedPillarBlock> COMET_WOOD = BLOCKS.register(
      "comet_wood",
      () -> new StrippableRotatedPillarBlock(
            Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_HYPHAE).mapColor(MapColor.COLOR_PURPLE), STRIPPED_COMET_WOOD
         )
   );
   public static final DeferredBlock<Block> COMET_PLANKS = BLOCKS.register(
      "comet_planks", () -> new Block(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_PLANKS).mapColor(MapColor.COLOR_PURPLE))
   );
   public static final DeferredBlock<StairBlock> COMET_STAIRS = BLOCKS.register(
      "comet_stairs",
      () -> new StairBlock(((Block)COMET_PLANKS.get()).defaultBlockState(), Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_STAIRS))
   );
   public static final DeferredBlock<SlabBlock> COMET_SLAB = BLOCKS.register(
      "comet_slab", () -> new SlabBlock(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_SLAB))
   );
   public static final DeferredBlock<FenceBlock> COMET_FENCE = BLOCKS.register(
      "comet_fence", () -> new FenceBlock(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_FENCE))
   );
   public static final DeferredBlock<FenceGateBlock> COMET_FENCE_GATE = BLOCKS.register(
      "comet_fence_gate", () -> new FenceGateBlock(ModWoodTypes.COMET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_FENCE_GATE))
   );
   public static final DeferredBlock<DoorBlock> COMET_DOOR = BLOCKS.register(
      "comet_door", () -> new DoorBlock(ModWoodTypes.COMET_SET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_DOOR))
   );
   public static final DeferredBlock<TrapDoorBlock> COMET_TRAPDOOR = BLOCKS.register(
      "comet_trapdoor", () -> new TrapDoorBlock(ModWoodTypes.COMET_SET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_TRAPDOOR))
   );
   public static final DeferredBlock<PressurePlateBlock> COMET_PRESSURE_PLATE = BLOCKS.register(
      "comet_pressure_plate",
      () -> new PressurePlateBlock(ModWoodTypes.COMET_SET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_PRESSURE_PLATE))
   );
   public static final DeferredBlock<ButtonBlock> COMET_BUTTON = BLOCKS.register(
      "comet_button", () -> new ButtonBlock(ModWoodTypes.COMET_SET, 30, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_BUTTON))
   );
   public static final DeferredBlock<LeavesBlock> COMET_LEAVES = BLOCKS.register(
      "comet_leaves", () -> new LeavesBlock(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_LEAVES).mapColor(MapColor.COLOR_PURPLE))
   );
   public static final DeferredBlock<SaplingBlock> COMET_SAPLING = BLOCKS.register(
      "comet_sapling",
      () -> new CometSaplingBlock(
            ModTreeGrowers.COMET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_SAPLING).mapColor(MapColor.COLOR_PURPLE)
         )
   );
   public static final DeferredBlock<StandingSignBlock> COMET_SIGN = BLOCKS.register(
      "comet_sign", () -> new StandingSignBlock(ModWoodTypes.COMET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_SIGN))
   );
   public static final DeferredBlock<WallSignBlock> COMET_WALL_SIGN = BLOCKS.register(
      "comet_wall_sign",
      () -> new WallSignBlock(ModWoodTypes.COMET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_WALL_SIGN).lootFrom(COMET_SIGN))
   );
   public static final DeferredBlock<CeilingHangingSignBlock> COMET_HANGING_SIGN = BLOCKS.register(
      "comet_hanging_sign",
      () -> new CeilingHangingSignBlock(ModWoodTypes.COMET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_HANGING_SIGN))
   );
   public static final DeferredBlock<WallHangingSignBlock> COMET_WALL_HANGING_SIGN = BLOCKS.register(
      "comet_wall_hanging_sign",
      () -> new WallHangingSignBlock(
            ModWoodTypes.COMET, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WARPED_WALL_HANGING_SIGN).lootFrom(COMET_HANGING_SIGN)
         )
   );
   public static final DeferredBlock<FlowerPotBlock> POTTED_COMET_SAPLING = BLOCKS.register(
      "potted_comet_sapling",
      () -> new FlowerPotBlock(
            () -> (FlowerPotBlock)net.minecraft.world.level.block.Blocks.FLOWER_POT,
            COMET_SAPLING,
            Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.POTTED_OAK_SAPLING)
         )
   );
   public static final DeferredBlock<Block> WIRELESS_ENERGY_TRANSMITTER = BLOCKS.register(
      "wireless_energy_transfer_device", () -> new WirelessEnergyTransmitterBlock(machineProperties().mapColor(MapColor.COLOR_PURPLE).noOcclusion())
   );
   public static final DeferredBlock<Block> SATELLITE_RECEIVER = BLOCKS.register(
      "satellite_receiver", () -> new SatelliteReceiverBlock(machineProperties().mapColor(MapColor.COLOR_PURPLE).noOcclusion())
   );
   public static final DeferredBlock<Block> CRYSTAL_LOGISTICS = BLOCKS.register(
      "wireless_logistics_transfer_device", () -> new CrystalLogisticsBlock(machineProperties().mapColor(MapColor.COLOR_BLUE).noOcclusion())
   );
   public static final DeferredBlock<Block> CRAFTING_PROCESSOR = BLOCKS.register(
      "crafting_processing_device", () -> new CraftingProcessorBlock(machineProperties().mapColor(MapColor.COLOR_BLUE))
   );
   public static final DeferredBlock<Block> TERRAIN_PROCESSOR = BLOCKS.register(
      "terrain_processing_device", () -> new TerrainProcessorBlock(machineProperties().mapColor(MapColor.COLOR_BLUE))
   );
   public static final DeferredBlock<Block> ANIMAL_BREEDER = BLOCKS.register(
      "automatic_animal_breeder", () -> new AnimalBreederBlock(machineProperties().mapColor(MapColor.COLOR_BLUE))
   );
   public static final DeferredBlock<Block> LAUNCH_SITE = BLOCKS.register(
      "rocket_launch_site", () -> new LaunchSiteBlock(machineProperties().mapColor(MapColor.COLOR_BLUE))
   );
   public static final DeferredBlock<Block> LAUNCH_PAD = BLOCKS.register("launch_pad", () -> new Block(machineProperties().mapColor(MapColor.COLOR_BLUE)));
   public static final DeferredBlock<Block> LAUNCH_GANTRY_FRAME = BLOCKS.register(
      "launch_gantry_frame",
      () -> new Block(
            machineProperties()
               .mapColor(MapColor.COLOR_BLUE)
               .noOcclusion()
               .isViewBlocking((var0, var1, var2) -> false)
               .isSuffocating((var0, var1, var2) -> false)
         )
   );
   public static final DeferredBlock<Block> SMALL_SATELLITE_ROCKET = BLOCKS.register(
      "small_satellite_rocket", () -> new SatelliteRocketBlock(1, 2, rocketProperties())
   );
   public static final DeferredBlock<Block> MEDIUM_SATELLITE_ROCKET = BLOCKS.register(
      "medium_satellite_rocket", () -> new SatelliteRocketBlock(2, 3, rocketProperties())
   );
   public static final DeferredBlock<Block> LARGE_SATELLITE_ROCKET = BLOCKS.register(
      "large_satellite_rocket", () -> new SatelliteRocketBlock(3, 4, rocketProperties())
   );
   public static final DeferredBlock<Block> SATELLITE_CONTROL_DEVICE = BLOCKS.register(
      "satellite_control_device",
      () -> new SatelliteControlDeviceBlock(
            machineProperties().mapColor(MapColor.COLOR_BLUE).lightLevel(var0 -> var0.getValue(SatelliteControlDeviceBlock.ACTIVE) ? 7 : 0)
         )
   );
   public static final DeferredBlock<Block> AP_STRUCTURAL_STEEL_BLOCK = BLOCKS.register(
      "ap_structural_steel_block", () -> new Block(metalBlock(MapColor.COLOR_LIGHT_GRAY))
   );
   public static final DeferredBlock<Block> INV_BLOCK = BLOCKS.register("inv_block", () -> new Block(metalBlock(MapColor.COLOR_PINK)));
   public static final DeferredBlock<Block> TYCON_BLOCK = BLOCKS.register("tycon_block", () -> new Block(metalBlock(MapColor.COLOR_ORANGE)));
   public static final DeferredBlock<Block> GREEN_IRON_BLOCK = BLOCKS.register("green_iron_block", () -> new Block(metalBlock(MapColor.COLOR_GREEN)));
   public static final DeferredBlock<Block> AP_STEEL_CASING = BLOCKS.register("ap_steel_casing", () -> new Block(metalBlock(MapColor.COLOR_GRAY)));
   public static final DeferredBlock<Block> AP_STEEL_FRAME = BLOCKS.register(
      "ap_steel_frame",
      () -> new Block(metalBlock(MapColor.COLOR_GRAY).noOcclusion().isViewBlocking((var0, var1, var2) -> false).isSuffocating((var0, var1, var2) -> false))
   );
   public static final DeferredBlock<Block> AP_AGRICULTURE_CONTROLLER = BLOCKS.register(
      "ap_agricultural_complex",
      () -> new ApControllerBlock(
            ApControllerBlock.Kind.AGRICULTURE,
            machineProperties().mapColor(MapColor.COLOR_GRAY).lightLevel(var0 -> var0.getValue(ApControllerBlock.ACTIVE) ? 8 : 0)
         )
   );
   public static final DeferredBlock<Block> AP_LIVESTOCK_CONTROLLER = BLOCKS.register(
      "ap_livestock_ranch",
      () -> new ApControllerBlock(
            ApControllerBlock.Kind.LIVESTOCK,
            machineProperties().mapColor(MapColor.COLOR_GRAY).lightLevel(var0 -> var0.getValue(ApControllerBlock.ACTIVE) ? 8 : 0)
         )
   );

   private static Properties metalBlock(MapColor var0) {
      return Properties.of().mapColor(var0).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL);
   }

   private ModBlocks() {
   }

   private static DeferredBlock<Block> registerCompressBlock(String var0, MapColor var1) {
      return BLOCKS.register(var0, () -> new Block(Properties.of().mapColor(var1).strength(0.8F).sound(SoundType.WART_BLOCK)));
   }

   private static DeferredBlock<Block> registerMachineBlock(String var0) {
      return BLOCKS.register(var0, () -> new Block(machineProperties()));
   }

   private static Properties rocketProperties() {
      return Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.BLOCK);
   }

   private static Properties machineProperties() {
      return Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.METAL);
   }

   private static DeferredBlock<Block> registerPipeBlock(String var0) {
      return BLOCKS.register(var0, () -> new TransportPipeBlock(Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.COPPER)));
   }

   private static DeferredBlock<Block> registerEnergyCable(String var0, MapColor var1) {
      return BLOCKS.register(var0, () -> new EnergyCableBlock(Properties.of().mapColor(var1).strength(1.0F).sound(SoundType.COPPER)));
   }

   private static Properties cropProperties() {
      return Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY);
   }

   static {
      BLOCKS.addAlias(
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "infinite_logistics_transfer_device"),
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "wireless_logistics_transfer_device")
      );
   }
}
