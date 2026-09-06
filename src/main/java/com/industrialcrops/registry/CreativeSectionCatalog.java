package com.industrialcrops.registry;

import com.industrialcrops.Carrote;
import com.industrialcrops.CarroteCurios;
import com.industrialcrops.curios.CarroteCuriosItems;
import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.crop.CropGenetics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Supplier;

public final class CreativeSectionCatalog {
    public record Section(Component title, ResourceLocation banner, List<Supplier<ItemStack>> items) {
    }

    private CreativeSectionCatalog() {
    }

    /** Shared ordering for the combined tab, search and sectioned client view. */
    public static List<Section> combinedSections() {
        return List.of(
                section("itemGroup.industrialcrops.section.industrial_crops",
                        IndustrialCrops.MOD_ID, "industrial_crops",
                        // Crops, building blocks and agricultural supplies.
                        crop(ModItems.INDUSTRIAL_CARROT),
                        crop(ModItems.BAGGED_INDUSTRIAL_CARROT),
                        crop(ModItems.INDUSTRIAL_POTATO),
                        crop(ModItems.BAGGED_INDUSTRIAL_POTATO),
                        crop(ModItems.INDUSTRIAL_WHEAT),
                        crop(ModItems.BAGGED_INDUSTRIAL_WHEAT_SEEDS),
                        crop(ModItems.BAGGED_INDUSTRIAL_MELON_SEEDS),
                        crop(ModItems.BAGGED_INDUSTRIAL_PUMPKIN_SEEDS),
                        crop(ModItems.INDUSTRIAL_MELON),
                        crop(ModItems.INDUSTRIAL_PUMPKIN),
                        item(ModItems.FUSION_MELON),
                        crop(ModItems.PRISM_POD_SEEDS),
                        crop(ModItems.BAGGED_PRISM_POD_SEEDS),
                        crop(ModItems.PRISM_POD),
                        crop(ModItems.EMBERCOIL_SEEDS),
                        crop(ModItems.BAGGED_EMBERCOIL_SEEDS),
                        crop(ModItems.EMBERCOIL),
                        crop(ModItems.STARBLOOM_SEEDS),
                        crop(ModItems.BAGGED_STARBLOOM_SEEDS),
                        crop(ModItems.STARBLOOM),
                        crop(ModItems.NEONBULB_SEEDS),
                        crop(ModItems.BAGGED_NEONBULB_SEEDS),
                        crop(ModItems.NEONBULB),
                        crop(ModItems.FLUXSTALK_SEEDS),
                        crop(ModItems.BAGGED_FLUXSTALK_SEEDS),
                        crop(ModItems.FLUXSTALK),
                        item(ModItems.PLASMA_BERRY),
                        item(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET),
                        item(ModItems.EMPTY_BAG),
                        item(ModItems.FERTILIZER_FAST_GROWTH),
                        item(ModItems.FERTILIZER_FERTILE_SOIL),
                        item(ModItems.STRANGE_FERTILIZER),
                        item(ModItems.COMET_SOIL),
                        item(ModItems.COMET_SAPLING),
                        item(ModItems.COMET_FRUIT),
                        item(ModItems.COMET_LEAVES),
                        item(ModItems.COMET_LOG),
                        item(ModItems.COMET_WOOD),
                        item(ModItems.STRIPPED_COMET_LOG),
                        item(ModItems.STRIPPED_COMET_WOOD),
                        item(ModItems.COMET_PLANKS),
                        item(ModItems.COMET_STAIRS),
                        item(ModItems.COMET_SLAB),
                        item(ModItems.COMET_FENCE),
                        item(ModItems.COMET_FENCE_GATE),
                        item(ModItems.COMET_DOOR),
                        item(ModItems.COMET_TRAPDOOR),
                        item(ModItems.COMET_PRESSURE_PLATE),
                        item(ModItems.COMET_BUTTON),
                        item(ModItems.COMET_SIGN),
                        item(ModItems.COMET_HANGING_SIGN),
                        item(ModItems.FEED_BAG_BASIC),
                        item(ModItems.FEED_BAG_HEALING),
                        item(ModItems.FEED_BAG_GROWTH),
                        item(ModItems.FEED_BAG_FAST_BREEDING),
                        item(ModItems.FEED_BAG_RESISTANCE),
                        item(ModItems.REDSTONE_BONEMEAL),
                        item(ModItems.INDUSTRIAL_CARROT_BLOCK),
                        item(ModItems.INDUSTRIAL_POTATO_BLOCK),
                        item(ModItems.INDUSTRIAL_WHEAT_BLOCK),
                        item(ModItems.INDUSTRIAL_MELON_BLOCK),
                        item(ModItems.INDUSTRIAL_PUMPKIN_BLOCK),
                        item(ModItems.FERTILE_FARMLAND),
                        // Copper.
                        item(ModItems.COPPER_DEVICE_CASING),
                        item(ModItems.ROOT_ORE_EXTRACTOR),
                        item(ModItems.CROP_COMPRESSOR),
                        item(ModItems.MIXER),
                        item(ModItems.BASIC_MANIPULATOR),
                        item(ModItems.GOLDEN_LAUNCH_SILO),
                        item(ModItems.EXPLOSIVE_POTATO),
                        item(ModItems.COPPER_FLUID_STORAGE_CABINET),
                        item(ModItems.INDUSTRIAL_CROP_STORAGE_ARRAY),
                        item(ModItems.CARROT_CONTROL_DEVICE),
                        item(ModItems.PIPE),
                        item(ModItems.INPUT_PIPE),
                        item(ModItems.OUTPUT_PIPE),
                        item(ModItems.BASIC_ENERGY_CABLE),
                        item(ModItems.INDUSTRIAL_STORAGE_COMPONENT_1),
                        item(ModItems.SPEED_COMPONENT_1),
                        item(ModItems.BROWN_CREATE_SLIME_SPAWN_EGG),
                        // Iron.
                        item(ModItems.IRON_DEVICE_CASING),
                        item(ModItems.CROP_ANALYSIS_DEVICE),
                        item(ModItems.GOURD_MODIFICATION_DEVICE),
                        item(ModItems.INCUBATOR),
                        item(ModItems.SLIME_INCUBATOR),
                        item(ModItems.REINFORCED_INDUSTRIAL_STORAGE_ARRAY),
                        item(ModItems.REINFORCED_CONTROL_DEVICE),
                        item(ModItems.REINFORCED_PIPE),
                        item(ModItems.REINFORCED_INPUT_PIPE),
                        item(ModItems.REINFORCED_OUTPUT_PIPE),
                        item(ModItems.UPGRADE_KIT_SUBSTRATE),
                        item(ModItems.INDUSTRIAL_STORAGE_COMPONENT_2),
                        item(ModItems.SPEED_COMPONENT_2),
                        item(ModItems.GRAY_GEAR_SLIME_SPAWN_EGG),
                        // Gold.
                        item(ModItems.PROCESSOR_GOLD_DEVICE_CASING),
                        item(ModItems.ADVANCED_MANIPULATOR),
                        item(ModItems.AUTOMATIC_PLANTER),
                        item(ModItems.PROCESSOR_PROGRAMMER),
                        item(ModItems.MATTER_DIGITIZER),
                        item(ModItems.DIGITIZED_ITEM_COPIER),
                        item(ModItems.MATTER_RECONSTRUCTOR),
                        item(ModItems.BIO_ENERGY_GENERATOR),
                        item(ModItems.RESIDUE_INCINERATOR),
                        item(ModItems.ELECTRIC_FURNACE),
                        item(ModItems.ENERGY_BATTERY),
                        item(ModItems.WIRELESS_ENERGY_TRANSMITTER),
                        item(ModItems.ADVANCED_ENERGY_CABLE),
                        item(ModItems.GOLD_PLASMA_EXTRACTOR),
                        item(ModItems.GOLD_FLUID_PIPE),
                        item(ModItems.DIGITAL_MINIATURE_FOREST),
                        item(ModItems.ADVANCED_INDUSTRIAL_STORAGE_DEVICE),
                        item(ModItems.ITEM_NETWORK_TERMINAL),
                        item(ModItems.REMOTE_ACCESS_DEVICE),
                        item(ModItems.ADVANCED_PIPE),
                        item(ModItems.ADVANCED_INPUT_PIPE),
                        item(ModItems.ADVANCED_OUTPUT_PIPE),
                        item(ModItems.PIPE_SORTER),
                        item(ModItems.COMPONENT_SUBSTRATE),
                        item(ModItems.GUIDANCE_COMPONENT),
                        item(ModItems.RAPID_FIRE_COMPONENT),
                        item(ModItems.POWER_COMPONENT),
                        item(ModItems.AUTOMATIC_COMPONENT),
                        item(ModItems.GOLD_UPGRADE_KIT),
                        item(ModItems.INDUSTRIAL_STORAGE_COMPONENT_3),
                        item(ModItems.SPEED_COMPONENT_3),
                        item(ModItems.DIMENSION_UPGRADE_COMPONENT),
                        item(ModItems.GOLDEN_REDSTONE_LAMP_SLIME_SPAWN_EGG),
                        item(ModItems.BLUE_COMPONENT_SUBSTRATE_SLIME_SPAWN_EGG),
                        // Crystal steel.
                        item(ModItems.CRYSTAL_STEEL_DEVICE_CASING),
                        item(ModItems.CRYSTAL_STEEL_WORKBENCH),
                        item(ModItems.ENERGY_CRYSTAL),
                        item(ModItems.CRYSTAL_INGOT),
                        item(ModItems.FUSION_INGOT),
                        item(ModItems.FUSION_INGOT_PICKAXE),
                        item(ModItems.INDUSTRIAL_STORAGE_COMPONENT_4),
                        item(ModItems.SPEED_COMPONENT_4),
                        item(ModItems.INFINITE_DIMENSION_UPGRADE_COMPONENT)),

                section("itemGroup.industrialcrops.section.carrote",
                        Carrote.MOD_ID, "carrote",
                        // Carrote steel.
                        item(CarroteItems.CARROTE_STEEL_DEVICE_CASING),
                        item(CarroteItems.CARROTE),
                        item(CarroteItems.CARROTE_STEEL_INGOT),
                        item(CarroteItems.CARROTE_STEEL_BLOCK),
                        item(CarroteItems.CARROTE_STEEL_FORGE),
                        item(CarroteItems.MATERIAL_HARDENING_DEVICE),
                        item(CarroteItems.UNIVERSAL_REPLICATION_DEVICE),
                        // Stable matter.
                        item(CarroteItems.STABLE_MATTER_INGOT),
                        item(CarroteItems.STABLE_MATTER_BLOCK),
                        item(CarroteItems.MIMIC_BLOCK)),

                section("itemGroup.carrote_curios.section",
                        CarroteCurios.MOD_ID, "carrote_curios",
                        // Normal.
                        item(CarroteCuriosItems.STRENGTH),
                        item(CarroteCuriosItems.AIRBORNE),
                        item(CarroteCuriosItems.LUCK),
                        item(CarroteCuriosItems.SMELTING),
                        item(CarroteCuriosItems.TASTY),
                        item(CarroteCuriosItems.PEACE),
                        item(CarroteCuriosItems.NIGHT_VISION),
                        // Good.
                        item(CarroteCuriosItems.ENCHANTING),
                        item(CarroteCuriosItems.ARCANE),
                        item(CarroteCuriosItems.STEEL),
                        item(CarroteCuriosItems.FALL),
                        item(CarroteCuriosItems.SUBSTITUTE),
                        // Excellent.
                        item(CarroteCuriosItems.GREED),
                        item(CarroteCuriosItems.FLIGHT),
                        item(CarroteCuriosItems.POWER),
                        item(CarroteCuriosItems.BLAST),
                        // Fine.
                        item(CarroteCuriosItems.HELMET))

        );
    }

    public static void acceptFlat(List<Section> sections, CreativeModeTab.Output output) {
        for (Section section : sections) {
            for (Supplier<ItemStack> item : section.items()) {
                // A removed/optional registry entry may resolve to air while
                // an existing world is being upgraded. Never feed an empty
                // stack into the creative tab (JEI treats that as an error).
                ItemStack stack = item.get();
                if (!stack.isEmpty()) {
                    output.accept(stack);
                }
            }
        }
    }

    @SafeVarargs
    private static Section section(String title, String namespace, String banner, Supplier<ItemStack>... items) {
        return new Section(Component.translatable(title),
                ResourceLocation.fromNamespaceAndPath(namespace, "creative_sections/" + banner),
                List.of(items));
    }

    private static Supplier<ItemStack> item(Supplier<? extends ItemLike> item) {
        return () -> new ItemStack(item.get());
    }

    private static Supplier<ItemStack> crop(Supplier<? extends ItemLike> item) {
        return () -> CropGenetics.createCreativeTemplate(new ItemStack(item.get()), RandomSource.create());
    }
}
