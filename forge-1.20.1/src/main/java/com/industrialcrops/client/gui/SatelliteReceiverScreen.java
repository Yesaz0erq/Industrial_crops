package com.industrialcrops.client.gui;
import com.industrialcrops.screen.SatelliteReceiverMenu;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
public final class SatelliteReceiverScreen extends IndustrialContainerScreen<SatelliteReceiverMenu> {
   private static final ResourceLocation GALAXY_MAP = new ResourceLocation("industrialcrops", "textures/gui/satellite_galaxy_map.png");
   private static final int MAP_TEXTURE_WIDTH = 256;
   private static final int MAP_TEXTURE_HEIGHT = 128;
   private static final int GUI_WIDTH = 248;
   private static final int GUI_HEIGHT = 196;
   private static final int MAP_X = 12;
   private static final int MAP_Y = 47;
   private static final int MAP_W = 224;
   private static final int MAP_H = 96;
   private static final int BAR_X = 12;
   private static final int BAR_Y = 36;
   private static final int BAR_W = 224;
   private static final int BAR_H = 6;
   private static final int STATUS_Y = 149;
   private static final int STATUS_H = 40;
   private static final int CYAN = -9904897;
   private static final int CYAN_DIM = -13660210;
   private static final int AMBER = -2055606;
   private double mapU = 16.0;
   private double mapV = 16.0;
   private boolean draggingMap;
   public SatelliteReceiverScreen(SatelliteReceiverMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 248;
      this.imageHeight = 196;
   }
   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      CrystalGuiStyle.drawContainer(graphics, x, y, this.imageWidth, this.imageHeight);
      CrystalGuiStyle.drawInsetPanel(graphics, x + 12 - 1, y + 36 - 1, 226, 8);
      graphics.fill(x + 12, y + 36, x + 12 + 224, y + 36 + 6, -15459797);
      int filled = (int)Math.min(224L, 224L * (long)Math.max(0, ((SatelliteReceiverMenu)this.menu).energy()) / (long)Math.max(1, 2000000));
      if (filled > 0) {
         graphics.fill(x + 12, y + 36, x + 12 + filled, y + 36 + 6, -13660210);
         graphics.fill(x + 12, y + 36, x + 12 + filled, y + 36 + 2, -9904897);
      }
      for (int tick = 1; tick < 8; tick++) {
         int tx = x + 12 + tick * 224 / 8;
         graphics.fill(tx, y + 36 + 6 - 2, tx + 1, y + 36 + 6, Integer.MIN_VALUE);
      }
      int mx = x + 12;
      int my = y + 47;
      graphics.fill(mx - 2, my - 2, mx + 224 + 2, my + 96 + 2, -14341320);
      CrystalGuiStyle.drawInsetPanel(graphics, mx - 1, my - 1, 226, 98);
      graphics.enableScissor(mx, my, mx + 224, my + 96);
      graphics.blit(GALAXY_MAP, mx, my, (float)Math.round(this.mapU), (float)Math.round(this.mapV), 224, 96, 256, 128);
      if (((SatelliteReceiverMenu)this.menu).active()) {
         long period = ((SatelliteReceiverMenu)this.menu).linked() ? 3000L : 1600L;
         int sweep = (int)(Util.getMillis() % period * 248L / period) - 12;
         for (int trail = 0; trail < 12; trail++) {
            int alpha = 70 - trail * 6;
            if (alpha <= 0) {
               break;
            }
            int sx = mx + sweep - trail;
            graphics.fill(sx, my, sx + 1, my + 96, alpha << 24 | 6872319);
         }
      } else {
         graphics.fill(mx, my, mx + 224, my + 96, -1341781484);
      }
      graphics.disableScissor();
      drawBrackets(graphics, mx + 2, my + 2, 220, 92, ((SatelliteReceiverMenu)this.menu).active() ? -13660210 : -11905424);
      CrystalGuiStyle.drawWorkPanel(graphics, x + 8, y + 149, this.imageWidth - 16, 40);
      int lampX = x + 14;
      int lampY = y + 149 + 6;
      boolean blink = !((SatelliteReceiverMenu)this.menu).linked() && Util.getMillis() / 400L % 2L == 0L;
      int lamp = !((SatelliteReceiverMenu)this.menu).active()
         ? -10868182
         : (((SatelliteReceiverMenu)this.menu).linked() ? -9904897 : (blink ? -2055606 : -9744092));
      graphics.fill(lampX - 1, lampY - 1, lampX + 6, lampY + 6, -14341320);
      graphics.fill(lampX, lampY, lampX + 5, lampY + 5, lamp);
      graphics.fill(lampX, lampY, lampX + 2, lampY + 2, -2130706433);
   }
   private static void drawBrackets(GuiGraphics graphics, int x, int y, int w, int h, int color) {
      int arm = 7;
      graphics.fill(x, y, x + arm, y + 1, color);
      graphics.fill(x, y, x + 1, y + arm, color);
      graphics.fill(x + w - arm, y, x + w, y + 1, color);
      graphics.fill(x + w - 1, y, x + w, y + arm, color);
      graphics.fill(x, y + h - 1, x + arm, y + h, color);
      graphics.fill(x, y + h - arm, x + 1, y + h, color);
      graphics.fill(x + w - arm, y + h - 1, x + w, y + h, color);
      graphics.fill(x + w - 1, y + h - arm, x + w, y + h, color);
   }
   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      IndustrialGuiStyle.drawFittedString(graphics, this.font, this.title.getString(), 12, 9, this.imageWidth - 24, -14142905, true);
      Component energy = Component.translatable(
         "gui.industrialcrops.energy",
         new Object[]{String.format("%,d", Math.max(0, ((SatelliteReceiverMenu)this.menu).energy())), String.format("%,d", 2000000)}
      );
      IndustrialGuiStyle.drawFittedString(graphics, this.font, energy.getString(), 12, 25, this.imageWidth - 24, -11509640, false);
      int textX = 24;
      int lineY = 154;
      int maxWidth = this.imageWidth - 24 - 14;
      this.status(
         graphics,
         Component.translatable(((SatelliteReceiverMenu)this.menu).active() ? "gui.industrialcrops.satellite.active" : "gui.industrialcrops.satellite.standby"),
         textX,
         lineY,
         maxWidth,
         -14142905
      );
      this.status(
         graphics,
         Component.translatable(
            ((SatelliteReceiverMenu)this.menu).linked() ? "gui.industrialcrops.satellite.linked" : "gui.industrialcrops.satellite.searching"
         ),
         textX,
         lineY + 11,
         maxWidth,
         -11509640
      );
      this.status(
         graphics,
         Component.translatable("gui.industrialcrops.satellite.orbit", new Object[]{((SatelliteReceiverMenu)this.menu).satellites()}),
         textX,
         lineY + 22,
         maxWidth,
         -11509640
      );
   }
   private void status(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
      IndustrialGuiStyle.drawFittedString(graphics, this.font, text.getString(), x, y, maxWidth, color, false);
   }
   private boolean overMap(double mouseX, double mouseY) {
      return mouseX >= (double)(this.leftPos + 12)
         && mouseX < (double)(this.leftPos + 12 + 224)
         && mouseY >= (double)(this.topPos + 47)
         && mouseY < (double)(this.topPos + 47 + 96);
   }
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.overMap(mouseX, mouseY)) {
         this.draggingMap = true;
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.draggingMap && button == 0) {
         this.mapU = Mth.clamp(this.mapU - dragX, 0.0, 32.0);
         this.mapV = Mth.clamp(this.mapV - dragY, 0.0, 32.0);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      boolean handled = this.draggingMap;
      this.draggingMap = false;
      return handled || super.mouseReleased(mouseX, mouseY, button);
   }
}
