package com.industrialcrops.mixin.client;

import com.industrialcrops.client.glitch.GlitchText;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The item name shown above the hotbar glitches too for Carrote items. */
@Mixin(Gui.class)
public abstract class GuiSelectedItemGlitchMixin {
    @Redirect(
            method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getHighlightTip(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;"
            ),
            require = 0
    )
    private Component industrialcrops$glitchHighlight(ItemStack stack, Component name) {
        Component tip = stack.getHighlightTip(name);
        return GlitchText.isCarrote(stack) ? GlitchText.corrupt(tip, GlitchText.seed(stack)) : tip;
    }
}
