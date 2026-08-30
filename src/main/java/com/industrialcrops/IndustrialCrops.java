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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;
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
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        modEventBus.addListener(EventPriority.LOWEST, this::registerCapabilities);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCauldronFluidContent);
        modEventBus.addListener(this::registerCometSignBlocks);

        LOGGER.info("Loaded Industrial Crops NeoForge branch.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            com.industrialcrops.registry.ModCauldronInteractions.bootstrap();
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(
                    ModBlocks.COMET_SAPLING.getId(), ModBlocks.POTTED_COMET_SAPLING);
        });
    }

    private void registerCometSignBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN,
                ModBlocks.COMET_SIGN.get(), ModBlocks.COMET_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN,
                ModBlocks.COMET_HANGING_SIGN.get(), ModBlocks.COMET_WALL_HANGING_SIGN.get());
    }

    private void registerCauldronFluidContent(RegisterCauldronFluidContentEvent event) {
        event.register(ModBlocks.PLASMA_JUICE_CAULDRON.get(),
                ModFluids.CONCENTRATED_PLASMA_JUICE.get(), FluidType.BUCKET_VOLUME, null);
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
