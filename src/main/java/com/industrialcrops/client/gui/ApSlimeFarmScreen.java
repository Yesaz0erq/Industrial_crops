package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.ApSlimeFarmBlockEntity;
import com.industrialcrops.screen.ApSlimeFarmMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * AP Slime Farm controller: the AP machine layout with a live "flux vat" in which the pen's slimes hop,
 * ore flows along the conduit into them and ingots come out, plus a chevron flow between the two rows.
 */
public final class ApSlimeFarmScreen extends ApMachineScreen<ApSlimeFarmMenu> {
   private static final ResourceLocation ATLAS = IndustrialGuiStyle.containerTexture("ap_slime_farm");
   private static final int TW = 256, TH = 128;
   private static final int VAT_X = 125, VAT_Y = 45, VAT_W = 92, VAT_H = 44;
   private static final int CYAN = 0xFF78FFF0;

   public ApSlimeFarmScreen(ApSlimeFarmMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      imageWidth = 230;
      imageHeight = 234;
   }

   @Override
   protected String machineKey() {
      return "ap_slime_farm";
   }

   private float time(float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      return (minecraft.level == null ? 0L : minecraft.level.getGameTime()) + partialTick;
   }

   private boolean running() {
      return menu.status() == ApSlimeFarmBlockEntity.STATUS_RUNNING;
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      int x = leftPos, y = topPos;
      float t = time(partialTick);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      renderSidePanels(graphics);
      ApGuiStyle.container(graphics, x, y, imageWidth, imageHeight);
      ApGuiStyle.well(graphics, x + 8, y + 24, 108, 68);
      ApGuiStyle.well(graphics, x + 120, y + 24, 102, 68);
      ApGuiStyle.structureMap(graphics, x + 14, y + 40, menu.structure());
      ApGuiStyle.bar(graphics, x + 125, y + 29, 92, 5, menu.energy(), ApSlimeFarmBlockEntity.ENERGY_CAPACITY, ApGuiStyle.ENERGY);
      drawVat(graphics, x + VAT_X, y + VAT_Y, t);
      ApGuiStyle.slots(graphics, x + ApSlimeFarmMenu.SLOTS_X, y + ApSlimeFarmMenu.ROW1_Y, 9);
      ApGuiStyle.slots(graphics, x + ApSlimeFarmMenu.SLOTS_X, y + ApSlimeFarmMenu.ROW2_Y, 9);
      graphics.fill(x + 32, y + ApSlimeFarmMenu.ROW2_Y - 1, x + 33, y + ApSlimeFarmMenu.ROW2_Y + 17, ApGuiStyle.ACCENT);
      drawFlow(graphics, x + 13, y + ApSlimeFarmMenu.ROW1_Y - 1, t);
      ApGuiStyle.playerInventory(graphics, x + ApSlimeFarmMenu.SLOTS_X, y + 152, y + 210);
   }

   private Item firstInput() {
      for (int slot = 0; slot < ApSlimeFarmBlockEntity.INPUT_SLOTS; slot++) {
         ItemStack stack = menu.getSlot(slot).getItem();
         if (!stack.isEmpty()) return stack.getItem();
      }
      return Items.AIR;
   }

   private void drawVat(GuiGraphics graphics, int x, int y, float t) {
      boolean running = running();
      graphics.blit(ATLAS, x, y, 0, 0, VAT_W, VAT_H, TW, TH);
      graphics.enableScissor(x + 1, y + 1, x + VAT_W - 1, y + VAT_H - 1);
      if (running) {
         // Violet glow breathing behind the glass.
         int glow = (int) (40 + 30 * Mth.sin(t * 0.15F));
         graphics.fill(x + 3, y + 12, x + VAT_W - 3, y + 30, glow << 24 | 0x9A4CFF);
         // Energy pulses running along the top conduit.
         for (int pulse = 0; pulse < 4; pulse++) {
            int px = (int) ((t * 1.6F + pulse * 23) % (VAT_W + 6)) - 5;
            graphics.blit(ATLAS, x + px, y + 4, 60, 48, 5, 3, TW, TH);
         }
      }
      // Ore in, ingot out, with a chevron stream between them.
      Item input = firstInput();
      Item output = menu.lastProduct();
      if (input != Items.AIR) graphics.renderItem(new ItemStack(input), x + 4, y + 13);
      if (output != Items.AIR) graphics.renderItem(new ItemStack(output), x + VAT_W - 20, y + 13);
      for (int dot = 0; dot < 5; dot++) {
         float travel = running ? (t * 0.9F + dot * 10.0F) % 50.0F : dot * 10.0F;
         int dx = x + 22 + (int) travel;
         int alpha = running ? (int) (255 * Mth.sin(travel / 50.0F * Mth.PI)) : 70;
         graphics.fill(dx, y + 20, dx + 2, y + 22, Math.max(0, Math.min(255, alpha)) << 24 | 0x78FFF0);
      }
      drawSlimes(graphics, x, y, t, running);
      graphics.disableScissor();
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 200.0F);
      graphics.blit(ATLAS, x, y, 100, 0, VAT_W, VAT_H, TW, TH);
      graphics.pose().popPose();
   }

   private void drawSlimes(GuiGraphics graphics, int x, int y, float t, boolean running) {
      int count = Math.min(5, menu.slimes());
      if (count == 0) return;
      float speed = running ? 1.0F : 0.45F;
      int cycle = menu.cycleTicks();
      for (int index = 0; index < count; index++) {
         float home = 10 + (index + 0.5F) * (VAT_W - 20) / count;
         float sway = Mth.sin(t * 0.03F * speed + index * 1.7F) * 5.0F;
         float phase = (t * speed + index * 11.0F) % 34.0F;
         int frame;
         float lift = 0.0F;
         if (phase < 12.0F) {
            lift = Mth.sin(phase / 12.0F * Mth.PI) * 9.0F;
            frame = 3;
         } else if (phase < 15.0F || phase > 31.0F) {
            frame = 1;
         } else {
            frame = ((int) (phase / 4)) % 2 == 0 ? 0 : 2;
         }
         int sx = Math.round(x + home + sway - 7);
         int sy = Math.round(y + 37 - 12 - lift);
         graphics.pose().pushPose();
         graphics.pose().translate(0.0F, 0.0F, 150.0F);
         graphics.blit(ATLAS, sx, sy, frame * 14, 48, 14, 12, TW, TH);
         if (running && (t + index * 3) % cycle < 8) {
            int puff = (int) (((t + index * 3) % cycle) / 2);
            graphics.blit(ATLAS, sx + 4, sy - 7, 84 + Math.min(3, puff) * 7, 48, 6, 6, TW, TH);
         }
         graphics.pose().popPose();
      }
   }

   private void drawFlow(GuiGraphics graphics, int x, int y, float t) {
      boolean running = running();
      graphics.fill(x - 1, y, x + 13, y + 36, ApGuiStyle.SHADOW);
      graphics.fill(x, y + 1, x + 12, y + 35, ApGuiStyle.WELL);
      graphics.enableScissor(x, y + 1, x + 12, y + 35);
      for (int chevron = 0; chevron < 5; chevron++) {
         float offset = running ? (t * 0.5F + chevron * 8.0F) % 40.0F : chevron * 8.0F;
         int cy = y + (int) offset - 4;
         graphics.blit(ATLAS, x, cy, 68, running ? 48 : 56, 12, 7, TW, TH);
      }
      graphics.disableScissor();
   }

   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      ApGuiStyle.text(graphics, font, title.getString(), 9, 8, imageWidth - 18, ApGuiStyle.TEXT);
      ApScreens.structureText(graphics, font, menu.structure());
      renderSideLabels(graphics);
      ApGuiStyle.text(graphics, font, Component.translatable("gui.industrialcrops.ap.energy", String.format("%,d", menu.energy())).getString(),
         125, 36, 92, ApGuiStyle.MUTED);
      int status = menu.status();
      int color = status == ApSlimeFarmBlockEntity.STATUS_RUNNING ? ApGuiStyle.OK
         : (status == ApSlimeFarmBlockEntity.STATUS_IDLE || status == ApSlimeFarmBlockEntity.STATUS_NO_SLIMES ? ApGuiStyle.MUTED : ApGuiStyle.BAD);
      ApGuiStyle.text(graphics, font, Component.translatable("gui.industrialcrops.ap_slime_farm.status." + status).getString(), 10, 82, 104, color);
      ApGuiStyle.text(graphics, font, Component.translatable("gui.industrialcrops.ap_slime_farm.ores").getString(), 34, 94, 50, ApGuiStyle.MUTED);
      String stats = Component.translatable("gui.industrialcrops.ap_slime_farm.stats", menu.slimes(), menu.knownTypes(),
         String.format("%,d", menu.converted())).getString();
      int width = Math.min(font.width(stats), 130);
      ApGuiStyle.text(graphics, font, stats, 196 - width, 94, width, ApGuiStyle.MUTED);
      graphics.drawString(font, playerInventoryTitle, 34, 142, ApGuiStyle.MUTED, false);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      if (renderSideTooltips(graphics, mouseX, mouseY)) return;
      int lx = mouseX - leftPos, ly = mouseY - topPos;
      if (lx >= VAT_X && lx < VAT_X + VAT_W && ly >= 27 && ly < VAT_Y + VAT_H) {
         float seconds = menu.cycleTicks() / 20.0F;
         graphics.renderComponentTooltip(font, List.of(
            Component.translatable("gui.industrialcrops.ap_slime_farm.vat").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("gui.industrialcrops.ap.energy", String.format("%,d", menu.energy())).withStyle(ChatFormatting.WHITE),
            Component.translatable("gui.industrialcrops.ap_slime_farm.rate", ApSlimeFarmBlockEntity.PER_SLIME,
               String.format("%.2f", seconds)).withStyle(ChatFormatting.AQUA),
            Component.translatable("gui.industrialcrops.ap_slime_farm.cost", ApSlimeFarmBlockEntity.ENERGY_PER_ITEM).withStyle(ChatFormatting.GRAY),
            Component.translatable("gui.industrialcrops.ap_slime_farm.vat.tip").withStyle(ChatFormatting.DARK_GRAY)
         ), mouseX, mouseY);
         return;
      }
      if (lx >= 12 && lx < 26 && ly >= ApSlimeFarmMenu.ROW1_Y - 1 && ly < ApSlimeFarmMenu.ROW1_Y + 35) {
         graphics.renderTooltip(font, font.split(Component.translatable("gui.industrialcrops.ap_slime_farm.flow"), 180), mouseX, mouseY);
         return;
      }
      if (!ApScreens.structureTooltip(graphics, font, leftPos, topPos, mouseX, mouseY, menu.structure(), "slime_cultivation_device")) {
         renderTooltip(graphics, mouseX, mouseY);
      }
   }
}
