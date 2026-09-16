package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModFluids;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
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

/** The fixed extractor conversion, using the machine's processing constants. */
public final class PlasmaExtractionRecipeCategory implements IRecipeCategory<PlasmaExtractionRecipeCategory.Recipe> {
    public record Recipe() {}
    public static final RecipeType<Recipe> TYPE = RecipeType.create(IndustrialCrops.MOD_ID, "extraction_device", Recipe.class);
    private final IDrawable icon;
    private final IDrawable slot;
    public PlasmaExtractionRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.GOLD_PLASMA_EXTRACTOR.asItem()));
        slot = helper.getSlotDrawable();
    }
    @Override public RecipeType<Recipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.industrialcrops.extraction_device"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 140; }
    @Override public int getHeight() { return 64; }
    @Override public void createRecipeExtras(IRecipeExtrasBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(57, 12);
    }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 12).setBackground(slot, -1, -1)
                .addItemStack(new ItemStack(ModItems.PLASMA_BERRY.get()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 12).setBackground(slot, -1, -1)
                .addFluidStack(ModFluids.CONCENTRATED_PLASMA_JUICE.get(), GoldPlasmaExtractorBlockEntity.OUTPUT_AMOUNT)
                .setFluidRenderer(GoldPlasmaExtractorBlockEntity.OUTPUT_AMOUNT, false, 16, 16);
    }
    @Override public void draw(Recipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, GoldPlasmaExtractorBlockEntity.OUTPUT_AMOUNT + " mB", 88, 34, 0xff444444, false);
        g.drawString(font, (GoldPlasmaExtractorBlockEntity.PROCESS_TICKS / 20) + " s / "
                + GoldPlasmaExtractorBlockEntity.PROCESS_TICKS * GoldPlasmaExtractorBlockEntity.ENERGY_PER_TICK + " FE", 15, 49, 0xff444444, false);
    }
}
