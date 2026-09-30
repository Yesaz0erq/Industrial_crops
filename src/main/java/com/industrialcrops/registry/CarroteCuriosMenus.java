package com.industrialcrops.registry;

import com.industrialcrops.CarroteCurios;
import com.industrialcrops.screen.UltimateCarroteMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CarroteCuriosMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CarroteCurios.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<UltimateCarroteMenu>> ULTIMATE =
            MENUS.register("ultimate_carrote", () -> new MenuType<>((IContainerFactory<UltimateCarroteMenu>) UltimateCarroteMenu::new,
                    FeatureFlags.DEFAULT_FLAGS));
    private CarroteCuriosMenus() {}
}
