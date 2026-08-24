package com.industrialcrops.client;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.client.gui.AdvancedIndustrialStorageScreen;
import com.industrialcrops.client.gui.BasicControlDeviceScreen;
import com.industrialcrops.client.gui.BasicManipulatorScreen;
import com.industrialcrops.client.gui.CropCompressorScreen;
import com.industrialcrops.client.gui.CropAnalysisDeviceScreen;
import com.industrialcrops.client.gui.GourdModificationDeviceScreen;
import com.industrialcrops.client.gui.ReinforcedControlDeviceScreen;
import com.industrialcrops.client.gui.RootOreExtractorScreen;
import com.industrialcrops.client.gui.ProcessorProgrammerScreen;
import com.industrialcrops.client.gui.IncubatorScreen;
import com.industrialcrops.client.gui.SlimeIncubatorScreen;
import com.industrialcrops.client.gui.GoldenLaunchSiloScreen;
import com.industrialcrops.client.gui.MatterMachineScreen;
import com.industrialcrops.client.gui.ItemNetworkTerminalScreen;
import com.industrialcrops.client.gui.BioEnergyScreen;
import com.industrialcrops.client.gui.ElectricFurnaceScreen;
import com.industrialcrops.client.gui.DigitalMiniatureForestScreen;
import com.industrialcrops.client.gui.AutomaticPlanterScreen;
import com.industrialcrops.client.renderer.BrownCreateSlimeRenderer;
import com.industrialcrops.client.renderer.DiamondProcessorSlimeRenderer;
import com.industrialcrops.client.renderer.DigitalMiniatureForestRenderer;
import com.industrialcrops.client.renderer.GrayGearSlimeRenderer;
import com.industrialcrops.client.renderer.GoldenRedstoneLampSlimeRenderer;
import com.industrialcrops.client.renderer.IncubatorBlockEntityRenderer;
import com.industrialcrops.client.renderer.GoldenRocketRenderer;
import com.industrialcrops.client.renderer.TargetMarkerRenderer;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModEntities;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.minecraft.resources.ResourceLocation;

@EventBusSubscriber(modid = IndustrialCrops.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class IndustrialCropsClient {
    private IndustrialCropsClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ROOT_ORE_EXTRACTOR.get(), RootOreExtractorScreen::new);
        event.register(ModMenus.CROP_COMPRESSOR.get(), CropCompressorScreen::new);
        event.register(ModMenus.CROP_ANALYSIS_DEVICE.get(), CropAnalysisDeviceScreen::new);
        event.register(ModMenus.GOURD_MODIFICATION_DEVICE.get(), GourdModificationDeviceScreen::new);
        event.register(ModMenus.BASIC_CONTROL_DEVICE.get(), BasicControlDeviceScreen::new);
        event.register(ModMenus.REINFORCED_CONTROL_DEVICE.get(), ReinforcedControlDeviceScreen::new);
        event.register(ModMenus.BASIC_MANIPULATOR.get(), BasicManipulatorScreen::new);
        event.register(ModMenus.INCUBATOR.get(), IncubatorScreen::new);
        event.register(ModMenus.SLIME_INCUBATOR.get(), SlimeIncubatorScreen::new);
        event.register(ModMenus.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), AdvancedIndustrialStorageScreen::new);
        event.register(ModMenus.GOLDEN_LAUNCH_SILO.get(), GoldenLaunchSiloScreen::new);
        event.register(ModMenus.PROCESSOR_PROGRAMMER.get(), ProcessorProgrammerScreen::new);
        event.register(ModMenus.AUTOMATIC_PLANTER.get(), AutomaticPlanterScreen::new);
        event.register(ModMenus.MATTER_MACHINE.get(), MatterMachineScreen::new);
        event.register(ModMenus.ITEM_NETWORK_TERMINAL.get(), ItemNetworkTerminalScreen::new);
        event.register(ModMenus.BIO_ENERGY_MACHINE.get(), BioEnergyScreen::new);
        event.register(ModMenus.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new);
        event.register(ModMenus.DIGITAL_MINIATURE_FOREST.get(), DigitalMiniatureForestScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderTypes(net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_CARROT_CROP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_POTATO_CROP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_WHEAT_CROP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_MELON_CROP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIGITAL_MINIATURE_FOREST.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            int age = state.getValue(CropBlock.AGE);
            int red = age * 32;
            int green = 255 - age * 8;
            int blue = age * 4;
            return red << 16 | green << 8 | blue;
        }, ModBlocks.INDUSTRIAL_MELON_CROP.get(), ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get());

        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 0) {
                return level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos)
                        : 0x91BD59;
            }
            if (tintIndex == 1) {
                return level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : 0x48B518;
            }
            return 0xFFFFFF;
        }, ModBlocks.DIGITAL_MINIATURE_FOREST.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> 0x91BD59;
            case 1 -> 0x48B518;
            default -> 0xFFFFFF;
        }, ModBlocks.DIGITAL_MINIATURE_FOREST.get());
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BROWN_CREATE_SLIME.get(), BrownCreateSlimeRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAY_GEAR_SLIME.get(), GrayGearSlimeRenderer::new);
        event.registerEntityRenderer(
                ModEntities.GOLDEN_REDSTONE_LAMP_SLIME.get(),
                GoldenRedstoneLampSlimeRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.DIAMOND_PROCESSOR_SLIME.get(),
                DiamondProcessorSlimeRenderer::new
        );
        event.registerBlockEntityRenderer(ModBlockEntities.INCUBATOR.get(), IncubatorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(), DigitalMiniatureForestRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLDEN_ROCKET.get(), GoldenRocketRenderer::new);
        event.registerEntityRenderer(ModEntities.TARGET_MARKER.get(), TargetMarkerRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "glitch_border"),
                GlitchGuiOverlay::render);
    }

}
