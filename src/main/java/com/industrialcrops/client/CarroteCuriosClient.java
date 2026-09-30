package com.industrialcrops.client;

import com.industrialcrops.CarroteCurios;
import com.industrialcrops.client.gui.UltimateCarroteScreen;
import com.industrialcrops.registry.CarroteCuriosMenus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

@EventBusSubscriber(modid = CarroteCurios.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CarroteCuriosClient {
    private CarroteCuriosClient() {}
    @SubscribeEvent public static void screens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(CarroteCuriosMenus.ULTIMATE.get(), UltimateCarroteScreen::new));
    }
}
