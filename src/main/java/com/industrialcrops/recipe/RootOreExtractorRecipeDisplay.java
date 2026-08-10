package com.industrialcrops.recipe;

import net.minecraft.world.item.ItemStack;

public record RootOreExtractorRecipeDisplay(
        ItemStack input,
        ItemStack catalyst,
        ItemStack bag,
        ItemStack output
) {
}
