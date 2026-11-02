package com.industrialcrops.client.glitch;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Draws a glitching item name: red/cyan chromatic ghosts either side of the text, corrupted characters,
 * and during bursts horizontally torn slices, noise blocks and a scanline.
 */
public final class ClientGlitchNameTooltip implements ClientTooltipComponent {
   private static final int PAD = 2;
   private final Component name;
   private final int seed;

   public ClientGlitchNameTooltip(GlitchNameTooltip tooltip) {
      this.name = tooltip.name();
      this.seed = tooltip.seed();
   }

   @Override
   public int getHeight() {
      return 10;
   }

   @Override
   public int getWidth(Font font) {
      return font.width(this.name) + PAD * 2;
   }

   @Override
   public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
      long frame = GlitchText.frame();
      boolean burst = GlitchText.burst(frame, this.seed);
      int rnd = GlitchText.mix(frame * 977L + this.seed);
      String plain = this.name.getString();
      int width = font.width(this.name);
      int tx = x + PAD;

      // Chromatic aberration: the plain name, split into red and cyan ghosts.
      int split = burst ? 1 + rnd % 2 : 1;
      int ghostAlpha = burst ? 0xB0 : 0x50;
      graphics.drawString(font, plain, tx - split, y, ghostAlpha << 24 | GlitchText.RED, false);
      graphics.drawString(font, plain, tx + split, y, ghostAlpha << 24 | GlitchText.CYAN, false);

      MutableComponent text = GlitchText.corrupt(this.name, this.seed, frame);
      if (!burst) {
         graphics.drawString(font, text, tx, y, 0xFFFFFFFF, true);
      } else {
         // Tear the line into three bands, each shoved sideways.
         int[] bands = {0, 3, 6, 10};
         for (int band = 0; band < 3; band++) {
            int shift = GlitchText.mix(frame * 53L + band * 7L + this.seed) % 5 - 2;
            graphics.enableScissor(x - 4, y + bands[band], x + width + PAD * 2 + 4, y + bands[band + 1]);
            graphics.drawString(font, text, tx + shift, y, 0xFFFFFFFF, true);
            graphics.disableScissor();
         }
         int blocks = 1 + rnd % 3;
         for (int block = 0; block < blocks; block++) {
            int noise = GlitchText.mix(frame * 389L + block * 101L + this.seed);
            int bx = tx + noise % Math.max(1, width);
            int bw = 2 + noise % 5;
            int by = y + (noise >>> 8) % 8;
            int color = (noise & 1) == 0 ? GlitchText.RED : GlitchText.CYAN;
            graphics.fill(bx, by, bx + bw, by + 1 + (noise >>> 4) % 2, 0x90 << 24 | color);
         }
      }
      if (rnd % 100 < (burst ? 45 : 6)) {
         int line = y + (rnd >>> 7) % 9;
         graphics.fill(x, line, x + width + PAD * 2, line + 1, 0x38FFFFFF);
      }
   }
}
