package com.industrialcrops.recipe;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ManipulatorRecipes {
    public static final TagKey<Item> STRIPPED_LOGS = TagKey.create(Registries.ITEM, new ResourceLocation(IndustrialCrops.MOD_ID, "stripped_logs"));
    private static final List<ManipulatorRecipeDisplay> BASIC_RECIPES = List.of(
            reinforcedControlDevice(),
            reinforcedPipe(),
            reinforcedInputPipe(),
            reinforcedOutputPipe()
    );
    private static final List<ManipulatorRecipeDisplay> ADVANCED_RECIPES = List.of(
            advancedPipe(), advancedInputPipe(), advancedOutputPipe(),
            matterDigitizer(), digitizedItemCopier(), matterReconstructor(), itemNetworkTerminal()
    );
    private static final List<ManipulatorRecipeDisplay> ALL_RECIPES = java.util.stream.Stream
            .concat(BASIC_RECIPES.stream(), ADVANCED_RECIPES.stream()).toList();

    private ManipulatorRecipes() {
    }

    public static ManipulatorRecipeDisplay ironDeviceCasing() {
        return new ManipulatorRecipeDisplay(
                List.of(
                        ManipulatorIngredient.ofItem(Items.IRON_INGOT, 10),
                        ManipulatorIngredient.ofOptions(getStrippedLogOptions(), new ItemStack(Items.STRIPPED_BIRCH_LOG), 1)
                ),
                new ItemStack(ModBlocks.IRON_DEVICE_CASING.get().asItem())
        );
    }

    public static ManipulatorRecipeDisplay reinforcedIndustrialStorageArray() {
        return new ManipulatorRecipeDisplay(
                List.of(
                        ManipulatorIngredient.ofItem(ModBlocks.IRON_DEVICE_CASING.get().asItem(), 1),
                        ManipulatorIngredient.ofItem(ModBlocks.INDUSTRIAL_CROP_STORAGE_ARRAY.get().asItem(), 1),
                        ManipulatorIngredient.ofItem(Items.IRON_INGOT, 5),
                        ManipulatorIngredient.ofOptions(getCopperChestOptions(), getCopperChestDisplayStack(), 3)
                ),
                new ItemStack(ModBlocks.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get().asItem())
        );
    }

    public static ManipulatorRecipeDisplay reinforcedControlDevice() {
        return new ManipulatorRecipeDisplay(
                List.of(
                        ManipulatorIngredient.ofItem(ModBlocks.IRON_DEVICE_CASING.get().asItem(), 1),
                        ManipulatorIngredient.ofItem(ModBlocks.CARROT_CONTROL_DEVICE.get().asItem(), 1),
                        ManipulatorIngredient.ofItem(Items.IRON_INGOT, 5),
                        ManipulatorIngredient.ofOptions(getCopperChestOptions(), getCopperChestDisplayStack(), 3)
                ),
                new ItemStack(ModBlocks.REINFORCED_CONTROL_DEVICE.get().asItem())
        );
    }

    public static ManipulatorRecipeDisplay reinforcedPipe() {
        return new ManipulatorRecipeDisplay(
                List.of(ManipulatorIngredient.ofItem(Items.IRON_INGOT, 16), ManipulatorIngredient.ofItem(ModBlocks.PIPE.get().asItem(), 4)),
                new ItemStack(ModBlocks.REINFORCED_PIPE.get().asItem(), 4)
        );
    }

    public static ManipulatorRecipeDisplay reinforcedInputPipe() {
        return new ManipulatorRecipeDisplay(
                List.of(ManipulatorIngredient.ofItem(Items.IRON_INGOT, 16), ManipulatorIngredient.ofItem(ModBlocks.INPUT_PIPE.get().asItem(), 4)),
                new ItemStack(ModBlocks.REINFORCED_INPUT_PIPE.get().asItem(), 4)
        );
    }

    public static ManipulatorRecipeDisplay reinforcedOutputPipe() {
        return new ManipulatorRecipeDisplay(
                List.of(ManipulatorIngredient.ofItem(Items.IRON_INGOT, 16), ManipulatorIngredient.ofItem(ModBlocks.OUTPUT_PIPE.get().asItem(), 4)),
                new ItemStack(ModBlocks.REINFORCED_OUTPUT_PIPE.get().asItem(), 4)
        );
    }

    public static ManipulatorRecipeDisplay advancedPipe() {
        return advancedPipeRecipe(ModBlocks.REINFORCED_PIPE.get().asItem(), ModBlocks.ADVANCED_PIPE.get().asItem());
    }

    public static ManipulatorRecipeDisplay advancedInputPipe() {
        return advancedPipeRecipe(ModBlocks.REINFORCED_INPUT_PIPE.get().asItem(), ModBlocks.ADVANCED_INPUT_PIPE.get().asItem());
    }

    public static ManipulatorRecipeDisplay advancedOutputPipe() {
        return advancedPipeRecipe(ModBlocks.REINFORCED_OUTPUT_PIPE.get().asItem(), ModBlocks.ADVANCED_OUTPUT_PIPE.get().asItem());
    }

    private static ManipulatorRecipeDisplay advancedPipeRecipe(Item reinforcedPipe, Item advancedPipe) {
        return new ManipulatorRecipeDisplay(
                List.of(
                        ManipulatorIngredient.ofItem(Items.GOLD_INGOT, 4),
                        ManipulatorIngredient.ofItem(reinforcedPipe, 4)
                ),
                new ItemStack(advancedPipe, 4)
        );
    }

    public static List<ManipulatorRecipeDisplay> all() {
        return ALL_RECIPES;
    }

    public static List<ManipulatorRecipeDisplay> forAdvanced(boolean advanced) {
        return advanced ? ALL_RECIPES : BASIC_RECIPES;
    }

    public static ManipulatorRecipeDisplay matterDigitizer() {
        return new ManipulatorRecipeDisplay(List.of(
                ManipulatorIngredient.ofItem(ModBlocks.PROCESSOR_GOLD_DEVICE_CASING.get().asItem(), 1),
                ManipulatorIngredient.ofItem(com.industrialcrops.registry.ModItems.COMPONENT_SUBSTRATE.get(), 2),
                ManipulatorIngredient.ofItem(Items.REDSTONE, 8),
                ManipulatorIngredient.ofItem(Items.GLASS, 4)),
                new ItemStack(ModBlocks.MATTER_DIGITIZER.get().asItem()));
    }

    public static ManipulatorRecipeDisplay digitizedItemCopier() {
        return new ManipulatorRecipeDisplay(List.of(
                ManipulatorIngredient.ofItem(ModBlocks.PROCESSOR_GOLD_DEVICE_CASING.get().asItem(), 1),
                ManipulatorIngredient.ofItem(com.industrialcrops.registry.ModItems.COMPONENT_SUBSTRATE.get(), 4),
                ManipulatorIngredient.ofItem(Items.DIAMOND, 4),
                ManipulatorIngredient.ofItem(com.industrialcrops.registry.ModItems.INDUSTRIAL_STORAGE_COMPONENT_1.get(), 1)),
                new ItemStack(ModBlocks.DIGITIZED_ITEM_COPIER.get().asItem()));
    }

    public static ManipulatorRecipeDisplay matterReconstructor() {
        return new ManipulatorRecipeDisplay(List.of(
                ManipulatorIngredient.ofItem(ModBlocks.PROCESSOR_GOLD_DEVICE_CASING.get().asItem(), 1),
                ManipulatorIngredient.ofItem(com.industrialcrops.registry.ModItems.COMPONENT_SUBSTRATE.get(), 4),
                ManipulatorIngredient.ofItem(Items.DIAMOND, 4),
                ManipulatorIngredient.ofItem(Items.REDSTONE_BLOCK, 2)),
                new ItemStack(ModBlocks.MATTER_RECONSTRUCTOR.get().asItem()));
    }

    public static ManipulatorRecipeDisplay itemNetworkTerminal() {
        return new ManipulatorRecipeDisplay(List.of(
                ManipulatorIngredient.ofItem(ModBlocks.PROCESSOR_GOLD_DEVICE_CASING.get().asItem(), 1),
                ManipulatorIngredient.ofItem(com.industrialcrops.registry.ModItems.COMPONENT_SUBSTRATE.get(), 4),
                ManipulatorIngredient.ofItem(com.industrialcrops.registry.ModItems.INDUSTRIAL_STORAGE_COMPONENT_1.get(), 2),
                ManipulatorIngredient.ofItem(Items.ENDER_PEARL, 4)),
                new ItemStack(ModBlocks.ITEM_NETWORK_TERMINAL.get().asItem()));
    }

    public static List<ItemStack> getStrippedLogOptions() {
        return List.of(
                new ItemStack(Items.STRIPPED_OAK_LOG),
                new ItemStack(Items.STRIPPED_SPRUCE_LOG),
                new ItemStack(Items.STRIPPED_BIRCH_LOG),
                new ItemStack(Items.STRIPPED_JUNGLE_LOG),
                new ItemStack(Items.STRIPPED_ACACIA_LOG),
                new ItemStack(Items.STRIPPED_DARK_OAK_LOG),
                new ItemStack(Items.STRIPPED_MANGROVE_LOG),
                new ItemStack(Items.STRIPPED_CHERRY_LOG),
                new ItemStack(Items.STRIPPED_CRIMSON_STEM),
                new ItemStack(Items.STRIPPED_WARPED_STEM)
        );
    }

    public static List<ItemStack> getCopperChestOptions() {
        return List.of(
                optionalItem("minecraft:copper_chest"),
                optionalItem("create:copper_chest"),
                optionalItem("ironchests:copper_chest"),
                optionalItem("ironchest:copper_chest"),
                optionalItem("reinfchest:copper_chest")
        ).stream().filter(stack -> !stack.isEmpty()).toList();
    }

    private static ItemStack getCopperChestDisplayStack() {
        List<ItemStack> options = getCopperChestOptions();
        return options.isEmpty() ? new ItemStack(Items.CHEST) : options.get(0).copy();
    }

    private static ItemStack optionalItem(String id) {
        ResourceLocation location = new ResourceLocation(id);
        return BuiltInRegistries.ITEM.getOptional(location).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
