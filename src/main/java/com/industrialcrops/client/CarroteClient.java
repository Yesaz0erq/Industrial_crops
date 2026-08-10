package com.industrialcrops.client;

import com.industrialcrops.Carrote;
import com.industrialcrops.client.gui.MaterialHardeningDeviceScreen;
import com.industrialcrops.registry.CarroteMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Carrote.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CarroteClient {
    private CarroteClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CarroteMenus.MATERIAL_HARDENING_DEVICE.get(), MaterialHardeningDeviceScreen::new);
    }
}
