package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.item.IncubatorBlockItem;
import com.industrialcrops.item.AnimalFeedBagItem;
import com.industrialcrops.item.BaggedCropItem;
import com.industrialcrops.item.FertilizerBagItem;
import com.industrialcrops.item.SeedBagItem;
import com.industrialcrops.item.AdvancedManipulatorUpgradeItem;
import com.industrialcrops.item.IndustrialStorageComponentItem;
import com.industrialcrops.item.RemoteAccessDeviceItem;
import com.industrialcrops.item.GeneticCropItem;
import com.industrialcrops.item.GeneticCropBlockItem;
import com.industrialcrops.item.GeneticCropProductBlockItem;
import com.industrialcrops.item.FusionMelonBlockItem;
import com.industrialcrops.item.FusionIngotPickaxeItem;
import com.industrialcrops.item.ShimmeringItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IndustrialCrops.MOD_ID);

    public static final DeferredItem<SeedBagItem> EMPTY_BAG = ITEMS.register("empty_bag",
            () -> new SeedBagItem(new Item.Properties()));
    public static final DeferredItem<GeneticCropBlockItem> INDUSTRIAL_CARROT = ITEMS.register("industrial_carrot",
            () -> new GeneticCropBlockItem(ModBlocks.INDUSTRIAL_CARROT_CROP.get(), new Item.Properties()));
    public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_CARROT = ITEMS.register("bagged_industrial_carrot",
            () -> new BaggedCropItem(ModBlocks.INDUSTRIAL_CARROT_CROP.get(), baggedProperties()));
    public static final DeferredItem<GeneticCropBlockItem> INDUSTRIAL_POTATO = ITEMS.register("industrial_potato",
            () -> new GeneticCropBlockItem(ModBlocks.INDUSTRIAL_POTATO_CROP.get(), new Item.Properties()));
    public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_POTATO = ITEMS.register("bagged_industrial_potato",
            () -> new BaggedCropItem(ModBlocks.INDUSTRIAL_POTATO_CROP.get(), baggedProperties()));
    public static final DeferredItem<GeneticCropItem> INDUSTRIAL_WHEAT = ITEMS.register("industrial_wheat",
            () -> new GeneticCropItem(new Item.Properties()));
    public static final DeferredItem<FertilizerBagItem> FERTILIZER_FAST_GROWTH = ITEMS.register("fertilizer_fast_growth",
            () -> new FertilizerBagItem(baggedProperties(), FertilizerBagItem.Mode.FAST_GROWTH));
    public static final DeferredItem<FertilizerBagItem> FERTILIZER_FERTILE_SOIL = ITEMS.register("fertilizer_fertile_soil",
            () -> new FertilizerBagItem(baggedProperties(), FertilizerBagItem.Mode.FERTILE_SOIL));
    public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_BASIC = registerFeedBag("feed_bag_basic", AnimalFeedBagItem.Mode.BASIC);
    public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_HEALING = registerFeedBag("feed_bag_healing", AnimalFeedBagItem.Mode.HEALING);
    public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_GROWTH = registerFeedBag("feed_bag_growth", AnimalFeedBagItem.Mode.GROWTH);
    public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_FAST_BREEDING = registerFeedBag("feed_bag_fast_breeding", AnimalFeedBagItem.Mode.FAST_BREEDING);
    public static final DeferredItem<AnimalFeedBagItem> FEED_BAG_RESISTANCE = registerFeedBag("feed_bag_resistance", AnimalFeedBagItem.Mode.RESISTANCE);
    public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_WHEAT_SEEDS = ITEMS.register("bagged_industrial_wheat_seeds",
            () -> new BaggedCropItem(ModBlocks.INDUSTRIAL_WHEAT_CROP.get(), baggedProperties()));
    public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_MELON_SEEDS = ITEMS.register("bagged_industrial_melon_seeds",
            () -> new BaggedCropItem(ModBlocks.INDUSTRIAL_MELON_CROP.get(), baggedProperties()));
    public static final DeferredItem<BaggedCropItem> BAGGED_INDUSTRIAL_PUMPKIN_SEEDS = ITEMS.register("bagged_industrial_pumpkin_seeds",
            () -> new BaggedCropItem(ModBlocks.INDUSTRIAL_PUMPKIN_CROP.get(), baggedProperties()));
    public static final DeferredItem<GeneticCropProductBlockItem> INDUSTRIAL_MELON = ITEMS.register("industrial_melon",
            () -> new GeneticCropProductBlockItem(ModBlocks.INDUSTRIAL_MELON_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<GeneticCropProductBlockItem> INDUSTRIAL_PUMPKIN = ITEMS.register("industrial_pumpkin",
            () -> new GeneticCropProductBlockItem(ModBlocks.INDUSTRIAL_PUMPKIN_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<FusionMelonBlockItem> FUSION_MELON = ITEMS.register("fusion_melon",
            () -> new FusionMelonBlockItem(ModBlocks.FUSION_MELON.get(),
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final DeferredItem<ShimmeringItem> FUSION_INGOT = ITEMS.register("fusion_ingot",
            () -> new ShimmeringItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final DeferredItem<FusionIngotPickaxeItem> FUSION_INGOT_PICKAXE = ITEMS.register("fusion_ingot_pickaxe",
            () -> new FusionIngotPickaxeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final DeferredItem<Item> REDSTONE_BONEMEAL = ITEMS.register("redstone_bonemeal",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COMPONENT_SUBSTRATE = ITEMS.register("component_substrate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_1 = ITEMS.register("industrial_storage_component_1",
            () -> new IndustrialStorageComponentItem(new Item.Properties().stacksTo(16), 1));
    public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_2 = ITEMS.register("industrial_storage_component_2",
            () -> new IndustrialStorageComponentItem(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON), 2));
    public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_3 = ITEMS.register("industrial_storage_component_3",
            () -> new IndustrialStorageComponentItem(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE), 4));
    public static final DeferredItem<IndustrialStorageComponentItem> INDUSTRIAL_STORAGE_COMPONENT_4 = ITEMS.register("industrial_storage_component_4",
            () -> new IndustrialStorageComponentItem(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC), 16));
    public static final DeferredItem<Item> GUIDANCE_COMPONENT = ITEMS.register("guidance_component",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> EXPLOSIVE_POTATO = ITEMS.register("explosive_potato",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<Item> RAPID_FIRE_COMPONENT = ITEMS.register("rapid_fire_component",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<Item> POWER_COMPONENT = ITEMS.register("power_component",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<Item> UPGRADE_KIT_SUBSTRATE = ITEMS.register("upgrade_kit_substrate",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GOLD_UPGRADE_KIT = ITEMS.register("gold_upgrade_kit",
            () -> new AdvancedManipulatorUpgradeItem(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<Item> SPEED_COMPONENT_1 = registerSpeedUpgrade("speed_component_1");
    public static final DeferredItem<Item> SPEED_COMPONENT_2 = registerSpeedUpgrade("speed_component_2");
    public static final DeferredItem<Item> SPEED_COMPONENT_3 = registerSpeedUpgrade("speed_component_3");
    public static final DeferredItem<Item> SPEED_COMPONENT_4 = registerSpeedUpgrade("speed_component_4");
    public static final DeferredItem<DeferredSpawnEggItem> BROWN_CREATE_SLIME_SPAWN_EGG =
            ITEMS.register("copper_gear_slime_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.BROWN_CREATE_SLIME,
                    0xB65B32,
                    0xD08A52,
                    new Item.Properties()
            ));
    public static final DeferredItem<DeferredSpawnEggItem> GRAY_GEAR_SLIME_SPAWN_EGG =
            ITEMS.register("gray_gear_slime_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.GRAY_GEAR_SLIME,
                    0x53585C,
                    0xB9A064,
                    new Item.Properties()
            ));
    public static final DeferredItem<DeferredSpawnEggItem> GOLDEN_REDSTONE_LAMP_SLIME_SPAWN_EGG =
            ITEMS.register("golden_redstone_lamp_slime_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.GOLDEN_REDSTONE_LAMP_SLIME,
                    0xE7A517,
                    0xFF5A00,
                    new Item.Properties()
            ));
    public static final DeferredItem<DeferredSpawnEggItem> BLUE_COMPONENT_SUBSTRATE_SLIME_SPAWN_EGG =
            ITEMS.register("blue_component_substrate_slime_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.DIAMOND_PROCESSOR_SLIME,
                    0x174A9C,
                    0x75A7FF,
                    new Item.Properties()
            ));

    public static final DeferredItem<BlockItem> INDUSTRIAL_CARROT_BLOCK = registerBlockItem("industrial_carrot_block", ModBlocks.INDUSTRIAL_CARROT_BLOCK);
    public static final DeferredItem<BlockItem> INDUSTRIAL_POTATO_BLOCK = registerBlockItem("industrial_potato_block", ModBlocks.INDUSTRIAL_POTATO_BLOCK);
    public static final DeferredItem<BlockItem> INDUSTRIAL_WHEAT_BLOCK = registerBlockItem("industrial_wheat_block", ModBlocks.INDUSTRIAL_WHEAT_BLOCK);
    public static final DeferredItem<BlockItem> INDUSTRIAL_MELON_BLOCK = registerBlockItem("industrial_melon_block", ModBlocks.INDUSTRIAL_MELON_BLOCK);
    public static final DeferredItem<BlockItem> INDUSTRIAL_PUMPKIN_BLOCK = registerBlockItem("industrial_pumpkin_block", ModBlocks.INDUSTRIAL_PUMPKIN_BLOCK);
    public static final DeferredItem<BlockItem> FERTILE_FARMLAND = registerBlockItem("fertile_farmland", ModBlocks.FERTILE_FARMLAND);
    public static final DeferredItem<BlockItem> COPPER_DEVICE_CASING = registerBlockItem("copper_device_casing", ModBlocks.COPPER_DEVICE_CASING);
    public static final DeferredItem<BlockItem> IRON_DEVICE_CASING = registerBlockItem("iron_device_casing", ModBlocks.IRON_DEVICE_CASING);
    public static final DeferredItem<BlockItem> PROCESSOR_GOLD_DEVICE_CASING = registerBlockItem("processor_integrated_gold_device_casing", ModBlocks.PROCESSOR_GOLD_DEVICE_CASING);
    public static final DeferredItem<BlockItem> ROOT_ORE_EXTRACTOR = registerBlockItem("basic_crop_conversion_device", ModBlocks.ROOT_ORE_EXTRACTOR);
    public static final DeferredItem<BlockItem> CROP_COMPRESSOR = registerBlockItem("crop_compressor", ModBlocks.CROP_COMPRESSOR);
    public static final DeferredItem<BlockItem> CROP_ANALYSIS_DEVICE =
            registerBlockItem("crop_analysis_device", ModBlocks.CROP_ANALYSIS_DEVICE);
    public static final DeferredItem<BlockItem> GOURD_MODIFICATION_DEVICE =
            registerBlockItem("gourd_modification_device", ModBlocks.GOURD_MODIFICATION_DEVICE);
    public static final DeferredItem<BlockItem> MIXER = registerBlockItem("mixer", ModBlocks.MIXER);
    public static final DeferredItem<BlockItem> INDUSTRIAL_CROP_STORAGE_ARRAY = registerBlockItem("basic_storage_component", ModBlocks.INDUSTRIAL_CROP_STORAGE_ARRAY);
    public static final DeferredItem<BlockItem> CARROT_CONTROL_DEVICE = registerBlockItem("basic_control_device", ModBlocks.CARROT_CONTROL_DEVICE);
    public static final DeferredItem<BlockItem> BASIC_MANIPULATOR = registerBlockItem("basic_manipulation_device", ModBlocks.BASIC_MANIPULATOR);
    public static final DeferredItem<BlockItem> ADVANCED_MANIPULATOR = registerBlockItem("advanced_manipulation_device", ModBlocks.ADVANCED_MANIPULATOR);
    public static final DeferredItem<BlockItem> PROCESSOR_PROGRAMMER = registerBlockItem("processor_programming_device", ModBlocks.PROCESSOR_PROGRAMMER);
    public static final DeferredItem<BlockItem> MATTER_DIGITIZER = registerBlockItem("matter_digitization_device", ModBlocks.MATTER_DIGITIZER);
    public static final DeferredItem<BlockItem> DIGITIZED_ITEM_COPIER = registerBlockItem("digitized_item_copier", ModBlocks.DIGITIZED_ITEM_COPIER);
    public static final DeferredItem<BlockItem> MATTER_RECONSTRUCTOR = registerBlockItem("matter_reconstruction_device", ModBlocks.MATTER_RECONSTRUCTOR);
    public static final DeferredItem<BlockItem> ITEM_NETWORK_TERMINAL = registerBlockItem("item_network_management_terminal", ModBlocks.ITEM_NETWORK_TERMINAL);
    public static final DeferredItem<BlockItem> BIO_ENERGY_GENERATOR = registerBlockItem("bio_energy_generator", ModBlocks.BIO_ENERGY_GENERATOR);
    public static final DeferredItem<BlockItem> ENERGY_BATTERY = registerBlockItem("energy_battery", ModBlocks.ENERGY_BATTERY);
    public static final DeferredItem<BlockItem> RESIDUE_INCINERATOR = registerBlockItem("residue_incinerator", ModBlocks.RESIDUE_INCINERATOR);
    public static final DeferredItem<BlockItem> ELECTRIC_FURNACE = registerBlockItem("electric_furnace", ModBlocks.ELECTRIC_FURNACE);
    public static final DeferredItem<BlockItem> DIGITAL_MINIATURE_FOREST = registerBlockItem("digital_miniature_forest", ModBlocks.DIGITAL_MINIATURE_FOREST);
    public static final DeferredItem<BlockItem> BASIC_ENERGY_CABLE = registerBlockItem("basic_energy_cable", ModBlocks.BASIC_ENERGY_CABLE);
    public static final DeferredItem<BlockItem> ADVANCED_ENERGY_CABLE = registerBlockItem("advanced_energy_cable", ModBlocks.ADVANCED_ENERGY_CABLE);
    public static final DeferredItem<BlockItem> REINFORCED_INDUSTRIAL_STORAGE_ARRAY = registerBlockItem("reinforced_storage_component", ModBlocks.REINFORCED_INDUSTRIAL_STORAGE_ARRAY);
    public static final DeferredItem<BlockItem> REINFORCED_CONTROL_DEVICE = registerBlockItem("reinforced_control_device", ModBlocks.REINFORCED_CONTROL_DEVICE);
    public static final DeferredItem<RemoteAccessDeviceItem> REMOTE_ACCESS_DEVICE = ITEMS.register("remote_access_device",
            () -> new RemoteAccessDeviceItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<BlockItem> ADVANCED_INDUSTRIAL_STORAGE_DEVICE = registerBlockItem("advanced_industrial_storage_device", ModBlocks.ADVANCED_INDUSTRIAL_STORAGE_DEVICE);
    public static final DeferredItem<IncubatorBlockItem> INCUBATOR = ITEMS.register("slime_converter",
            () -> new IncubatorBlockItem(ModBlocks.INCUBATOR.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BlockItem> SLIME_INCUBATOR = registerBlockItem("slime_incubator", ModBlocks.SLIME_INCUBATOR);
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

    private ModItems() {
    }

    private static DeferredItem<BlockItem> registerBlockItem(String id, net.neoforged.neoforge.registries.DeferredBlock<?> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static DeferredItem<Item> registerSpeedUpgrade(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties().stacksTo(1)
                .rarity(net.minecraft.world.item.Rarity.RARE)));
    }

    private static DeferredItem<AnimalFeedBagItem> registerFeedBag(String id, AnimalFeedBagItem.Mode mode) {
        return ITEMS.register(id, () -> new AnimalFeedBagItem(baggedProperties(), mode));
    }

    private static Item.Properties baggedProperties() {
        return new Item.Properties().craftRemainder(EMPTY_BAG.get());
    }
}
