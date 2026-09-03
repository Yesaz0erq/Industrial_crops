package com.industrialcrops.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record MixerRecipeDisplay(List<ItemStack> inputs, ItemStack bag, ItemStack output) {
}
