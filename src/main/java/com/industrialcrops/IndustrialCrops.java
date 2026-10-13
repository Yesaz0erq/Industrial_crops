package com.industrialcrops;

import com.industrialcrops.effect.CropCombatEvents;
import com.industrialcrops.machine.DimensionUpgradeHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModCapabilities;
import com.industrialcrops.registry.ModCauldronInteractions;
import com.industrialcrops.registry.ModCreativeTabs;
import com.industrialcrops.registry.ModEffects;
import com.industrialcrops.registry.ModEntities;
import com.industrialcrops.registry.ModFluids;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.registry.ModPotions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("industrialcrops")
public final class IndustrialCrops {
   public static final String MOD_ID = "industrialcrops";
   public static final Logger LOGGER = LoggerFactory.getLogger("industrialcrops");

   public IndustrialCrops(IEventBus modEventBus, ModContainer modContainer) {
      modContainer.registerConfig(Type.COMMON, IndustrialCropsConfig.SPEC);
      ShapedRecipePattern.setCraftingSize(5, 5);
      ModBlocks.BLOCKS.register(modEventBus);
      ModEntities.ENTITY_TYPES.register(modEventBus);
      ModItems.ITEMS.register(modEventBus);
      ModFluids.FLUID_TYPES.register(modEventBus);
      ModFluids.FLUIDS.register(modEventBus);
      ModEffects.EFFECTS.register(modEventBus);
      ModPotions.POTIONS.register(modEventBus);
      NeoForge.EVENT_BUS.addListener(ModPotions::brewing);
      NeoForge.EVENT_BUS.register(CropCombatEvents.class);
      ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
      ModMenus.MENUS.register(modEventBus);
      ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
      modEventBus.addListener(EventPriority.LOWEST, this::registerCapabilities);
      modEventBus.addListener(this::registerEntityAttributes);
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(this::registerCauldronFluidContent);
      modEventBus.addListener(this::registerCometSignBlocks);
      modEventBus.addListener(this::registerTicketControllers);
      LOGGER.info("Loaded Industrial Crops NeoForge branch.");
   }

   private void registerTicketControllers(RegisterTicketControllersEvent event) {
      DimensionUpgradeHelper.registerTicketController(event);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         ModCauldronInteractions.bootstrap();
         ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(ModBlocks.COMET_SAPLING.getId(), ModBlocks.POTTED_COMET_SAPLING);
      });
   }

   private void registerCometSignBlocks(BlockEntityTypeAddBlocksEvent event) {
      event.modify(BlockEntityType.SIGN, new Block[]{(Block)ModBlocks.COMET_SIGN.get(), (Block)ModBlocks.COMET_WALL_SIGN.get()});
      event.modify(BlockEntityType.HANGING_SIGN, new Block[]{(Block)ModBlocks.COMET_HANGING_SIGN.get(), (Block)ModBlocks.COMET_WALL_HANGING_SIGN.get()});
   }

   private void registerCauldronFluidContent(RegisterCauldronFluidContentEvent event) {
      event.register((Block)ModBlocks.PLASMA_JUICE_CAULDRON.get(), (Fluid)ModFluids.CONCENTRATED_PLASMA_JUICE.get(), 1000, null);
   }

   private void registerCapabilities(RegisterCapabilitiesEvent event) {
      ModCapabilities.register(event);
   }

   private void registerEntityAttributes(EntityAttributeCreationEvent event) {
      event.put((EntityType)ModEntities.BROWN_CREATE_SLIME.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).build());
      event.put((EntityType)ModEntities.GRAY_GEAR_SLIME.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).build());
      event.put((EntityType)ModEntities.GOLDEN_REDSTONE_LAMP_SLIME.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).build());
      event.put((EntityType)ModEntities.DIAMOND_PROCESSOR_SLIME.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).build());
   }
}
