package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.ManipulatorIngredient;
import com.industrialcrops.recipe.ManipulatorRecipeDisplay;
import com.industrialcrops.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ManipulatorRecipeCategory implements IRecipeCategory<ManipulatorRecipeDisplay> {
    private static final int WIDTH = 118;
    private static final int HEIGHT = 54;
    public static final RecipeType<ManipulatorRecipeDisplay> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID,
            "basic_manipulation_device",
            ManipulatorRecipeDisplay.class
    );

    private final IDrawable icon;

    public ManipulatorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BASIC_MANIPULATOR.asItem()));
    }

    @Override
    public RecipeType<ManipulatorRecipeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.industrialcrops.basic_manipulation_device");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ManipulatorRecipeDisplay recipe, IFocusGroup focuses) {
        builder.setShapeless(48, 20);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 8, 19)
                .addItemStack(recipe.output());

        int[][] inputPositions = {
                {72, 10},
                {92, 10},
                {72, 30},
                {92, 30}
        };

        for (int i = 0; i < recipe.ingredients().size() && i < inputPositions.length; i++) {
            ManipulatorIngredient ingredient = recipe.ingredients().get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, inputPositions[i][0], inputPositions[i][1])
                    .addItemStacks(ingredient.acceptedStacks().stream()
                            .map(stack -> stack.copyWithCount(ingredient.count()))
                            .toList());
        }
    }
}
