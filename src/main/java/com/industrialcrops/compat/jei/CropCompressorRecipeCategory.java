package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.CropCompressorRecipeDisplay;
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

public final class CropCompressorRecipeCategory implements IRecipeCategory<CropCompressorRecipeDisplay> {
    private static final int WIDTH = 88;
    private static final int HEIGHT = 36;
    public static final RecipeType<CropCompressorRecipeDisplay> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID,
            "crop_compressor",
            CropCompressorRecipeDisplay.class
    );

    private final IDrawable icon;

    public CropCompressorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CROP_COMPRESSOR.asItem()));
    }

    @Override
    public RecipeType<CropCompressorRecipeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.industrialcrops.crop_compressor");
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
            CropCompressorRecipeDisplay recipe,
            IFocusGroup focuses
    ) {
        builder.addRecipeArrow().setPosition(34, 10);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CropCompressorRecipeDisplay recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 10)
                .addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 58, 10)
                .addItemStack(recipe.output());
    }
}
