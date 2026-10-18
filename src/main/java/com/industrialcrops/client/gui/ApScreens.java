package com.industrialcrops.client.gui;

import com.industrialcrops.machine.ApStructure;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Shared structure summary; every detail comes from the server's packed status. */
final class ApScreens {
   private ApScreens() {}

   static int chunks(ApStructure.Status status) {
      return status.width() * status.depth() / 256;
   }

   static net.minecraft.network.chat.MutableComponent footprint(ApStructure.Status status) {
      return Component.translatable("gui.industrialcrops.ap.footprint", status.width(), status.depth(), chunks(status));
   }

   static Component category(ApStructure.Category category) {
      return Component.translatable("gui.industrialcrops.ap.category." + category.name().toLowerCase(java.util.Locale.ROOT));
   }

   static void structureText(GuiGraphics graphics, Font font, ApStructure.Status status) {
      ApGuiStyle.text(graphics, font, Component.translatable("gui.industrialcrops.ap.structure").getString(), 12, 28, 100, ApGuiStyle.TEXT);
      ApGuiStyle.text(graphics, font, footprint(status).getString(), 52, 40, 60, ApGuiStyle.TEXT);
      ApGuiStyle.text(graphics, font, Component.translatable("gui.industrialcrops.ap.checks", status.completedCategories(), 9).getString(), 52, 50, 60, status.formed() ? ApGuiStyle.OK : ApGuiStyle.BAD);
      ApGuiStyle.text(graphics, font, Component.translatable("gui.industrialcrops.ap.expansion", status.tier()).getString(), 52, 60, 60, ApGuiStyle.MUTED);
      ApGuiStyle.text(graphics, font, Component.translatable(status.loaded() ? "gui.industrialcrops.ap.details" : "gui.industrialcrops.ap.unloaded").getString(), 52, 71, 60, status.loaded() ? ApGuiStyle.MUTED : ApGuiStyle.BAD);
   }

   static void line(GuiGraphics graphics, Font font, Component text, int x, int y, int width, boolean complete) {
      ApGuiStyle.text(graphics, font, (complete ? "✔ " : "✖ ") + text.getString(), x, y, width, complete ? ApGuiStyle.OK : ApGuiStyle.BAD);
   }

   static void addStructureHelp(ArrayList<FormattedCharSequence> lines, Font font, ApStructure.Status status) {
      lines.add(Component.translatable("gui.industrialcrops.ap.help.title").withStyle(ChatFormatting.GOLD).getVisualOrderText());
      lines.add(footprint(status).withStyle(ChatFormatting.WHITE).getVisualOrderText());
      if (!status.loaded()) lines.add(Component.translatable("gui.industrialcrops.ap.unloaded").withStyle(ChatFormatting.RED).getVisualOrderText());
      for (ApStructure.Category category : ApStructure.Category.values()) {
         boolean complete = status.complete(category);
         lines.add(Component.literal(complete ? "✔ " : "✖ ").append(category(category)).withStyle(complete ? ChatFormatting.GREEN : ChatFormatting.RED).getVisualOrderText());
      }
      String type = status.livestock() ? "ranch" : "farm";
      for (String key : new String[]{"origin", type, "expansion", "projection"}) {
         lines.addAll(font.split(Component.translatable("gui.industrialcrops.ap.help." + key).withStyle(ChatFormatting.GRAY), 240));
      }
   }

   static boolean structureTooltip(GuiGraphics graphics, Font font, int left, int top, int mouseX, int mouseY, ApStructure.Status status, String unusedLegacyCore) {
      if (mouseX < left + 8 || mouseX >= left + 116 || mouseY < top + 24 || mouseY >= top + 80) return false;
      ArrayList<FormattedCharSequence> lines = new ArrayList<>();
      addStructureHelp(lines, font, status);
      graphics.renderTooltip(font, lines, mouseX, mouseY);
      return true;
   }
}
