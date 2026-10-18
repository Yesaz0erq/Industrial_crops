package com.industrialcrops.registry;

import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.SuperCompressorMenu;
import com.industrialcrops.screen.AnimalBreederMenu;
import com.industrialcrops.screen.ApAgricultureMenu;
import com.industrialcrops.screen.ApLivestockMenu;
import com.industrialcrops.screen.AutomaticPlanterMenu;
import com.industrialcrops.screen.BasicControlDeviceMenu;
import com.industrialcrops.screen.BasicManipulatorMenu;
import com.industrialcrops.screen.BioEnergyMenu;
import com.industrialcrops.screen.CraftingProcessorMenu;
import com.industrialcrops.screen.CropAnalysisDeviceMenu;
import com.industrialcrops.screen.CropCompressorMenu;
import com.industrialcrops.screen.CrystalLogisticsMenu;
import com.industrialcrops.screen.CrystalSteelWorkbenchMenu;
import com.industrialcrops.screen.DigitalMiniatureForestMenu;
import com.industrialcrops.screen.ElectricFurnaceMenu;
import com.industrialcrops.screen.GoldPlasmaExtractorMenu;
import com.industrialcrops.screen.GoldenLaunchSiloMenu;
import com.industrialcrops.screen.GourdModificationDeviceMenu;
import com.industrialcrops.screen.IncubatorMenu;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import com.industrialcrops.screen.LaunchSiteMenu;
import com.industrialcrops.screen.MatterMachineMenu;
import com.industrialcrops.screen.PipeSorterMenu;
import com.industrialcrops.screen.ProcessorProgrammerMenu;
import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import com.industrialcrops.screen.RootOreExtractorMenu;
import com.industrialcrops.screen.SatelliteControlMenu;
import com.industrialcrops.screen.SatelliteReceiverMenu;
import com.industrialcrops.screen.SlimeIncubatorMenu;
import com.industrialcrops.screen.TerrainProcessorMenu;
import com.industrialcrops.screen.UltraHighPressureInvForgeMenu;
import com.industrialcrops.screen.WirelessEnergyTransmitterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "industrialcrops");
   public static final DeferredHolder<MenuType<?>, MenuType<CrystalSteelWorkbenchMenu>> CRYSTAL_STEEL_WORKBENCH = MENUS.register(
      "crystal_steel_workbench", () -> IMenuTypeExtension.create(CrystalSteelWorkbenchMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<RootOreExtractorMenu>> ROOT_ORE_EXTRACTOR = MENUS.register(
      "basic_crop_conversion_device", () -> IMenuTypeExtension.create(RootOreExtractorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<CropCompressorMenu>> CROP_COMPRESSOR = MENUS.register(
      "crop_compressor", () -> IMenuTypeExtension.create(CropCompressorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<CropAnalysisDeviceMenu>> CROP_ANALYSIS_DEVICE = MENUS.register(
      "crop_analysis_device", () -> IMenuTypeExtension.create(CropAnalysisDeviceMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<GourdModificationDeviceMenu>> MELON_MODIFICATION_DEVICE = MENUS.register(
      "melon_modification_device", () -> IMenuTypeExtension.create(GourdModificationDeviceMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<BasicControlDeviceMenu>> BASIC_CONTROL_DEVICE = MENUS.register(
      "basic_control_device", () -> IMenuTypeExtension.create(BasicControlDeviceMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ReinforcedControlDeviceMenu>> REINFORCED_CONTROL_DEVICE = MENUS.register(
      "reinforced_control_device", () -> IMenuTypeExtension.create(ReinforcedControlDeviceMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<BasicManipulatorMenu>> BASIC_MANIPULATOR = MENUS.register(
      "basic_manipulation_device", () -> IMenuTypeExtension.create(BasicManipulatorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<IncubatorMenu>> INCUBATOR = MENUS.register(
      "slime_conversion_device", () -> IMenuTypeExtension.create(IncubatorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<SlimeIncubatorMenu>> SLIME_INCUBATOR = MENUS.register(
      "slime_cultivation_device", () -> IMenuTypeExtension.create(SlimeIncubatorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<AdvancedIndustrialStorageMenu>> ADVANCED_INDUSTRIAL_STORAGE_DEVICE = MENUS.register(
      "advanced_industrial_storage_device", () -> IMenuTypeExtension.create(AdvancedIndustrialStorageMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<GoldenLaunchSiloMenu>> GOLDEN_LAUNCH_SILO = MENUS.register(
      "explosive_potato_launcher", () -> IMenuTypeExtension.create(GoldenLaunchSiloMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ProcessorProgrammerMenu>> PROCESSOR_PROGRAMMER = MENUS.register(
      "component_programming_device", () -> IMenuTypeExtension.create(ProcessorProgrammerMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<AutomaticPlanterMenu>> AUTOMATIC_PLANTER = MENUS.register(
      "automatic_sowing_device", () -> IMenuTypeExtension.create(AutomaticPlanterMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<MatterMachineMenu>> MATTER_MACHINE = MENUS.register(
      "matter_machine", () -> IMenuTypeExtension.create(MatterMachineMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ItemNetworkTerminalMenu>> ITEM_NETWORK_TERMINAL = MENUS.register(
      "item_network_management_terminal", () -> IMenuTypeExtension.create(ItemNetworkTerminalMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<BioEnergyMenu>> BIO_ENERGY_MACHINE = MENUS.register(
      "bio_energy_machine", () -> IMenuTypeExtension.create(BioEnergyMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE = MENUS.register(
      "electric_furnace", () -> IMenuTypeExtension.create(ElectricFurnaceMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<com.industrialcrops.screen.IntegratedSmeltingFurnaceMenu>> INTEGRATED_SMELTING_FURNACE = MENUS.register(
      "integrated_smelting_furnace", () -> IMenuTypeExtension.create(com.industrialcrops.screen.IntegratedSmeltingFurnaceMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<SuperCompressorMenu>> SUPER_COMPRESSOR = MENUS.register(
      "super_compressor", () -> IMenuTypeExtension.create(SuperCompressorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<com.industrialcrops.screen.SuperCropCultivatorMenu>> SUPER_CROP_CULTIVATOR = MENUS.register(
      "super_crop_cultivator", () -> IMenuTypeExtension.create(com.industrialcrops.screen.SuperCropCultivatorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<UltraHighPressureInvForgeMenu>> ULTRA_HIGH_PRESSURE_INV_FORGE = MENUS.register(
      "ultra_high_pressure_inv_forge", () -> IMenuTypeExtension.create(UltraHighPressureInvForgeMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<DigitalMiniatureForestMenu>> DIGITAL_MINIATURE_FOREST = MENUS.register(
      "digital_miniature_forest", () -> IMenuTypeExtension.create(DigitalMiniatureForestMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<PipeSorterMenu>> PIPE_SORTER = MENUS.register(
      "pipe_sorter", () -> IMenuTypeExtension.create(PipeSorterMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<GoldPlasmaExtractorMenu>> GOLD_PLASMA_EXTRACTOR = MENUS.register(
      "extraction_device", () -> IMenuTypeExtension.create(GoldPlasmaExtractorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<WirelessEnergyTransmitterMenu>> WIRELESS_ENERGY_TRANSMITTER = MENUS.register(
      "wireless_energy_transfer_device", () -> IMenuTypeExtension.create(WirelessEnergyTransmitterMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<SatelliteReceiverMenu>> SATELLITE_RECEIVER = MENUS.register(
      "satellite_receiver", () -> IMenuTypeExtension.create(SatelliteReceiverMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<CrystalLogisticsMenu>> CRYSTAL_LOGISTICS = MENUS.register(
      "wireless_logistics_transfer_device", () -> IMenuTypeExtension.create(CrystalLogisticsMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<CraftingProcessorMenu>> CRAFTING_PROCESSOR = MENUS.register(
      "crafting_processing_device", () -> IMenuTypeExtension.create(CraftingProcessorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<TerrainProcessorMenu>> TERRAIN_PROCESSOR = MENUS.register(
      "terrain_processing_device", () -> IMenuTypeExtension.create(TerrainProcessorMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<AnimalBreederMenu>> ANIMAL_BREEDER = MENUS.register(
      "automatic_animal_breeder", () -> IMenuTypeExtension.create(AnimalBreederMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<SatelliteControlMenu>> SATELLITE_CONTROL_DEVICE = MENUS.register(
      "satellite_control_device", () -> IMenuTypeExtension.create(SatelliteControlMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<LaunchSiteMenu>> LAUNCH_SITE = MENUS.register(
      "rocket_launch_site", () -> IMenuTypeExtension.create(LaunchSiteMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ApAgricultureMenu>> AP_AGRICULTURE = MENUS.register(
      "ap_agricultural_complex", () -> IMenuTypeExtension.create(ApAgricultureMenu::new)
   );
   public static final DeferredHolder<MenuType<?>, MenuType<ApLivestockMenu>> AP_LIVESTOCK = MENUS.register(
      "ap_livestock_ranch", () -> IMenuTypeExtension.create(ApLivestockMenu::new)
   );

   private ModMenus() {
   }

   static {
      MENUS.addAlias(
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "infinite_logistics_transfer_device"),
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "wireless_logistics_transfer_device")
      );
   }
}
