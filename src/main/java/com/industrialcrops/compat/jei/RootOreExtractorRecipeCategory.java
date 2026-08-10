package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.RootOreExtractorRecipeDisplay;
import com.industrialcrops.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class RootOreExtractorRecipeCategory implements IRecipeCategory<RootOreExtractorRecipeDisplay> {
    private static final int WIDTH = 112;
    private static final int HEIGHT = 62;
    public static final RecipeType<RootOreExtractorRecipeDisplay> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID,
            "basic_crop_conversion_device",
            RootOreExtractorRecipeDisplay.class
    );

    private final IDrawable icon;

    public RootOreExtractorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ROOT_ORE_EXTRACTOR.asItem()));
    }

    @Override
    public RecipeType<RootOreExtractorRecipeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.industrialcrops.basic_crop_conversion_device");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            RootOreExtractorRecipeDisplay recipe,
            IFocusGroup focuses
    ) {
        builder.addRecipeArrow().setPosition(42, 23);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RootOreExtractorRecipeDisplay recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 8)
                .addItemStack(recipe.input());
        if (!recipe.bag().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 24, 8)
                    .addItemStack(recipe.bag());
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 13, 36)
                .addItemStack(recipe.catalyst());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 22)
                .addItemStack(recipe.output());
    }
}
