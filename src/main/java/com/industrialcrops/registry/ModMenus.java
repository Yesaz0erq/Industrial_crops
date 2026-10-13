package com.industrialcrops.registry;
import net.minecraftforge.registries.RegistryObject;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
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
import com.industrialcrops.screen.TyconPressurizedFurnaceMenu;
import com.industrialcrops.screen.WirelessEnergyTransmitterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
public final class ModMenus {
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "industrialcrops");
   public static final RegistryObject<MenuType<CrystalSteelWorkbenchMenu>> CRYSTAL_STEEL_WORKBENCH = MENUS.register(
      "crystal_steel_workbench", () -> IForgeMenuType.create(CrystalSteelWorkbenchMenu::new)
   );
   public static final RegistryObject<MenuType<RootOreExtractorMenu>> ROOT_ORE_EXTRACTOR = MENUS.register(
      "basic_crop_conversion_device", () -> IForgeMenuType.create(RootOreExtractorMenu::new)
   );
   public static final RegistryObject<MenuType<CropCompressorMenu>> CROP_COMPRESSOR = MENUS.register(
      "crop_compressor", () -> IForgeMenuType.create(CropCompressorMenu::new)
   );
   public static final RegistryObject<MenuType<CropAnalysisDeviceMenu>> CROP_ANALYSIS_DEVICE = MENUS.register(
      "crop_analysis_device", () -> IForgeMenuType.create(CropAnalysisDeviceMenu::new)
   );
   public static final RegistryObject<MenuType<GourdModificationDeviceMenu>> GOURD_MODIFICATION_DEVICE = MENUS.register(
      "gourd_modification_device", () -> IForgeMenuType.create(GourdModificationDeviceMenu::new)
   );
   public static final RegistryObject<MenuType<BasicControlDeviceMenu>> BASIC_CONTROL_DEVICE = MENUS.register(
      "basic_control_device", () -> IForgeMenuType.create(BasicControlDeviceMenu::new)
   );
   public static final RegistryObject<MenuType<ReinforcedControlDeviceMenu>> REINFORCED_CONTROL_DEVICE = MENUS.register(
      "reinforced_control_device", () -> IForgeMenuType.create(ReinforcedControlDeviceMenu::new)
   );
   public static final RegistryObject<MenuType<BasicManipulatorMenu>> BASIC_MANIPULATOR = MENUS.register(
      "basic_manipulation_device", () -> IForgeMenuType.create(BasicManipulatorMenu::new)
   );
   public static final RegistryObject<MenuType<IncubatorMenu>> INCUBATOR = MENUS.register(
      "slime_conversion_device", () -> IForgeMenuType.create(IncubatorMenu::new)
   );
   public static final RegistryObject<MenuType<SlimeIncubatorMenu>> SLIME_INCUBATOR = MENUS.register(
      "slime_cultivation_device", () -> IForgeMenuType.create(SlimeIncubatorMenu::new)
   );
   public static final RegistryObject<MenuType<AdvancedIndustrialStorageMenu>> ADVANCED_INDUSTRIAL_STORAGE_DEVICE = MENUS.register(
      "advanced_industrial_storage_device", () -> IForgeMenuType.create(AdvancedIndustrialStorageMenu::new)
   );
   public static final RegistryObject<MenuType<GoldenLaunchSiloMenu>> GOLDEN_LAUNCH_SILO = MENUS.register(
      "explosive_potato_launcher", () -> IForgeMenuType.create(GoldenLaunchSiloMenu::new)
   );
   public static final RegistryObject<MenuType<ProcessorProgrammerMenu>> PROCESSOR_PROGRAMMER = MENUS.register(
      "component_programming_device", () -> IForgeMenuType.create(ProcessorProgrammerMenu::new)
   );
   public static final RegistryObject<MenuType<AutomaticPlanterMenu>> AUTOMATIC_PLANTER = MENUS.register(
      "automatic_sowing_device", () -> IForgeMenuType.create(AutomaticPlanterMenu::new)
   );
   public static final RegistryObject<MenuType<MatterMachineMenu>> MATTER_MACHINE = MENUS.register(
      "matter_machine", () -> IForgeMenuType.create(MatterMachineMenu::new)
   );
   public static final RegistryObject<MenuType<ItemNetworkTerminalMenu>> ITEM_NETWORK_TERMINAL = MENUS.register(
      "item_network_management_terminal", () -> IForgeMenuType.create(ItemNetworkTerminalMenu::new)
   );
   public static final RegistryObject<MenuType<BioEnergyMenu>> BIO_ENERGY_MACHINE = MENUS.register(
      "bio_energy_machine", () -> IForgeMenuType.create(BioEnergyMenu::new)
   );
   public static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE = MENUS.register(
      "electric_furnace", () -> IForgeMenuType.create(ElectricFurnaceMenu::new)
   );
   public static final RegistryObject<MenuType<TyconPressurizedFurnaceMenu>> TYCON_PRESSURIZED_FURNACE = MENUS.register(
      "tycon_pressurized_furnace", () -> IForgeMenuType.create(TyconPressurizedFurnaceMenu::new)
   );
   public static final RegistryObject<MenuType<DigitalMiniatureForestMenu>> DIGITAL_MINIATURE_FOREST = MENUS.register(
      "digital_miniature_forest", () -> IForgeMenuType.create(DigitalMiniatureForestMenu::new)
   );
   public static final RegistryObject<MenuType<PipeSorterMenu>> PIPE_SORTER = MENUS.register(
      "pipe_sorter", () -> IForgeMenuType.create(PipeSorterMenu::new)
   );
   public static final RegistryObject<MenuType<GoldPlasmaExtractorMenu>> GOLD_PLASMA_EXTRACTOR = MENUS.register(
      "extraction_device", () -> IForgeMenuType.create(GoldPlasmaExtractorMenu::new)
   );
   public static final RegistryObject<MenuType<WirelessEnergyTransmitterMenu>> WIRELESS_ENERGY_TRANSMITTER = MENUS.register(
      "wireless_energy_transfer_device", () -> IForgeMenuType.create(WirelessEnergyTransmitterMenu::new)
   );
   public static final RegistryObject<MenuType<SatelliteReceiverMenu>> SATELLITE_RECEIVER = MENUS.register(
      "satellite_receiver", () -> IForgeMenuType.create(SatelliteReceiverMenu::new)
   );
   public static final RegistryObject<MenuType<CrystalLogisticsMenu>> CRYSTAL_LOGISTICS = MENUS.register(
      "wireless_logistics_transfer_device", () -> IForgeMenuType.create(CrystalLogisticsMenu::new)
   );
   public static final RegistryObject<MenuType<CraftingProcessorMenu>> CRAFTING_PROCESSOR = MENUS.register(
      "crafting_processing_device", () -> IForgeMenuType.create(CraftingProcessorMenu::new)
   );
   public static final RegistryObject<MenuType<TerrainProcessorMenu>> TERRAIN_PROCESSOR = MENUS.register(
      "terrain_processing_device", () -> IForgeMenuType.create(TerrainProcessorMenu::new)
   );
   public static final RegistryObject<MenuType<AnimalBreederMenu>> ANIMAL_BREEDER = MENUS.register(
      "automatic_animal_breeder", () -> IForgeMenuType.create(AnimalBreederMenu::new)
   );
   public static final RegistryObject<MenuType<SatelliteControlMenu>> SATELLITE_CONTROL_DEVICE = MENUS.register(
      "satellite_control_device", () -> IForgeMenuType.create(SatelliteControlMenu::new)
   );
   public static final RegistryObject<MenuType<LaunchSiteMenu>> LAUNCH_SITE = MENUS.register(
      "rocket_launch_site", () -> IForgeMenuType.create(LaunchSiteMenu::new)
   );
   public static final RegistryObject<MenuType<ApAgricultureMenu>> AP_AGRICULTURE = MENUS.register(
      "ap_agricultural_complex", () -> IForgeMenuType.create(ApAgricultureMenu::new)
   );
   public static final RegistryObject<MenuType<ApLivestockMenu>> AP_LIVESTOCK = MENUS.register(
      "ap_livestock_ranch", () -> IForgeMenuType.create(ApLivestockMenu::new)
   );
   private ModMenus() {
   }
}
