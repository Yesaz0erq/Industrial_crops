package com.industrialcrops;

import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModCreativeTabs;
import com.industrialcrops.registry.ModEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModEffects;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IndustrialCrops.MOD_ID)
public final class IndustrialCrops {
    public static final String MOD_ID = "industrialcrops";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public IndustrialCrops(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        modEventBus.addListener(EventPriority.LOWEST, this::registerCapabilities);
        modEventBus.addListener(this::registerEntityAttributes);

        LOGGER.info("Loaded Industrial Crops NeoForge branch.");
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
