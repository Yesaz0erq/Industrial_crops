package com.industrialcrops.compat.jei;
import com.industrialcrops.recipe.CrystalWorkbenchRecipes;
import com.industrialcrops.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CrystalWorkbenchRecipeCategory implements IRecipeCategory<CrystalWorkbenchRecipes.Recipe> {
    public static final RecipeType<CrystalWorkbenchRecipes.Recipe> TYPE=RecipeType.create("industrialcrops","crystal_workbench",CrystalWorkbenchRecipes.Recipe.class);
    private final IDrawable icon, slot;
    public CrystalWorkbenchRecipeCategory(IGuiHelper helper) {
        icon=helper.createDrawableItemStack(new ItemStack(ModItems.CRYSTAL_STEEL_WORKBENCH.get()));
        slot=helper.getSlotDrawable();
    }
    @Override public RecipeType<CrystalWorkbenchRecipes.Recipe> getRecipeType(){return TYPE;}
    @Override public Component getTitle(){return Component.translatable("block.industrialcrops.crystal_steel_workbench");}
    @Override public int getWidth(){return 166;}
    @Override public int getHeight(){return 96;}
    @Override public IDrawable getIcon(){return icon;}
    @Override public void setRecipe(IRecipeLayoutBuilder builder,CrystalWorkbenchRecipes.Recipe recipe,IFocusGroup focuses){
        for(int i=0;i<25;i++) builder.addSlot(RecipeIngredientRole.INPUT,2+i%5*18,2+i/5*18)
                .setBackground(slot,-1,-1).addItemStack(recipe.inputs().get(i));
        builder.addSlot(RecipeIngredientRole.OUTPUT,110,38).setBackground(slot,-1,-1).addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.INPUT,144,8).addFluidStack(recipe.fluid().getFluid(),recipe.fluid().getAmount())
                .setFluidRenderer(recipe.fluid().getAmount(),false,16,76);
    }
}
