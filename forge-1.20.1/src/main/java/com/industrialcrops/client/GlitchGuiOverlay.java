package com.industrialcrops.client;

import com.industrialcrops.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class GlitchGuiOverlay {
    private GlitchGuiOverlay() { }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.hasEffect(ModEffects.GLITCH.get())) return;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int tick = minecraft.player.tickCount;
        int cyan = 0xA040F4FF;
        int magenta = 0xA0D44CFF;
        int dark = 0x90070A12;
        int border = 5 + tick % 3;
        graphics.fill(0, 0, width, border, dark);
        graphics.fill(0, height - border, width, height, dark);
        graphics.fill(0, 0, border, height, dark);
        graphics.fill(width - border, 0, width, height, dark);
        for (int index = 0; index < 14; index++) {
            int seed = tick * 31 + index * 73;
            int length = 8 + Math.floorMod(seed, 42);
            int y = Math.floorMod(seed * 17, Math.max(1, height - 3));
            boolean left = (index & 1) == 0;
            int x1 = left ? 0 : width - length;
            graphics.fill(x1, y, left ? length : width, y + 1 + Math.floorMod(seed, 3),
                    index % 3 == 0 ? magenta : cyan);
        }
        for (int index = 0; index < 8; index++) {
            int seed = tick * 19 + index * 47;
            int x = Math.floorMod(seed * 13, Math.max(1, width - 12));
            int y = (index & 1) == 0 ? Math.floorMod(seed, 10) : height - 2 - Math.floorMod(seed, 10);
            graphics.fill(x, y, Math.min(width, x + 4 + Math.floorMod(seed, 16)), Math.min(height, y + 2),
                    index % 2 == 0 ? cyan : magenta);
        }
    }
}
