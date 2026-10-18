package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.SuperCultivationRecipes;
import com.industrialcrops.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Super Crop Cultivator: Super crop + two materials -> new plant variety. */
public final class SuperCultivationRecipeCategory implements IRecipeCategory<SuperCultivationRecipes.Recipe> {
    public static final RecipeType<SuperCultivationRecipes.Recipe> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID, "super_cultivation", SuperCultivationRecipes.Recipe.class);

    private final IDrawable icon;

    public SuperCultivationRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SUPER_CROP_CULTIVATOR.get().asItem()));
    }

    @Override
    public RecipeType<SuperCultivationRecipes.Recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.industrialcrops.super_crop_cultivator");
    }

    @Override
    public int getWidth() {
        return 150;
    }

    @Override
    public int getHeight() {
        return 46;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SuperCultivationRecipes.Recipe recipe, IFocusGroup focuses) {
        builder.addRecipePlusSign().setPosition(23, 7);
        builder.addAnimatedRecipeArrow(recipe.duration()).setPosition(96, 6);
        builder.addText(Component.translatable("jei.industrialcrops.super_cultivation.time", recipe.duration() / 20), 122, 10).setPosition(0, 27);
        builder.addText(Component.translatable("jei.industrialcrops.super_cultivation.super_only"), 122, 10).setPosition(0, 36);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SuperCultivationRecipes.Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 5).addItemStack(recipe.displayCrop());
        // Three material slots, like the machine; unused ones stay empty.
        java.util.List<ItemStack> materials = recipe.materialStacks();
        for (int i = 0; i < SuperCultivationRecipes.MAX_MATERIALS; i++) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, 38 + i * 18, 5);
            if (i < materials.size()) slot.addItemStack(materials.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 5).addItemStack(recipe.resultStack());
        // Returned containers (e.g. the Dragon's Breath bottle).
        if (!recipe.remainders().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 26).addItemStacks(recipe.remainders());
        }
    }
}
