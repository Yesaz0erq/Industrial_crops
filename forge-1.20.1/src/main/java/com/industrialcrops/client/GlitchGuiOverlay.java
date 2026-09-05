package com.industrialcrops.client;

import com.industrialcrops.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;

public final class GlitchGuiOverlay {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "industrialcrops", "textures/gui/glitch_overlay.png");
    private static final int FRAME_WIDTH = 256;
    private static final int FRAME_HEIGHT = 144;
    private static final int FRAMES = 8;
    private GlitchGuiOverlay() { }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.hasEffect(ModEffects.GLITCH.get())) return;
        if (minecraft.options.hideGui) return;
        int frame = (minecraft.player.tickCount / 4) % FRAMES;
        // Alpha is authored into the texture: broad interference with a clear central view.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, 0, 0, graphics.guiWidth(), graphics.guiHeight(),
                0F, (float) (frame * FRAME_HEIGHT), FRAME_WIDTH, FRAME_HEIGHT,
                FRAME_WIDTH, FRAME_HEIGHT * FRAMES);
        RenderSystem.disableBlend();
    }
}
