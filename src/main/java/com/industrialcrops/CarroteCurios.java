package com.industrialcrops;

import com.industrialcrops.curios.CarroteCuriosItems;
import com.industrialcrops.curios.CarroteCuriosEvents;
import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CuriosIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(CarroteCurios.MOD_ID)
public final class CarroteCurios {
    public static final String MOD_ID = "carrote_curios";

    public CarroteCurios(IEventBus bus) {
        CarroteCuriosItems.ITEMS.register(bus);
        NeoForge.EVENT_BUS.register(CarroteCuriosEvents.class);
        bus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (CarroteCuriosEffects.curiosLoaded()) event.enqueueWork(CuriosIntegration::registerItems);
    }
}
