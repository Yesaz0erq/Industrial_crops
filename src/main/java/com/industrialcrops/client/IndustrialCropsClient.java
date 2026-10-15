package com.industrialcrops.client;

import com.industrialcrops.client.gui.AdvancedIndustrialStorageScreen;
import com.industrialcrops.client.gui.AnimalBreederScreen;
import com.industrialcrops.client.gui.ApAgricultureScreen;
import com.industrialcrops.client.gui.ApLivestockScreen;
import com.industrialcrops.client.gui.AutomaticPlanterScreen;
import com.industrialcrops.client.gui.BasicControlDeviceScreen;
import com.industrialcrops.client.gui.BasicManipulatorScreen;
import com.industrialcrops.client.gui.BioEnergyScreen;
import com.industrialcrops.client.gui.CraftingProcessorScreen;
import com.industrialcrops.client.gui.CropAnalysisDeviceScreen;
import com.industrialcrops.client.gui.CropCompressorScreen;
import com.industrialcrops.client.gui.CrystalLogisticsScreen;
import com.industrialcrops.client.gui.CrystalSteelWorkbenchScreen;
import com.industrialcrops.client.gui.DigitalMiniatureForestScreen;
import com.industrialcrops.client.gui.ElectricFurnaceScreen;
import com.industrialcrops.client.gui.GoldPlasmaExtractorScreen;
import com.industrialcrops.client.gui.GoldenLaunchSiloScreen;
import com.industrialcrops.client.gui.GourdModificationDeviceScreen;
import com.industrialcrops.client.gui.IncubatorScreen;
import com.industrialcrops.client.gui.ItemNetworkTerminalScreen;
import com.industrialcrops.client.gui.LaunchSiteScreen;
import com.industrialcrops.client.gui.MatterMachineScreen;
import com.industrialcrops.client.gui.PipeSorterScreen;
import com.industrialcrops.client.gui.ProcessorProgrammerScreen;
import com.industrialcrops.client.gui.ReinforcedControlDeviceScreen;
import com.industrialcrops.client.gui.RootOreExtractorScreen;
import com.industrialcrops.client.gui.SatelliteControlScreen;
import com.industrialcrops.client.gui.SatelliteReceiverScreen;
import com.industrialcrops.client.gui.SlimeIncubatorScreen;
import com.industrialcrops.client.gui.TerrainProcessorScreen;
import com.industrialcrops.client.gui.UltraHighPressureInvForgeScreen;
import com.industrialcrops.client.gui.WirelessEnergyTransmitterScreen;
import com.industrialcrops.client.renderer.ApHologramRenderer;
import com.industrialcrops.client.renderer.BrownCreateSlimeRenderer;
import com.industrialcrops.client.renderer.CopperFluidStorageCabinetRenderer;
import com.industrialcrops.client.renderer.CrystalLogisticsItemRenderer;
import com.industrialcrops.client.renderer.CrystalLogisticsRenderer;
import com.industrialcrops.client.renderer.CrystalSteelWorkbenchRenderer;
import com.industrialcrops.client.renderer.DiamondProcessorSlimeRenderer;
import com.industrialcrops.client.renderer.DigitalMiniatureForestRenderer;
import com.industrialcrops.client.renderer.GoldenRedstoneLampSlimeRenderer;
import com.industrialcrops.client.renderer.GoldenRocketRenderer;
import com.industrialcrops.client.renderer.GrayGearSlimeRenderer;
import com.industrialcrops.client.renderer.IncubatorBlockEntityRenderer;
import com.industrialcrops.client.renderer.SatelliteReceiverRenderer;
import com.industrialcrops.client.renderer.SatelliteRocketRenderer;
import com.industrialcrops.client.renderer.TargetMarkerRenderer;
import com.industrialcrops.client.renderer.WirelessEnergyTransmitterRenderer;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModEntities;
import com.industrialcrops.registry.ModFluids;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.registry.ModWoodTypes;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;

@EventBusSubscriber(
   modid = "industrialcrops",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public final class IndustrialCropsClient {
   private IndustrialCropsClient() {
   }

   @SubscribeEvent
   public static void clientSetup(FMLClientSetupEvent var0) {
      var0.enqueueWork(() -> Sheets.addWoodType(ModWoodTypes.COMET));
   }

   @SubscribeEvent
   public static void registerScreens(RegisterMenuScreensEvent var0) {
      var0.register((MenuType)ModMenus.WIRELESS_ENERGY_TRANSMITTER.get(), WirelessEnergyTransmitterScreen::new);
      var0.register((MenuType)ModMenus.SATELLITE_RECEIVER.get(), SatelliteReceiverScreen::new);
      var0.register((MenuType)ModMenus.LAUNCH_SITE.get(), LaunchSiteScreen::new);
      var0.register((MenuType)ModMenus.SATELLITE_CONTROL_DEVICE.get(), SatelliteControlScreen::new);
      var0.register((MenuType)ModMenus.ROOT_ORE_EXTRACTOR.get(), RootOreExtractorScreen::new);
      var0.register((MenuType)ModMenus.CROP_COMPRESSOR.get(), CropCompressorScreen::new);
      var0.register((MenuType)ModMenus.CROP_ANALYSIS_DEVICE.get(), CropAnalysisDeviceScreen::new);
      var0.register((MenuType)ModMenus.GOURD_MODIFICATION_DEVICE.get(), GourdModificationDeviceScreen::new);
      var0.register((MenuType)ModMenus.BASIC_CONTROL_DEVICE.get(), BasicControlDeviceScreen::new);
      var0.register((MenuType)ModMenus.REINFORCED_CONTROL_DEVICE.get(), ReinforcedControlDeviceScreen::new);
      var0.register((MenuType)ModMenus.BASIC_MANIPULATOR.get(), BasicManipulatorScreen::new);
      var0.register((MenuType)ModMenus.INCUBATOR.get(), IncubatorScreen::new);
      var0.register((MenuType)ModMenus.SLIME_INCUBATOR.get(), SlimeIncubatorScreen::new);
      var0.register((MenuType)ModMenus.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), AdvancedIndustrialStorageScreen::new);
      var0.register((MenuType)ModMenus.GOLDEN_LAUNCH_SILO.get(), GoldenLaunchSiloScreen::new);
      var0.register((MenuType)ModMenus.PROCESSOR_PROGRAMMER.get(), ProcessorProgrammerScreen::new);
      var0.register((MenuType)ModMenus.AUTOMATIC_PLANTER.get(), AutomaticPlanterScreen::new);
      var0.register((MenuType)ModMenus.MATTER_MACHINE.get(), MatterMachineScreen::new);
      var0.register((MenuType)ModMenus.ITEM_NETWORK_TERMINAL.get(), ItemNetworkTerminalScreen::new);
      var0.register((MenuType)ModMenus.BIO_ENERGY_MACHINE.get(), BioEnergyScreen::new);
      var0.register((MenuType)ModMenus.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new);
      var0.register((MenuType)ModMenus.ULTRA_HIGH_PRESSURE_INV_FORGE.get(), UltraHighPressureInvForgeScreen::new);
      var0.register((MenuType)ModMenus.AP_AGRICULTURE.get(), ApAgricultureScreen::new);
      var0.register((MenuType)ModMenus.AP_LIVESTOCK.get(), ApLivestockScreen::new);
      var0.register((MenuType)ModMenus.DIGITAL_MINIATURE_FOREST.get(), DigitalMiniatureForestScreen::new);
      var0.register((MenuType)ModMenus.PIPE_SORTER.get(), PipeSorterScreen::new);
      var0.register((MenuType)ModMenus.ANIMAL_BREEDER.get(), AnimalBreederScreen::new);
      var0.register((MenuType)ModMenus.TERRAIN_PROCESSOR.get(), TerrainProcessorScreen::new);
      var0.register((MenuType)ModMenus.CRAFTING_PROCESSOR.get(), CraftingProcessorScreen::new);
      var0.register((MenuType)ModMenus.CRYSTAL_LOGISTICS.get(), CrystalLogisticsScreen::new);
      var0.register((MenuType)ModMenus.CRYSTAL_STEEL_WORKBENCH.get(), CrystalSteelWorkbenchScreen::new);
      var0.register((MenuType)ModMenus.GOLD_PLASMA_EXTRACTOR.get(), GoldPlasmaExtractorScreen::new);
   }

   @SubscribeEvent
   public static void registerRenderTypes(RegisterNamedRenderTypesEvent var0) {
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.INDUSTRIAL_CARROT_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.INDUSTRIAL_POTATO_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.INDUSTRIAL_WHEAT_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.INDUSTRIAL_MELON_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.PRISM_POD_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.EMBERCOIL_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.STARBLOOM_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.NEONBULB_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.FLUXSTALK_CROP.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.ENERGY_BUSH.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.DIGITAL_MINIATURE_FOREST.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.COMET_SAPLING.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.POTTED_COMET_SAPLING.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.COMET_LEAVES.get(), RenderType.translucent());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.COMET_DOOR.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.COMET_TRAPDOOR.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.COPPER_FLUID_STORAGE_CABINET.get(), RenderType.cutoutMipped());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.LAUNCH_GANTRY_FRAME.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.SMALL_SATELLITE_ROCKET.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.MEDIUM_SATELLITE_ROCKET.get(), RenderType.cutout());
      ItemBlockRenderTypes.setRenderLayer((Block)ModBlocks.LARGE_SATELLITE_ROCKET.get(), RenderType.cutout());
   }

   @SubscribeEvent
   public static void registerFluidTypeExtensions(RegisterClientExtensionsEvent var0) {
      var0.registerItem(new IClientItemExtensions() {
         private CrystalLogisticsItemRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = new CrystalLogisticsItemRenderer();
            }

            return this.renderer;
         }
      }, new Item[]{((Block)ModBlocks.CRYSTAL_LOGISTICS.get()).asItem()});
      var0.registerFluidType(new IClientFluidTypeExtensions() {
         private final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath("industrialcrops", "block/concentrated_plasma_juice_still");
         private final ResourceLocation FLOWING = ResourceLocation.fromNamespaceAndPath("industrialcrops", "block/concentrated_plasma_juice_flowing");

         public ResourceLocation getStillTexture() {
            return this.STILL;
         }

         public ResourceLocation getFlowingTexture() {
            return this.FLOWING;
         }

         public int getTintColor() {
            return -11550977;
         }
      }, new FluidType[]{(FluidType)ModFluids.CONCENTRATED_PLASMA_JUICE_TYPE.get()});
   }

   @SubscribeEvent
   public static void registerBlockColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block var0) {
      var0.register((var0x, var1, var2, var3) -> {
         int var4 = (Integer)var0x.getValue(CropBlock.AGE);
         int var5 = var4 * 32;
         int var6 = 255 - var4 * 8;
         int var7 = var4 * 4;
         return var5 << 16 | var6 << 8 | var7;
      }, new Block[]{(Block)ModBlocks.INDUSTRIAL_MELON_CROP.get(), (Block)ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get()});
      var0.register((var0x, var1, var2, var3) -> {
         if (var3 == 0) {
            return var1 != null && var2 != null ? BiomeColors.getAverageGrassColor(var1, var2) : 9551193;
         } else if (var3 != 1) {
            return 16777215;
         } else {
            return var1 != null && var2 != null ? BiomeColors.getAverageFoliageColor(var1, var2) : 4764952;
         }
      }, new Block[]{(Block)ModBlocks.DIGITAL_MINIATURE_FOREST.get()});
   }

   @SubscribeEvent
   public static void registerItemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item var0) {
      var0.register((var0x, var1) -> {
         return switch (var1) {
            case 0 -> 9551193;
            case 1 -> 4764952;
            default -> 16777215;
         };
      }, new ItemLike[]{(ItemLike)ModBlocks.DIGITAL_MINIATURE_FOREST.get()});
   }

   @SubscribeEvent
   public static void registerEntityRenderers(RegisterRenderers var0) {
      var0.registerEntityRenderer((EntityType)ModEntities.BROWN_CREATE_SLIME.get(), BrownCreateSlimeRenderer::new);
      var0.registerEntityRenderer((EntityType)ModEntities.GRAY_GEAR_SLIME.get(), GrayGearSlimeRenderer::new);
      var0.registerEntityRenderer((EntityType)ModEntities.GOLDEN_REDSTONE_LAMP_SLIME.get(), GoldenRedstoneLampSlimeRenderer::new);
      var0.registerEntityRenderer((EntityType)ModEntities.DIAMOND_PROCESSOR_SLIME.get(), DiamondProcessorSlimeRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.WIRELESS_ENERGY_TRANSMITTER.get(), WirelessEnergyTransmitterRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.SATELLITE_RECEIVER.get(), SatelliteReceiverRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.AP_AGRICULTURE.get(), ApHologramRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.AP_LIVESTOCK.get(), ApHologramRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.INCUBATOR.get(), IncubatorBlockEntityRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(), DigitalMiniatureForestRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.CRYSTAL_STEEL_WORKBENCH.get(), CrystalSteelWorkbenchRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.CRYSTAL_LOGISTICS.get(), CrystalLogisticsRenderer::new);
      var0.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.COPPER_FLUID_STORAGE_CABINET.get(), CopperFluidStorageCabinetRenderer::new);
      var0.registerEntityRenderer((EntityType)ModEntities.GOLDEN_ROCKET.get(), GoldenRocketRenderer::new);
      var0.registerEntityRenderer((EntityType)ModEntities.SATELLITE_ROCKET.get(), SatelliteRocketRenderer::new);
      var0.registerEntityRenderer((EntityType)ModEntities.TARGET_MARKER.get(), TargetMarkerRenderer::new);
   }

   @SubscribeEvent
   public static void registerGuiLayers(RegisterGuiLayersEvent var0) {
      var0.registerAboveAll(ResourceLocation.fromNamespaceAndPath("industrialcrops", "glitch_border"), GlitchGuiOverlay::render);
   }
}
