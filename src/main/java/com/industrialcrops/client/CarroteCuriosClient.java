package com.industrialcrops.client;

import com.industrialcrops.CarroteCurios;
import com.industrialcrops.client.gui.UltimateCarroteScreen;
import com.industrialcrops.registry.CarroteCuriosMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = CarroteCurios.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CarroteCuriosClient {
    private CarroteCuriosClient() {}
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent event) {
        event.register(CarroteCuriosMenus.ULTIMATE.get(), UltimateCarroteScreen::new);
    }
}
