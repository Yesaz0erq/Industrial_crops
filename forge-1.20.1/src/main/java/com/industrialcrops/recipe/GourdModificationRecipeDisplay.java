package com.industrialcrops.recipe;

import net.minecraft.world.item.ItemStack;

public record GourdModificationRecipeDisplay(
        ItemStack melon,
        ItemStack pumpkin,
        ItemStack output
) {
}
