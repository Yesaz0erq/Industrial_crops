package com.industrialcrops.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class IndustrialGuiStyle {
   private static final ResourceLocation WIDGETS = containerTexture("widgets");
   private static final ResourceLocation RS2_GRID_ROW = containerTexture("rs2_grid_row");
   private static final ResourceLocation RS2_SLOT = containerTexture("rs2_slot");
   private static final ResourceLocation RS2_SEARCH = containerTexture("rs2_search");
   private static final ResourceLocation RS2_SCROLLBAR = containerTexture("rs2_scrollbar");
   private static final ResourceLocation RS2_SCROLLBAR_CLICKED = containerTexture("rs2_scrollbar_clicked");
   private static final ResourceLocation RS2_SCROLLBAR_DISABLED = containerTexture("rs2_scrollbar_disabled");
   private static final ResourceLocation MEKANISM_SMALL_RIGHT = containerTexture("mekanism_small_right");
   private static final ResourceLocation MEKANISM_BAR = containerTexture("mekanism_bar");
   private static final ResourceLocation METER_TICKS = containerTexture("meter_ticks");
   private static final int WIDGET_TEXTURE_WIDTH = 128;
   private static final int WIDGET_TEXTURE_HEIGHT = 64;
   private static final int SLOT_SIZE = 18;
   static final int PANEL = -3750202;
   static final int SLOT = -7631989;
   static final int SHADOW = -12236985;
   static final int DARK_SHADOW = -12236985;
   static final int HIGHLIGHT = -790037;
   private static final int[] METAL_RIM_COLORS = new int[]{-15131876, -790037, -6906980};
   static final int TEXT = -12566464;
   static final int MUTED_TEXT = -10461088;
   static final int ACTIVE = -5871035;
   static final int INACTIVE = -11184811;
   static final int WARNING = -5636096;
   static final int ENERGY_RED = -3785418;
   static final int RESIDUE_BROWN = -11916261;
   static final int TYCON_PANEL = -14017731;
   static final int TYCON_SURFACE = -11915672;
   static final int TYCON_SHADOW = -15332060;
   static final int TYCON_HIGHLIGHT = -5010727;
   static final int TYCON_TEXT = -924161;
   static final int TYCON_MUTED_TEXT = -3096603;
   static final int TYCON_ACCENT = -4895489;
   static final int TYCON_ENERGY = -3910145;
   static final int VERTICAL_METER_WIDTH = 18;

   private IndustrialGuiStyle() {
   }

   static ResourceLocation containerTexture(String name) {
      return ResourceLocation.fromNamespaceAndPath("industrialcrops", "textures/gui/container/" + name + ".png");
   }

   static void drawBackground(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
      graphics.blit(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      drawMetalRim(graphics, x, y, width, height);
   }

   private static void drawMetalRim(GuiGraphics graphics, int x, int y, int width, int height) {
      for (int inset = 0; inset < METAL_RIM_COLORS.length; inset++) {
         int left = x + inset;
         int top = y + inset;
         int right = x + width - inset;
         int bottom = y + height - inset;
         graphics.fill(left, top, right, top + 1, METAL_RIM_COLORS[inset]);
         graphics.fill(left, top, left + 1, bottom, METAL_RIM_COLORS[inset]);
         graphics.fill(left, bottom - 1, right, bottom, inset == 1 ? -12236985 : METAL_RIM_COLORS[inset]);
         graphics.fill(right - 1, top, right, bottom, inset == 1 ? -12236985 : METAL_RIM_COLORS[inset]);
      }
   }

   static void drawRs2StretchingBackground(GuiGraphics graphics, ResourceLocation texture, int x, int y, int rows, int bottomHeight) {
      int width = 193;
      int topHeight = 19;
      int rowHeight = 18;
      graphics.blit(texture, x, y, 0.0F, 0.0F, 193, 19, 256, 256);

      for (int row = 0; row < rows; row++) {
         int sourceY = row == 0 ? 19 : (row == rows - 1 ? 55 : 37);
         graphics.blit(texture, x, y + 19 + row * 18, 0.0F, (float)sourceY, 193, 18, 256, 256);
      }

      graphics.blit(texture, x, y + 19 + rows * 18, 0.0F, 73.0F, 193, bottomHeight, 256, 256);
   }

   static void drawRs2TerminalBackground(GuiGraphics graphics, ResourceLocation texture, int x, int y, int rows) {
      int width = 193;
      int lowerWidth = 176;
      int topHeight = 19;
      int rowHeight = 18;
      int networkHeight = rows * 18;
      int playerInventoryY = 36 + networkHeight;
      int playerHotbarY = playerInventoryY + 58;
      int height = playerHotbarY + 24;
      int networkBottom = 19 + networkHeight;
      int lowerTop = networkBottom + 6;
      drawTerminalChassis(graphics, x, y, height, lowerTop);

      for (int row = 0; row < rows; row++) {
         drawRs2GridRow(graphics, x + 7, y + 19 + row * 18);
      }

      drawInsetPanel(graphics, x + 172, y + 19, 18, networkHeight);

      for (int row = 0; row < 3; row++) {
         drawRs2GridRow(graphics, x + 7, y + playerInventoryY - 1 + row * 18);
      }

      drawRs2GridRow(graphics, x + 7, y + playerHotbarY - 1);
   }

   static void drawRs2GridRow(GuiGraphics graphics, int x, int y) {
      graphics.blit(RS2_GRID_ROW, x, y, 0.0F, 0.0F, 162, 18, 162, 18);
   }

   static void drawTerminalChassis(GuiGraphics graphics, int x, int y, int height, int lowerTop) {
      int[] layers = new int[]{-15131876, -790037, -6906980, -790037, -3750202};

      for (int inset = 0; inset < layers.length; inset++) {
         graphics.fill(x + inset, y + inset, x + 193 - inset, y + lowerTop - inset, layers[inset]);
         graphics.fill(x + inset, y + lowerTop - inset, x + 176 - inset, y + height - inset, layers[inset]);
      }

      drawInsetPanel(graphics, x + 93, y + 4, 76, 12);
   }

   static void drawRs2Slot(GuiGraphics graphics, int x, int y) {
      graphics.blit(RS2_SLOT, x, y, 0.0F, 0.0F, 18, 18, 18, 18);
   }

   static void drawRs2SearchIcon(GuiGraphics graphics, int x, int y) {
      graphics.blit(RS2_SEARCH, x, y, 0.0F, 0.0F, 12, 12, 12, 12);
   }

   static void drawRs2Scrollbar(GuiGraphics graphics, int x, int y, boolean clicked, boolean enabled) {
      ResourceLocation texture = !enabled ? RS2_SCROLLBAR_DISABLED : (clicked ? RS2_SCROLLBAR_CLICKED : RS2_SCROLLBAR);
      graphics.blit(texture, x, y, 0.0F, 0.0F, 12, 15, 12, 15);
   }

   static void drawMarqueeTitle(GuiGraphics graphics, Font font, Component title, int x, int y, int maxWidth, int screenX, int screenY) {
      int textWidth = font.width(title);
      if (textWidth <= maxWidth) {
         graphics.drawString(font, title, x, y, -12566464, false);
      } else {
         int overflow = textWidth - maxWidth;
         long travelMillis = (long)Math.max(1, overflow) * 70L;
         long cycle = 2000L + travelMillis;
         long time = System.currentTimeMillis() % cycle;
         int offset;
         if (time < 1000L) {
            offset = 0;
         } else if (time < 1000L + travelMillis) {
            offset = (int)((time - 1000L) * (long)overflow / travelMillis);
         } else {
            offset = overflow;
         }

         graphics.enableScissor(screenX + x, screenY + y - 1, screenX + x + maxWidth, screenY + y + 10);
         graphics.drawString(font, title, x - offset, y, -12566464, false);
         graphics.disableScissor();
      }
   }

   static String fitText(Font font, Component text, int maxWidth) {
      String value = text.getString();
      if (font.width(value) <= maxWidth) {
         return value;
      } else {
         String ellipsis = "...";
         return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis;
      }
   }

   static void drawFittedString(GuiGraphics graphics, Font font, String text, int x, int y, int maxWidth, int color, boolean centered) {
      int width = Math.max(1, font.width(text));
      float scale = Math.min(1.0F, (float)maxWidth / (float)width);
      graphics.pose().pushPose();
      float drawX = centered ? (float)x + (float)maxWidth / 2.0F - (float)width * scale / 2.0F : (float)x;
      graphics.pose().translate(drawX, (float)y, 0.0F);
      graphics.pose().scale(scale, scale, 1.0F);
      graphics.drawString(font, text, 0, 0, color, false);
      graphics.pose().popPose();
   }

   static void drawFittedComponent(GuiGraphics graphics, Font font, Component text, int x, int y, int maxWidth, int color, boolean centered) {
      int width = Math.max(1, font.width(text));
      float scale = Math.min(1.0F, (float)maxWidth / (float)width);
      graphics.pose().pushPose();
      float drawX = centered ? (float)x + (float)maxWidth / 2.0F - (float)width * scale / 2.0F : (float)x;
      graphics.pose().translate(drawX, (float)y, 0.0F);
      graphics.pose().scale(scale, scale, 1.0F);
      graphics.drawString(font, text, 0, 0, color, false);
      graphics.pose().popPose();
   }

   static void drawContainer(GuiGraphics graphics, int x, int y, int width, int height) {
      drawCommonPanel(graphics, x, y, width, height);
      drawPanel(graphics, x + 6, y + 5, width - 12, 13, -4604740);
      graphics.fill(x + 12, y + 18, x + width - 12, y + 19, -5871035);
   }

   static void drawTyconContainer(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x, y, x + width, y + height, -15332060);
      graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -5010727);
      graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, -14017731);
      graphics.fill(x + 6, y + 5, x + width - 6, y + 19, -11915672);
      graphics.fill(x + 7, y + 6, x + width - 7, y + 7, -5010727);
      graphics.fill(x + 12, y + 19, x + width - 12, y + 20, -4895489);
      graphics.fill(x + 4, y + height - 4, x + width - 4, y + height - 3, -15332060);
   }

   static void drawTyconSlot(GuiGraphics graphics, int x, int y) {
      graphics.fill(x, y, x + 18, y + 18, -15332060);
      graphics.fill(x + 1, y + 1, x + 17, y + 17, -11915672);
      graphics.fill(x + 1, y + 1, x + 17, y + 2, -5010727);
      graphics.fill(x + 1, y + 1, x + 2, y + 17, -5010727);
      graphics.fill(x + 1, y + 17, x + 18, y + 18, -15332060);
      graphics.fill(x + 17, y + 1, x + 18, y + 18, -15332060);
   }

   static void drawTyconInventory(GuiGraphics graphics, int leftPos, int topPos, int x, int inventoryY, int hotbarY) {
      graphics.fill(leftPos + x - 3, topPos + inventoryY - 3, leftPos + x + 163, topPos + hotbarY + 19, -15332060);
      graphics.fill(leftPos + x - 1, topPos + inventoryY - 1, leftPos + x + 161, topPos + hotbarY + 17, -11915672);

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            drawTyconSlot(graphics, leftPos + x + column * 18 - 1, topPos + inventoryY + row * 18 - 1);
         }
      }

      for (int column = 0; column < 9; column++) {
         drawTyconSlot(graphics, leftPos + x + column * 18 - 1, topPos + hotbarY - 1);
      }
   }

   static void drawTyconButton(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
      int fill = hovered ? -9156960 : -11915672;
      graphics.fill(x, y, x + width, y + height, -15332060);
      graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
      graphics.fill(x + 1, y + 1, x + width - 1, y + 2, -5010727);
      graphics.fill(x + 1, y + 1, x + 2, y + height - 1, -5010727);
   }

   static void drawWorkPanel(GuiGraphics graphics, int x, int y, int width, int height) {
      drawPanel(graphics, x, y, width, height, -6906980);
      drawPanel(graphics, x + 2, y + 2, width - 4, height - 4, -4933448);
      graphics.fill(x + 3, y + 3, x + width - 3, y + 4, -9142656);
      graphics.fill(x + 3, y + 3, x + 4, y + height - 3, -9142656);
   }

   static void drawMachineWell(GuiGraphics graphics, int x, int y) {
      drawPanel(graphics, x - 3, y - 3, 24, 24, -9406085);
      drawSlot(graphics, x, y);
      graphics.fill(x + 2, y - 3, x + 16, y - 2, -5871035);
   }

   static void drawCommonPanel(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x, y, x + width, y + height, -15131876);
      drawPanel(graphics, x + 1, y + 1, width - 2, height - 2, -6906980);
      drawPanel(graphics, x + 3, y + 3, width - 6, height - 6, -3750202);
   }

   static void drawParadoxContainer(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x, y, x + width, y + height, -16185075);
      graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -8510154);
      graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, -14211795);
      graphics.fill(x + 5, y + 5, x + width - 5, y + 18, -12898245);
      graphics.fill(x + 12, y + 18, x + width - 12, y + 19, -3194796);
      graphics.fill(x + 3, y + height - 4, x + width - 3, y + height - 3, -11655627);
   }

   static void drawParadoxSlot(GuiGraphics graphics, int x, int y) {
      graphics.fill(x, y, x + 18, y + 18, -15725034);
      graphics.fill(x + 1, y + 1, x + 17, y + 17, -12568768);
      graphics.fill(x + 1, y + 17, x + 18, y + 18, -6926236);
      graphics.fill(x + 17, y + 1, x + 18, y + 18, -6926236);
   }

   static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int fill) {
      graphics.fill(x, y, x + width, y + height, fill);
      graphics.fill(x, y, x + width, y + 1, -790037);
      graphics.fill(x, y, x + 1, y + height, -790037);
      graphics.fill(x, y + height - 1, x + width, y + height, -12236985);
      graphics.fill(x + width - 1, y, x + width, y + height, -12236985);
   }

   static void drawInsetPanel(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -7631989);
      graphics.fill(x, y, x + width - 1, y + 1, -12236985);
      graphics.fill(x, y, x + 1, y + height - 1, -12236985);
      graphics.fill(x + 1, y + height - 1, x + width, y + height, -790037);
      graphics.fill(x + width - 1, y + 1, x + width, y + height, -790037);
   }

   static void drawVerticalMeter(GuiGraphics graphics, int x, int y, int height, int value, int capacity, int color, boolean lavaTexture) {
      int width = 18;
      graphics.fill(x, y, x + width, y + height, -8947849);
      graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -14934496);
      graphics.fill(x, y, x + width - 1, y + 1, -12960448);
      graphics.fill(x, y, x + 1, y + height - 1, -12960448);
      graphics.fill(x + width - 1, y + 1, x + width, y + height, -5197648);
      graphics.fill(x + 1, y + height - 1, x + width, y + height, -5197648);
      int innerHeight = height - 4;
      int filled = capacity <= 0 ? 0 : (int)((long)innerHeight * (long)Math.max(0, value) / (long)capacity);
      if (value > 0 && capacity > 0) {
         filled = Math.max(2, filled);
      }

      filled = Math.max(0, Math.min(innerHeight, filled));
      if (filled > 0) {
         int fillTop = y + 2 + innerHeight - filled;
         if (lavaTexture) {
            int fillLeft = x + 1;
            int fillRight = x + width - 1;
            graphics.enableScissor(fillLeft, fillTop, fillRight, y + height - 2);

            for (int tileY = y + height - 18; tileY >= fillTop - 16; tileY -= 16) {
               graphics.blit(fillLeft, tileY, 0, 16, 16, ModelBakery.LAVA_FLOW.sprite());
            }

            graphics.disableScissor();
         } else {
            graphics.fill(x + 1, fillTop, x + width - 1, y + height - 2, color);
            if (filled > 2) {
               graphics.fill(x + 1, fillTop, x + 2, y + height - 2, lighten(color));
            }
         }
      }

      graphics.blit(METER_TICKS, x + 1, y, 0.0F, 0.0F, 16, height, 16, 58);
   }

   private static int lighten(int color) {
      int r = Math.min(255, (color >> 16 & 0xFF) + 28);
      int g = Math.min(255, (color >> 8 & 0xFF) + 28);
      int b = Math.min(255, (color & 0xFF) + 28);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   static void drawSlot(GuiGraphics graphics, int x, int y) {
      graphics.blit(WIDGETS, x, y, 0.0F, 0.0F, 18, 18, 128, 64);
   }

   static void drawDisabledSlot(GuiGraphics graphics, int x, int y) {
      graphics.blit(WIDGETS, x, y, 18.0F, 0.0F, 18, 18, 128, 64);
   }

   static void drawLock(GuiGraphics graphics, int x, int y) {
      graphics.blit(WIDGETS, x, y, 96.0F, 0.0F, 10, 14, 128, 64);
   }

   static void drawPlayerInventory(GuiGraphics graphics, int leftPos, int topPos, int x, int inventoryY, int hotbarY) {
      drawWorkPanel(graphics, leftPos + x - 3, topPos + inventoryY - 3, 166, hotbarY - inventoryY + 22);

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            drawSlot(graphics, leftPos + x + column * 18 - 1, topPos + inventoryY + row * 18 - 1);
         }
      }

      for (int column = 0; column < 9; column++) {
         drawSlot(graphics, leftPos + x + column * 18 - 1, topPos + hotbarY - 1);
      }
   }

   static void drawProgressArrow(GuiGraphics graphics, int x, int y, int progress, int width) {
      int scaled = width <= 0 ? 0 : Math.max(0, Math.min(28, progress * 28 / width));
      drawMekanismSmallRight(graphics, x, y, scaled);
   }

   static void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int progress) {
      int scaled = width <= 0 ? 0 : Math.max(0, Math.min(25, progress * 25 / width));
      drawMekanismBar(graphics, x, y, scaled);
   }

   static void drawMekanismSmallRight(GuiGraphics graphics, int x, int y, int filled) {
      graphics.blit(MEKANISM_SMALL_RIGHT, x, y, 0.0F, 0.0F, 28, 8, 28, 24);
      int clipped = Math.max(0, Math.min(28, filled));
      if (clipped > 0) {
         graphics.blit(MEKANISM_SMALL_RIGHT, x, y, clipped, 8, 0.0F, 8.0F, clipped, 8, 28, 24);
      }
   }

   static void drawMekanismBar(GuiGraphics graphics, int x, int y, int filled) {
      graphics.blit(MEKANISM_BAR, x, y, 0.0F, 0.0F, 25, 9, 25, 27);
      int clipped = Math.max(0, Math.min(25, filled));
      if (clipped > 0) {
         graphics.blit(MEKANISM_BAR, x, y, clipped, 9, 0.0F, 9.0F, clipped, 9, 25, 27);
      }
   }

   static void drawSmallButton(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
      int fill = hovered ? -6579301 : -7631989;
      graphics.fill(x, y, x + width, y + height, fill);
      graphics.fill(x, y, x + width, y + 1, -790037);
      graphics.fill(x, y, x + 1, y + height, -790037);
      graphics.fill(x, y + height - 1, x + width, y + height, -12236985);
      graphics.fill(x + width - 1, y, x + width, y + height, -12236985);
   }
}
