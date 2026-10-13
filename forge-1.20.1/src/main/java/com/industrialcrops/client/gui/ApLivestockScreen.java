package com.industrialcrops.client.gui;
import com.industrialcrops.screen.ApLivestockMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class ApLivestockScreen extends ApMachineScreen<ApLivestockMenu> {
   private static final String[] TASKS = new String[]{"breed", "shear", "cull", "grow"};
   private final ApGuiStyle.ApButton[] toggles = new ApGuiStyle.ApButton[4];
   private ApGuiStyle.ApButton cap;
   public ApLivestockScreen(ApLivestockMenu var1, Inventory var2, Component var3) {
      super(var1, var2, var3);
      this.imageWidth = 230;
      this.imageHeight = 234;
   }
   @Override
   protected String machineKey() {
      return "ap_livestock";
   }
   @Override
   protected void init() {
      super.init();
      for (int var1 = 0; var1 < 4; var1++) {
         int var2 = var1;
         this.toggles[var1] = (ApGuiStyle.ApButton)this.addRenderableWidget(
            new ApGuiStyle.ApButton(
               this.leftPos + 124 + var1 % 2 * 48,
               this.topPos + 44 + var1 / 2 * 15,
               46,
               14,
               Component.translatable("gui.industrialcrops.ap_livestock." + TASKS[var1]),
               var2x -> this.click(var2)
            )
         );
      }
      this.cap = (ApGuiStyle.ApButton)this.addRenderableWidget(
         new ApGuiStyle.ApButton(this.leftPos + 124, this.topPos + 74, 94, 14, Component.empty(), var1x -> this.click(4))
      );
   }
   @Override
   protected void containerTick() {
      super.containerTick();
      for (int var1 = 0; var1 < 4; var1++) {
         this.toggles[var1].setLit((((ApLivestockMenu)this.menu).flags() & 1 << var1) != 0);
      }
      this.cap.setMessage(Component.translatable("gui.industrialcrops.ap_livestock.cap", new Object[]{((ApLivestockMenu)this.menu).cap()}));
      this.cap.setLit(true);
   }
   protected void renderBg(GuiGraphics var1, float var2, int var3, int var4) {
      int var5 = this.leftPos;
      int var6 = this.topPos;
      this.renderSidePanels(var1);
      ApGuiStyle.container(var1, var5, var6, this.imageWidth, this.imageHeight);
      ApGuiStyle.well(var1, var5 + 8, var6 + 24, 108, 68);
      ApGuiStyle.well(var1, var5 + 120, var6 + 24, 102, 68);
      ApGuiStyle.structureMap(var1, var5 + 14, var6 + 40, ((ApLivestockMenu)this.menu).structure());
      ApGuiStyle.bar(var1, var5 + 125, var6 + 29, 92, 5, ((ApLivestockMenu)this.menu).energy(), 1000000, -1529542);
      ApGuiStyle.slots(var1, var5 + 34, var6 + 104, 9);
      ApGuiStyle.slots(var1, var5 + 34, var6 + 122, 9);
      var1.fill(var5 + 34 - 2, var6 + 122 - 1, var5 + 34 - 1, var6 + 122 + 17, -1529542);
      ApGuiStyle.playerInventory(var1, var5 + 34, var6 + 152, var6 + 210);
   }
   @Override
   protected void renderLabels(GuiGraphics var1, int var2, int var3) {
      ApGuiStyle.text(var1, this.font, this.title.getString(), 9, 8, this.imageWidth - 18, -920586);
      ApScreens.structureText(var1, this.font, ((ApLivestockMenu)this.menu).structure());
      this.renderSideLabels(var1);
      ApGuiStyle.text(
         var1,
         this.font,
         Component.translatable("gui.industrialcrops.ap.energy", new Object[]{String.format("%,d", ((ApLivestockMenu)this.menu).energy())}).getString(),
         125,
         36,
         92,
         -4801081
      );
      int var4 = ((ApLivestockMenu)this.menu).status();
      ApGuiStyle.text(
         var1,
         this.font,
         Component.translatable("gui.industrialcrops.ap_livestock.status." + var4).getString(),
         10,
         82,
         104,
         var4 == 0 ? -8396660 : (var4 != 5 && var4 != 3 ? -38037 : -4801081)
      );
      ApGuiStyle.text(var1, this.font, Component.translatable("gui.industrialcrops.ap_livestock.feed").getString(), 34, 94, 50, -4801081);
      String var5 = Component.translatable(
            "gui.industrialcrops.ap_livestock.stats",
            new Object[]{
               ((ApLivestockMenu)this.menu).animals(),
               ((ApLivestockMenu)this.menu).births(),
               ((ApLivestockMenu)this.menu).shorn(),
               ((ApLivestockMenu)this.menu).culled()
            }
         )
         .getString();
      int var6 = Math.min(this.font.width(var5), 110);
      ApGuiStyle.text(var1, this.font, var5, 196 - var6, 94, var6, -4801081);
      var1.drawString(this.font, this.playerInventoryTitle, 34, 142, -4801081, false);
   }
   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      if (!this.renderSideTooltips(var1, var2, var3)) {
         for (int var5 = 0; var5 < 4; var5++) {
            if (this.toggles[var5].isHovered()) {
               var1.renderTooltip(
                  this.font, this.font.split(Component.translatable("gui.industrialcrops.ap_livestock." + TASKS[var5] + ".tip"), 180), var2, var3
               );
               return;
            }
         }
         if (!ApScreens.structureTooltip(
            var1, this.font, this.leftPos, this.topPos, var2, var3, ((ApLivestockMenu)this.menu).structure(), "automatic_animal_breeder"
         )) {
            this.renderTooltip(var1, var2, var3);
         }
      }
   }
}
