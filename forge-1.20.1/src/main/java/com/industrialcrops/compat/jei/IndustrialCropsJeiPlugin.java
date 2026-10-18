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
                new SuperCultivationRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
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
        registration.addRecipes(SuperCultivationRecipeCategory.TYPE, com.industrialcrops.recipe.SuperCultivationRecipes.all());
        registration.addRecipes(MixerRecipeCategory.TYPE, MixerRecipes.all());
        registration.addRecipes(ProcessorProgrammingRecipeCategory.TYPE, ProcessorProgrammingRecipes.all());
        registration.addRecipes(BioEnergyRecipeCategory.TYPE, bioEnergyRecipes());
        registration.addItemStackInfo(new ItemStack(com.industrialcrops.registry.ModItems.SUPER_COMPRESSOR.get()),
                net.minecraft.network.chat.Component.translatable("jei.industrialcrops.info.super_compressor"));
        registration.addItemStackInfo(new ItemStack(com.industrialcrops.registry.ModItems.COMPRESSED_POTATO_10K.get()),
                net.minecraft.network.chat.Component.translatable("jei.industrialcrops.info.compressed_potato_10k"));
        registration.addItemStackInfo(new ItemStack(com.industrialcrops.registry.ModItems.EXPANDABLE_STORAGE_DEVICE.get()),
                net.minecraft.network.chat.Component.translatable("jei.industrialcrops.info.expandable_storage_device"));
        registration.addItemStackInfo(new ItemStack(com.industrialcrops.registry.ModItems.INTEGRATED_SMELTING_FURNACE.get()),
                net.minecraft.network.chat.Component.translatable("jei.industrialcrops.info.integrated_smelting_furnace"));
        registerAcquisitionInfo(registration);
        registerComponentInfo(registration);
    }

    private static ItemStack stack(String namespace, String path) {
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                new net.minecraft.resources.ResourceLocation(namespace, path));
        return item == net.minecraft.world.item.Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static void info(IRecipeRegistration registration, String key, ItemStack... stacks) {
        java.util.List<ItemStack> list = java.util.Arrays.stream(stacks).filter(s -> !s.isEmpty()).toList();
        if (!list.isEmpty()) {
            registration.addItemStackInfo(list, net.minecraft.network.chat.Component.translatable("jei.industrialcrops.info." + key));
        }
    }

    /** How to obtain items and blocks that have no crafting/machine recipe. */
    private static void registerAcquisitionInfo(IRecipeRegistration registration) {
        info(registration, "carrote", stack("carrote", "carrote"));
        info(registration, "carrote_accessories", com.industrialcrops.curios.CarroteCuriosItems.ITEMS.getEntries().stream()
                .map(entry -> new ItemStack(entry.get())).toArray(ItemStack[]::new));
        info(registration, "paradox_steel_forge", stack("carrote", "paradox_steel_forge"));
        info(registration, "paradox_steel_ingot", stack("carrote", "paradox_steel_ingot"));
        info(registration, "stable_matter_ingot", stack("carrote", "stable_matter_ingot"));
        info(registration, "exotic_seeds", new ItemStack(ModItems.PRISM_POD_SEEDS.get()),
                new ItemStack(ModItems.EMBERCOIL_SEEDS.get()), new ItemStack(ModItems.STARBLOOM_SEEDS.get()));
        info(registration, "space_ores", new ItemStack(ModItems.INV_ORE.get()),
                new ItemStack(ModItems.GREEN_IRON_ORE.get()), new ItemStack(ModItems.TYCON_ORE.get()));
        info(registration, "comet_soil", new ItemStack(ModBlocks.COMET_SOIL.get().asItem()));
        info(registration, "comet_sapling", new ItemStack(ModBlocks.COMET_SAPLING.get().asItem()));
        info(registration, "comet_essence", new ItemStack(ModItems.COMET_FRUIT.get()));
        info(registration, "plasma_fruit", new ItemStack(ModItems.PLASMA_BERRY.get()));
        info(registration, "omni_potion", new ItemStack(ModItems.OMNI_POTION.get()));
        info(registration, "omni_bucket", new ItemStack(ModItems.OMNI_BUCKET.get()));
    }

    /** Component functions and device compatibility share the same data as item tooltips. */
    private static void registerComponentInfo(IRecipeRegistration registration) {
        for (String path : com.industrialcrops.machine.UpgradeCompatibility.componentPaths()) {
            ItemStack component = com.industrialcrops.machine.UpgradeCompatibility.componentStack(path);
            if (component.isEmpty()) continue;
            java.util.List<net.minecraft.network.chat.Component> lines = new java.util.ArrayList<>();
            lines.add(net.minecraft.network.chat.Component.translatable("tooltip.industrialcrops.function." + path));
            java.util.List<ItemStack> machines = com.industrialcrops.machine.UpgradeCompatibility.compatibleMachines(component.getItem());
            if (machines.isEmpty()) {
                lines.add(net.minecraft.network.chat.Component.translatable("tooltip.industrialcrops.upgrade.no_devices"));
            } else {
                lines.add(net.minecraft.network.chat.Component.translatable("jei.industrialcrops.info.upgrade_machines"));
                for (ItemStack machine : machines) {
                    lines.add(net.minecraft.network.chat.Component.literal("\u2022 ").append(machine.getHoverName()));
                }
            }
            registration.addItemStackInfo(component, lines.toArray(net.minecraft.network.chat.Component[]::new));
        }
    }

    @Override
    public void registerGuiHandlers(mezz.jei.api.registration.IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(com.industrialcrops.client.gui.IntegratedSmeltingFurnaceScreen.class,
                new mezz.jei.api.gui.handlers.IGuiContainerHandler<com.industrialcrops.client.gui.IntegratedSmeltingFurnaceScreen>() {
                    @Override
                    public java.util.List<net.minecraft.client.renderer.Rect2i> getGuiExtraAreas(
                            com.industrialcrops.client.gui.IntegratedSmeltingFurnaceScreen screen) {
                        return screen.extraAreas();
                    }
                });
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
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MELON_MODIFICATION_DEVICE.get().asItem()), GourdModificationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SUPER_CROP_CULTIVATOR.get().asItem()), SuperCultivationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INTEGRATED_SMELTING_FURNACE.get().asItem()), RecipeTypes.SMELTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MIXER.get().asItem()), MixerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PROCESSOR_PROGRAMMER.get().asItem()), ProcessorProgrammingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BIO_ENERGY_GENERATOR.get().asItem()), BioEnergyRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BIO_ENERGY_REACTOR.get().asItem()), BioEnergyRecipeCategory.TYPE);
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
