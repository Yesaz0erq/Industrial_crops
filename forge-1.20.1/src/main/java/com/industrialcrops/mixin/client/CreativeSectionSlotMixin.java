package com.industrialcrops.mixin.client;

import com.industrialcrops.client.creative.CreativeSectionClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class CreativeSectionSlotMixin {
    @Shadow
    public int index;

    @Inject(method = "isHighlightable", at = @At("HEAD"), cancellable = true)
    private void industrialcrops$hideSectionBannerHighlight(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen
                && CreativeSectionClient.isBannerVisibleSlot(index)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
