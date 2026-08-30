package com.industrialcrops.mixin.client;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementTab.class)
public abstract class AdvancementTabBackgroundMixin {
    @Unique
    private static final ResourceLocation INDUSTRIALCROPS$SPACE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            IndustrialCrops.MOD_ID,
            "textures/gui/advancements/backgrounds/space.png"
    );

    @Unique
    private boolean industrialcrops$backgroundDrawn;

    @Inject(method = "drawContents", at = @At("HEAD"))
    private void industrialcrops$resetBackgroundFlag(GuiGraphics graphics, int x, int y, CallbackInfo ci) {
        this.industrialcrops$backgroundDrawn = false;
    }

    @Redirect(
            method = "drawContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"
            )
    )
    private void industrialcrops$drawSpaceBackground(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        if (!INDUSTRIALCROPS$SPACE_BACKGROUND.equals(texture)) {
            graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
            return;
        }

        // Vanilla repeats advancement backgrounds in 16x16 tiles. Draw the supplied
        // landscape once, scaled proportionally, while the vanilla scissor clips it.
        if (!this.industrialcrops$backgroundDrawn) {
            this.industrialcrops$backgroundDrawn = true;
            graphics.blit(texture, 0, -24, 234, 160, 0.0F, 0.0F, 1216, 832, 1216, 832);
        }
    }
}
