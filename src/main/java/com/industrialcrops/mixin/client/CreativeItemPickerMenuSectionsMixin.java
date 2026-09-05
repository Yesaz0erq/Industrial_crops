package com.industrialcrops.mixin.client;

import com.industrialcrops.client.creative.CreativeSectionClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu.class)
public abstract class CreativeItemPickerMenuSectionsMixin {
    @Shadow
    protected abstract int getRowIndexForScroll(float scroll);

    @Inject(method = "scrollTo", at = @At("HEAD"))
    private void industrialcrops$trackSectionRow(float scroll, CallbackInfo callbackInfo) {
        CreativeSectionClient.setCurrentRow(this.getRowIndexForScroll(scroll));
    }
}
