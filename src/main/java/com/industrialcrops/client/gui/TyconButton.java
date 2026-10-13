package com.industrialcrops.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

final class TyconButton extends Button {
   TyconButton(int x, int y, int width, int height, Component text, OnPress press) {
      super(x, y, width, height, text, press, DEFAULT_NARRATION);
   }

   protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      IndustrialGuiStyle.drawTyconButton(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.isHoveredOrFocused());
      this.renderScrollingString(graphics, Minecraft.getInstance().font, 2, this.active ? -924161 : -3096603);
   }
}
