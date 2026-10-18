package com.industrialcrops.registry;

import com.industrialcrops.item.AdvancedManipulatorUpgradeItem;
import com.industrialcrops.item.AnimalFeedBagItem;
import com.industrialcrops.item.BaggedCropItem;
import com.industrialcrops.item.FertilizerBagItem;
import com.industrialcrops.item.FieldGuideItem;
import com.industrialcrops.item.FusionIngotPickaxeItem;
import com.industrialcrops.item.FusionMelonBlockItem;
import com.industrialcrops.item.GeneticCropBlockItem;
import com.industrialcrops.item.GeneticCropItem;
import com.industrialcrops.item.GeneticCropProductBlockItem;
import com.industrialcrops.item.IncubatorBlockItem;
import com.industrialcrops.item.IndustrialStorageComponentItem;
import com.industrialcrops.item.ExpandableStorageComponentItem;
import com.industrialcrops.item.RemoteAccessDeviceItem;
import com.industrialcrops.item.SatelliteRocketItem;
import com.industrialcrops.item.SeedBagItem;
import com.industrialcrops.item.ShimmeringItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public final class ModItems {
   public static final Items ITEMS = DeferredRegister.createItems("industrialcrops");
   public static final DeferredItem<SeedBagItem> EMPTY_BAG = ITEMS.register("empty_bag", () -> new SeedBagItem(new Properties()));
   public static final DeferredItem<FieldGuideItem> FIELD_GUIDE = ITEMS.register("field_guide", () -> new FieldGuideItem(new Properties().stacksTo(1)));
   public static final DeferredItem<GeneticCropBlockItem> INDUSTRIAL_CARROT = ITEMS.register(
      "industrial_carrot", () -> new GeneticCropBlockItem((Block)ModBlocks.INDUSTRIAL_CARROT_CROP.get(), new Properties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_CARROT = ITEMS.register(
      "bagged_industrial_carrot", () -> new BaggedCropItem((Block)ModBlocks.INDUSTRIAL_CARROT_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropBlockItem> INDUSTRIAL_POTATO = ITEMS.register(
      "industrial_potato", () -> new GeneticCropBlockItem((Block)ModBlocks.INDUSTRIAL_POTATO_CROP.get(), new Properties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_POTATO = ITEMS.register(
      "bagged_industrial_potato", () -> new BaggedCropItem((Block)ModBlocks.INDUSTRIAL_POTATO_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropItem> INDUSTRIAL_WHEAT = ITEMS.register("industrial_wheat", () -> new GeneticCropItem(new Properties()));
   public static final DeferredItem<FertilizerBagItem> FERTILIZER_FAST_GROWTH = ITEMS.register(
      "rapid_growth_fertilizer_bag", () -> new FertilizerBagItem(baggedProperties(), FertilizerBagItem.Mode.FAST_GROWTH)
   );
   public static final DeferredItem<FertilizerBagItem> FERTILIZER_FERTILE_SOIL = ITEMS.register(
      "fertile_soil_fertilizer_bag", () -> new FertilizerBagItem(baggedProperties(), FertilizerBagItem.Mode.FERTILE_SOIL)
   );
   public static final DeferredItem<FertilizerBagItem> STRANGE_FERTILIZER = ITEMS.register(
      "strange_fertilizer_bag", () -> new FertilizerBagItem(baggedProperties(), FertilizerBagItem.Mode.COMET_SOIL)
   );
   public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_BASIC = registerFeedBag("normal_feed_bag", AnimalFeedBagItem.Mode.BASIC);
   public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_HEALING = registerFeedBag("health_restoration_feed_bag", AnimalFeedBagItem.Mode.HEALING);
   public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_GROWTH = registerFeedBag("rapid_growth_feed_bag", AnimalFeedBagItem.Mode.GROWTH);
   public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_FAST_BREEDING = registerFeedBag("rapid_breeding_feed_bag", AnimalFeedBagItem.Mode.FAST_BREEDING);
   public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_RESISTANCE = registerFeedBag("resistance_boost_feed_bag", AnimalFeedBagItem.Mode.RESISTANCE);
   public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_WHEAT_SEEDS = ITEMS.register(
      "bagged_industrial_wheat_seeds", () -> new BaggedCropItem((Block)ModBlocks.INDUSTRIAL_WHEAT_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_MELON_SEEDS = ITEMS.register(
      "bagged_industrial_melon_seeds", () -> new BaggedCropItem((Block)ModBlocks.INDUSTRIAL_MELON_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_PUMPKIN_SEEDS = ITEMS.register(
      "bagged_industrial_pumpkin_seeds", () -> new BaggedCropItem((Block)ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropBlockItem> PRISM_POD_SEEDS = ITEMS.register(
      "prism_pod_seeds", () -> new GeneticCropBlockItem((Block)ModBlocks.PRISM_POD_CROP.get(), new Properties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_PRISM_POD_SEEDS = ITEMS.register(
      "bagged_prism_pod_seeds", () -> new BaggedCropItem((Block)ModBlocks.PRISM_POD_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropItem> PRISM_POD = ITEMS.register("prism_pod", () -> new GeneticCropItem(new Properties()));
   public static final DeferredItem<GeneticCropBlockItem> EMBERCOIL_SEEDS = ITEMS.register(
      "embercoil_seeds", () -> new GeneticCropBlockItem((Block)ModBlocks.EMBERCOIL_CROP.get(), new Properties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_EMBERCOIL_SEEDS = ITEMS.register(
      "bagged_embercoil_seeds", () -> new BaggedCropItem((Block)ModBlocks.EMBERCOIL_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropItem> EMBERCOIL = ITEMS.register("embercoil", () -> new GeneticCropItem(new Properties()));
   public static final DeferredItem<GeneticCropBlockItem> STARBLOOM_SEEDS = ITEMS.register(
      "starbloom_seeds", () -> new GeneticCropBlockItem((Block)ModBlocks.STARBLOOM_CROP.get(), new Properties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_STARBLOOM_SEEDS = ITEMS.register(
      "bagged_starbloom_seeds", () -> new BaggedCropItem((Block)ModBlocks.STARBLOOM_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropItem> STARBLOOM = ITEMS.register("starbloom", () -> new GeneticCropItem(new Properties()));
   public static final DeferredItem<GeneticCropBlockItem> NEONBULB_SEEDS = ITEMS.register(
      "neonbulb_seeds", () -> new GeneticCropBlockItem((Block)ModBlocks.NEONBULB_CROP.get(), new Properties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_NEONBULB_SEEDS = ITEMS.register(
      "bagged_neonbulb_seeds", () -> new BaggedCropItem((Block)ModBlocks.NEONBULB_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropItem> NEONBULB = ITEMS.register(
      "neonbulb", () -> new GeneticCropItem(new Properties().food(new Builder().nutrition(4).saturationModifier(1.25F).build()))
   );
   public static final DeferredItem<GeneticCropBlockItem> FLUXSTALK_SEEDS = ITEMS.register(
      "fluxstalk_seeds", () -> new GeneticCropBlockItem((Block)ModBlocks.FLUXSTALK_CROP.get(), new Properties())
   );
   public static final DeferredItem<BaggedCropItem> BAGGED_FLUXSTALK_SEEDS = ITEMS.register(
      "bagged_fluxstalk_seeds", () -> new BaggedCropItem((Block)ModBlocks.FLUXSTALK_CROP.get(), baggedProperties())
   );
   public static final DeferredItem<GeneticCropItem> FLUXSTALK = ITEMS.register(
      "fluxstalk", () -> new GeneticCropItem(new Properties().food(new Builder().nutrition(10).saturationModifier(0.2F).build()))
   );
   public static final DeferredItem<ItemNameBlockItem> PLASMA_BERRY = ITEMS.register(
      "plasma_fruit",
      () -> new ItemNameBlockItem((Block)ModBlocks.ENERGY_BUSH.get(), new Properties().food(new Builder().nutrition(3).saturationModifier(0.35F).build()))
   );
   public static final DeferredItem<BucketItem> CONCENTRATED_PLASMA_JUICE_BUCKET = ITEMS.register(
      "plasma_juice_bucket", () -> new BucketItem((Fluid)ModFluids.CONCENTRATED_PLASMA_JUICE.get(), new Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> ENERGY_CRYSTAL = ITEMS.register("energy_crystal", () -> new Item(new Properties().rarity(Rarity.RARE)));
   public static final DeferredItem<Item> CRYSTAL_INGOT = ITEMS.register("crystal_ingot", () -> new Item(new Properties().rarity(Rarity.RARE)));
   public static final DeferredItem<Item> COMET_FRUIT = ITEMS.register("comet_essence", () -> new Item(new Properties().rarity(Rarity.UNCOMMON)));
   public static final DeferredItem<GeneticCropProductBlockItem> INDUSTRIAL_MELON = ITEMS.register(
      "industrial_melon", () -> new GeneticCropProductBlockItem((Block)ModBlocks.INDUSTRIAL_MELON_BLOCK.get(), new Properties())
   );
   public static final DeferredItem<GeneticCropProductBlockItem> INDUSTRIAL_PUMPKIN = ITEMS.register(
      "industrial_pumpkin", () -> new GeneticCropProductBlockItem((Block)ModBlocks.INDUSTRIAL_PUMPKIN_BLOCK.get(), new Properties())
   );
   public static final DeferredItem<FusionMelonBlockItem> FUSION_MELON = ITEMS.register(
      "fusion_melon", () -> new FusionMelonBlockItem((Block)ModBlocks.FUSION_MELON.get(), new Properties().rarity(Rarity.EPIC))
   );
   public static final DeferredItem<ShimmeringItem> FUSION_INGOT = ITEMS.register(
      "fusion_ingot", () -> new ShimmeringItem(new Properties().rarity(Rarity.EPIC))
   );
   public static final DeferredItem<FusionIngotPickaxeItem> FUSION_INGOT_PICKAXE = ITEMS.register(
      "fusion_ingot_pickaxe", () -> new FusionIngotPickaxeItem(new Properties().rarity(Rarity.EPIC))
   );
   public static final DeferredItem<Item> REDSTONE_BONEMEAL = ITEMS.register("redstone_bonemeal", () -> new Item(new Properties()));
   public static final DeferredItem<Item> COMPONENT_SUBSTRATE = ITEMS.register("component_substrate", () -> new Item(new Properties()));
   public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_1 = ITEMS.register(
      "industrial_storage_component_1", () -> new IndustrialStorageComponentItem(new Properties().stacksTo(16), 1)
   );
   public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_2 = ITEMS.register(
      "industrial_storage_component_2", () -> new IndustrialStorageComponentItem(new Properties().stacksTo(16).rarity(Rarity.UNCOMMON), 2)
   );
   public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_3 = ITEMS.register(
      "industrial_storage_component_3", () -> new IndustrialStorageComponentItem(new Properties().stacksTo(16).rarity(Rarity.RARE), 4)
   );
   public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_4 = ITEMS.register(
      "industrial_storage_component_4", () -> new IndustrialStorageComponentItem(new Properties().stacksTo(16).rarity(Rarity.EPIC), 16)
   );
   public static final DeferredItem<Item> GUIDANCE_COMPONENT = ITEMS.register("guidance_component", () -> new Item(new Properties()));
   public static final DeferredItem<Item> EXPLOSIVE_POTATO = ITEMS.register(
      "explosive_potato", () -> new Item(new Properties().stacksTo(16).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> RAPID_FIRE_COMPONENT = ITEMS.register(
      "rapid_fire_component", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> POWER_COMPONENT = ITEMS.register("power_component", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE)));
   public static final DeferredItem<Item> AUTOMATIC_COMPONENT = ITEMS.register(
      "automatic_component", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> DIMENSION_UPGRADE_COMPONENT = ITEMS.register(
      "dimension_upgrade_component", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> INFINITE_DIMENSION_UPGRADE_COMPONENT = ITEMS.register(
      "infinite_dimension_upgrade_component", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.EPIC))
   );
   public static final DeferredItem<Item> AP_EXPANSION_COMPONENT_1 = ITEMS.register(
      "ap_expansion_component_1", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> AP_EXPANSION_COMPONENT_2 = ITEMS.register(
      "ap_expansion_component_2", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> AP_EXPANSION_COMPONENT_3 = ITEMS.register(
      "ap_expansion_component_3", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> UPGRADE_KIT_SUBSTRATE = ITEMS.register("upgrade_kit_substrate", () -> new Item(new Properties()));
   public static final DeferredItem<Item> GOLD_UPGRADE_KIT = ITEMS.register(
      "gold_upgrade_kit", () -> new AdvancedManipulatorUpgradeItem(new Properties().stacksTo(16).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> CRYSTAL_STEEL_UPGRADE_KIT = ITEMS.register(
      "crystal_steel_upgrade_kit", () -> new Item(new Properties().stacksTo(16).rarity(Rarity.EPIC))
   );
   public static final DeferredItem<Item> MINING_UPGRADE_COMPONENT = ITEMS.register(
      "mining_upgrade_component", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.EPIC))
   );
   public static final DeferredItem<Item> SPEED_COMPONENT_1 = registerSpeedUpgrade("speed_component_1");
   public static final DeferredItem<Item> SPEED_COMPONENT_2 = registerSpeedUpgrade("speed_component_2");
   public static final DeferredItem<Item> SPEED_COMPONENT_3 = registerSpeedUpgrade("speed_component_3");
   public static final DeferredItem<Item> SPEED_COMPONENT_4 = registerSpeedUpgrade("speed_component_4");
   public static final DeferredItem<DeferredSpawnEggItem> BROWN_CREATE_SLIME_SPAWN_EGG = ITEMS.register(
      "copper_gear_slime_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.BROWN_CREATE_SLIME, 11950898, 13666898, new Properties())
   );
   public static final DeferredItem<DeferredSpawnEggItem> GRAY_GEAR_SLIME_SPAWN_EGG = ITEMS.register(
      "gray_gear_slime_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.GRAY_GEAR_SLIME, 5462108, 12165220, new Properties())
   );
   public static final DeferredItem<DeferredSpawnEggItem> GOLDEN_REDSTONE_LAMP_SLIME_SPAWN_EGG = ITEMS.register(
      "golden_redstone_lamp_slime_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.GOLDEN_REDSTONE_LAMP_SLIME, 15181079, 16734720, new Properties())
   );
   public static final DeferredItem<DeferredSpawnEggItem> BLUE_COMPONENT_SUBSTRATE_SLIME_SPAWN_EGG = ITEMS.register(
      "blue_component_substrate_slime_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.DIAMOND_PROCESSOR_SLIME, 1526428, 7710719, new Properties())
   );
   public static final DeferredItem<BlockItem> INDUSTRIAL_CARROT_BLOCK = registerBlockItem("industrial_carrot_block", ModBlocks.INDUSTRIAL_CARROT_BLOCK);
   public static final DeferredItem<BlockItem> INDUSTRIAL_POTATO_BLOCK = registerBlockItem("industrial_potato_block", ModBlocks.INDUSTRIAL_POTATO_BLOCK);
   public static final DeferredItem<BlockItem> INDUSTRIAL_WHEAT_BLOCK = registerBlockItem("industrial_wheat_block", ModBlocks.INDUSTRIAL_WHEAT_BLOCK);
   public static final DeferredItem<BlockItem> INDUSTRIAL_MELON_BLOCK = registerBlockItem("industrial_melon_block", ModBlocks.INDUSTRIAL_MELON_BLOCK);
   public static final DeferredItem<BlockItem> INDUSTRIAL_PUMPKIN_BLOCK = registerBlockItem("industrial_pumpkin_block", ModBlocks.INDUSTRIAL_PUMPKIN_BLOCK);
   public static final DeferredItem<BlockItem> FERTILE_FARMLAND = registerBlockItem("fertile_soil", ModBlocks.FERTILE_FARMLAND);
   public static final DeferredItem<BlockItem> COPPER_DEVICE_CASING = registerBlockItem("copper_device_casing", ModBlocks.COPPER_DEVICE_CASING);
   public static final DeferredItem<BlockItem> IRON_DEVICE_CASING = registerBlockItem("iron_device_casing", ModBlocks.IRON_DEVICE_CASING);
   public static final DeferredItem<BlockItem> PROCESSOR_GOLD_DEVICE_CASING = registerBlockItem(
      "component_substrate_integrated_gold_device_casing", ModBlocks.PROCESSOR_GOLD_DEVICE_CASING
   );
   public static final DeferredItem<BlockItem> CRYSTAL_STEEL_WORKBENCH = registerBlockItem("crystal_steel_workbench", ModBlocks.CRYSTAL_STEEL_WORKBENCH);
   public static final DeferredItem<BlockItem> CRYSTAL_STEEL_DEVICE_CASING = registerBlockItem("crystal_steel_casing", ModBlocks.CRYSTAL_STEEL_DEVICE_CASING);
   public static final DeferredItem<BlockItem> ROOT_ORE_EXTRACTOR = registerBlockItem("basic_crop_conversion_device", ModBlocks.ROOT_ORE_EXTRACTOR);
   public static final DeferredItem<BlockItem> CROP_COMPRESSOR = registerBlockItem("crop_compressor", ModBlocks.CROP_COMPRESSOR);
   public static final DeferredItem<BlockItem> CROP_ANALYSIS_DEVICE = registerBlockItem("crop_analysis_device", ModBlocks.CROP_ANALYSIS_DEVICE);
   public static final DeferredItem<BlockItem> MELON_MODIFICATION_DEVICE = registerBlockItem("melon_modification_device", ModBlocks.MELON_MODIFICATION_DEVICE);
   public static final DeferredItem<BlockItem> MIXER = registerBlockItem("mixer", ModBlocks.MIXER);
   public static final DeferredItem<BlockItem> INDUSTRIAL_CROP_STORAGE_ARRAY = registerBlockItem(
      "basic_storage_component", ModBlocks.INDUSTRIAL_CROP_STORAGE_ARRAY
   );
   public static final DeferredItem<BlockItem> CARROT_CONTROL_DEVICE = registerBlockItem("basic_control_device", ModBlocks.CARROT_CONTROL_DEVICE);
   public static final DeferredItem<BlockItem> BASIC_MANIPULATOR = registerBlockItem("basic_manipulation_device", ModBlocks.BASIC_MANIPULATOR);
   public static final DeferredItem<BlockItem> ADVANCED_MANIPULATOR = registerBlockItem("advanced_manipulation_device", ModBlocks.ADVANCED_MANIPULATOR);
   public static final DeferredItem<BlockItem> PROCESSOR_PROGRAMMER = registerBlockItem("component_programming_device", ModBlocks.PROCESSOR_PROGRAMMER);
   public static final DeferredItem<BlockItem> AUTOMATIC_PLANTER = registerBlockItem("automatic_sowing_device", ModBlocks.AUTOMATIC_PLANTER);
   public static final DeferredItem<BlockItem> MATTER_DIGITIZER = registerBlockItem("matter_digitization_device", ModBlocks.MATTER_DIGITIZER);
   public static final DeferredItem<BlockItem> DIGITIZED_ITEM_COPIER = registerBlockItem("data_copying_device", ModBlocks.DIGITIZED_ITEM_COPIER);
   public static final DeferredItem<BlockItem> MATTER_RECONSTRUCTOR = registerBlockItem("matter_reconstruction_device", ModBlocks.MATTER_RECONSTRUCTOR);
   public static final DeferredItem<BlockItem> ITEM_NETWORK_TERMINAL = registerBlockItem("item_network_management_terminal", ModBlocks.ITEM_NETWORK_TERMINAL);
   public static final DeferredItem<BlockItem> BIO_ENERGY_GENERATOR = registerBlockItem("bio_energy_generation_device", ModBlocks.BIO_ENERGY_GENERATOR);
   public static final DeferredItem<BlockItem> BIO_ENERGY_REACTOR = registerBlockItem("bio_energy_reaction_device", ModBlocks.BIO_ENERGY_REACTOR);
   public static final DeferredItem<BlockItem> ENERGY_BATTERY = registerBlockItem("gold_energy_storage_cabinet", ModBlocks.ENERGY_BATTERY);
   public static final DeferredItem<BlockItem> RESIDUE_INCINERATOR = registerBlockItem("incinerator", ModBlocks.RESIDUE_INCINERATOR);
   public static final DeferredItem<BlockItem> ELECTRIC_FURNACE = registerBlockItem("electric_furnace", ModBlocks.ELECTRIC_FURNACE);
   public static final DeferredItem<BlockItem> ULTRA_HIGH_PRESSURE_INV_FORGE = registerBlockItem("ultra_high_pressure_inv_forge", ModBlocks.ULTRA_HIGH_PRESSURE_INV_FORGE);
   public static final DeferredItem<BlockItem> DIGITAL_MINIATURE_FOREST = registerBlockItem("digital_miniature_forest", ModBlocks.DIGITAL_MINIATURE_FOREST);
   public static final DeferredItem<BlockItem> BASIC_ENERGY_CABLE = registerBlockItem("basic_energy_cable", ModBlocks.BASIC_ENERGY_CABLE);
   public static final DeferredItem<BlockItem> ADVANCED_ENERGY_CABLE = registerBlockItem("advanced_energy_cable", ModBlocks.ADVANCED_ENERGY_CABLE);
   public static final DeferredItem<BlockItem> REINFORCED_INDUSTRIAL_STORAGE_ARRAY = registerBlockItem(
      "reinforced_storage_component", ModBlocks.REINFORCED_INDUSTRIAL_STORAGE_ARRAY
   );
   public static final DeferredItem<BlockItem> REINFORCED_CONTROL_DEVICE = registerBlockItem("reinforced_control_device", ModBlocks.REINFORCED_CONTROL_DEVICE);
   public static final DeferredItem<RemoteAccessDeviceItem> REMOTE_ACCESS_DEVICE = ITEMS.register(
      "remote_access_device", () -> new RemoteAccessDeviceItem(new Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<BlockItem> ADVANCED_INDUSTRIAL_STORAGE_DEVICE = registerBlockItem(
      "advanced_industrial_storage_device", ModBlocks.ADVANCED_INDUSTRIAL_STORAGE_DEVICE
   );
   public static final DeferredItem<BlockItem> EXPANDABLE_STORAGE_DEVICE = registerBlockItem(
      "expandable_storage_device", ModBlocks.EXPANDABLE_STORAGE_DEVICE
   );
   public static final DeferredItem<BlockItem> SUPER_COMPRESSOR = registerBlockItem("super_compressor", ModBlocks.SUPER_COMPRESSOR);
   public static final DeferredItem<BlockItem> INTEGRATED_SMELTING_FURNACE = registerBlockItem("integrated_smelting_furnace", ModBlocks.INTEGRATED_SMELTING_FURNACE);
   /** Compressed Industrial Potato: 10,000 Industrial Potatoes pressed by the Super Compressor. */
   public static final DeferredItem<Item> COMPRESSED_POTATO_10K = ITEMS.register(
      "compressed_potato_10k", () -> new ShimmeringItem(new Properties().rarity(Rarity.EPIC))
   );
   /** 10K Storage Component: 9,999 items per item type in the Expandable Storage Device, 54 item types each. */
   public static final DeferredItem<ExpandableStorageComponentItem> STORAGE_COMPONENT_10K = ITEMS.register(
      "storage_component_10k", () -> new ExpandableStorageComponentItem(new Properties().stacksTo(16).rarity(Rarity.EPIC), 54, 9_999)
   );
   public static final DeferredItem<BlockItem> SUPER_CROP_CULTIVATOR = registerBlockItem("super_crop_cultivator", ModBlocks.SUPER_CROP_CULTIVATOR);
   /** Black Nebula Carrot: Super Industrial Carrot + Comet Essence + Prism Pod in the Super Crop Cultivator. Restores 16 hunger and 16 saturation. */
   public static final DeferredItem<Item> BLACK_NEBULA_CARROT = ITEMS.register(
      "black_nebula_carrot",
      () -> new com.industrialcrops.item.ShimmeringCarrotBlockItem(ModBlocks.BLACK_NEBULA_CARROT_CROP.get(), new Properties().rarity(Rarity.EPIC).food(new Builder().nutrition(16).saturationModifier(0.5F).build()))
   );
   /** Purple Dragon Carrot: Super Industrial Carrot + Dragon's Breath + Neon Bulb. 8 hunger, 8 saturation and 30 s of Dragon Breath. */
   public static final DeferredItem<Item> PURPLE_DRAGON_CARROT = ITEMS.register(
      "purple_dragon_carrot",
      () -> new com.industrialcrops.item.ShimmeringCarrotBlockItem(ModBlocks.PURPLE_DRAGON_CARROT_CROP.get(), new Properties().rarity(Rarity.EPIC).food(new Builder().nutrition(8).saturationModifier(0.5F).alwaysEdible()
         .effect(() -> new net.minecraft.world.effect.MobEffectInstance(ModEffects.DRAGON_BREATH, 20 * 30, 0), 1.0F).build()))
   );
   /** Omni Bucket: crafted on the Crystal Steel Workbench; pours an endless cauldron of Omni Potion. */
   public static final DeferredItem<Item> OMNI_BUCKET = ITEMS.register(
      "omni_bucket", () -> new ShimmeringItem(new Properties().stacksTo(1).rarity(Rarity.EPIC))
   );
   /** Omni Potion: every beneficial vanilla effect for 3 minutes. */
   public static final DeferredItem<Item> OMNI_POTION = ITEMS.register(
      "omni_potion", () -> new com.industrialcrops.item.OmniPotionItem(new Properties().stacksTo(16).rarity(Rarity.EPIC)
         .craftRemainder(net.minecraft.world.item.Items.GLASS_BOTTLE))
   );
   /** Omni Carrot: Super Industrial Carrot + Omni Potion; 12 hunger, 12 saturation and every beneficial vanilla effect for 5 minutes. */
   public static final DeferredItem<Item> OMNI_CARROT = ITEMS.register(
      "omni_carrot", () -> new com.industrialcrops.item.OmniCarrotItem(ModBlocks.OMNI_CARROT_CROP.get(), new Properties().rarity(Rarity.EPIC)
         .food(new Builder().nutrition(12).saturationModifier(0.5F).alwaysEdible().build()))
   );
   public static final DeferredItem<IncubatorBlockItem> INCUBATOR = ITEMS.register(
      "slime_conversion_device", () -> new IncubatorBlockItem((Block)ModBlocks.INCUBATOR.get(), new Properties().stacksTo(1))
   );
   public static final DeferredItem<BlockItem> SLIME_INCUBATOR = registerBlockItem("slime_cultivation_device", ModBlocks.SLIME_INCUBATOR);
   public static final DeferredItem<BlockItem> GOLDEN_LAUNCH_SILO = registerBlockItem("explosive_potato_launcher", ModBlocks.GOLDEN_LAUNCH_SILO);
   public static final DeferredItem<BlockItem> PIPE = registerBlockItem("basic_pipe", ModBlocks.PIPE);
   public static final DeferredItem<BlockItem> INPUT_PIPE = registerBlockItem("basic_input_pipe", ModBlocks.INPUT_PIPE);
   public static final DeferredItem<BlockItem> OUTPUT_PIPE = registerBlockItem("basic_output_pipe", ModBlocks.OUTPUT_PIPE);
   public static final DeferredItem<BlockItem> REINFORCED_PIPE = registerBlockItem("reinforced_pipe", ModBlocks.REINFORCED_PIPE);
   public static final DeferredItem<BlockItem> REINFORCED_INPUT_PIPE = registerBlockItem("reinforced_input_pipe", ModBlocks.REINFORCED_INPUT_PIPE);
   public static final DeferredItem<BlockItem> REINFORCED_OUTPUT_PIPE = registerBlockItem("reinforced_output_pipe", ModBlocks.REINFORCED_OUTPUT_PIPE);
   public static final DeferredItem<BlockItem> ADVANCED_PIPE = registerBlockItem("advanced_pipe", ModBlocks.ADVANCED_PIPE);
   public static final DeferredItem<BlockItem> ADVANCED_INPUT_PIPE = registerBlockItem("advanced_input_pipe", ModBlocks.ADVANCED_INPUT_PIPE);
   public static final DeferredItem<BlockItem> ADVANCED_OUTPUT_PIPE = registerBlockItem("advanced_output_pipe", ModBlocks.ADVANCED_OUTPUT_PIPE);
   public static final DeferredItem<BlockItem> PIPE_SORTER = registerBlockItem("pipe_sorter", ModBlocks.PIPE_SORTER);
   public static final DeferredItem<BlockItem> GOLD_FLUID_PIPE = registerBlockItem("gold_fluid_pipe", ModBlocks.GOLD_FLUID_PIPE);
   public static final DeferredItem<BlockItem> GOLD_PLASMA_EXTRACTOR = registerBlockItem("extraction_device", ModBlocks.GOLD_PLASMA_EXTRACTOR);
   public static final DeferredItem<BlockItem> COPPER_FLUID_STORAGE_CABINET = registerBlockItem(
      "copper_fluid_storage_cabinet", ModBlocks.COPPER_FLUID_STORAGE_CABINET
   );
   public static final DeferredItem<BlockItem> COMET_SOIL = registerBlockItem("comet_soil", ModBlocks.COMET_SOIL);
   public static final DeferredItem<BlockItem> COMET_LOG = registerBlockItem("comet_log", ModBlocks.COMET_LOG);
   public static final DeferredItem<BlockItem> COMET_WOOD = registerBlockItem("comet_wood", ModBlocks.COMET_WOOD);
   public static final DeferredItem<BlockItem> STRIPPED_COMET_LOG = registerBlockItem("stripped_comet_log", ModBlocks.STRIPPED_COMET_LOG);
   public static final DeferredItem<BlockItem> STRIPPED_COMET_WOOD = registerBlockItem("stripped_comet_wood", ModBlocks.STRIPPED_COMET_WOOD);
   public static final DeferredItem<BlockItem> COMET_PLANKS = registerBlockItem("comet_planks", ModBlocks.COMET_PLANKS);
   public static final DeferredItem<BlockItem> COMET_STAIRS = registerBlockItem("comet_stairs", ModBlocks.COMET_STAIRS);
   public static final DeferredItem<BlockItem> COMET_SLAB = registerBlockItem("comet_slab", ModBlocks.COMET_SLAB);
   public static final DeferredItem<BlockItem> COMET_FENCE = registerBlockItem("comet_fence", ModBlocks.COMET_FENCE);
   public static final DeferredItem<BlockItem> COMET_FENCE_GATE = registerBlockItem("comet_fence_gate", ModBlocks.COMET_FENCE_GATE);
   public static final DeferredItem<BlockItem> COMET_DOOR = registerBlockItem("comet_door", ModBlocks.COMET_DOOR);
   public static final DeferredItem<BlockItem> COMET_TRAPDOOR = registerBlockItem("comet_trapdoor", ModBlocks.COMET_TRAPDOOR);
   public static final DeferredItem<BlockItem> COMET_PRESSURE_PLATE = registerBlockItem("comet_pressure_plate", ModBlocks.COMET_PRESSURE_PLATE);
   public static final DeferredItem<BlockItem> COMET_BUTTON = registerBlockItem("comet_button", ModBlocks.COMET_BUTTON);
   public static final DeferredItem<BlockItem> COMET_LEAVES = registerBlockItem("comet_leaves", ModBlocks.COMET_LEAVES);
   public static final DeferredItem<BlockItem> COMET_SAPLING = registerBlockItem("comet_sapling", ModBlocks.COMET_SAPLING);
   public static final DeferredItem<SignItem> COMET_SIGN = ITEMS.register(
      "comet_sign", () -> new SignItem(new Properties().stacksTo(16), (Block)ModBlocks.COMET_SIGN.get(), (Block)ModBlocks.COMET_WALL_SIGN.get())
   );
   public static final DeferredItem<HangingSignItem> COMET_HANGING_SIGN = ITEMS.register(
      "comet_hanging_sign",
      () -> new HangingSignItem((Block)ModBlocks.COMET_HANGING_SIGN.get(), (Block)ModBlocks.COMET_WALL_HANGING_SIGN.get(), new Properties().stacksTo(16))
   );
   public static final DeferredItem<BlockItem> WIRELESS_ENERGY_TRANSMITTER = registerBlockItem(
      "wireless_energy_transfer_device", ModBlocks.WIRELESS_ENERGY_TRANSMITTER
   );
   public static final DeferredItem<BlockItem> SATELLITE_RECEIVER = registerBlockItem("satellite_receiver", ModBlocks.SATELLITE_RECEIVER);
   public static final DeferredItem<BlockItem> CRYSTAL_LOGISTICS = registerBlockItem("wireless_logistics_transfer_device", ModBlocks.CRYSTAL_LOGISTICS);
   public static final DeferredItem<BlockItem> CRAFTING_PROCESSOR = registerBlockItem("crafting_processing_device", ModBlocks.CRAFTING_PROCESSOR);
   public static final DeferredItem<BlockItem> ANIMAL_BREEDER = registerBlockItem("automatic_animal_breeder", ModBlocks.ANIMAL_BREEDER);
   public static final DeferredItem<BlockItem> TERRAIN_PROCESSOR = registerBlockItem("terrain_processing_device", ModBlocks.TERRAIN_PROCESSOR);
   public static final DeferredItem<BlockItem> LAUNCH_SITE = registerBlockItem("rocket_launch_site", ModBlocks.LAUNCH_SITE);
   public static final DeferredItem<BlockItem> LAUNCH_PAD = registerBlockItem("launch_pad", ModBlocks.LAUNCH_PAD);
   public static final DeferredItem<BlockItem> LAUNCH_GANTRY_FRAME = registerBlockItem("launch_gantry_frame", ModBlocks.LAUNCH_GANTRY_FRAME);
   public static final DeferredItem<SatelliteRocketItem> SMALL_SATELLITE_ROCKET = ITEMS.register(
      "small_satellite_rocket", () -> new SatelliteRocketItem((Block)ModBlocks.SMALL_SATELLITE_ROCKET.get(), new Properties().stacksTo(16))
   );
   public static final DeferredItem<SatelliteRocketItem> MEDIUM_SATELLITE_ROCKET = ITEMS.register(
      "medium_satellite_rocket", () -> new SatelliteRocketItem((Block)ModBlocks.MEDIUM_SATELLITE_ROCKET.get(), new Properties().stacksTo(16))
   );
   public static final DeferredItem<SatelliteRocketItem> LARGE_SATELLITE_ROCKET = ITEMS.register(
      "large_satellite_rocket", () -> new SatelliteRocketItem((Block)ModBlocks.LARGE_SATELLITE_ROCKET.get(), new Properties().stacksTo(16).rarity(Rarity.RARE))
   );
   public static final DeferredItem<BlockItem> SATELLITE_CONTROL_DEVICE = registerBlockItem("satellite_control_device", ModBlocks.SATELLITE_CONTROL_DEVICE);
   public static final DeferredItem<Item> INV_ORE = ITEMS.register("inv_ore", () -> new Item(new Properties()));
   public static final DeferredItem<Item> TYCON_ORE = ITEMS.register("tycon_ore", () -> new Item(new Properties().rarity(Rarity.UNCOMMON)));
   public static final DeferredItem<Item> GREEN_IRON_ORE = ITEMS.register("green_iron_ore", () -> new Item(new Properties()));
   public static final DeferredItem<Item> INV_INGOT = ITEMS.register("inv_ingot", () -> new Item(new Properties()));
   public static final DeferredItem<Item> TYCON_INGOT = ITEMS.register("tycon_ingot", () -> new Item(new Properties().rarity(Rarity.UNCOMMON)));
   public static final DeferredItem<Item> GREEN_IRON_INGOT = ITEMS.register("green_iron_ingot", () -> new Item(new Properties()));
   public static final DeferredItem<Item> TYCON_PRESSURIZED_INGOT = ITEMS.register(
      "tycon_pressurized_ingot", () -> new Item(new Properties().rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> AP_STRUCTURAL_STEEL_PLATE = ITEMS.register(
      "ap_structural_steel_plate", () -> new Item(new Properties().rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<BlockItem> AP_STRUCTURAL_STEEL_BLOCK = registerBlockItem("ap_structural_steel_block", ModBlocks.AP_STRUCTURAL_STEEL_BLOCK);
   public static final DeferredItem<BlockItem> INV_BLOCK = registerBlockItem("inv_block", ModBlocks.INV_BLOCK);
   public static final DeferredItem<BlockItem> TYCON_BLOCK = registerBlockItem("tycon_block", ModBlocks.TYCON_BLOCK);
   public static final DeferredItem<BlockItem> GREEN_IRON_BLOCK = registerBlockItem("green_iron_block", ModBlocks.GREEN_IRON_BLOCK);
   public static final DeferredItem<BlockItem> AP_STEEL_CASING = registerBlockItem("ap_steel_casing", ModBlocks.AP_STEEL_CASING);
   public static final DeferredItem<BlockItem> AP_STEEL_FRAME = registerBlockItem("ap_steel_frame", ModBlocks.AP_STEEL_FRAME);
   public static final DeferredItem<BlockItem> AP_AGRICULTURE_CONTROLLER = registerBlockItem("ap_agricultural_complex", ModBlocks.AP_AGRICULTURE_CONTROLLER);
   public static final DeferredItem<BlockItem> AP_LIVESTOCK_CONTROLLER = registerBlockItem("ap_livestock_ranch", ModBlocks.AP_LIVESTOCK_CONTROLLER);

   private ModItems() {
   }

   private static DeferredItem<BlockItem> registerBlockItem(String var0, DeferredBlock<?> var1) {
      return ITEMS.register(var0, () -> new BlockItem((Block)var1.get(), new Properties()));
   }

   private static DeferredItem<Item> registerSpeedUpgrade(String var0) {
      return ITEMS.register(var0, () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE)));
   }

   private static DeferredItem<AnimalFeedBagItem> registerFeedBag(String var0, AnimalFeedBagItem.Mode var1) {
      return ITEMS.register(var0, () -> new AnimalFeedBagItem(baggedProperties(), var1));
   }

   private static Properties baggedProperties() {
      return new Properties().craftRemainder((Item)EMPTY_BAG.get());
   }

   static {
      ITEMS.addAlias(
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "infinite_logistics_transfer_device"),
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "wireless_logistics_transfer_device")
      );
      // Renamed: Gourd Modification Device -> Melon Modification Device; keeps existing worlds intact.
      ITEMS.addAlias(
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "gourd_modification_device"),
         ResourceLocation.fromNamespaceAndPath("industrialcrops", "melon_modification_device")
      );
   }
}
