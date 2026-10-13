package com.industrialcrops.client.gui;
import com.industrialcrops.screen.SatelliteControlMenu;
import com.industrialcrops.world.SpaceMiningAreas;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
public final class SatelliteControlScreen extends IndustrialContainerScreen<SatelliteControlMenu> {
   private static final ResourceLocation GALAXY_MAP = new ResourceLocation("industrialcrops", "textures/gui/satellite_galaxy_map.png");
   private static final int MAP_TEXTURE_WIDTH = 256;
   private static final int MAP_TEXTURE_HEIGHT = 128;
   private static final int GUI_WIDTH = 272;
   private static final int GUI_HEIGHT = 226;
   private static final int BAR_X = 12;
   private static final int BAR_Y = 36;
   private static final int BAR_W = 248;
   private static final int BAR_H = 6;
   private static final int MAP_X = 12;
   private static final int MAP_Y = 47;
   private static final int MAP_W = 248;
   private static final int MAP_H = 92;
   private static final int PANEL_Y = 145;
   private static final int PANEL_H = 54;
   private static final int LEFT_X = 8;
   private static final int LEFT_W = 144;
   private static final int RIGHT_X = 156;
   private static final int RIGHT_W = 108;
   private static final int PLANET_X = 18;
   private static final int PLANET_Y = 46;
   private static final int[] ORBIT = new int[]{0, 46, 104, 170};
   private static final double[] ANGLE = new double[]{0.0, -28.0, 18.0, -12.0};
   private static final int NODE = 15;
   private static final int CYAN = -9904897;
   private static final int CYAN_DIM = -13660210;
   private static final int AMBER = -2055606;
   private static final int LOCKED = -9801082;
   private static final int[] AREA_COLOR = new int[]{0, -9449606, -1934652, -1661364};
   private static final String[] NUMERAL = new String[]{"", "I", "II", "III"};
   private CrystalButton toggle;
   public SatelliteControlScreen(SatelliteControlMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 272;
      this.imageHeight = 226;
   }
   protected void init() {
      super.init();
      this.toggle = (CrystalButton)this.addRenderableWidget(
         new CrystalButton(this.leftPos + this.imageWidth - 84, this.topPos + 202, 72, 16, this.toggleLabel(), button -> this.click(0))
      );
   }
   private Component toggleLabel() {
      return Component.translatable(
         ((SatelliteControlMenu)this.menu).running() ? "gui.industrialcrops.satellite_control.stop" : "gui.industrialcrops.satellite_control.start"
      );
   }
   protected void containerTick() {
      super.containerTick();
      if (this.toggle != null) {
         this.toggle.setMessage(this.toggleLabel());
      }
   }
   private void click(int id) {
      if (this.minecraft != null && this.minecraft.gameMode != null) {
         this.minecraft.gameMode.handleInventoryButtonClick(((SatelliteControlMenu)this.menu).containerId, id);
      }
   }
   private static int nodeX(int area) {
      return 18 + (int)Math.round((double)ORBIT[area] * Math.cos(Math.toRadians(ANGLE[area])));
   }
   private static int nodeY(int area) {
      return 46 + (int)Math.round((double)ORBIT[area] * Math.sin(Math.toRadians(ANGLE[area])));
   }
   private boolean reachable(int area) {
      return area <= ((SatelliteControlMenu)this.menu).maxReach();
   }
   private int servingTier(int area) {
      for (int tier = area; tier <= 3; tier++) {
         if (((SatelliteControlMenu)this.menu).count(tier) > 0) {
            return tier;
         }
      }
      return 0;
   }
   private int hoveredArea(double mouseX, double mouseY) {
      for (int area = 1; area <= 3; area++) {
         int cx = this.leftPos + 12 + nodeX(area);
         int cy = this.topPos + 47 + nodeY(area);
         if (Math.abs(mouseX - (double)cx) <= 8.0 && Math.abs(mouseY - (double)cy) <= 8.0) {
            return area;
         }
      }
      return 0;
   }
   private static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
      for (int dy = -r; dy <= r; dy++) {
         int half = (int)Math.floor(Math.sqrt((double)(r * r - dy * dy) + 0.5));
         g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
      }
   }
   private static void dottedOrbit(GuiGraphics g, int cx, int cy, int r, int color, int phase) {
      int steps = Math.max(24, (int)((Math.PI * 2) * (double)r / 1.5));
      for (int i = 0; i < steps; i++) {
         if ((i + phase) % 4 != 3) {
            double a = (Math.PI * 2) * (double)i / (double)steps;
            int x = cx + (int)Math.round((double)r * Math.cos(a));
            int y = cy + (int)Math.round((double)r * Math.sin(a));
            g.fill(x, y, x + 1, y + 1, color);
         }
      }
   }
   private static void line(GuiGraphics g, int x0, int y0, int x1, int y1, int color, int dash, int offset) {
      int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
      for (int i = 0; i <= steps; i++) {
         if (dash <= 0 || (i + offset) / dash % 2 != 1) {
            int x = x0 + (x1 - x0) * i / Math.max(1, steps);
            int y = y0 + (y1 - y0) * i / Math.max(1, steps);
            g.fill(x, y, x + 1, y + 1, color);
         }
      }
   }
   private static void brackets(GuiGraphics g, int x, int y, int w, int h, int color, int arm) {
      g.fill(x, y, x + arm, y + 1, color);
      g.fill(x, y, x + 1, y + arm, color);
      g.fill(x + w - arm, y, x + w, y + 1, color);
      g.fill(x + w - 1, y, x + w, y + arm, color);
      g.fill(x, y + h - 1, x + arm, y + h, color);
      g.fill(x, y + h - arm, x + 1, y + h, color);
      g.fill(x + w - arm, y + h - 1, x + w, y + h, color);
      g.fill(x + w - 1, y + h - arm, x + w, y + h, color);
   }
   private static void lamp(GuiGraphics g, int x, int y, int color) {
      g.fill(x - 1, y - 1, x + 6, y + 6, -14341320);
      g.fill(x, y, x + 5, y + 5, color);
      g.fill(x, y, x + 2, y + 2, -2130706433);
   }
   private static void bar(GuiGraphics g, int x, int y, int w, int h, float fraction, int color, int highlight) {
      CrystalGuiStyle.drawInsetPanel(g, x - 1, y - 1, w + 2, h + 2);
      g.fill(x, y, x + w, y + h, -15459797);
      int filled = Math.round((float)w * Mth.clamp(fraction, 0.0F, 1.0F));
      if (filled > 0) {
         g.fill(x, y, x + filled, y + h, color);
         g.fill(x, y, x + filled, y + Math.min(h, 2), highlight);
      }
   }
   protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      long now = Util.getMillis();
      CrystalGuiStyle.drawContainer(g, x, y, this.imageWidth, this.imageHeight);
      bar(g, x + 12, y + 36, 248, 6, (float)((SatelliteControlMenu)this.menu).energy() / 1000000.0F, -13660210, -9904897);
      int mx = x + 12;
      int my = y + 47;
      g.fill(mx - 2, my - 2, mx + 248 + 2, my + 92 + 2, -14341320);
      CrystalGuiStyle.drawInsetPanel(g, mx - 1, my - 1, 250, 94);
      g.enableScissor(mx, my, mx + 248, my + 92);
      g.blit(GALAXY_MAP, mx, my, 4.0F, 18.0F, 248, 92, 256, 128);
      g.fill(mx, my, mx + 248, my + 92, 939919892);
      int px = mx + 18;
      int py = my + 46;
      int phase = (int)(now / 220L % 3L);
      for (int area = 1; area <= 3; area++) {
         dottedOrbit(g, px, py, ORBIT[area], this.reachable(area) ? -798434049 : -1600083784, this.reachable(area) ? phase : 0);
      }
      int selected = ((SatelliteControlMenu)this.menu).area();
      if (((SatelliteControlMenu)this.menu).status() == 0) {
         line(g, px, py, mx + nodeX(selected), my + nodeY(selected), -1061621505, 3, (int)(now / 90L % 6L));
      }
      fillCircle(g, px, py, 10, -14861466);
      fillCircle(g, px, py, 9, -13668434);
      g.fill(px - 6, py - 4, px - 1, py - 1, -11555222);
      g.fill(px + 1, py + 2, px + 6, py + 5, -11555222);
      g.fill(px - 3, py + 5, px + 1, py + 7, -11555222);
      fillCircle(g, px - 3, py - 4, 2, 1358954495);
      for (int tier = 1; tier <= 3; tier++) {
         if (((SatelliteControlMenu)this.menu).count(tier) > 0) {
            double t = ((double)now / 6000.0 + (double)tier * 0.37) % 1.0;
            double a = Math.toRadians(-40.0 + 80.0 * t);
            int sx = px + (int)Math.round((double)ORBIT[tier] * Math.cos(a));
            int sy = py + (int)Math.round((double)ORBIT[tier] * Math.sin(a));
            g.fill(sx - 1, sy - 1, sx + 2, sy + 2, -1);
            g.fill(sx - 3, sy, sx - 1, sy + 1, -9904897);
            g.fill(sx + 2, sy, sx + 4, sy + 1, -9904897);
         }
      }
      int hovered = this.hoveredArea((double)mouseX, (double)mouseY);
      for (int area = 1; area <= 3; area++) {
         int cx = mx + nodeX(area);
         int cy = my + nodeY(area);
         int nx = cx - 7;
         int ny = cy - 7;
         boolean open = this.reachable(area);
         int frame = open ? (area == hovered ? -4656897 : -13660210) : -9801082;
         g.fill(nx - 1, ny - 1, nx + 15 + 1, ny + 15 + 1, -16051678);
         g.fill(nx, ny, nx + 15, ny + 15, frame);
         g.fill(nx + 1, ny + 1, nx + 15 - 1, ny + 15 - 1, -15721936);
         int rock = AREA_COLOR[area];
         g.fill(nx + 3, ny + 4, nx + 7, ny + 8, rock);
         g.fill(nx + 8, ny + 3, nx + 11, ny + 6, rock);
         g.fill(nx + 6, ny + 9, nx + 11, ny + 12, rock);
         g.fill(nx + 3, ny + 4, nx + 5, ny + 5, -2130706433);
         g.fill(nx + 8, ny + 3, nx + 9, ny + 4, -2130706433);
         if (!open) {
            g.fill(nx + 1, ny + 1, nx + 15 - 1, ny + 15 - 1, -1609559008);
            g.fill(cx - 2, cy - 4, cx + 3, cy - 3, -4932146);
            g.fill(cx - 2, cy - 4, cx - 1, cy, -4932146);
            g.fill(cx + 2, cy - 4, cx + 3, cy, -4932146);
            g.fill(cx - 3, cy - 1, cx + 4, cy + 4, -4932146);
            g.fill(cx, cy, cx + 1, cy + 2, -12959406);
         }
         if (area == selected) {
            int pulse = (int)(now / 250L % 2L);
            brackets(g, nx - 3 - pulse, ny - 3 - pulse, 21 + pulse * 2, 21 + pulse * 2, open ? -1 : -2055606, 4);
         }
      }
      g.disableScissor();
      brackets(g, mx + 2, my + 2, 244, 88, -13660210, 7);
      CrystalGuiStyle.drawWorkPanel(g, x + 8, y + 145, 144, 54);
      CrystalGuiStyle.drawWorkPanel(g, x + 156, y + 145, 108, 54);
      int cycle = SpaceMiningAreas.cycleTicks(selected);
      bar(g, x + 8 + 6, y + 145 + 43, 132, 5, (float)((SatelliteControlMenu)this.menu).progress() / (float)Math.max(1, cycle), -13660210, -9904897);
      boolean blink = now / 400L % 2L == 0L;
      lamp(
         g,
         x + 156 + 5,
         y + 145 + 6,
         !((SatelliteControlMenu)this.menu).receiverFound()
            ? -7720912
            : (((SatelliteControlMenu)this.menu).receiverLinked() ? -9904897 : (blink ? -2055606 : -9744092))
      );
      lamp(g, x + 156 + 5, y + 145 + 17, ((SatelliteControlMenu)this.menu).terminalFound() ? -9904897 : -7720912);
      int tierx = this.servingTier(selected);
      float wear = tierx == 0
         ? 0.0F
         : (float)((SatelliteControlMenu)this.menu).durability(tierx) / (float)Math.max(1, ((SatelliteControlMenu)this.menu).maxDurability(tierx));
      bar(g, x + 156 + 6, y + 145 + 47, 96, 3, wear, wear > 0.25F ? -11555222 : -4173766, wear > 0.25F ? -7348064 : -25990);
   }
   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      IndustrialGuiStyle.drawFittedString(g, this.font, this.title.getString(), 12, 9, this.imageWidth - 24, -14142905, true);
      this.text(
         g,
         Component.translatable(
            "gui.industrialcrops.energy",
            new Object[]{String.format("%,d", Math.max(0, ((SatelliteControlMenu)this.menu).energy())), String.format("%,d", 1000000)}
         ),
         12,
         25,
         this.imageWidth - 24,
         -11509640
      );
      for (int area = 1; area <= 3; area++) {
         int lx = 12 + nodeX(area);
         int ly = 47 + nodeY(area) + 7 + 3;
         int color = this.reachable(area) ? -2558721 : -7695706;
         if (ly + 8 > 139) {
            ly = 47 + nodeY(area) - 7 - 10;
         }
         g.drawString(this.font, NUMERAL[area], lx - this.font.width(NUMERAL[area]) / 2, ly, color, true);
      }
      int area = ((SatelliteControlMenu)this.menu).area();
      int lx = 14;
      int lw = 132;
      int top = 150;
      this.text(
         g,
         Component.literal(NUMERAL[area] + " · ").append(Component.translatable("gui.industrialcrops.satellite_control.area." + area)),
         lx,
         top,
         lw,
         -14142905
      );
      this.text(g, Component.translatable("gui.industrialcrops.satellite_control.yield." + area), lx, top + 11, lw, -11509640);
      Component status;
      int statusColor;
      if (((SatelliteControlMenu)this.menu).satelliteLost()) {
         status = Component.translatable("gui.industrialcrops.satellite_control.lost");
         statusColor = -6661614;
      } else {
         int code = ((SatelliteControlMenu)this.menu).status();
         status = Component.translatable("gui.industrialcrops.satellite_control.status." + code);
         statusColor = code == 0 ? -14717016 : (code == 1 ? -11509640 : -6541264);
      }
      this.text(g, status, lx, top + 23, lw, statusColor);
      int rx = 169;
      int rw = 91;
      this.text(
         g,
         Component.translatable(
            !((SatelliteControlMenu)this.menu).receiverFound()
               ? "gui.industrialcrops.satellite_control.receiver.none"
               : (
                  ((SatelliteControlMenu)this.menu).receiverLinked()
                     ? "gui.industrialcrops.satellite_control.receiver.linked"
                     : "gui.industrialcrops.satellite_control.receiver.offline"
               )
         ),
         rx,
         top,
         rw,
         -14142905
      );
      this.text(
         g,
         Component.translatable(
            ((SatelliteControlMenu)this.menu).terminalFound()
               ? "gui.industrialcrops.satellite_control.terminal.found"
               : "gui.industrialcrops.satellite_control.terminal.none"
         ),
         rx,
         top + 11,
         rw,
         -14142905
      );
      this.text(
         g,
         Component.translatable(
            "gui.industrialcrops.satellite_control.orbit",
            new Object[]{((SatelliteControlMenu)this.menu).count(1), ((SatelliteControlMenu)this.menu).count(2), ((SatelliteControlMenu)this.menu).count(3)}
         ),
         162,
         top + 22,
         96,
         -11509640
      );
      int tier = this.servingTier(area);
      Component wear = tier == 0
         ? Component.translatable("gui.industrialcrops.satellite_control.no_durability")
         : Component.translatable(
            "gui.industrialcrops.satellite_control.durability",
            new Object[]{
               String.format("%,d", ((SatelliteControlMenu)this.menu).durability(tier)),
               String.format("%,d", ((SatelliteControlMenu)this.menu).maxDurability(tier))
            }
         );
      this.text(g, wear, 162, top + 32, 96, -11509640);
      this.text(
         g,
         Component.translatable(
            "gui.industrialcrops.satellite_control.delivered", new Object[]{String.format("%,d", ((SatelliteControlMenu)this.menu).mined())}
         ),
         12,
         206,
         this.imageWidth - 104,
         -11509640
      );
   }
   private void text(GuiGraphics g, Component text, int x, int y, int width, int color) {
      IndustrialGuiStyle.drawFittedString(g, this.font, text.getString(), x, y, width, color, false);
   }
   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.render(g, mouseX, mouseY, partialTick);
      int area = this.hoveredArea((double)mouseX, (double)mouseY);
      if (area > 0) {
         List<FormattedCharSequence> lines = new ArrayList<>();
         lines.add(
            Component.literal(NUMERAL[area] + " · ").append(Component.translatable("gui.industrialcrops.satellite_control.area." + area)).getVisualOrderText()
         );
         lines.add(Component.translatable("gui.industrialcrops.satellite_control.yield." + area).withStyle(ChatFormatting.GRAY).getVisualOrderText());
         lines.add(Component.translatable("gui.industrialcrops.satellite_control.reach." + area).withStyle(ChatFormatting.AQUA).getVisualOrderText());
         lines.add(
            Component.translatable(this.reachable(area) ? "gui.industrialcrops.satellite_control.click" : "gui.industrialcrops.satellite_control.locked")
               .withStyle(this.reachable(area) ? ChatFormatting.DARK_AQUA : ChatFormatting.RED)
               .getVisualOrderText()
         );
         g.renderTooltip(this.font, lines, mouseX, mouseY);
      } else {
         this.renderTooltip(g, mouseX, mouseY);
      }
   }
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int area = this.hoveredArea(mouseX, mouseY);
      if (button == 0 && area > 0) {
         if (this.reachable(area) && area != ((SatelliteControlMenu)this.menu).area() && this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.click(area);
         }
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }
}
