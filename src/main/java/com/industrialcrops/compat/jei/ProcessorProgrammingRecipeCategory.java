package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.ManipulatorIngredient;
import com.industrialcrops.recipe.ManipulatorRecipeDisplay;
import com.industrialcrops.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ProcessorProgrammingRecipeCategory implements IRecipeCategory<ManipulatorRecipeDisplay> {
    public static final RecipeType<ManipulatorRecipeDisplay> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID, "processor_programming", ManipulatorRecipeDisplay.class);
    private final IDrawable icon;

    public ProcessorProgrammingRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.PROCESSOR_PROGRAMMER.asItem()));
    }

    @Override public RecipeType<ManipulatorRecipeDisplay> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.industrialcrops.processor_programming_device"); }
    @Override public int getWidth() { return 138; }
    @Override public int getHeight() { return 42; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ManipulatorRecipeDisplay recipe, IFocusGroup focuses) {
        builder.setShapeless(68, 20);
        for (int index = 0; index < recipe.ingredients().size(); index++) {
            ManipulatorIngredient ingredient = recipe.ingredients().get(index);
            builder.addSlot(RecipeIngredientRole.INPUT, 2 + index * 20, 13)
                    .addItemStacks(ingredient.acceptedStacks().stream()
                            .map(stack -> stack.copyWithCount(ingredient.count())).toList());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 118, 13).addItemStack(recipe.output());
    }
}
