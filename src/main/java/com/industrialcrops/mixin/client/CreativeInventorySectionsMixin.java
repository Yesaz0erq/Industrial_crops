package com.industrialcrops.mixin.client;

import com.industrialcrops.client.creative.CreativeSectionClient;
import com.industrialcrops.Carrote;
import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.registry.ModCreativeTabs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventorySectionsMixin {
    @Shadow
    private static CreativeModeTab selectedTab;

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void industrialcrops$renderSectionBanners(
            GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo callbackInfo) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        CreativeSectionClient.render(graphics, selectedTab, screen.getGuiLeft(), screen.getGuiTop());
    }

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void industrialcrops$removeCreativeTabNameFromTooltip(
            ItemStack stack, CallbackInfoReturnable<List<Component>> callbackInfo) {
        String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        if (!IndustrialCrops.MOD_ID.equals(namespace) && !Carrote.MOD_ID.equals(namespace)
                && !com.industrialcrops.CarroteCurios.MOD_ID.equals(namespace)) {
            return;
        }

        List<Component> tooltip = new ArrayList<>(callbackInfo.getReturnValue());
        String combinedTabName = ModCreativeTabs.INDUSTRIAL_CROPS.get().getDisplayName().getString();
        for (int index = tooltip.size() - 1; index >= 1; index--) {
            if (tooltip.get(index).getString().equals(combinedTabName)) {
                tooltip.remove(index);
            }
        }
        callbackInfo.setReturnValue(tooltip);
    }
}
