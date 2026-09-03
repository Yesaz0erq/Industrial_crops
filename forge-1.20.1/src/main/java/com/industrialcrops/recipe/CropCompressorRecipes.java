package com.industrialcrops.recipe;

import com.industrialcrops.block.entity.CropCompressorBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class CropCompressorRecipes {
    private static final List<CropCompressorRecipeDisplay> RECIPES = List.of(
            recipe(new ItemStack(ModItems.INDUSTRIAL_CARROT.get()), new ItemStack(ModBlocks.INDUSTRIAL_CARROT_BLOCK.get().asItem())),
            recipe(new ItemStack(ModItems.INDUSTRIAL_POTATO.get()), new ItemStack(ModBlocks.INDUSTRIAL_POTATO_BLOCK.get().asItem())),
            recipe(new ItemStack(ModItems.INDUSTRIAL_WHEAT.get()), new ItemStack(ModBlocks.INDUSTRIAL_WHEAT_BLOCK.get().asItem()))
    );

    private CropCompressorRecipes() {
    }

    public static List<CropCompressorRecipeDisplay> all() {
        return RECIPES;
    }

    private static CropCompressorRecipeDisplay recipe(ItemStack input, ItemStack output) {
        input.setCount(CropCompressorBlockEntity.REQUIRED_COUNT);
        return new CropCompressorRecipeDisplay(input, output);
    }
}
