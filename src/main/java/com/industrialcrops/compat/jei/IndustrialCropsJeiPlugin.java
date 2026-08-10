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
import net.minecraft.world.item.crafting.RecipeHolder;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import com.industrialcrops.network.payload.StorageCraftingTransferPayload;

@JeiPlugin
public final class IndustrialCropsJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new RootOreExtractorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ManipulatorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CropCompressorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new GourdModificationRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new MixerRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ProcessorProgrammingRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RootOreExtractorRecipeCategory.TYPE, RootOreExtractorRecipes.all());
        registration.addRecipes(ManipulatorRecipeCategory.TYPE, ManipulatorRecipes.all());
        registration.addRecipes(CropCompressorRecipeCategory.TYPE, CropCompressorRecipes.all());
        registration.addRecipes(GourdModificationRecipeCategory.TYPE, GourdModificationRecipes.all());
        registration.addRecipes(MixerRecipeCategory.TYPE, MixerRecipes.all());
        registration.addRecipes(ProcessorProgrammingRecipeCategory.TYPE, ProcessorProgrammingRecipes.all());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROOT_ORE_EXTRACTOR.asItem()), RootOreExtractorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BASIC_MANIPULATOR.asItem()), ManipulatorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_MANIPULATOR.asItem()), ManipulatorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CROP_COMPRESSOR.asItem()), CropCompressorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GOURD_MODIFICATION_DEVICE.asItem()), GourdModificationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MIXER.asItem()), MixerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PROCESSOR_PROGRAMMER.asItem()), ProcessorProgrammingRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new IRecipeTransferHandler<AdvancedIndustrialStorageMenu, RecipeHolder<CraftingRecipe>>() {
            @Override
            public Class<? extends AdvancedIndustrialStorageMenu> getContainerClass() {
                return AdvancedIndustrialStorageMenu.class;
            }

            @Override
            public Optional<MenuType<AdvancedIndustrialStorageMenu>> getMenuType() {
                return Optional.of(ModMenus.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get());
            }

            @Override
            public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
                return RecipeTypes.CRAFTING;
            }

            @Override
            public IRecipeTransferError transferRecipe(AdvancedIndustrialStorageMenu menu,
                                                       RecipeHolder<CraftingRecipe> recipe,
                                                       IRecipeSlotsView recipeSlots,
                                                       Player player,
                                                       boolean maxTransfer,
                                                       boolean doTransfer) {
                if (doTransfer) PacketDistributor.sendToServer(new StorageCraftingTransferPayload(recipe.id()));
                return null;
            }
        }, RecipeTypes.CRAFTING);
    }
}
