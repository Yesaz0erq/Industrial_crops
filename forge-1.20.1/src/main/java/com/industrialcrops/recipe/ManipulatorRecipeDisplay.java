package com.industrialcrops.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record ManipulatorRecipeDisplay(List<ManipulatorIngredient> ingredients, ItemStack output) {
}
