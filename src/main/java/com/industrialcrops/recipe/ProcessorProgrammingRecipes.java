package com.industrialcrops.recipe;

import com.industrialcrops.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ProcessorProgrammingRecipes {
    private static final List<ManipulatorRecipeDisplay> RECIPES = List.of(
            recipe(ModItems.COMPONENT_SUBSTRATE.get(),
                    ingredient(Items.COMPARATOR, 2),
                    ingredient(Items.REDSTONE_TORCH, 1),
                    ingredient(Items.GOLD_INGOT, 1)),
            recipe(ModItems.UPGRADE_KIT_SUBSTRATE.get(),
                    ingredient(Items.COMPARATOR, 2),
                    ingredient(Items.REDSTONE_TORCH, 1),
                    ingredient(Items.IRON_INGOT, 1)),
            recipe(ModItems.GOLD_UPGRADE_KIT.get(),
                    ingredient(ModItems.UPGRADE_KIT_SUBSTRATE.get(), 1),
                    ingredient(Items.GOLD_INGOT, 5)),
            recipe(ModItems.CRYSTAL_STEEL_UPGRADE_KIT.get(),
                    ingredient(ModItems.UPGRADE_KIT_SUBSTRATE.get(), 1),
                    ingredient(ModItems.CRYSTAL_INGOT.get(), 5)),
            recipe(ModItems.INDUSTRIAL_CROP_STORAGE_ARRAY.get(),
                    ingredient(ModItems.COPPER_DEVICE_CASING.get(), 1),
                    ingredient(Items.COPPER_INGOT, 6),
                    ingredient(Items.HOPPER, 1),
                    ingredient(Items.CHEST, 1)),
            reinforcedStorageComponent(),
            recipe(ModItems.STORAGE_COMPONENT_10K.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 4),
                    ingredient(ModItems.TYCON_PRESSURIZED_INGOT.get(), 4),
                    ingredient(ModItems.COMPRESSED_POTATO_10K.get(), 1)),
            recipe(ModItems.GUIDANCE_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.GOLD_INGOT, 4),
                    ingredient(Items.REDSTONE, 4),
                    ingredient(Items.COMPASS, 1)),
            recipe(ModItems.INDUSTRIAL_STORAGE_COMPONENT_1.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.CHEST, 1),
                    ingredient(Items.COPPER_INGOT, 1)),
            recipe(ModItems.INDUSTRIAL_STORAGE_COMPONENT_2.get(),
                    ingredient(ModItems.INDUSTRIAL_STORAGE_COMPONENT_1.get(), 1),
                    ingredient(Items.CHEST, 2),
                    ingredient(Items.IRON_INGOT, 1)),
            recipe(ModItems.INDUSTRIAL_STORAGE_COMPONENT_3.get(),
                    ingredient(ModItems.INDUSTRIAL_STORAGE_COMPONENT_2.get(), 1),
                    ingredient(Items.CHEST, 4),
                    ingredient(Items.GOLD_INGOT, 1)),
            recipe(ModItems.INDUSTRIAL_STORAGE_COMPONENT_4.get(),
                    ingredient(ModItems.INDUSTRIAL_STORAGE_COMPONENT_3.get(), 1),
                    ingredient(Items.CHEST, 16),
                    ingredient(Items.DIAMOND, 1),
                    ingredient(Items.REDSTONE, 2)),
            recipe(ModItems.RAPID_FIRE_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.COMPARATOR, 2),
                    ingredient(Items.GOLD_INGOT, 2),
                    ingredient(Items.REDSTONE, 4)),
            recipe(ModItems.POWER_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.TNT, 4),
                    ingredient(Items.GOLD_INGOT, 2),
                    ingredient(Items.REDSTONE, 4)),
            recipe(ModItems.AUTOMATIC_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.COMPARATOR, 3),
                    ingredient(Items.REDSTONE, 4)),
            recipe(ModItems.MINING_UPGRADE_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(ModItems.CRYSTAL_INGOT.get(), 4),
                    ingredient(ModItems.AUTOMATIC_COMPONENT.get(), 1),
                    ingredient(Items.DIAMOND, 2),
                    ingredient(Items.NETHERITE_PICKAXE, 1)),
            recipe(ModItems.DIMENSION_UPGRADE_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.ENDER_PEARL, 4),
                    ingredient(Items.COMPASS, 1),
                    ingredient(Items.OBSIDIAN, 4)),
            recipe(ModItems.INFINITE_DIMENSION_UPGRADE_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.ENDER_EYE, 4),
                    ingredient(Items.NETHER_STAR, 1),
                    ingredient(Items.CHORUS_FRUIT, 4)),
            recipe(ModItems.SPEED_COMPONENT_1.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.REDSTONE, 4),
                    ingredient(Items.QUARTZ, 2),
                    ingredient(Items.GOLD_INGOT, 2)),
            recipe(ModItems.SPEED_COMPONENT_2.get(),
                    ingredient(ModItems.SPEED_COMPONENT_1.get(), 1),
                    ingredient(Items.GLOWSTONE_DUST, 2),
                    ingredient(Items.REDSTONE, 2)),
            recipe(ModItems.SPEED_COMPONENT_3.get(),
                    ingredient(ModItems.SPEED_COMPONENT_2.get(), 2),
                    ingredient(Items.GLOWSTONE_DUST, 4),
                    ingredient(Items.REDSTONE, 8)),
            recipe(ModItems.SPEED_COMPONENT_4.get(),
                    ingredient(ModItems.SPEED_COMPONENT_3.get(), 2),
                    ingredient(Items.GLOWSTONE_DUST, 4),
                    ingredient(Items.REDSTONE, 4),
                    ingredient(Items.BLAZE_POWDER, 1)),
            recipe(ModItems.AP_EXPANSION_COMPONENT_1.get(),
                    ingredient(ModItems.AP_STRUCTURAL_STEEL_PLATE.get(), 4),
                    ingredient(ModItems.AP_STEEL_FRAME.get(), 2),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 2),
                    ingredient(ModItems.DIMENSION_UPGRADE_COMPONENT.get(), 1)),
            recipe(ModItems.AP_EXPANSION_COMPONENT_2.get(),
                    ingredient(ModItems.AP_STRUCTURAL_STEEL_PLATE.get(), 4),
                    ingredient(ModItems.AP_STEEL_FRAME.get(), 2),
                    ingredient(ModItems.CRYSTAL_INGOT.get(), 2),
                    ingredient(ModItems.AP_EXPANSION_COMPONENT_1.get(), 1)),
            recipe(ModItems.AP_EXPANSION_COMPONENT_3.get(),
                    ingredient(ModItems.AP_STRUCTURAL_STEEL_PLATE.get(), 4),
                    ingredient(ModItems.AP_STEEL_FRAME.get(), 2),
                    ingredient(ModItems.CRYSTAL_INGOT.get(), 2),
                    ingredient(ModItems.AP_EXPANSION_COMPONENT_2.get(), 1))
    );

    private ProcessorProgrammingRecipes() {
    }

    public static List<ManipulatorRecipeDisplay> all() {
        return RECIPES;
    }

    public static boolean isIngredient(ItemStack stack) {
        return RECIPES.stream().flatMap(recipe -> recipe.ingredients().stream())
                .flatMap(ingredient -> ingredient.acceptedStacks().stream())
                .anyMatch(accepted -> ItemStack.isSameItemSameTags(stack, accepted));
    }

    private static ManipulatorIngredient ingredient(net.minecraft.world.level.ItemLike item, int count) {
        return ManipulatorIngredient.ofItem(item, count);
    }

    private static ManipulatorRecipeDisplay reinforcedStorageComponent() {
        List<ItemStack> chests = new ArrayList<>(ManipulatorRecipes.getCopperChestOptions());
        if (chests.isEmpty()) {
            chests.add(new ItemStack(Items.CHEST));
        }
        return recipe(ModItems.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get(),
                ingredient(ModItems.IRON_DEVICE_CASING.get(), 1),
                ingredient(ModItems.INDUSTRIAL_CROP_STORAGE_ARRAY.get(), 1),
                ingredient(Items.IRON_INGOT, 5),
                ManipulatorIngredient.ofOptions(chests, chests.get(0), 3));
    }

    private static ManipulatorRecipeDisplay recipe(
            net.minecraft.world.level.ItemLike output,
            ManipulatorIngredient... ingredients
    ) {
        return new ManipulatorRecipeDisplay(List.of(ingredients), new ItemStack(output));
    }
}
