package com.industrialcrops.client.gui;

import com.industrialcrops.recipe.UltraHighPressureInvForgeRecipes;
import com.industrialcrops.screen.UltraHighPressureInvForgeMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class UltraHighPressureInvForgeScreen extends IndustrialContainerScreen<UltraHighPressureInvForgeMenu> {
   private static final int GUI_WIDTH = 300;
   private static final int GUI_HEIGHT = 194;
   private static final int LIST_X = 7;
   private static final int LIST_Y = 23;
   private static final int LIST_W = 104;
   private static final int LIST_H = 164;
   private static final int ROWS_Y = 40;
   private static final int ROW_H = 32;
   private static final int VISIBLE_ROWS = 4;
   private static final int MX = 118;
   private static final int METER_X = 275;
   private static final int METER_Y = 23;
   private static final int METER_H = 58;
   private static final int PANEL_X = 122;
   private static final int PANEL_Y = 21;
   private static final int PANEL_W = 149;
   private static final int PANEL_H = 79;
   private static final int NEEDS_Y = 75;
   private static final int STATUS_Y = 89;
   private static final int OK = -7540070;
   private static final int MISSING = -34182;
   private static final int EMBER = -19878;
   private int scroll;

   public UltraHighPressureInvForgeScreen(UltraHighPressureInvForgeMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 300;
      this.imageHeight = 194;
      this.inventoryLabelY = 101;
   }

   private int recipeCount() {
      return UltraHighPressureInvForgeRecipes.count();
   }

   private int maxScroll() {
      return Math.max(0, this.recipeCount() - 4);
   }

   private int rowWidth() {
      return 98 - (this.maxScroll() > 0 ? 4 : 0);
   }

   private int rowAt(double mouseX, double mouseY) {
      double x = mouseX - (double)this.leftPos;
      double y = mouseY - (double)this.topPos;
      if (!(x < 10.0) && !(x >= (double)(10 + this.rowWidth())) && !(y < 40.0)) {
         int row = (int)((y - 40.0) / 32.0);
         if (row < 4 && !((y - 40.0) % 32.0 >= 30.0)) {
            int index = row + this.scroll;
            return index < this.recipeCount() ? index : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static String name(ItemStack stack) {
      return ChatFormatting.stripFormatting(stack.getHoverName().getString());
   }

   private static String product(UltraHighPressureInvForgeRecipes.Recipe recipe) {
      ItemStack out = recipe.output();
      return name(out) + (out.getCount() > 1 ? " ×" + out.getCount() : "");
   }

   private void smallItem(GuiGraphics g, ItemStack stack, int x, int y, String label, int labelColor) {
      g.pose().pushPose();
      g.pose().translate((float)x, (float)y, 0.0F);
      g.pose().scale(0.75F, 0.75F, 1.0F);
      g.renderItem(stack, 0, 0);
      g.pose().popPose();
      if (label != null) {
         g.pose().pushPose();
         g.pose().translate((float)(x + 12), (float)(y + 7), 200.0F);
         g.pose().scale(0.6F, 0.6F, 1.0F);
         g.drawString(this.font, label, 0, 0, labelColor, true);
         g.pose().popPose();
      }
   }

   protected void renderBg(GuiGraphics g, float tick, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      IndustrialGuiStyle.drawTyconContainer(g, x, y, this.imageWidth, this.imageHeight);
      UltraHighPressureInvForgeRecipes.Recipe recipe = ((UltraHighPressureInvForgeMenu)this.menu).selectedRecipeData();
      inset(g, x + 7, y + 23, 104, 164);
      g.fill(x + 7 + 3, y + 40 - 3, x + 7 + 104 - 3, y + 40 - 2, -4895489);
      int hovered = this.rowAt((double)mouseX, (double)mouseY);

      for (int row = 0; row < 4; row++) {
         int index = row + this.scroll;
         if (index >= this.recipeCount()) {
            break;
         }

         int rx = x + 7 + 3;
         int ry = y + 40 + row * 32;
         int rw = this.rowWidth();
         boolean selected = index == ((UltraHighPressureInvForgeMenu)this.menu).selectedRecipe();
         int fill = selected ? -9814890 : (index == hovered ? -11586701 : -13426101);
         g.fill(rx, ry, rx + rw, ry + 32 - 2, -15332060);
         g.fill(rx + 1, ry + 1, rx + rw - 1, ry + 32 - 3, fill);
         if (selected) {
            g.fill(rx + 1, ry + 1, rx + 3, ry + 32 - 3, -4895489);
            g.fill(rx + 1, ry + 1, rx + rw - 1, ry + 2, -5010727);
         }

         g.fill(rx + 5, ry + 16, rx + rw - 4, ry + 17, 1090519039);
         UltraHighPressureInvForgeRecipes.Recipe r = UltraHighPressureInvForgeRecipes.get(index);
         g.renderItem(r.output(), rx + 4, ry + 1);
         List<ItemStack> inputs = r.inputs();

         for (int i = 0; i < inputs.size(); i++) {
            ItemStack need = inputs.get(i);
            this.smallItem(g, need, rx + 5 + i * 22, ry + 18, need.getCount() > 1 ? "×" + need.getCount() : null, -924161);
         }
      }

      if (this.maxScroll() > 0) {
         int trackX = x + 7 + 104 - 6;
         int trackY = y + 40;
         int trackH = 126;
         g.fill(trackX, trackY, trackX + 3, trackY + trackH, -15332060);
         int knobH = Math.max(10, trackH * 4 / this.recipeCount());
         int knobY = trackY + (trackH - knobH) * this.scroll / this.maxScroll();
         g.fill(trackX, knobY, trackX + 3, knobY + knobH, -5010727);
      }

      inset(g, x + 122, y + 21, 149, 79);
      int gx = x + 132 - 1;
      int gy = y + 36 - 1;
      g.fill(gx - 3, gy - 3, gx + 39, gy + 39, -15332060);
      g.fill(gx - 2, gy - 2, gx + 38, gy + 38, -11915672);

      for (int i = 0; i < 4; i++) {
         int sx = gx + i % 2 * 18;
         int sy = gy + i / 2 * 18;
         IndustrialGuiStyle.drawTyconSlot(g, sx, sy);
         if (((UltraHighPressureInvForgeMenu)this.menu).getSlot(i).getItem().isEmpty() && i < recipe.inputs().size()) {
            g.renderFakeItem(recipe.inputs().get(i), sx + 1, sy + 1);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, -1473635523);
         }
      }

      int arrowX = x + 118 + 63;
      int arrowY = y + 45 + 4;
      IndustrialGuiStyle.drawMekanismSmallRight(
         g,
         arrowX,
         arrowY,
         Math.min(28, ((UltraHighPressureInvForgeMenu)this.menu).progress() * 28 / Math.max(1, ((UltraHighPressureInvForgeMenu)this.menu).maxProgress()))
      );
      int ox = x + 221 - 5;
      int oy = y + 45 - 5;
      g.fill(ox, oy, ox + 26, oy + 26, -15332060);
      g.fill(ox + 1, oy + 1, ox + 25, oy + 25, -5010727);
      g.fill(ox + 2, oy + 2, ox + 25, oy + 25, -15069146);
      g.fill(ox + 2, oy + 2, ox + 24, oy + 24, -11915672);
      g.fill(ox - 1, oy - 2, ox + 27, oy - 1, -4895489);
      if (((UltraHighPressureInvForgeMenu)this.menu).progress() > 0) {
         int glow = (int)(48.0 + 32.0 * Math.sin((double)Util.getMillis() / 180.0));
         g.fill(ox + 2, oy + 2, ox + 24, oy + 24, glow << 24 | 16747050);
      }

      g.fill(x + 122 + 4, y + 75 - 4, x + 122 + 149 - 4, y + 75 - 3, 1353943769);
      int fx = x + 122 + 4;
      int fy = y + 89 - 1;
      boolean heat = ((UltraHighPressureInvForgeMenu)this.menu).hasHeatSource();
      int flicker = heat ? (int)(Util.getMillis() / 120L % 3L) : 0;
      int outer = heat ? -30166 : -11912610;
      int inner = heat ? -8070 : -9741696;
      g.fill(fx + 2, fy + 1 - flicker / 2, fx + 6, fy + 9, outer);
      g.fill(fx + 1, fy + 4, fx + 7, fy + 9, outer);
      g.fill(fx + 3, fy + 4, fx + 5, fy + 9, inner);
      IndustrialGuiStyle.drawVerticalMeter(g, x + 275, y + 23, 58, ((UltraHighPressureInvForgeMenu)this.menu).energy(), 100000, -3910145, false);
      IndustrialGuiStyle.drawTyconInventory(g, x, y, 128, 112, 170);
   }

   private static void inset(GuiGraphics g, int x, int y, int w, int h) {
      g.fill(x, y, x + w, y + h, -15332060);
      g.fill(x + 1, y + 1, x + w, y + h, -5010727);
      g.fill(x + 1, y + 1, x + w - 1, y + h - 1, -14609105);
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      IndustrialGuiStyle.drawFittedString(g, this.font, this.title.getString(), 8, 8, this.imageWidth - 16, -924161, true);
      IndustrialGuiStyle.drawFittedComponent(
         g,
         this.font,
         Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.recipes", new Object[]{this.recipeCount()}),
         12,
         28,
         94,
         -924161,
         false
      );

      for (int row = 0; row < 4; row++) {
         int index = row + this.scroll;
         if (index >= this.recipeCount()) {
            break;
         }

         int rx = 10;
         int ry = 40 + row * 32;
         IndustrialGuiStyle.drawFittedString(g, this.font, product(UltraHighPressureInvForgeRecipes.get(index)), rx + 22, ry + 5, this.rowWidth() - 26, -924161, false);
      }

      IndustrialGuiStyle.drawFittedString(
         g, this.font, Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.hint").getString(), 12, 175, 94, -3096603, false
      );
      UltraHighPressureInvForgeRecipes.Recipe recipe = ((UltraHighPressureInvForgeMenu)this.menu).selectedRecipeData();
      IndustrialGuiStyle.drawFittedString(
         g,
         this.font,
         Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.selected", new Object[]{product(recipe)}).getString(),
         127,
         25,
         139,
         -924161,
         false
      );
      IndustrialGuiStyle.drawFittedString(
         g, this.font, Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.needs_title").getString(), 127, 77, 30, -3096603, false
      );
      List<ItemStack> inputs = recipe.inputs();
      int step = Math.min(30, 109 / Math.max(1, inputs.size()));

      for (int i = 0; i < inputs.size(); i++) {
         ItemStack need = inputs.get(i);
         int have = ((UltraHighPressureInvForgeMenu)this.menu).inputCount(need);
         this.smallItem(g, need, 159 + i * step, 75, have + "/" + need.getCount(), have >= need.getCount() ? -7540070 : -34182);
      }

      boolean heat = ((UltraHighPressureInvForgeMenu)this.menu).hasHeatSource();
      IndustrialGuiStyle.drawFittedString(
         g,
         this.font,
         Component.translatable(heat ? "gui.industrialcrops.ultra_high_pressure_inv_forge.heat_ready" : "gui.industrialcrops.ultra_high_pressure_inv_forge.no_heat")
            .getString(),
         136,
         89,
         64,
         heat ? -19878 : -3096603,
         false
      );
      String cost = Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.cost", new Object[]{recipe.ticks() / 20, recipe.energyPerTick()})
         .getString();
      int cw = Math.min(this.font.width(cost), 63);
      IndustrialGuiStyle.drawFittedString(g, this.font, cost, 266 - cw, 89, cw, -3096603, false);
      g.drawString(this.font, this.playerInventoryTitle, 128, this.inventoryLabelY, -3096603, false);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float tick) {
      super.render(g, mouseX, mouseY, tick);
      int row = this.rowAt((double)mouseX, (double)mouseY);
      if (row >= 0) {
         UltraHighPressureInvForgeRecipes.Recipe recipe = UltraHighPressureInvForgeRecipes.get(row);
         List<FormattedCharSequence> lines = new ArrayList<>();
         lines.add(Component.literal(product(recipe)).withStyle(ChatFormatting.WHITE).getVisualOrderText());
         lines.add(Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.needs_title").withStyle(ChatFormatting.GRAY).getVisualOrderText());

         for (ItemStack need : recipe.inputs()) {
            lines.add(
               Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.ingredient", new Object[]{need.getCount(), need.getHoverName()})
                  .withStyle(ChatFormatting.GRAY)
                  .getVisualOrderText()
            );
         }

         lines.add(
            Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.cost", new Object[]{recipe.ticks() / 20, recipe.energyPerTick()})
               .withStyle(ChatFormatting.DARK_PURPLE)
               .getVisualOrderText()
         );
         if (row != ((UltraHighPressureInvForgeMenu)this.menu).selectedRecipe()) {
            lines.add(Component.translatable("gui.industrialcrops.ultra_high_pressure_inv_forge.click").withStyle(ChatFormatting.LIGHT_PURPLE).getVisualOrderText());
         }

         g.renderTooltip(this.font, lines, mouseX, mouseY);
      } else if (mouseX >= this.leftPos + 275 && mouseX < this.leftPos + 275 + 18 && mouseY >= this.topPos + 23 && mouseY < this.topPos + 23 + 58) {
         g.renderTooltip(
            this.font,
            Component.translatable(
               "gui.industrialcrops.energy",
               new Object[]{String.format("%,d", ((UltraHighPressureInvForgeMenu)this.menu).energy()), String.format("%,d", 100000)}
            ),
            mouseX,
            mouseY
         );
      } else {
         this.renderTooltip(g, mouseX, mouseY);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int row = this.rowAt(mouseX, mouseY);
      if (button == 0 && row >= 0) {
         if (row != ((UltraHighPressureInvForgeMenu)this.menu).selectedRecipe() && this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(((UltraHighPressureInvForgeMenu)this.menu).containerId, 100 + row);
         }

         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.maxScroll() > 0
         && mouseX >= (double)(this.leftPos + 7)
         && mouseX < (double)(this.leftPos + 7 + 104)
         && mouseY >= (double)(this.topPos + 23)
         && mouseY < (double)(this.topPos + 23 + 164)) {
         this.scroll = Mth.clamp(this.scroll - (int)Math.signum(scrollY), 0, this.maxScroll());
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }
}
