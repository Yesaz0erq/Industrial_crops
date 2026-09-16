package com.industrialcrops.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Blue-gray controls keep the crystal machines consistent with their chassis. */
final class CrystalButton extends Button {
    CrystalButton(int x,int y,int width,int height,Component text,OnPress press) {
        super(x,y,width,height,text,press,DEFAULT_NARRATION);
    }
    @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial) {
        int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        CrystalGuiStyle.drawWorkPanel(g,x,y,w,h);
        g.fill(x+3,y+3,x+w-3,y+h-3,!active?0xff737c90:isHoveredOrFocused()?0xff788bad:0xff596b8c);
        renderScrollingString(g,Minecraft.getInstance().font,2,active?0xfff0f3ff:0xffb4bdce);
    }
}
