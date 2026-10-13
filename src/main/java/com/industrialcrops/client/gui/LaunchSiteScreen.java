package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.LaunchSiteBlockEntity;
import com.industrialcrops.screen.LaunchSiteMenu;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class LaunchSiteScreen extends IndustrialContainerScreen<LaunchSiteMenu> {
   private static final int GUI_WIDTH = 296;
   private static final int GUI_HEIGHT = 210;
   private static final int VIEW_X = 12;
   private static final int VIEW_Y = 27;
   private static final int VIEW_W = 96;
   private static final int VIEW_H = 150;
   private static final int PANEL_X = 114;
   private static final int PANEL_Y = 27;
   private static final int PANEL_W = 170;
   private static final int PANEL_H = 150;
   private static final int CYAN = -9904897;
   private static final int CYAN_DIM = -13660210;
   private static final int RED = -3126710;
   private static final int PLASMA = -8690976;
   private static final int OK_TEXT = -13668454;
   private static final int BAD_TEXT = -5751748;
   private static final int[] ROCKET_HEIGHT = new int[]{0, 2, 3, 4};
   private CrystalButton launchButton;

   public LaunchSiteScreen(LaunchSiteMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 296;
      this.imageHeight = 210;
   }

   protected void init() {
      super.init();
      this.launchButton = (CrystalButton)this.addRenderableWidget(
         new CrystalButton(
            this.leftPos + this.imageWidth - 12 - 72, this.topPos + 183, 72, 18, Component.translatable("gui.industrialcrops.launch.button"), button -> {
               if (this.minecraft != null && this.minecraft.gameMode != null) {
                  this.minecraft.gameMode.handleInventoryButtonClick(((LaunchSiteMenu)this.menu).containerId, 0);
               }
            }
         )
      );
   }

   protected void containerTick() {
      super.containerTick();
      if (this.launchButton != null) {
         this.launchButton.active = ((LaunchSiteMenu)this.menu).status() == 0;
      }
   }

   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      CrystalGuiStyle.drawContainer(graphics, x, y, this.imageWidth, this.imageHeight);
      this.drawSchematic(graphics, x + 12, y + 27);
      CrystalGuiStyle.drawWorkPanel(graphics, x + 114, y + 27, 170, 150);
      int tier = ((LaunchSiteMenu)this.menu).rocketTier();
      drawGauge(
         graphics,
         x + 114 + 6,
         y + 27 + 93,
         158,
         ((LaunchSiteMenu)this.menu).energy(),
         500000,
         -13660210,
         -9904897,
         tier > 0 ? LaunchSiteBlockEntity.ENERGY_COST[tier] : -1
      );
      drawGauge(
         graphics,
         x + 114 + 6,
         y + 27 + 117,
         158,
         ((LaunchSiteMenu)this.menu).fuel(),
         8000,
         -8690976,
         -5728001,
         tier > 0 ? LaunchSiteBlockEntity.FUEL_COST[tier] : -1
      );
      lamp(graphics, x + 114 + 6, y + 27 + 18, ((LaunchSiteMenu)this.menu).pads() == 8);
      lamp(graphics, x + 114 + 6, y + 27 + 29, ((LaunchSiteMenu)this.menu).frames() == 8);
      lamp(graphics, x + 114 + 6, y + 27 + 49, tier > 0);
      lamp(graphics, x + 114 + 6, y + 27 + 60, tier > 0 && ((LaunchSiteMenu)this.menu).skyClear());
   }

   private static void lamp(GuiGraphics graphics, int x, int y, boolean ok) {
      graphics.fill(x, y, x + 6, y + 6, -14341320);
      graphics.fill(x + 1, y + 1, x + 5, y + 5, ok ? -9904897 : -3126710);
      graphics.fill(x + 1, y + 1, x + 3, y + 3, 1895825407);
   }

   private static void drawGauge(GuiGraphics graphics, int x, int y, int w, int value, int capacity, int color, int highlight, int requirement) {
      CrystalGuiStyle.drawInsetPanel(graphics, x - 1, y - 1, w + 2, 8);
      graphics.fill(x, y, x + w, y + 6, -15459797);
      int filled = (int)Math.min((long)w, (long)w * (long)Math.max(0, value) / (long)Math.max(1, capacity));
      if (filled > 0) {
         graphics.fill(x, y, x + filled, y + 6, color);
         graphics.fill(x, y, x + filled, y + 2, highlight);
      }

      if (requirement > 0) {
         int mark = x + (int)Math.min((long)(w - 1), (long)w * (long)requirement / (long)Math.max(1, capacity));
         graphics.fill(mark, y - 2, mark + 1, y + 8, value >= requirement ? -1 : -3126710);
      }
   }

   private void drawSchematic(GuiGraphics graphics, int sx, int sy) {
      graphics.fill(sx - 2, sy - 2, sx + 96 + 2, sy + 150 + 2, -14341320);
      CrystalGuiStyle.drawInsetPanel(graphics, sx - 1, sy - 1, 98, 152);
      graphics.fill(sx, sy, sx + 96, sy + 150, -16117728);

      for (int gx = sx + 8; gx < sx + 96; gx += 16) {
         graphics.fill(gx, sy, gx + 1, sy + 150, -15590349);
      }

      for (int gy = sy + 150 - 8; gy > sy; gy -= 16) {
         graphics.fill(sx, gy, sx + 96, gy + 1, -15590349);
      }

      int cell = 22;
      int left = sx + (96 - cell * 3) / 2;
      int ground = sy + 150 - 12;
      long time = Util.getMillis();
      boolean padsOk = ((LaunchSiteMenu)this.menu).pads() == 8;

      for (int i = 0; i < 3; i++) {
         int px = left + i * cell;
         graphics.fill(px, ground - 8, px + cell, ground, padsOk ? -13682607 : -12967128);
         graphics.fill(px, ground - 8, px + cell, ground - 7, padsOk ? -13660210 : -3126710);
         graphics.fill(px, ground - 8, px + 1, ground, -16117728);
      }

      graphics.fill(sx + 2, ground, sx + 96 - 2, ground + 1, -12760216);
      boolean framesOk = ((LaunchSiteMenu)this.menu).frames() == 8;
      int frameColor = framesOk ? -10653808 : -9815488;
      int frameAccent = framesOk ? -13660210 : -3126710;

      for (int side = 0; side < 2; side++) {
         int fx = left + side * cell * 2 + 3;
         int fw = cell - 6;
         int top = ground - 8 - cell * 2;
         graphics.fill(fx, top, fx + 2, ground - 8, frameColor);
         graphics.fill(fx + fw - 2, top, fx + fw, ground - 8, frameColor);

         for (int seg = 0; seg < 4; seg++) {
            int y0 = top + seg * cell / 2;
            graphics.fill(fx, y0, fx + fw, y0 + 1, frameAccent);

            for (int k = 0; k < fw; k += 2) {
               int dy = k * (cell / 2) / fw;
               graphics.fill(fx + k, y0 + dy, fx + k + 1, y0 + dy + 1, frameColor);
            }
         }
      }

      int tier = ((LaunchSiteMenu)this.menu).rocketTier();
      int status = ((LaunchSiteMenu)this.menu).status();
      int cx = left + cell + cell / 2;
      if (tier > 0) {
         int h = ROCKET_HEIGHT[tier] * cell - 4;
         int base = ground - 8;
         int lift = 0;
         if (status == 6 && ((LaunchSiteMenu)this.menu).countdown() < 20) {
            lift = (20 - ((LaunchSiteMenu)this.menu).countdown()) / 6;
         }

         int bottom = base - lift;
         int bodyHalf = tier == 3 ? 5 : 4;
         int top = bottom - h;
         graphics.fill(cx - bodyHalf, top + 8, cx + bodyHalf, bottom, -6640696);
         graphics.fill(cx - bodyHalf, top + 8, cx - bodyHalf + 2, bottom, -3878168);
         graphics.fill(cx + bodyHalf - 2, top + 8, cx + bodyHalf, bottom, -8877141);

         for (int row = 0; row < 8; row++) {
            int half = Math.max(1, bodyHalf * (row + 1) / 8);
            graphics.fill(cx - half, top + row, cx + half, top + row + 1, row < 2 ? -9904897 : -6640696);
         }

         int band = tier == 1 ? -9904897 : (tier == 2 ? -10514486 : -10924870);

         for (int b = 1; b < ROCKET_HEIGHT[tier]; b++) {
            int by = bottom - b * cell + 4;
            graphics.fill(cx - bodyHalf, by, cx + bodyHalf, by + 2, band);
         }

         graphics.fill(cx - bodyHalf - 4, bottom - 9, cx - bodyHalf, bottom, -12760216);
         graphics.fill(cx + bodyHalf, bottom - 9, cx + bodyHalf + 4, bottom, -12760216);
         graphics.fill(cx - 2, top + 14, cx + 2, top + 18, -12635737);
         if (status == 6 && ((LaunchSiteMenu)this.menu).countdown() < 40) {
            int flicker = (int)(time / 60L % 3L);
            graphics.fill(cx - 3, bottom, cx + 3, bottom + 3 + flicker, -11654);
            graphics.fill(cx - 2, bottom + 2, cx + 2, bottom + 5 + flicker, -30148);
         }
      } else if (status == 7) {
         for (int puff = 0; puff < 9; puff++) {
            int py = ground - 12 - puff * 13 - (int)(time / 40L % 13L);
            if (py >= sy + 2) {
               int r = 2 + puff / 3;
               graphics.fill(cx - r, py, cx + r, py + 3, puff % 2 == 0 ? -1866149144 : 1895825407);
            }
         }
      } else {
         int blink = (int)(time / 500L % 2L);
         graphics.fill(cx - 5, ground - 10, cx + 5, ground - 9, blink == 0 ? -13660210 : -14865852);
      }

      int c = -13660210;
      int arm = 6;
      graphics.fill(sx + 2, sy + 2, sx + 2 + arm, sy + 3, c);
      graphics.fill(sx + 2, sy + 2, sx + 3, sy + 2 + arm, c);
      graphics.fill(sx + 96 - 2 - arm, sy + 2, sx + 96 - 2, sy + 3, c);
      graphics.fill(sx + 96 - 3, sy + 2, sx + 96 - 2, sy + 2 + arm, c);
   }

   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      IndustrialGuiStyle.drawFittedString(graphics, this.font, this.title.getString(), 12, 9, this.imageWidth - 24, -14142905, true);
      int px = 120;
      int w = 158;
      int tier = ((LaunchSiteMenu)this.menu).rocketTier();
      this.heading(graphics, "gui.industrialcrops.launch.structure", px, 33, w);
      this.text(
         graphics,
         Component.translatable("gui.industrialcrops.launch.pads", new Object[]{((LaunchSiteMenu)this.menu).pads()}),
         px + 10,
         45,
         w - 10,
         ((LaunchSiteMenu)this.menu).pads() == 8 ? -13668454 : -5751748
      );
      this.text(
         graphics,
         Component.translatable("gui.industrialcrops.launch.frames", new Object[]{((LaunchSiteMenu)this.menu).frames()}),
         px + 10,
         56,
         w - 10,
         ((LaunchSiteMenu)this.menu).frames() == 8 ? -13668454 : -5751748
      );
      this.heading(graphics, "gui.industrialcrops.launch.payload", px, 66, w);
      Component rocket = tier > 0
         ? Component.translatable("gui.industrialcrops.launch.rocket", new Object[]{rocketName(tier)})
         : Component.translatable("gui.industrialcrops.launch.no_rocket");
      this.text(graphics, rocket, px + 10, 76, w - 10, tier > 0 ? -13668454 : -5751748);
      this.text(
         graphics,
         Component.translatable(
            tier > 0 && ((LaunchSiteMenu)this.menu).skyClear() ? "gui.industrialcrops.launch.sky_clear" : "gui.industrialcrops.launch.sky_blocked"
         ),
         px + 10,
         87,
         w - 10,
         tier > 0 && ((LaunchSiteMenu)this.menu).skyClear() ? -13668454 : -11509640
      );
      if (tier > 0) {
         this.text(
            graphics,
            Component.translatable(
               "gui.industrialcrops.launch.cost",
               new Object[]{String.format("%,d", LaunchSiteBlockEntity.ENERGY_COST[tier]), String.format("%,d", LaunchSiteBlockEntity.FUEL_COST[tier])}
            ),
            px,
            99,
            w,
            -11509640
         );
      }

      this.text(
         graphics,
         Component.translatable(
            "gui.industrialcrops.launch.energy", new Object[]{String.format("%,d", ((LaunchSiteMenu)this.menu).energy()), String.format("%,d", 500000)}
         ),
         px,
         110,
         w,
         -14142905
      );
      this.text(
         graphics,
         Component.translatable(
            "gui.industrialcrops.launch.plasma", new Object[]{String.format("%,d", ((LaunchSiteMenu)this.menu).fuel()), String.format("%,d", 8000)}
         ),
         px,
         134,
         w,
         -14142905
      );
      this.text(
         graphics, Component.translatable("gui.industrialcrops.launch.orbit", new Object[]{((LaunchSiteMenu)this.menu).satellites()}), px, 161, w, -14142905
      );
      int status = ((LaunchSiteMenu)this.menu).status();
      Component statusText = Component.translatable("gui.industrialcrops.launch.status." + status);
      int statusColor = status != 0 && status != 7 ? (status == 6 ? -14142905 : -5751748) : -13668454;
      this.text(graphics, statusText, 12, 188, this.imageWidth - 24 - 80, statusColor);
      if (status == 6) {
         String t = Component.translatable("gui.industrialcrops.launch.countdown", new Object[]{(((LaunchSiteMenu)this.menu).countdown() + 19) / 20})
            .getString();
         graphics.pose().pushPose();
         graphics.pose().translate(60.0F, 37.0F, 0.0F);
         graphics.pose().scale(2.0F, 2.0F, 1.0F);
         graphics.drawString(this.font, t, -this.font.width(t) / 2, 0, -9904897, true);
         graphics.pose().popPose();
      }
   }

   private static Component rocketName(int tier) {
      String id = tier == 3 ? "large" : (tier == 2 ? "medium" : "small");
      return Component.translatable("block.industrialcrops." + id + "_satellite_rocket");
   }

   private void heading(GuiGraphics graphics, String key, int x, int y, int w) {
      IndustrialGuiStyle.drawFittedString(graphics, this.font, Component.translatable(key).getString(), x, y, w, -14142905, false);
   }

   private void text(GuiGraphics graphics, Component text, int x, int y, int w, int color) {
      IndustrialGuiStyle.drawFittedString(graphics, this.font, text.getString(), x, y, w, color, false);
   }

   protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
      super.renderTooltip(graphics, mouseX, mouseY);
      if (mouseX >= this.leftPos + 12 && mouseX < this.leftPos + 12 + 96 && mouseY >= this.topPos + 27 && mouseY < this.topPos + 27 + 150) {
         graphics.renderComponentTooltip(
            this.font,
            List.of(
               Component.translatable("gui.industrialcrops.launch.help.title"),
               Component.translatable("gui.industrialcrops.launch.help.1"),
               Component.translatable("gui.industrialcrops.launch.help.2"),
               Component.translatable("gui.industrialcrops.launch.help.3")
            ),
            mouseX,
            mouseY
         );
      }
   }
}
