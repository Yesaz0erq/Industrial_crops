package com.industrialcrops.client.tooltip;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Renders compatible machine item models directly on the tooltip, up to 8 per row. */
public final class ClientCompatibleMachinesTooltip implements ClientTooltipComponent {
   private static final int PER_ROW = 8;
   private static final int CELL = 18;
   private final List<ItemStack> machines;

   public ClientCompatibleMachinesTooltip(CompatibleMachinesTooltip tooltip) {
      this.machines = tooltip.machines();
   }

   private int rows() {
      return (this.machines.size() + PER_ROW - 1) / PER_ROW;
   }

   @Override
   public int getHeight() {
      return this.rows() * CELL + 2;
   }

   @Override
   public int getWidth(Font font) {
      return Math.min(PER_ROW, this.machines.size()) * CELL;
   }

   @Override
   public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
      for (int i = 0; i < this.machines.size(); i++) {
         int cx = x + (i % PER_ROW) * CELL;
         int cy = y + (i / PER_ROW) * CELL;
         graphics.renderItem(this.machines.get(i), cx + 1, cy + 1);
      }
   }
}
