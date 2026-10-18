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
import net.minecraftforge.items.IItemHandler;
abstract class ApMachineScreen<M extends AbstractContainerMenu & ApMachineMenu> extends IndustrialContainerScreen<M> {
   static final int TAB_X = -22;
   static final int TAB_W = 24;
   static final int TAB_H = 22;
   static final int TAB_UPGRADES_Y = 26;
   static final int TAB_HOLO_Y = 50;
   static final int PANEL_X = -112;
   static final int PANEL_W = 90;
   static final int PANEL_Y = 22;
   static final int PANEL_H = 166;
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
         return var5 >= -112.0 && var5 < -22.0 && var7 >= 22.0 && var7 < 188.0;
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
         ApGuiStyle.container(var1, var2 + -112, var3 + 22, 90, PANEL_H);
         if (this.panel == ApMachineScreen.Panel.UPGRADES) {
            ApGuiStyle.well(var1, var2 + -84 - 4, var3 + 48 - 4, 44, 62);
            for (int var4 = 0; var4 < ApUpgrades.SLOTS; var4++) {
               ApGuiStyle.slot(var1, var2 + -84 - 1 + var4 % 2 * 18, var3 + 48 - 1 + var4 / 2 * 18);
            }
            ApGuiStyle.well(var1, var2 + -112 + 5, var3 + 108, 80, 74);
         } else {
            ApGuiStyle.well(var1, var2 + -112 + 5, var3 + 22 + 42, 80, 118);
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
   protected void renderSideLabels(GuiGraphics graphics) {
      if (this.panel == Panel.NONE) return;
      int x = -103, width = 72;
      ApStructure.Status status = ((ApMachineMenu)this.menu).structure();
      if (this.panel == Panel.UPGRADES) {
         ApGuiStyle.text(graphics, this.font, Component.translatable("gui.industrialcrops.ap.upgrades").getString(), x, 30, width, ApGuiStyle.TEXT);
         IItemHandler upgrades = ((ApMachineMenu)this.menu).upgrades();
         int speed = ApUpgrades.speedTier(upgrades);
         int saving = ApUpgrades.savingPercent(upgrades);
         boolean automatic = ApUpgrades.automatic(upgrades);
         this.effect(graphics, Component.translatable("gui.industrialcrops.ap.upgrade.speed", speed == 0 ? "-" : roman(speed)), x, 112, width, speed > 0);
         this.effect(graphics, Component.translatable("gui.industrialcrops.ap.upgrade.power", saving), x, 123, width, saving > 0);
         Object detail = this.automaticDetail();
         Component autoText = detail != null && automatic
            ? Component.translatable("gui.industrialcrops." + this.machineKey() + ".upgrade.auto_count", detail)
            : Component.translatable("gui.industrialcrops." + this.machineKey() + ".upgrade.auto");
         this.effect(graphics, autoText, x, 134, width, automatic);
         this.effect(graphics, Component.translatable("gui.industrialcrops.ap.upgrade.chunk"), x, 145, width, !ApUpgrades.dimension(upgrades).isEmpty());
         this.effect(graphics, Component.translatable("gui.industrialcrops.ap.expansion", status.tier()), x, 156, width, status.tier() > 0);
         ApGuiStyle.text(graphics, this.font, ApScreens.footprint(status).getString(), x, 168, width, ApGuiStyle.MUTED);
      } else {
         ApGuiStyle.text(graphics, this.font, Component.translatable("gui.industrialcrops.ap.projection").getString(), x, 30, width, ApGuiStyle.TEXT);
         ApGuiStyle.text(graphics, this.font, ApScreens.footprint(status).getString(), x, 69, width, ApGuiStyle.TEXT);
         ApGuiStyle.text(graphics, this.font, Component.translatable("gui.industrialcrops.ap.checks", status.completedCategories(), 9).getString(), x, 81, width, status.formed() ? ApGuiStyle.OK : ApGuiStyle.BAD);
         ApGuiStyle.text(graphics, this.font, Component.translatable(status.loaded() ? "gui.industrialcrops.ap.details" : "gui.industrialcrops.ap.unloaded").getString(), x, 93, width, status.loaded() ? ApGuiStyle.MUTED : ApGuiStyle.BAD);
         ApGuiStyle.text(graphics, this.font, Component.translatable("gui.industrialcrops.ap.projection.ghost").getString(), x, 112, width, HOLO);
         ApGuiStyle.text(graphics, this.font, Component.translatable("gui.industrialcrops.ap.projection.wrong").getString(), x, 124, width, ApGuiStyle.BAD);
         ApGuiStyle.text(graphics, this.font, Component.translatable("gui.industrialcrops.ap.projection.budget").getString(), x, 142, width, ApGuiStyle.MUTED);
         ApGuiStyle.text(graphics, this.font, Component.translatable("gui.industrialcrops.ap.projection.move").getString(), x, 154, width, ApGuiStyle.MUTED);
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
         if (this.panel == Panel.HOLOGRAM && this.overPanel((double)var2, (double)var3) && var3 >= this.topPos + 64) {
            ArrayList<net.minecraft.util.FormattedCharSequence> lines = new ArrayList<>();
            ApScreens.addStructureHelp(lines, this.font, ((ApMachineMenu)this.menu).structure());
            var1.renderTooltip(this.font, lines, var2, var3);
            return true;
         }
         if (this.panel == ApMachineScreen.Panel.UPGRADES
            && this.overPanel((double)var2, (double)var3)
            && (this.hoveredSlot == null || !this.hoveredSlot.hasItem())) {
            int var4 = var3 - this.topPos;
            if (var4 >= 108) {
               ArrayList var5 = new ArrayList();
               var5.add(Component.translatable("gui.industrialcrops.ap.upgrade.help").withStyle(ChatFormatting.GOLD).getVisualOrderText());
               for (String var9 : new String[]{
                  this.machineKey() + ".upgrade.speed_help", "ap.upgrade.power_help", this.machineKey() + ".upgrade.auto_help", "ap.upgrade.chunk_help", "ap.upgrade.expansion_help"
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
