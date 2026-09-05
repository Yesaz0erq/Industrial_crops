package com.industrialcrops;

import com.industrialcrops.curios.CarroteCuriosItems;
import com.industrialcrops.curios.CarroteCuriosEvents;
import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CuriosIntegration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(CarroteCurios.MOD_ID)
public final class CarroteCurios {
    public static final String MOD_ID = "carrote_curios";

    public CarroteCurios() {
        IEventBus bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        CarroteCuriosItems.ITEMS.register(bus);
        MinecraftForge.EVENT_BUS.register(CarroteCuriosEvents.class);
        bus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (CarroteCuriosEffects.curiosLoaded()) event.enqueueWork(CuriosIntegration::registerItems);
    }
}
