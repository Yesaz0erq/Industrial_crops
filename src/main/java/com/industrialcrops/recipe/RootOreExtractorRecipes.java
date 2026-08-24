package com.industrialcrops.recipe;

import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.block.entity.RootOreExtractorBlockEntity;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class RootOreExtractorRecipes {
    private static final List<RootOreExtractorRecipeDisplay> RECIPES = List.of(
            recipe(new ItemStack(Items.CARROT), new ItemStack(ModItems.BAGGED_INDUSTRIAL_CARROT.get())),
            recipe(new ItemStack(Items.POTATO), new ItemStack(ModItems.BAGGED_INDUSTRIAL_POTATO.get())),
            recipe(new ItemStack(Items.WHEAT), new ItemStack(ModItems.INDUSTRIAL_WHEAT.get())),
            recipe(new ItemStack(Items.WHEAT_SEEDS), new ItemStack(ModItems.BAGGED_INDUSTRIAL_WHEAT_SEEDS.get())),
            recipe(new ItemStack(Items.MELON_SEEDS), new ItemStack(ModItems.BAGGED_INDUSTRIAL_MELON_SEEDS.get())),
            recipe(new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(ModItems.BAGGED_INDUSTRIAL_PUMPKIN_SEEDS.get())),
            recipe(new ItemStack(Blocks.HAY_BLOCK.asItem()), new ItemStack(ModBlocks.INDUSTRIAL_WHEAT_BLOCK.get().asItem())),
            recipe(new ItemStack(Blocks.MELON.asItem()), new ItemStack(ModItems.INDUSTRIAL_MELON.get())),
            recipe(new ItemStack(Blocks.PUMPKIN.asItem()), new ItemStack(ModItems.INDUSTRIAL_PUMPKIN.get()))
    );

    private RootOreExtractorRecipes() {
    }

    public static List<RootOreExtractorRecipeDisplay> all() {
        return RECIPES;
    }

    private static RootOreExtractorRecipeDisplay recipe(ItemStack input, ItemStack output) {
        ItemStack bag = RootOreExtractorBlockEntity.requiresBag(input)
                ? new ItemStack(ModItems.EMPTY_BAG.get())
                : ItemStack.EMPTY;
        return new RootOreExtractorRecipeDisplay(
                input,
                new ItemStack(ModItems.REDSTONE_BONEMEAL.get()),
                bag,
                output
        );
    }
}
