package com.industrialcrops.client.gui;

import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.ApUpgrades;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.ApMachineMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;

abstract class ApMachineScreen<M extends AbstractContainerMenu & ApMachineMenu> extends IndustrialContainerScreen<M> {
   static final int TAB_X = -22;
   static final int TAB_W = 24;
   static final int TAB_H = 22;
   static final int TAB_UPGRADES_Y = 26;
   static final int TAB_HOLO_Y = 50;
   static final int PANEL_X = -112;
   static final int PANEL_W = 90;
   static final int PANEL_Y = 22;
   static final int PANEL_H = 112;
   static final int HOLO = -10492929;
   static final int HOLO_DIM = -13926506;
   private static ApMachineScreen.Panel lastPanel = ApMachineScreen.Panel.NONE;
   private ApMachineScreen.Panel panel = ApMachineScreen.Panel.NONE;
   private ApGuiStyle.ApButton projectionButton;

   protected ApMachineScreen(M var1, Inventory var2, Component var3) {
      super((M)var1, var2, var3);
   }

   protected abstract String machineKey();

   protected Object automaticDetail() {
      return null;
   }

   protected void init() {
      super.init();
      this.projectionButton = (ApGuiStyle.ApButton)this.addRenderableWidget(
         new ApGuiStyle.ApButton(this.leftPos + -112 + 6, this.topPos + 22 + 24, 78, 14, Component.empty(), var1 -> this.click(20))
      );
      this.setPanel(lastPanel);
      this.updateProjectionButton();
   }

   protected void click(int var1) {
      if (this.minecraft != null && this.minecraft.gameMode != null) {
         this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, var1);
      }
   }

   private void setPanel(ApMachineScreen.Panel var1) {
      this.panel = var1;
      lastPanel = var1;
      ((ApMachineMenu)this.menu).setUpgradesVisible(var1 == ApMachineScreen.Panel.UPGRADES);
      if (this.projectionButton != null) {
         this.projectionButton.visible = var1 == ApMachineScreen.Panel.HOLOGRAM;
      }
   }

   private void updateProjectionButton() {
      boolean var1 = ((ApMachineMenu)this.menu).projection();
      this.projectionButton.setMessage(Component.translatable(var1 ? "gui.industrialcrops.ap.projection.on" : "gui.industrialcrops.ap.projection.off"));
      this.projectionButton.setLit(var1);
   }

   protected void containerTick() {
      super.containerTick();
      this.updateProjectionButton();
   }

   public void removed() {
      ((ApMachineMenu)this.menu).setUpgradesVisible(false);
      super.removed();
   }

   private boolean overTab(double var1, double var3, int var5) {
      double var6 = var1 - (double)this.leftPos;
      double var8 = var3 - (double)this.topPos;
      return var6 >= -22.0 && var6 < 0.0 && var8 >= (double)var5 && var8 < (double)(var5 + 22);
   }

   private boolean overPanel(double var1, double var3) {
      if (this.panel == ApMachineScreen.Panel.NONE) {
         return false;
      } else {
         double var5 = var1 - (double)this.leftPos;
         double var7 = var3 - (double)this.topPos;
         return var5 >= -112.0 && var5 < -22.0 && var7 >= 22.0 && var7 < 134.0;
      }
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      ApMachineScreen.Panel var6 = this.overTab(var1, var3, 26)
         ? ApMachineScreen.Panel.UPGRADES
         : (this.overTab(var1, var3, 50) ? ApMachineScreen.Panel.HOLOGRAM : null);
      if (var6 != null && var5 == 0) {
         this.setPanel(this.panel == var6 ? ApMachineScreen.Panel.NONE : var6);
         if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

         return true;
      } else {
         return super.mouseClicked(var1, var3, var5);
      }
   }

   protected boolean hasClickedOutside(double var1, double var3, int var5, int var6, int var7) {
      return super.hasClickedOutside(var1, var3, var5, var6, var7)
         && !this.overPanel(var1, var3)
         && !this.overTab(var1, var3, 26)
         && !this.overTab(var1, var3, 50);
   }

   protected void renderSidePanels(GuiGraphics var1) {
      int var2 = this.leftPos;
      int var3 = this.topPos;
      tab(var1, var2 + -22, var3 + 26, this.panel == ApMachineScreen.Panel.UPGRADES);
      var1.renderItem(new ItemStack((ItemLike)ModItems.SPEED_COMPONENT_2.get()), var2 + -22 + 3, var3 + 26 + 3);
      tab(var1, var2 + -22, var3 + 50, this.panel == ApMachineScreen.Panel.HOLOGRAM);
      hologramIcon(var1, var2 + -22 + 4, var3 + 50 + 4, ((ApMachineMenu)this.menu).projection());
      if (this.panel != ApMachineScreen.Panel.NONE) {
         ApGuiStyle.container(var1, var2 + -112, var3 + 22, 90, 112);
         if (this.panel == ApMachineScreen.Panel.UPGRADES) {
            ApGuiStyle.well(var1, var2 + -84 - 4, var3 + 48 - 4, 44, 44);

            for (int var4 = 0; var4 < 4; var4++) {
               ApGuiStyle.slot(var1, var2 + -84 - 1 + var4 % 2 * 18, var3 + 48 - 1 + var4 / 2 * 18);
            }

            ApGuiStyle.well(var1, var2 + -112 + 5, var3 + 90, 80, 38);
         } else {
            ApGuiStyle.well(var1, var2 + -112 + 5, var3 + 22 + 42, 80, 65);
         }
      }
   }

   private static void tab(GuiGraphics var0, int var1, int var2, boolean var3) {
      var0.fill(var1, var2, var1 + 24, var2 + 22, -15395045);
      var0.fill(var1 + 1, var2 + 1, var1 + 24, var2 + 22 - 1, -5656132);
      var0.fill(var1 + 2, var2 + 2, var1 + 24, var2 + 22 - 1, var3 ? -12499121 : -13749704);
      if (var3) {
         var0.fill(var1 + 1, var2 + 2, var1 + 3, var2 + 22 - 2, -1529542);
      }
   }

   private static void hologramIcon(GuiGraphics var0, int var1, int var2, boolean var3) {
      int var4 = var3 ? -10492929 : -8551024;
      int var5 = var3 ? -13926506 : -11906983;
      var0.fill(var1 + 4, var2 + 1, var1 + 14, var2 + 11, var5 & 1627389951);
      outline(var0, var1 + 4, var2 + 1, 10, 10, var5);
      var0.fill(var1 + 1, var2 + 4, var1 + 11, var2 + 14, var4 & 1358954495);
      outline(var0, var1 + 1, var2 + 4, 10, 10, var4);

      for (int var6 = 1; var6 < 3; var6++) {
         var0.fill(var1 + 1 + var6, var2 + 4 - var6, var1 + 2 + var6, var2 + 5 - var6, var4);
         var0.fill(var1 + 10 + var6, var2 + 4 - var6, var1 + 11 + var6, var2 + 5 - var6, var4);
         var0.fill(var1 + 10 + var6, var2 + 13 - var6, var1 + 11 + var6, var2 + 14 - var6, var4);
      }

      if (var3) {
         var0.fill(var1 + 5, var2 + 8, var1 + 7, var2 + 10, -1);
      }
   }

   private static void outline(GuiGraphics var0, int var1, int var2, int var3, int var4, int var5) {
      var0.fill(var1, var2, var1 + var3, var2 + 1, var5);
      var0.fill(var1, var2 + var4 - 1, var1 + var3, var2 + var4, var5);
      var0.fill(var1, var2, var1 + 1, var2 + var4, var5);
      var0.fill(var1 + var3 - 1, var2, var1 + var3, var2 + var4, var5);
   }

   protected void renderSideLabels(GuiGraphics var1) {
      if (this.panel != ApMachineScreen.Panel.NONE) {
         byte var2 = -103;
         byte var3 = 72;
         if (this.panel == ApMachineScreen.Panel.UPGRADES) {
            ApGuiStyle.text(var1, this.font, Component.translatable("gui.industrialcrops.ap.upgrades").getString(), var2, 30, var3, -920586);
            IItemHandler var4 = ((ApMachineMenu)this.menu).upgrades();
            int var5 = ApUpgrades.speedTier(var4);
            int var6 = ApUpgrades.savingPercent(var4);
            boolean var7 = ApUpgrades.automatic(var4);
            boolean var8 = !ApUpgrades.dimension(var4).isEmpty();
            byte var9 = -103;
            byte var10 = 72;
            this.effect(
               var1, Component.translatable("gui.industrialcrops.ap.upgrade.speed", new Object[]{var5 == 0 ? "-" : roman(var5)}), var9, 93, var10, var5 > 0
            );
            this.effect(var1, Component.translatable("gui.industrialcrops.ap.upgrade.power", new Object[]{var6}), var9, 102, var10, var6 > 0);
            Object var11 = this.automaticDetail();
            this.effect(
               var1,
               var11 != null && var7
                  ? Component.translatable("gui.industrialcrops." + this.machineKey() + ".upgrade.auto_count", new Object[]{var11})
                  : Component.translatable("gui.industrialcrops." + this.machineKey() + ".upgrade.auto"),
               var9,
               111,
               var10,
               var7
            );
            this.effect(var1, Component.translatable("gui.industrialcrops.ap.upgrade.chunk"), var9, 120, var10, var8);
         } else {
            ApGuiStyle.text(var1, this.font, Component.translatable("gui.industrialcrops.ap.projection").getString(), var2, 30, var3, -920586);
            ApStructure.Status var12 = ((ApMachineMenu)this.menu).structure();
            byte var13 = -103;
            byte var14 = 72;
            byte var15 = 68;
            if (var12.formed()) {
               ApGuiStyle.text(
                  var1, this.font, "✔ " + Component.translatable("gui.industrialcrops.ap.projection.complete").getString(), var13, var15, var14, -8396660
               );
            } else {
               ApScreens.line(
                  var1,
                  this.font,
                  Component.translatable("gui.industrialcrops.ap.casings", new Object[]{var12.casings(), 8}),
                  var13,
                  var15,
                  var14,
                  var12.casings() == 8
               );
               ApScreens.line(
                  var1,
                  this.font,
                  Component.translatable("gui.industrialcrops.ap.frames", new Object[]{var12.frames(), 8}),
                  var13,
                  var15 + 9,
                  var14,
                  var12.frames() == 8
               );
               ApScreens.line(var1, this.font, Component.translatable("gui.industrialcrops.ap.core"), var13, var15 + 18, var14, var12.core());
               ApScreens.line(var1, this.font, Component.translatable("gui.industrialcrops.ap.cap"), var13, var15 + 27, var14, var12.cap());
            }

            ApGuiStyle.text(var1, this.font, Component.translatable("gui.industrialcrops.ap.projection.ghost").getString(), var13, var15 + 39, var14, -10492929);
            ApGuiStyle.text(var1, this.font, Component.translatable("gui.industrialcrops.ap.projection.wrong").getString(), var13, var15 + 49, var14, -38037);
         }
      }
   }

   private void effect(GuiGraphics var1, Component var2, int var3, int var4, int var5, boolean var6) {
      ApGuiStyle.text(var1, this.font, (var6 ? "● " : "○ ") + var2.getString(), var3, var4, var5, var6 ? -1529542 : -4801081);
   }

   static String roman(int var0) {
      return switch (var0) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         default -> "-";
      };
   }

   protected boolean renderSideTooltips(GuiGraphics var1, int var2, int var3) {
      if (this.overTab((double)var2, (double)var3, 26)) {
         var1.renderTooltip(this.font, Component.translatable("gui.industrialcrops.ap.upgrades"), var2, var3);
         return true;
      } else if (this.overTab((double)var2, (double)var3, 50)) {
         List var10 = List.of(
            Component.translatable("gui.industrialcrops.ap.projection"),
            Component.translatable(((ApMachineMenu)this.menu).projection() ? "gui.industrialcrops.ap.projection.on" : "gui.industrialcrops.ap.projection.off")
               .withStyle(((ApMachineMenu)this.menu).projection() ? ChatFormatting.AQUA : ChatFormatting.GRAY)
         );
         var1.renderComponentTooltip(this.font, var10, var2, var3);
         return true;
      } else {
         if (this.panel == ApMachineScreen.Panel.UPGRADES
            && this.overPanel((double)var2, (double)var3)
            && (this.hoveredSlot == null || !this.hoveredSlot.hasItem())) {
            int var4 = var3 - this.topPos;
            if (var4 >= 90) {
               ArrayList var5 = new ArrayList();
               var5.add(Component.translatable("gui.industrialcrops.ap.upgrade.help").withStyle(ChatFormatting.GOLD).getVisualOrderText());

               for (String var9 : new String[]{
                  this.machineKey() + ".upgrade.speed_help", "ap.upgrade.power_help", this.machineKey() + ".upgrade.auto_help", "ap.upgrade.chunk_help"
               }) {
                  var5.addAll(this.font.split(Component.translatable("gui.industrialcrops." + var9).withStyle(ChatFormatting.GRAY), 200));
               }

               var1.renderTooltip(this.font, var5, var2, var3);
               return true;
            }
         }

         return false;
      }
   }

   static enum Panel {
      NONE,
      UPGRADES,
      HOLOGRAM;
   }
}
