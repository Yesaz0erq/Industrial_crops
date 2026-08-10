package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.MixerRecipeDisplay;
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

public final class MixerRecipeCategory implements IRecipeCategory<MixerRecipeDisplay> {
    public static final RecipeType<MixerRecipeDisplay> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID, "mixer", MixerRecipeDisplay.class
    );

    private final IDrawable icon;

    public MixerRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MIXER.asItem()));
    }

    @Override
    public RecipeType<MixerRecipeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.industrialcrops.mixer");
    }

    @Override
    public int getWidth() {
        return 126;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MixerRecipeDisplay recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(70, 18);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MixerRecipeDisplay recipe, IFocusGroup focuses) {
        for (int index = 0; index < recipe.inputs().size(); index++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 2 + index * 20, 8)
                    .addItemStack(recipe.inputs().get(index));
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 22, 30).addItemStack(recipe.bag());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 18).addItemStack(recipe.output());
    }
}
