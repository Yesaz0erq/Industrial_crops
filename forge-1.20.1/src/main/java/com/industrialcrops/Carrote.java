package com.industrialcrops;

import com.industrialcrops.registry.CarroteBlockEntities;
import com.industrialcrops.registry.CarroteBlocks;
import com.industrialcrops.registry.CarroteItems;
import com.industrialcrops.registry.CarroteMenus;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Carrote.MOD_ID)
public final class Carrote {
    public static final String MOD_ID = "carrote";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Carrote() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CarroteBlocks.BLOCKS.register(modEventBus);
        CarroteItems.ITEMS.register(modEventBus);
        CarroteBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CarroteMenus.MENUS.register(modEventBus);
        modEventBus.addListener(EventPriority.LOWEST, this::registerCapabilities);

        LOGGER.info("Loaded Carrote Forge 1.20.1 branch.");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        com.industrialcrops.registry.CarroteCapabilities.register(event);
    }

}
