package com.industrialcrops.recipe;

import com.industrialcrops.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public final class MixerRecipes {
    private static final List<MixerRecipeDisplay> RECIPES = List.of(
            recipe(
                    List.of(new ItemStack(ModItems.INDUSTRIAL_WHEAT.get(), 2)),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.FEED_BAG_BASIC.get())
            ),
            recipe(
                    List.of(
                            new ItemStack(ModItems.INDUSTRIAL_WHEAT.get(), 2),
                            new ItemStack(Items.REDSTONE, 2)
                    ),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.FEED_BAG_FAST_BREEDING.get())
            ),
            recipe(
                    List.of(
                            new ItemStack(ModItems.INDUSTRIAL_WHEAT.get(), 2),
                            new ItemStack(Items.SUGAR, 2)
                    ),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.FEED_BAG_GROWTH.get())
            ),
            recipe(
                    List.of(
                            new ItemStack(ModItems.INDUSTRIAL_WHEAT.get(), 2),
                            new ItemStack(Items.GOLDEN_CARROT, 2)
                    ),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.FEED_BAG_HEALING.get())
            ),
            recipe(
                    List.of(
                            new ItemStack(ModItems.INDUSTRIAL_WHEAT.get(), 2),
                            new ItemStack(Items.IRON_INGOT, 2)
                    ),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.FEED_BAG_RESISTANCE.get())
            ),
            recipe(
                    List.of(
                            new ItemStack(Items.BONE_MEAL),
                            new ItemStack(ModItems.INDUSTRIAL_WHEAT.get()),
                            new ItemStack(Items.SUGAR)
                    ),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.FERTILIZER_FAST_GROWTH.get())
            ),
            recipe(
                    List.of(
                            new ItemStack(Items.DIRT),
                            new ItemStack(Items.BONE_MEAL),
                            new ItemStack(ModItems.INDUSTRIAL_WHEAT.get())
                    ),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.FERTILIZER_FERTILE_SOIL.get())
            ),
            recipe(
                    List.of(
                            new ItemStack(ModItems.PRISM_POD.get()),
                            new ItemStack(ModItems.EMBERCOIL.get()),
                            new ItemStack(ModItems.STARBLOOM.get()),
                            new ItemStack(ModItems.NEONBULB.get()),
                            new ItemStack(ModItems.FLUXSTALK.get())
                    ),
                    new ItemStack(ModItems.EMPTY_BAG.get()),
                    new ItemStack(ModItems.STRANGE_FERTILIZER.get())
            )
    );

    private MixerRecipes() {
    }

    public static List<MixerRecipeDisplay> all() {
        return RECIPES;
    }

    public static boolean isIngredient(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RECIPES.stream()
                .flatMap(recipe -> recipe.inputs().stream())
                .anyMatch(required -> stack.is(required.getItem()));
    }

    private static MixerRecipeDisplay recipe(List<ItemStack> inputs, ItemStack bag, ItemStack output) {
        return new MixerRecipeDisplay(inputs, bag, output);
    }
}
