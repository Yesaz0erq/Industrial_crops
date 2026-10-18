package com.industrialcrops.client.gui;

import com.industrialcrops.machine.ApStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

final class ApGuiStyle {
   static final int SHADOW = -15395045;
   static final int PANEL = -13749704;
   static final int SURFACE = -12499121;
   static final int HIGHLIGHT = -5656132;
   static final int WELL = -14868443;
   static final int TEXT = -920586;
   static final int MUTED = -4801081;
   static final int ACCENT = -1529542;
   static final int ACCENT_DIM = -7707868;
   static final int OK = -8396660;
   static final int BAD = -38037;
   static final int ENERGY = -1529542;

   private ApGuiStyle() {
   }

   static void container(GuiGraphics var0, int var1, int var2, int var3, int var4) {
      var0.fill(var1, var2, var1 + var3, var2 + var4, -15395045);
      var0.fill(var1 + 1, var2 + 1, var1 + var3 - 1, var2 + var4 - 1, -5656132);
      var0.fill(var1 + 2, var2 + 2, var1 + var3 - 1, var2 + var4 - 1, -10788244);
      var0.fill(var1 + 3, var2 + 3, var1 + var3 - 3, var2 + var4 - 3, -13749704);
      var0.fill(var1 + 6, var2 + 5, var1 + var3 - 6, var2 + 18, -12499121);
      var0.fill(var1 + 6, var2 + 5, var1 + var3 - 6, var2 + 6, -5656132);

      for (int var5 = var1 + 6; var5 < var1 + var3 - 6; var5 += 6) {
         var0.fill(var5, var2 + 18, Math.min(var5 + 3, var1 + var3 - 6), var2 + 20, -1529542);
         var0.fill(Math.min(var5 + 3, var1 + var3 - 6), var2 + 18, Math.min(var5 + 6, var1 + var3 - 6), var2 + 20, -14276306);
      }
   }

   static void well(GuiGraphics var0, int var1, int var2, int var3, int var4) {
      var0.fill(var1, var2, var1 + var3, var2 + var4, -15395045);
      var0.fill(var1 + 1, var2 + 1, var1 + var3, var2 + var4, -9472126);
      var0.fill(var1 + 1, var2 + 1, var1 + var3 - 1, var2 + var4 - 1, -14868443);
   }

   static void slot(GuiGraphics var0, int var1, int var2) {
      var0.fill(var1, var2, var1 + 18, var2 + 18, -15395045);
      var0.fill(var1 + 1, var2 + 1, var1 + 18, var2 + 18, -9472126);
      var0.fill(var1 + 1, var2 + 1, var1 + 17, var2 + 17, -14276048);
   }

   static void slots(GuiGraphics var0, int var1, int var2, int var3) {
      for (int var4 = 0; var4 < var3; var4++) {
         slot(var0, var1 - 1 + var4 * 18, var2 - 1);
      }
   }

   static void playerInventory(GuiGraphics var0, int var1, int var2, int var3) {
      for (int var4 = 0; var4 < 3; var4++) {
         slots(var0, var1, var2 + var4 * 18, 9);
      }

      slots(var0, var1, var3, 9);
   }

   static void bar(GuiGraphics var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      well(var0, var1 - 1, var2 - 1, var3 + 2, var4 + 2);
      int var8 = var6 <= 0 ? 0 : (int)((long)var3 * (long)Math.max(0, var5) / (long)var6);
      if (var8 > 0) {
         var0.fill(var1, var2, var1 + var8, var2 + var4, var7);
         var0.fill(var1, var2, var1 + var8, var2 + 1, 1627389951);
      }
   }

   static void text(GuiGraphics var0, Font var1, String var2, int var3, int var4, int var5, int var6) {
      IndustrialGuiStyle.drawFittedString(var0, var1, var2, var3, var4, var5, var6, false);
   }

   static void structureMap(GuiGraphics graphics, int x, int y, ApStructure.Status status) {
      ApStructure.Category[] categories = ApStructure.Category.values();
      for (int index = 0; index < categories.length; index++) {
         int px = x + index % 3 * 12;
         int py = y + index / 3 * 12;
         int color = status.complete(categories[index]) ? OK : BAD;
         graphics.fill(px, py, px + 10, py + 10, SHADOW);
         graphics.fill(px + 1, py + 1, px + 9, py + 9, color);
         graphics.fill(px + 1, py + 1, px + 9, py + 2, 1358954495);
      }
   }

   static final class ApButton extends Button {
      private boolean lit;

      ApButton(int var1, int var2, int var3, int var4, Component var5, OnPress var6) {
         super(var1, var2, var3, var4, var5, var6, DEFAULT_NARRATION);
      }

      void setLit(boolean var1) {
         this.lit = var1;
      }

      protected void renderWidget(GuiGraphics var1, int var2, int var3, float var4) {
         int var5 = this.getX();
         int var6 = this.getY();
         int var7 = this.getWidth();
         int var8 = this.getHeight();
         var1.fill(var5, var6, var5 + var7, var6 + var8, -15395045);
         var1.fill(var5 + 1, var6 + 1, var5 + var7 - 1, var6 + var8 - 1, this.isHoveredOrFocused() ? -10919572 : -12499121);
         var1.fill(var5 + 1, var6 + 1, var5 + var7 - 1, var6 + 2, -5656132);
         if (this.lit) {
            var1.fill(var5 + 1, var6 + var8 - 3, var5 + var7 - 1, var6 + var8 - 1, -1529542);
         }

         this.renderScrollingString(var1, Minecraft.getInstance().font, 2, this.lit ? -920586 : -4801081);
      }
   }
}
