package com.industrialcrops.compat.jei;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.recipe.CropCompressorRecipes;
import com.industrialcrops.recipe.GourdModificationRecipes;
import com.industrialcrops.recipe.MixerRecipes;
import com.industrialcrops.recipe.ManipulatorRecipes;
import com.industrialcrops.recipe.RootOreExtractorRecipes;
import com.industrialcrops.recipe.ProcessorProgrammingRecipes;
import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
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
                new PlasmaExtractionRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CrystalWorkbenchRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RootOreExtractorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ManipulatorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ManipulatorRecipeCategory(registration.getJeiHelpers().getGuiHelper(), true),
                new CropCompressorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new GourdModificationRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new MixerRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ProcessorProgrammingRecipeCategory(registration.getJeiHelpers().getGuiHelper())
                , new BioEnergyRecipeCategory(registration.getJeiHelpers().getGuiHelper())
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
        registration.addRecipes(BioEnergyRecipeCategory.TYPE, bioEnergyRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GOLD_PLASMA_EXTRACTOR.asItem()), PlasmaExtractionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRAFTING_PROCESSOR.asItem()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRYSTAL_STEEL_WORKBENCH.asItem()), CrystalWorkbenchRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROOT_ORE_EXTRACTOR.asItem()), RootOreExtractorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BASIC_MANIPULATOR.asItem()), ManipulatorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_MANIPULATOR.asItem()), ManipulatorRecipeCategory.TYPE, ManipulatorRecipeCategory.ADVANCED_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CROP_COMPRESSOR.asItem()), CropCompressorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GOURD_MODIFICATION_DEVICE.asItem()), GourdModificationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MIXER.asItem()), MixerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PROCESSOR_PROGRAMMER.asItem()), ProcessorProgrammingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BIO_ENERGY_GENERATOR.asItem()), BioEnergyRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BIO_ENERGY_REACTOR.asItem()), BioEnergyRecipeCategory.TYPE);
    }

    private static java.util.List<BioEnergyRecipeCategory.Recipe> bioEnergyRecipes() {
        java.util.List<ItemStack> fuels = java.util.List.of(
                new ItemStack(net.minecraft.world.item.Items.WHEAT),
                new ItemStack(net.minecraft.world.item.Items.CARROT),
                new ItemStack(net.minecraft.world.item.Items.POTATO),
                new ItemStack(net.minecraft.world.item.Items.BEETROOT),
                new ItemStack(net.minecraft.world.item.Items.WHEAT_SEEDS),
                new ItemStack(ModItems.INDUSTRIAL_CARROT.get()),
                new ItemStack(ModItems.INDUSTRIAL_POTATO.get()),
                new ItemStack(ModItems.INDUSTRIAL_WHEAT.get()),
                new ItemStack(ModItems.INDUSTRIAL_MELON.get()),
                new ItemStack(ModItems.INDUSTRIAL_PUMPKIN.get()),
                new ItemStack(ModItems.PRISM_POD_SEEDS.get()),
                new ItemStack(ModItems.PRISM_POD.get()),
                new ItemStack(ModItems.EMBERCOIL_SEEDS.get()),
                new ItemStack(ModItems.EMBERCOIL.get()),
                new ItemStack(ModItems.STARBLOOM_SEEDS.get()),
                new ItemStack(ModItems.STARBLOOM.get()),
                new ItemStack(ModItems.NEONBULB_SEEDS.get()),
                new ItemStack(ModItems.NEONBULB.get()),
                new ItemStack(ModItems.FLUXSTALK_SEEDS.get()),
                new ItemStack(ModItems.FLUXSTALK.get()),
                new ItemStack(ModItems.INDUSTRIAL_CARROT_BLOCK.get()),
                new ItemStack(ModItems.INDUSTRIAL_POTATO_BLOCK.get()),
                new ItemStack(ModItems.INDUSTRIAL_WHEAT_BLOCK.get()),
                new ItemStack(ModItems.INDUSTRIAL_MELON_BLOCK.get()),
                new ItemStack(ModItems.INDUSTRIAL_PUMPKIN_BLOCK.get()));
        return fuels.stream().map(stack -> {
            var tier = BioEnergyMachineBlockEntity.classifyFuel(stack);
            return new BioEnergyRecipeCategory.Recipe(stack, tier.energy(), tier.residue());
        }).toList();
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
        registration.addRecipeTransferHandler(new IRecipeTransferHandler<com.industrialcrops.screen.CraftingProcessorMenu, RecipeHolder<CraftingRecipe>>() {
            @Override
            public Class<? extends com.industrialcrops.screen.CraftingProcessorMenu> getContainerClass() {
                return com.industrialcrops.screen.CraftingProcessorMenu.class;
            }

            @Override
            public Optional<MenuType<com.industrialcrops.screen.CraftingProcessorMenu>> getMenuType() {
                return Optional.of(ModMenus.CRAFTING_PROCESSOR.get());
            }

            @Override
            public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
                return RecipeTypes.CRAFTING;
            }

            @Override
            public IRecipeTransferError transferRecipe(com.industrialcrops.screen.CraftingProcessorMenu menu,
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
