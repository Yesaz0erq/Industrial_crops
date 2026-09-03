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
import com.industrialcrops.screen.AutomaticPlanterMenu;
import com.industrialcrops.screen.PipeSorterMenu;
import com.industrialcrops.screen.GoldPlasmaExtractorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IndustrialCrops.MOD_ID);

    public static final RegistryObject<MenuType<RootOreExtractorMenu>> ROOT_ORE_EXTRACTOR =
            MENUS.register("basic_crop_conversion_device", () -> new MenuType<>(
                    (IContainerFactory<RootOreExtractorMenu>) RootOreExtractorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<CropCompressorMenu>> CROP_COMPRESSOR =
            MENUS.register("crop_compressor", () -> new MenuType<>(
                    (IContainerFactory<CropCompressorMenu>) CropCompressorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<CropAnalysisDeviceMenu>> CROP_ANALYSIS_DEVICE =
            MENUS.register("crop_analysis_device", () -> new MenuType<>(
                    (IContainerFactory<CropAnalysisDeviceMenu>) CropAnalysisDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<GourdModificationDeviceMenu>> GOURD_MODIFICATION_DEVICE =
            MENUS.register("gourd_modification_device", () -> new MenuType<>(
                    (IContainerFactory<GourdModificationDeviceMenu>) GourdModificationDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<BasicControlDeviceMenu>> BASIC_CONTROL_DEVICE =
            MENUS.register("basic_control_device", () -> new MenuType<>(
                    (IContainerFactory<BasicControlDeviceMenu>) BasicControlDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<ReinforcedControlDeviceMenu>> REINFORCED_CONTROL_DEVICE =
            MENUS.register("reinforced_control_device", () -> new MenuType<>(
                    (IContainerFactory<ReinforcedControlDeviceMenu>) ReinforcedControlDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<BasicManipulatorMenu>> BASIC_MANIPULATOR =
            MENUS.register("basic_manipulation_device", () -> new MenuType<>(
                    (IContainerFactory<BasicManipulatorMenu>) BasicManipulatorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<IncubatorMenu>> INCUBATOR =
            MENUS.register("slime_converter", () -> new MenuType<>(
                    (IContainerFactory<IncubatorMenu>) IncubatorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<SlimeIncubatorMenu>> SLIME_INCUBATOR =
            MENUS.register("slime_incubator", () -> new MenuType<>(
                    (IContainerFactory<SlimeIncubatorMenu>) SlimeIncubatorMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<AdvancedIndustrialStorageMenu>> ADVANCED_INDUSTRIAL_STORAGE_DEVICE =
            MENUS.register("advanced_industrial_storage_device", () -> new MenuType<>(
                    (IContainerFactory<AdvancedIndustrialStorageMenu>) AdvancedIndustrialStorageMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<GoldenLaunchSiloMenu>> GOLDEN_LAUNCH_SILO =
            MENUS.register("explosive_potato_launcher", () -> new MenuType<>(
                    (IContainerFactory<GoldenLaunchSiloMenu>) GoldenLaunchSiloMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static final RegistryObject<MenuType<ProcessorProgrammerMenu>> PROCESSOR_PROGRAMMER =
            MENUS.register("processor_programming_device", () -> new MenuType<>(
                    (IContainerFactory<ProcessorProgrammerMenu>) ProcessorProgrammerMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));
    public static final RegistryObject<MenuType<AutomaticPlanterMenu>> AUTOMATIC_PLANTER =
            MENUS.register("automatic_planter", () -> new MenuType<>(
                    (IContainerFactory<AutomaticPlanterMenu>) AutomaticPlanterMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));
    public static final RegistryObject<MenuType<MatterMachineMenu>> MATTER_MACHINE =
            MENUS.register("matter_machine", () -> new MenuType<>(
                    (IContainerFactory<MatterMachineMenu>) MatterMachineMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<ItemNetworkTerminalMenu>> ITEM_NETWORK_TERMINAL =
            MENUS.register("item_network_management_terminal", () -> new MenuType<>(
                    (IContainerFactory<ItemNetworkTerminalMenu>) ItemNetworkTerminalMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<BioEnergyMenu>> BIO_ENERGY_MACHINE =
            MENUS.register("bio_energy_machine", () -> new MenuType<>(
                    (IContainerFactory<BioEnergyMenu>) BioEnergyMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace", () -> new MenuType<>(
                    (IContainerFactory<ElectricFurnaceMenu>) ElectricFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<DigitalMiniatureForestMenu>> DIGITAL_MINIATURE_FOREST =
            MENUS.register("digital_miniature_forest", () -> new MenuType<>(
                    (IContainerFactory<DigitalMiniatureForestMenu>) DigitalMiniatureForestMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<PipeSorterMenu>> PIPE_SORTER =
            MENUS.register("pipe_sorter", () -> new MenuType<>(
                    (IContainerFactory<PipeSorterMenu>) PipeSorterMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<GoldPlasmaExtractorMenu>> GOLD_PLASMA_EXTRACTOR =
            MENUS.register("gold_plasma_extractor", () -> new MenuType<>(
                    (IContainerFactory<GoldPlasmaExtractorMenu>) GoldPlasmaExtractorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {
    }
}
