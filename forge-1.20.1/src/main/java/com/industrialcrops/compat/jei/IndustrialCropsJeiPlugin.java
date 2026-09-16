package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.CropCompressorRecipes;
import com.industrialcrops.recipe.GourdModificationRecipes;
import com.industrialcrops.recipe.MixerRecipes;
import com.industrialcrops.recipe.ManipulatorRecipes;
import com.industrialcrops.recipe.RootOreExtractorRecipes;
import com.industrialcrops.recipe.ProcessorProgrammingRecipes;
import com.industrialcrops.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import com.industrialcrops.network.ModNetworking;
import com.industrialcrops.network.payload.StorageCraftingTransferPayload;

@JeiPlugin
public final class IndustrialCropsJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(IndustrialCrops.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new PlasmaExtractionRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CrystalWorkbenchRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RootOreExtractorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ManipulatorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ManipulatorRecipeCategory(registration.getJeiHelpers().getGuiHelper(), true),
                new CropCompressorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new GourdModificationRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new MixerRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ProcessorProgrammingRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PlasmaExtractionRecipeCategory.TYPE, java.util.List.of(new PlasmaExtractionRecipeCategory.Recipe()));
        registration.addRecipes(CrystalWorkbenchRecipeCategory.TYPE, com.industrialcrops.recipe.CrystalWorkbenchRecipes.all());
        registration.addRecipes(RootOreExtractorRecipeCategory.TYPE, RootOreExtractorRecipes.all());
        registration.addRecipes(ManipulatorRecipeCategory.TYPE, ManipulatorRecipes.forAdvanced(false));
        registration.addRecipes(ManipulatorRecipeCategory.ADVANCED_TYPE, ManipulatorRecipes.forAdvanced(true).stream().filter(r -> !ManipulatorRecipes.forAdvanced(false).contains(r)).toList());
        registration.addRecipes(CropCompressorRecipeCategory.TYPE, CropCompressorRecipes.all());
        registration.addRecipes(GourdModificationRecipeCategory.TYPE, GourdModificationRecipes.all());
        registration.addRecipes(MixerRecipeCategory.TYPE, MixerRecipes.all());
        registration.addRecipes(ProcessorProgrammingRecipeCategory.TYPE, ProcessorProgrammingRecipes.all());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GOLD_PLASMA_EXTRACTOR.get().asItem()), PlasmaExtractionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRAFTING_PROCESSOR.get().asItem()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRYSTAL_STEEL_WORKBENCH.get().asItem()), CrystalWorkbenchRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROOT_ORE_EXTRACTOR.get().asItem()), RootOreExtractorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BASIC_MANIPULATOR.get().asItem()), ManipulatorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_MANIPULATOR.get().asItem()), ManipulatorRecipeCategory.TYPE, ManipulatorRecipeCategory.ADVANCED_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CROP_COMPRESSOR.get().asItem()), CropCompressorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GOURD_MODIFICATION_DEVICE.get().asItem()), GourdModificationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MIXER.get().asItem()), MixerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PROCESSOR_PROGRAMMER.get().asItem()), ProcessorProgrammingRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new IRecipeTransferHandler<AdvancedIndustrialStorageMenu, CraftingRecipe>() {
            @Override
            public Class<? extends AdvancedIndustrialStorageMenu> getContainerClass() {
                return AdvancedIndustrialStorageMenu.class;
            }

            @Override
            public Optional<MenuType<AdvancedIndustrialStorageMenu>> getMenuType() {
                return Optional.of(ModMenus.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get());
            }

            @Override
            public RecipeType<CraftingRecipe> getRecipeType() {
                return RecipeTypes.CRAFTING;
            }

            @Override
            public IRecipeTransferError transferRecipe(AdvancedIndustrialStorageMenu menu,
                                                       CraftingRecipe recipe,
                                                       IRecipeSlotsView recipeSlots,
                                                       Player player,
                                                       boolean maxTransfer,
                                                       boolean doTransfer) {
                if (doTransfer) ModNetworking.sendToServer(new StorageCraftingTransferPayload(recipe.getId()));
                return null;
            }
        }, RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new IRecipeTransferHandler<com.industrialcrops.screen.CraftingProcessorMenu, CraftingRecipe>() {
            @Override
            public Class<? extends com.industrialcrops.screen.CraftingProcessorMenu> getContainerClass() {
                return com.industrialcrops.screen.CraftingProcessorMenu.class;
            }

            @Override
            public Optional<MenuType<com.industrialcrops.screen.CraftingProcessorMenu>> getMenuType() {
                return Optional.of(ModMenus.CRAFTING_PROCESSOR.get());
            }

            @Override
            public RecipeType<CraftingRecipe> getRecipeType() {
                return RecipeTypes.CRAFTING;
            }

            @Override
            public IRecipeTransferError transferRecipe(com.industrialcrops.screen.CraftingProcessorMenu menu,
                                                       CraftingRecipe recipe,
                                                       IRecipeSlotsView recipeSlots,
                                                       Player player,
                                                       boolean maxTransfer,
                                                       boolean doTransfer) {
                if (doTransfer) com.industrialcrops.network.ModNetworking.sendToServer(new StorageCraftingTransferPayload(recipe.getId()));
                return null;
            }
        }, RecipeTypes.CRAFTING);
    }
}
