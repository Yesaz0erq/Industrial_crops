package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Displays every crop and seed currently accepted by both bio-energy machines. */
public final class BioEnergyRecipeCategory implements IRecipeCategory<BioEnergyRecipeCategory.Recipe> {
    public record Recipe(ItemStack input, int energy, int residue) {}
    public static final RecipeType<Recipe> TYPE = RecipeType.create(
            IndustrialCrops.MOD_ID, "bio_energy_generation", Recipe.class);
    private final IDrawable icon;

    public BioEnergyRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.BIO_ENERGY_GENERATOR.asItem()));
    }

    @Override public RecipeType<Recipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.industrialcrops.bio_energy_generation_device"); }
    @Override public int getWidth() { return 150; }
    @Override public int getHeight() { return 44; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(50, 12);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 14, 12).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 12)
                .addItemStack(new ItemStack(ModBlocks.ENERGY_BATTERY.asItem()));
    }

    @Override
    public void draw(Recipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,
            GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, recipe.energy() + " FE", 58, 2, 0xff444444, false);
        graphics.drawString(font, Component.translatable("gui.industrialcrops.bio_generator.residue",
                recipe.residue()).getString(), 106, 17, 0xff6b4030, false);
    }
}
