package com.industrialcrops.registry;

import com.industrialcrops.Carrote;
import com.industrialcrops.screen.MaterialHardeningDeviceMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class CarroteMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Carrote.MOD_ID);

    public static final RegistryObject<MenuType<MaterialHardeningDeviceMenu>> MATERIAL_HARDENING_DEVICE =
            MENUS.register("material_hardening_device", () -> new MenuType<>(
                    (IContainerFactory<MaterialHardeningDeviceMenu>) MaterialHardeningDeviceMenu::new,
                    FeatureFlags.DEFAULT_FLAGS));

    private CarroteMenus() {
    }
}
