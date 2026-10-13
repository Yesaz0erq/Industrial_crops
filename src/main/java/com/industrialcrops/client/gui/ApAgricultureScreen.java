package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.ApAgricultureBlockEntity;
import com.industrialcrops.machine.ApUpgrades;
import com.industrialcrops.screen.ApAgricultureMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ApAgricultureScreen extends ApMachineScreen<ApAgricultureMenu> {
   static final int W = 230;
   static final int H = 234;
   private ApGuiStyle.ApButton less;
   private ApGuiStyle.ApButton more;

   public ApAgricultureScreen(ApAgricultureMenu var1, Inventory var2, Component var3) {
      super(var1, var2, var3);
      this.imageWidth = 230;
      this.imageHeight = 234;
   }

   @Override
   protected String machineKey() {
      return "ap_agriculture";
   }

   @Override
   protected Object automaticDetail() {
      return String.format("%,d", ((ApAgricultureMenu)this.menu).harvested());
   }

   @Override
   protected void init() {
      super.init();
      this.less = (ApGuiStyle.ApButton)this.addRenderableWidget(
         new ApGuiStyle.ApButton(this.leftPos + 126, this.topPos + 55, 14, 13, Component.literal("-"), var1 -> this.click(0))
      );
      this.more = (ApGuiStyle.ApButton)this.addRenderableWidget(
         new ApGuiStyle.ApButton(this.leftPos + 206, this.topPos + 55, 14, 13, Component.literal("+"), var1 -> this.click(1))
      );
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      this.less.active = ((ApAgricultureMenu)this.menu).boost() > 0;
      this.more.active = ((ApAgricultureMenu)this.menu).boost() < 4;
   }

   protected void renderBg(GuiGraphics var1, float var2, int var3, int var4) {
      int var5 = this.leftPos;
      int var6 = this.topPos;
      this.renderSidePanels(var1);
      ApGuiStyle.container(var1, var5, var6, this.imageWidth, this.imageHeight);
      ApGuiStyle.well(var1, var5 + 8, var6 + 24, 108, 68);
      ApGuiStyle.well(var1, var5 + 120, var6 + 24, 102, 68);
      ApGuiStyle.structureMap(var1, var5 + 14, var6 + 40, ((ApAgricultureMenu)this.menu).structure());
      ApGuiStyle.bar(var1, var5 + 125, var6 + 29, 92, 5, ((ApAgricultureMenu)this.menu).energy(), 1000000, -1529542);

      for (int var7 = 0; var7 < 4; var7++) {
         int var8 = var5 + 145 + var7 * 15;
         boolean var9 = var7 < ((ApAgricultureMenu)this.menu).boost();
         var1.fill(var8, var6 + 57, var8 + 12, var6 + 66, -15395045);
         var1.fill(var8 + 1, var6 + 58, var8 + 11, var6 + 65, var9 ? -1529542 : -12959930);
         if (var9) {
            var1.fill(var8 + 1, var6 + 58, var8 + 11, var6 + 59, -7520);
         }
      }

      ApGuiStyle.slots(var1, var5 + 34, var6 + 104, 9);
      ApGuiStyle.slots(var1, var5 + 34, var6 + 122, 9);
      var1.fill(var5 + 34 - 2, var6 + 122 - 1, var5 + 34 - 1, var6 + 122 + 17, -1529542);
      ApGuiStyle.playerInventory(var1, var5 + 34, var6 + 152, var6 + 210);
   }

   @Override
   protected void renderLabels(GuiGraphics var1, int var2, int var3) {
      ApGuiStyle.text(var1, this.font, this.title.getString(), 9, 8, this.imageWidth - 18, -920586);
      ApScreens.structureText(var1, this.font, ((ApAgricultureMenu)this.menu).structure());
      this.renderSideLabels(var1);
      ApGuiStyle.text(
         var1,
         this.font,
         Component.translatable("gui.industrialcrops.ap.energy", new Object[]{String.format("%,d", ((ApAgricultureMenu)this.menu).energy())}).getString(),
         125,
         37,
         92,
         -4801081
      );
      ApGuiStyle.text(
         var1,
         this.font,
         Component.translatable(
               "gui.industrialcrops.ap_agriculture.boost",
               new Object[]{
                  ((ApAgricultureMenu)this.menu).boost(),
                  ((ApAgricultureMenu)this.menu).boost()
                     * ApAgricultureBlockEntity.ticksPerLevel(ApUpgrades.speedTier(((ApAgricultureMenu)this.menu).upgrades()))
               }
            )
            .getString(),
         125,
         46,
         92,
         -920586
      );
      int var4 = ((ApAgricultureMenu)this.menu).status();
      ApGuiStyle.text(
         var1,
         this.font,
         Component.translatable("gui.industrialcrops.ap_agriculture.status." + var4).getString(),
         12,
         82,
         100,
         var4 == 0 ? -8396660 : (var4 == 4 ? -4801081 : -38037)
      );
      ApGuiStyle.text(
         var1,
         this.font,
         Component.translatable("gui.industrialcrops.ap_agriculture.boosted", new Object[]{((ApAgricultureMenu)this.menu).boostedThousands()}).getString(),
         125,
         76,
         92,
         -4801081
      );
      ApGuiStyle.text(var1, this.font, Component.translatable("gui.industrialcrops.ap_agriculture.seeds").getString(), 34, 94, 60, -4801081);
      String var5 = Component.translatable(
            "gui.industrialcrops.ap_agriculture.stats",
            new Object[]{((ApAgricultureMenu)this.menu).crops(), String.format("%,d", ((ApAgricultureMenu)this.menu).planted())}
         )
         .getString();
      int var6 = Math.min(this.font.width(var5), 100);
      ApGuiStyle.text(var1, this.font, var5, 196 - var6, 94, var6, -4801081);
      var1.drawString(this.font, this.playerInventoryTitle, 34, 142, -4801081, false);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      if (!this.renderSideTooltips(var1, var2, var3)) {
         if (!ApScreens.structureTooltip(
            var1, this.font, this.leftPos, this.topPos, var2, var3, ((ApAgricultureMenu)this.menu).structure(), "automatic_sowing_device"
         )) {
            this.renderTooltip(var1, var2, var3);
         }
      }
   }
}
