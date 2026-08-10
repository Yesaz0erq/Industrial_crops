package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.BasicControlDeviceMenu;
import com.industrialcrops.screen.BasicManipulatorMenu;
import com.industrialcrops.screen.CropCompressorMenu;
import com.industrialcrops.screen.CropAnalysisDeviceMenu;
import com.industrialcrops.screen.GourdModificationDeviceMenu;
import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import com.industrialcrops.screen.RootOreExtractorMenu;
import com.industrialcrops.screen.ProcessorProgrammerMenu;
import com.industrialcrops.screen.IncubatorMenu;
import com.industrialcrops.screen.SlimeIncubatorMenu;
import com.industrialcrops.screen.GoldenLaunchSiloMenu;
import com.industrialcrops.screen.MatterMachineMenu;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import com.industrialcrops.screen.BioEnergyMenu;
import com.industrialcrops.screen.ElectricFurnaceMenu;
import com.industrialcrops.screen.DigitalMiniatureForestMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IndustrialCrops.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<RootOreExtractorMenu>> ROOT_ORE_EXTRACTOR =
            MENUS.register("basic_crop_conversion_device", () -> new MenuType<>(
                    (IContainerFactory<RootOreExtractorMenu>) RootOreExtractorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<CropCompressorMenu>> CROP_COMPRESSOR =
            MENUS.register("crop_compressor", () -> new MenuType<>(
                    (IContainerFactory<CropCompressorMenu>) CropCompressorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<CropAnalysisDeviceMenu>> CROP_ANALYSIS_DEVICE =
            MENUS.register("crop_analysis_device", () -> new MenuType<>(
                    (IContainerFactory<CropAnalysisDeviceMenu>) CropAnalysisDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<GourdModificationDeviceMenu>> GOURD_MODIFICATION_DEVICE =
            MENUS.register("gourd_modification_device", () -> new MenuType<>(
                    (IContainerFactory<GourdModificationDeviceMenu>) GourdModificationDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<BasicControlDeviceMenu>> BASIC_CONTROL_DEVICE =
            MENUS.register("basic_control_device", () -> new MenuType<>(
                    (IContainerFactory<BasicControlDeviceMenu>) BasicControlDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<ReinforcedControlDeviceMenu>> REINFORCED_CONTROL_DEVICE =
            MENUS.register("reinforced_control_device", () -> new MenuType<>(
                    (IContainerFactory<ReinforcedControlDeviceMenu>) ReinforcedControlDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<BasicManipulatorMenu>> BASIC_MANIPULATOR =
            MENUS.register("basic_manipulation_device", () -> new MenuType<>(
                    (IContainerFactory<BasicManipulatorMenu>) BasicManipulatorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<IncubatorMenu>> INCUBATOR =
            MENUS.register("slime_converter", () -> new MenuType<>(
                    (IContainerFactory<IncubatorMenu>) IncubatorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<SlimeIncubatorMenu>> SLIME_INCUBATOR =
            MENUS.register("slime_incubator", () -> new MenuType<>(
                    (IContainerFactory<SlimeIncubatorMenu>) SlimeIncubatorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedIndustrialStorageMenu>> ADVANCED_INDUSTRIAL_STORAGE_DEVICE =
            MENUS.register("advanced_industrial_storage_device", () -> new MenuType<>(
                    (IContainerFactory<AdvancedIndustrialStorageMenu>) AdvancedIndustrialStorageMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<GoldenLaunchSiloMenu>> GOLDEN_LAUNCH_SILO =
            MENUS.register("explosive_potato_launcher", () -> new MenuType<>(
                    (IContainerFactory<GoldenLaunchSiloMenu>) GoldenLaunchSiloMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<ProcessorProgrammerMenu>> PROCESSOR_PROGRAMMER =
            MENUS.register("processor_programming_device", () -> new MenuType<>(
                    (IContainerFactory<ProcessorProgrammerMenu>) ProcessorProgrammerMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));
    public static final DeferredHolder<MenuType<?>, MenuType<MatterMachineMenu>> MATTER_MACHINE =
            MENUS.register("matter_machine", () -> new MenuType<>(
                    (IContainerFactory<MatterMachineMenu>) MatterMachineMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<ItemNetworkTerminalMenu>> ITEM_NETWORK_TERMINAL =
            MENUS.register("item_network_management_terminal", () -> new MenuType<>(
                    (IContainerFactory<ItemNetworkTerminalMenu>) ItemNetworkTerminalMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<BioEnergyMenu>> BIO_ENERGY_MACHINE =
            MENUS.register("bio_energy_machine", () -> new MenuType<>(
                    (IContainerFactory<BioEnergyMenu>) BioEnergyMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace", () -> new MenuType<>(
                    (IContainerFactory<ElectricFurnaceMenu>) ElectricFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<DigitalMiniatureForestMenu>> DIGITAL_MINIATURE_FOREST =
            MENUS.register("digital_miniature_forest", () -> new MenuType<>(
                    (IContainerFactory<DigitalMiniatureForestMenu>) DigitalMiniatureForestMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {
    }
}
