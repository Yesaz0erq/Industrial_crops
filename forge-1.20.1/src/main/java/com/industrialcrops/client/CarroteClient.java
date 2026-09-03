package com.industrialcrops.client;

import com.industrialcrops.Carrote;
import com.industrialcrops.client.gui.MaterialHardeningDeviceScreen;
import com.industrialcrops.registry.CarroteMenus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Carrote.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CarroteClient {
    private CarroteClient() {
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
                CarroteMenus.MATERIAL_HARDENING_DEVICE.get(), MaterialHardeningDeviceScreen::new));
    }
}
