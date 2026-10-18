package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

/** Preserve the previous wireless logistics identifier in existing worlds. */
@Mod.EventBusSubscriber(modid = IndustrialCrops.MOD_ID)
public final class LegacyRegistryMappings {
    private LegacyRegistryMappings() { }

    @SubscribeEvent
    public static void missingMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Block> mapping : event.getMappings(Registries.BLOCK, IndustrialCrops.MOD_ID)) {
            if (mapping.getKey().getPath().equals("infinite_logistics_transfer_device")) {
                mapping.remap(ModBlocks.CRYSTAL_LOGISTICS.get());
            } else if (mapping.getKey().getPath().equals("gourd_modification_device")) {
                mapping.remap(ModBlocks.MELON_MODIFICATION_DEVICE.get());
            }
        }
        for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(Registries.ITEM, IndustrialCrops.MOD_ID)) {
            if (mapping.getKey().getPath().equals("infinite_logistics_transfer_device")) {
                mapping.remap(ModItems.CRYSTAL_LOGISTICS.get());
            } else if (mapping.getKey().getPath().equals("gourd_modification_device")) {
                mapping.remap(ModItems.MELON_MODIFICATION_DEVICE.get());
            }
        }
        for (MissingMappingsEvent.Mapping<net.minecraft.world.level.block.entity.BlockEntityType<?>> mapping
                : event.getMappings(Registries.BLOCK_ENTITY_TYPE, IndustrialCrops.MOD_ID)) {
            if (mapping.getKey().getPath().equals("gourd_modification_device")) {
                mapping.remap(ModBlockEntities.MELON_MODIFICATION_DEVICE.get());
            } else if (mapping.getKey().getPath().equals("infinite_logistics_transfer_device")) {
                mapping.remap(ModBlockEntities.CRYSTAL_LOGISTICS.get());
            }
        }
        for (MissingMappingsEvent.Mapping<net.minecraft.world.inventory.MenuType<?>> mapping
                : event.getMappings(Registries.MENU, IndustrialCrops.MOD_ID)) {
            if (mapping.getKey().getPath().equals("gourd_modification_device")) {
                mapping.remap(ModMenus.MELON_MODIFICATION_DEVICE.get());
            } else if (mapping.getKey().getPath().equals("infinite_logistics_transfer_device")) {
                mapping.remap(ModMenus.CRYSTAL_LOGISTICS.get());
            }
        }
    }
}
