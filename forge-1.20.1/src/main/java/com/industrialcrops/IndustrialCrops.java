package com.industrialcrops;

import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModCreativeTabs;
import com.industrialcrops.registry.ModEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModEffects;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.registry.ModFluids;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IndustrialCrops.MOD_ID)
public final class IndustrialCrops {
    public static final String MOD_ID = "industrialcrops";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public IndustrialCrops() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        com.industrialcrops.network.ModNetworking.init();
        modEventBus.addListener(EventPriority.LOWEST, this::registerCapabilities);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Loaded Industrial Crops Forge 1.20.1 branch.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            com.industrialcrops.registry.ModCauldronInteractions.bootstrap();
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(
                    ModBlocks.COMET_SAPLING.getId(), ModBlocks.POTTED_COMET_SAPLING);
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        com.industrialcrops.registry.ModCapabilities.register(event);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.BROWN_CREATE_SLIME.get(),
                Monster.createMonsterAttributes()
                        .add(Attributes.MAX_HEALTH, 10.0D)
                        .build()
        );
        event.put(
                ModEntities.GRAY_GEAR_SLIME.get(),
                Monster.createMonsterAttributes()
                        .add(Attributes.MAX_HEALTH, 10.0D)
                        .build()
        );
        event.put(
                ModEntities.GOLDEN_REDSTONE_LAMP_SLIME.get(),
                Monster.createMonsterAttributes()
                        .add(Attributes.MAX_HEALTH, 10.0D)
                        .build()
        );
        event.put(
                ModEntities.DIAMOND_PROCESSOR_SLIME.get(),
                Monster.createMonsterAttributes()
                        .add(Attributes.MAX_HEALTH, 10.0D)
                        .build()
        );
    }
}
