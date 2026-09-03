package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.GourdModificationRecipeDisplay;
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

public final class GourdModificationRecipeCategory
        implements IRecipeCategory<GourdModificationRecipeDisplay> {
    public static final RecipeType<GourdModificationRecipeDisplay> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID,
            "gourd_modification",
            GourdModificationRecipeDisplay.class
    );

    private final IDrawable icon;

    public GourdModificationRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.GOURD_MODIFICATION_DEVICE.get().asItem())
        );
    }

    @Override
    public RecipeType<GourdModificationRecipeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.industrialcrops.gourd_modification_device");
    }

    @Override
    public int getWidth() {
        return 116;
    }

    @Override
    public int getHeight() {
        return 36;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            GourdModificationRecipeDisplay recipe,
            IFocusGroup focuses
    ) {
        builder.addRecipeArrow().setPosition(53, 10);
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            GourdModificationRecipeDisplay recipe,
            IFocusGroup focuses
    ) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 10).addItemStack(recipe.melon());
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 10).addItemStack(recipe.pumpkin());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 10).addItemStack(recipe.output());
    }
}
