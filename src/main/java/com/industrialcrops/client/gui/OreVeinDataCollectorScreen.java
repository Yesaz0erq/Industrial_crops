package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.OreVeinDataCollectorBlockEntity;
import com.industrialcrops.screen.OreVeinDataCollectorMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Mekanism-style frame around a geological survey CRT: scrolling rock strata swept by a scan beam,
 * discovered veins sparkling in place, a seismograph trace, a rotating radar ring around the ore being
 * scanned, a gel vial that fills over the whole survey and a paged ledger of every natural metal ore.
 */
public final class OreVeinDataCollectorScreen extends IndustrialContainerScreen<OreVeinDataCollectorMenu> {
   private static final ResourceLocation ATLAS = IndustrialGuiStyle.containerTexture("ore_vein_data_collector");
   private static final int TEX = 256;
   private static final int DX = 38, DY = 22, DW = 108, DH = 70;
   private static final int VIEW_W = 68, VIEW_H = 54;
   private static final int METER_X = 150;
   private static final int LEDGER_X = 8, LEDGER_Y = 99, PER_PAGE = 9, PAGE_TICKS = 100, SLIDE_TICKS = 10;
   private static final int VIAL_X = 15, VIAL_Y = 43;
   private static final int CYAN = 0xFF5EF0E2;
   private static final int CYAN_DIM = 0xFF2E7F7A;
   private static final int AMBER = 0xFFFFBE3C;
   private static final int VIOLET = 0xFFC08CFF;
   private static final int RED = 0xFFFF6B5E;

   public OreVeinDataCollectorScreen(OreVeinDataCollectorMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      imageWidth = 176;
      imageHeight = 222;
      titleLabelY = 6;
      inventoryLabelY = OreVeinDataCollectorMenu.INVENTORY_Y - 12;
   }

   private float time(float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      return (minecraft.level == null ? 0L : minecraft.level.getGameTime()) + partialTick;
   }

   private boolean scanning() {
      return menu.status() == OreVeinDataCollectorBlockEntity.STATUS_SCANNING;
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      int x = leftPos, y = topPos;
      float t = time(partialTick);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      IndustrialGuiStyle.drawContainer(graphics, x, y, imageWidth, imageHeight);

      // Left column: slimeball in, survey vial, Flux Ore Gel out.
      IndustrialGuiStyle.drawWorkPanel(graphics, x + 7, y + 19, 28, 76);
      IndustrialGuiStyle.drawSlot(graphics, x + OreVeinDataCollectorMenu.INPUT_X - 1, y + OreVeinDataCollectorMenu.INPUT_Y - 1);
      IndustrialGuiStyle.drawSlot(graphics, x + OreVeinDataCollectorMenu.OUTPUT_X - 1, y + OreVeinDataCollectorMenu.OUTPUT_Y - 1);
      graphics.fill(x + OreVeinDataCollectorMenu.OUTPUT_X + 1, y + OreVeinDataCollectorMenu.OUTPUT_Y - 2,
         x + OreVeinDataCollectorMenu.OUTPUT_X + 15, y + OreVeinDataCollectorMenu.OUTPUT_Y - 1, VIOLET);
      if (menu.getSlot(0).getItem().isEmpty()) {
         ghostItem(graphics, new ItemStack(Items.SLIME_BALL), x + OreVeinDataCollectorMenu.INPUT_X, y + OreVeinDataCollectorMenu.INPUT_Y);
      }
      drawVial(graphics, x + VIAL_X, y + VIAL_Y, t);

      // Survey display.
      IndustrialGuiStyle.drawWorkPanel(graphics, x + DX - 3, y + DY - 3, DW + 6, DH + 6);
      drawDisplay(graphics, x + DX, y + DY, t);

      IndustrialGuiStyle.drawVerticalMeter(graphics, x + METER_X, y + DY, DH, menu.energy(),
         OreVeinDataCollectorBlockEntity.ENERGY_CAPACITY, IndustrialGuiStyle.ENERGY_RED, false);

      drawLedger(graphics, x, y, t);
      IndustrialGuiStyle.drawPlayerInventory(graphics, x, y, 8, OreVeinDataCollectorMenu.INVENTORY_Y, OreVeinDataCollectorMenu.HOTBAR_Y);
   }

   private void ghostItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
      graphics.renderFakeItem(stack, x, y);
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 200.0F);
      graphics.fill(x, y, x + 16, y + 16, 0x9A8B8B8B);
      graphics.pose().popPose();
   }

   // ---- vial ------------------------------------------------------------------------------------

   private void drawVial(GuiGraphics graphics, int x, int y, float t) {
      int total = Math.max(1, menu.total());
      float done = Mth.clamp((menu.scanned() * (float) OreVeinDataCollectorBlockEntity.SCAN_TICKS + menu.progress())
         / (total * (float) OreVeinDataCollectorBlockEntity.SCAN_TICKS), 0.0F, 1.0F);
      int fill = Math.round(done * 22);
      graphics.fill(x + 2, y + 3, x + 12, y + 26, 0xFF101519);
      if (fill > 0) {
         // The meniscus wobbles while the survey is running.
         int wobble = scanning() ? Math.round(Mth.sin(t * 0.25F)) : 0;
         int top = Math.max(0, fill + wobble);
         graphics.blit(ATLAS, x + 2, y + 3 + 23 - top, 186, 144 + 24 - top, 10, top, TEX, TEX);
         if (scanning()) {
            for (int bubble = 0; bubble < 3; bubble++) {
               float phase = (t * 0.6F + bubble * 7.3F) % 22.0F;
               int by = y + 3 + 23 - 3 - Math.round(phase);
               if (by > y + 3 + 23 - top) {
                  graphics.blit(ATLAS, x + 3 + (bubble * 3) % 7, by, 198, 144, 3, 3, TEX, TEX);
               }
            }
         }
      }
      graphics.blit(ATLAS, x, y, 170, 144, 14, 28, TEX, TEX);
   }

   // ---- survey display ----------------------------------------------------------------------------

   private void drawDisplay(GuiGraphics graphics, int x, int y, float t) {
      boolean active = scanning();
      graphics.fill(x, y, x + DW, y + DH, 0xFF071014);
      int vx = x + 2, vy = y + 2;
      float scroll = (t * 0.18F) % 108.0F;
      int offset = (int) scroll;
      graphics.enableScissor(vx, vy, vx + VIEW_W, vy + VIEW_H);
      graphics.blit(ATLAS, vx, vy, offset + 2, 2, VIEW_W, VIEW_H, TEX, TEX);

      // Discovered veins stay pinned to their rock as the strata drift.
      int scanned = menu.scanned();
      float beam = active ? (t % 50.0F) / 50.0F * (VIEW_W + 14) - 7 : -100;
      for (int index = 0; index < Math.min(menu.total(), 48); index++) {
         int nx = Math.floorMod(hash(index * 31 + 7), 108);
         int ny = 5 + Math.floorMod(hash(index * 17 + 3), 38);
         int sx = Math.floorMod(nx - offset, 108);
         if (sx > VIEW_W - 4) continue;
         boolean known = index < scanned;
         boolean current = index == scanned && active;
         if (!known && !current) continue;
         boolean lit = Math.abs(sx - beam) < 6;
         int frame = current ? (int) (t / 3) % 4 : (int) ((t / 5) + index) % 4;
         if (current) RenderSystem.setShaderColor(1.0F, 0.75F, 0.3F, 0.6F + 0.4F * Mth.sin(t * 0.4F));
         else if (lit) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         else RenderSystem.setShaderColor(0.65F, 1.0F, 0.95F, 0.75F);
         graphics.blit(ATLAS, vx + sx - 3, vy + ny - 3, 136 + frame * 8, 144, 7, 7, TEX, TEX);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      // Seismograph trace along the bottom of the strata view.
      int mid = vy + VIEW_H - 7;
      float amp = active ? 1.0F : 0.18F;
      int previous = Integer.MIN_VALUE;
      for (int column = 0; column < VIEW_W; column++) {
         float wave = Mth.sin((column + t * 1.6F) * 0.42F) * 2.2F + Mth.sin((column * 0.7F - t * 0.9F) * 0.23F) * 1.6F;
         int spike = hash((int) ((column + t * 1.6F) / 9)) % 11 == 0 && (int) (column + t * 1.6F) % 9 < 2 ? 4 : 0;
         int value = mid - Math.round((wave + spike) * amp);
         int from = previous == Integer.MIN_VALUE ? value : Math.min(previous, value);
         int to = previous == Integer.MIN_VALUE ? value : Math.max(previous, value);
         graphics.fill(vx + column, from, vx + column + 1, to + 1, active ? 0xE65EF0E2 : 0x8C2E7F7A);
         previous = value;
      }

      if (active) {
         int beamX = Math.round(beam);
         graphics.blit(ATLAS, vx + beamX - 7, vy, 110, 74, 10, VIEW_H, TEX, TEX);
      }
      graphics.disableScissor();

      // Radar column with the ore currently being surveyed.
      int rx = x + 72, ry = y + 4;
      graphics.fill(x + 71, y + 2, x + 106, y + 56, 0xFF0A1A1E);
      int frame = active ? (int) (t / 2.5F) % 4 : 0;
      if (!active) RenderSystem.setShaderColor(0.55F, 0.55F, 0.55F, 0.8F);
      graphics.blit(ATLAS, rx, ry, frame * 34, 144, 34, 34, TEX, TEX);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      List<OreVeinDataCollectorMenu.Entry> entries = menu.entries();
      if (scanned < entries.size() && menu.status() != OreVeinDataCollectorBlockEntity.STATUS_NO_SLIMEBALL) {
         float bob = active ? Mth.sin(t * 0.2F) * 1.2F : 0.0F;
         graphics.pose().pushPose();
         graphics.pose().translate(0.0F, bob, 0.0F);
         graphics.renderItem(new ItemStack(entries.get(scanned).icon()), rx + 9, ry + 9);
         graphics.pose().popPose();
      }
      // Ten-cell progress for the current 30-second scan.
      int cells = active ? menu.progress() * 10 / OreVeinDataCollectorBlockEntity.SCAN_TICKS : 0;
      for (int cell = 0; cell < 10; cell++) {
         int cx = x + 74 + cell * 3;
         int color = cell < cells ? CYAN : (cell == cells && active && ((int) (t / 4)) % 2 == 0 ? AMBER : 0xFF1E3A3E);
         graphics.fill(cx, y + 41, cx + 2, y + 45, color);
      }
      String count = Math.min(scanned, menu.total()) + "/" + menu.total();
      IndustrialGuiStyle.drawFittedString(graphics, font, count, x + 72, y + 47, 34, active ? CYAN : CYAN_DIM, true);

      // Status footer.
      graphics.fill(x + 2, y + 58, x + 106, y + 68, 0xFF061013);
      Component status = statusLine();
      int color = switch (menu.status()) {
         case OreVeinDataCollectorBlockEntity.STATUS_SCANNING -> CYAN;
         case OreVeinDataCollectorBlockEntity.STATUS_NO_SLIMEBALL -> VIOLET;
         default -> RED;
      };
      IndustrialGuiStyle.drawFittedString(graphics, font, status.getString(), x + 4, y + 60, DW - 8, color, false);

      graphics.blit(ATLAS, x, y, 0, 72, DW, DH, TEX, TEX);
   }

   private Component statusLine() {
      int status = menu.status();
      if (status == OreVeinDataCollectorBlockEntity.STATUS_SCANNING && menu.scanned() < menu.entries().size()) {
         int seconds = Mth.ceil((OreVeinDataCollectorBlockEntity.SCAN_TICKS - menu.progress()) / 20.0F);
         return Component.translatable("gui.industrialcrops.ore_vein_data_collector.scanning",
            menu.entries().get(menu.scanned()).ingot().getDescription(), seconds);
      }
      return Component.translatable("gui.industrialcrops.ore_vein_data_collector.status." + status);
   }

   private static int hash(int value) {
      int h = value * 0x9E3779B1;
      h ^= h >>> 15;
      h *= 0x85EBCA77;
      h ^= h >>> 13;
      return h & 0x7FFFFFFF;
   }

   // ---- ledger ------------------------------------------------------------------------------------

   private int pages() {
      return Math.max(1, (menu.entries().size() + PER_PAGE - 1) / PER_PAGE);
   }

   private void drawLedger(GuiGraphics graphics, int x, int y, float t) {
      IndustrialGuiStyle.drawInsetPanel(graphics, x + LEDGER_X - 1, y + LEDGER_Y - 2, PER_PAGE * 18 + 2, 22);
      List<OreVeinDataCollectorMenu.Entry> entries = menu.entries();
      if (entries.isEmpty()) {
         IndustrialGuiStyle.drawFittedString(graphics, font, Component.translatable("gui.industrialcrops.ore_vein_data_collector.status.4").getString(),
            x + LEDGER_X + 4, y + LEDGER_Y + 4, PER_PAGE * 18 - 8, IndustrialGuiStyle.MUTED_TEXT, false);
         return;
      }
      int pages = pages();
      int page = pages == 1 ? 0 : (int) (t / PAGE_TICKS) % pages;
      float local = t % PAGE_TICKS;
      float slide = pages > 1 && local > PAGE_TICKS - SLIDE_TICKS ? (local - (PAGE_TICKS - SLIDE_TICKS)) / SLIDE_TICKS : 0.0F;
      int shift = Math.round(Mth.clamp(slide, 0.0F, 1.0F) * PER_PAGE * 18);
      int left = x + LEDGER_X, top = y + LEDGER_Y;
      graphics.enableScissor(left, top - 1, left + PER_PAGE * 18, top + 19);
      drawLedgerPage(graphics, page, left - shift, top, t);
      if (shift > 0) drawLedgerPage(graphics, (page + 1) % pages, left + PER_PAGE * 18 - shift, top, t);
      graphics.disableScissor();
      if (pages > 1) {
         for (int dot = 0; dot < pages; dot++) {
            int dx = x + 88 - pages * 2 + dot * 4;
            graphics.fill(dx, y + LEDGER_Y + 19, dx + 2, y + LEDGER_Y + 20, dot == page ? AMBER : 0xFF5A5A5A);
         }
      }
   }

   private void drawLedgerPage(GuiGraphics graphics, int page, int left, int top, float t) {
      List<OreVeinDataCollectorMenu.Entry> entries = menu.entries();
      int scanned = menu.scanned();
      boolean active = scanning();
      for (int cell = 0; cell < PER_PAGE; cell++) {
         int index = page * PER_PAGE + cell;
         int cx = left + cell * 18, cy = top - 1;
         graphics.fill(cx, cy, cx + 18, cy + 18, 0xFF161D21);
         graphics.fill(cx + 1, cy + 1, cx + 17, cy + 17, 0xFF233036);
         if (index >= entries.size()) continue;
         graphics.renderItem(new ItemStack(entries.get(index).icon()), cx + 1, cy + 1);
         graphics.pose().pushPose();
         graphics.pose().translate(0.0F, 0.0F, 210.0F);
         if (index < scanned) {
            graphics.blit(ATLAS, cx, cy, 0, 180, 18, 18, TEX, TEX);
            graphics.blit(ATLAS, cx + 10, cy + 11, 54, 180, 7, 7, TEX, TEX);
         } else if (index == scanned && active) {
            float pulse = 0.55F + 0.45F * Mth.sin(t * 0.35F);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, pulse);
            graphics.blit(ATLAS, cx, cy, 18, 180, 18, 18, TEX, TEX);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int fill = menu.progress() * 16 / OreVeinDataCollectorBlockEntity.SCAN_TICKS;
            graphics.fill(cx + 1, cy + 16, cx + 1 + fill, cy + 17, AMBER);
         } else {
            graphics.blit(ATLAS, cx, cy, 36, 180, 18, 18, TEX, TEX);
         }
         graphics.pose().popPose();
      }
   }

   private int hoveredLedger(int mouseX, int mouseY) {
      int lx = mouseX - leftPos - LEDGER_X, ly = mouseY - topPos - LEDGER_Y + 1;
      if (lx < 0 || ly < 0 || lx >= PER_PAGE * 18 || ly >= 18) return -1;
      float t = time(0.0F);
      int pages = pages();
      int page = pages == 1 ? 0 : (int) (t / PAGE_TICKS) % pages;
      int index = page * PER_PAGE + lx / 18;
      return index < menu.entries().size() ? index : -1;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      int lx = mouseX - leftPos, ly = mouseY - topPos;
      int index = hoveredLedger(mouseX, mouseY);
      if (index >= 0) {
         OreVeinDataCollectorMenu.Entry entry = menu.entries().get(index);
         List<Component> lines = new ArrayList<>();
         lines.add(entry.icon().getDescription().copy().withStyle(ChatFormatting.WHITE));
         lines.add(Component.translatable("gui.industrialcrops.ore_vein_data_collector.yields", entry.ingot().getDescription())
            .withStyle(ChatFormatting.AQUA));
         String state = index < menu.scanned() ? "done" : (index == menu.scanned() && scanning() ? "active" : "pending");
         lines.add(Component.translatable("gui.industrialcrops.ore_vein_data_collector.entry." + state).withStyle(ChatFormatting.GRAY));
         graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
         return;
      }
      if (lx >= METER_X && lx < METER_X + 18 && ly >= DY && ly < DY + DH) {
         graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.integrated_smelting_furnace.energy",
            String.format("%,d", menu.energy()), String.format("%,d", OreVeinDataCollectorBlockEntity.ENERGY_CAPACITY)), mouseX, mouseY);
         return;
      }
      if (lx >= DX && lx < DX + DW && ly >= DY && ly < DY + DH) {
         List<Component> lines = new ArrayList<>();
         lines.add(Component.translatable("gui.industrialcrops.ore_vein_data_collector.survey").withStyle(ChatFormatting.AQUA));
         lines.add(statusLine().copy().withStyle(ChatFormatting.WHITE));
         int remaining = Math.max(0, (menu.total() - menu.scanned()) * OreVeinDataCollectorBlockEntity.SCAN_TICKS - menu.progress()) / 20;
         lines.add(Component.translatable("gui.industrialcrops.ore_vein_data_collector.remaining", remaining / 60, String.format("%02d", remaining % 60))
            .withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("gui.industrialcrops.ore_vein_data_collector.rate",
            OreVeinDataCollectorBlockEntity.ENERGY_PER_TICK).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("gui.industrialcrops.ore_vein_data_collector.hint").withStyle(ChatFormatting.DARK_GRAY));
         graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
         return;
      }
      renderTooltip(graphics, mouseX, mouseY);
   }
}
