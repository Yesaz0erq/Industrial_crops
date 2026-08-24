package com.industrialcrops.recipe;

import com.industrialcrops.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ProcessorProgrammingRecipes {
    private static final List<ManipulatorRecipeDisplay> RECIPES = List.of(
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
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.CHEST, 2),
                    ingredient(Items.IRON_INGOT, 1)),
            recipe(ModItems.INDUSTRIAL_STORAGE_COMPONENT_3.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.CHEST, 4),
                    ingredient(Items.GOLD_INGOT, 1)),
            recipe(ModItems.INDUSTRIAL_STORAGE_COMPONENT_4.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.CHEST, 16),
                    ingredient(Items.DIAMOND, 1),
                    ingredient(Items.REDSTONE, 2)),
            recipe(ModItems.RAPID_FIRE_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.REPEATER, 2),
                    ingredient(Items.GOLD_INGOT, 2),
                    ingredient(Items.REDSTONE, 4)),
            recipe(ModItems.POWER_COMPONENT.get(),
                    ingredient(ModItems.COMPONENT_SUBSTRATE.get(), 1),
                    ingredient(Items.TNT, 4),
                    ingredient(Items.GOLD_INGOT, 2),
                    ingredient(Items.REDSTONE, 4)),
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
                    ingredient(Items.BLAZE_POWDER, 1))
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

    private static ManipulatorRecipeDisplay recipe(
            net.minecraft.world.level.ItemLike output,
            ManipulatorIngredient... ingredients
    ) {
        return new ManipulatorRecipeDisplay(List.of(ingredients), new ItemStack(output));
    }
}
