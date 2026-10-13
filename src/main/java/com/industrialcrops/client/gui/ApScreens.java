package com.industrialcrops.client.gui;
import com.industrialcrops.machine.ApStructure;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
final class ApScreens {
   private ApScreens() {
   }
   static void structureText(GuiGraphics var0, Font var1, ApStructure.Status var2) {
      ApGuiStyle.text(var0, var1, Component.translatable("gui.industrialcrops.ap.structure").getString(), 12, 28, 100, -920586);
      byte var3 = 52;
      byte var4 = 60;
      line(var0, var1, Component.translatable("gui.industrialcrops.ap.casings", new Object[]{var2.casings(), 8}), var3, 40, var4, var2.casings() == 8);
      line(var0, var1, Component.translatable("gui.industrialcrops.ap.frames", new Object[]{var2.frames(), 8}), var3, 49, var4, var2.frames() == 8);
      line(var0, var1, Component.translatable("gui.industrialcrops.ap.core"), var3, 58, var4, var2.core());
      line(var0, var1, Component.translatable("gui.industrialcrops.ap.cap"), var3, 67, var4, var2.cap());
   }
   static void line(GuiGraphics var0, Font var1, Component var2, int var3, int var4, int var5, boolean var6) {
      ApGuiStyle.text(var0, var1, (var6 ? "✔ " : "✖ ") + var2.getString(), var3, var4, var5, var6 ? -8396660 : -38037);
   }
   static boolean structureTooltip(GuiGraphics var0, Font var1, int var2, int var3, int var4, int var5, ApStructure.Status var6, String var7) {
      if (var4 >= var2 + 8 && var4 < var2 + 116 && var5 >= var3 + 24 && var5 < var3 + 80) {
         ArrayList var8 = new ArrayList();
         var8.add(Component.translatable("gui.industrialcrops.ap.help.title").withStyle(ChatFormatting.GOLD).getVisualOrderText());
         for (int var9 = 1; var9 <= 4; var9++) {
            MutableComponent var10 = var9 == 3
               ? Component.translatable("gui.industrialcrops.ap.help.3", new Object[]{Component.translatable("block.industrialcrops." + var7)})
               : Component.translatable("gui.industrialcrops.ap.help." + var9);
            var8.addAll(var1.split(var10.copy().withStyle(ChatFormatting.GRAY), 220));
         }
         var0.renderTooltip(var1, var8, var4, var5);
         return true;
      } else {
         return false;
      }
   }
}
