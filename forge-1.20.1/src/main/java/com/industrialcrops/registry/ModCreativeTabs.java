package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IndustrialCrops.MOD_ID);

    public static final RegistryObject<CreativeModeTab> INDUSTRIAL_CROPS =
            CREATIVE_TABS.register("industrial_crops", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.industrialcrops.industrial_crops"))
                    .icon(() -> ModItems.IRON_DEVICE_CASING.get().getDefaultInstance())
                    .displayItems((parameters, output) -> CreativeSectionCatalog.acceptFlat(
                            CreativeSectionCatalog.combinedSections(), output))
                    .build());

    private ModCreativeTabs() {
    }
}
