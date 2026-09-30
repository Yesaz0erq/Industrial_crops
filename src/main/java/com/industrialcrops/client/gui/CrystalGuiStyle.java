package com.industrialcrops.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/** Blue crystal-steel chassis shared by the crystal machine screens. */
final class CrystalGuiStyle {
    static final int TEXT = 0xff283247;
    static final int MUTED_TEXT = 0xff506078;
    static final int ENERGY_BLUE = 0xff2f8fce;
    static final int RESIDUE_BLUE = 0xff536bd1;

    private CrystalGuiStyle() {}
    private static void panel(GuiGraphics g,int x,int y,int w,int h,int color) {
        g.fill(x,y,x+w,y+h,color);
        g.fill(x,y,x+w,y+1,0xffedf0f5);g.fill(x,y,x+1,y+h,0xffedf0f5);
        g.fill(x,y+h-1,x+w,y+h,0xff454953);g.fill(x+w-1,y,x+w,y+h,0xff454953);
    }
    static void drawContainer(GuiGraphics g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0xff292d39);
        panel(g,x+1,y+1,w-2,h-2,0xff97a1b6);panel(g,x+3,y+3,w-6,h-6,0xffc5c7cd);
        panel(g,x+6,y+5,w-12,15,0xffaab1bd);
        g.fill(x+12,y+20,x+w-12,y+21,0xff5f6393);
    }
    static void drawWorkPanel(GuiGraphics g,int x,int y,int w,int h) {
        panel(g,x,y,w,h,0xff97a1b6);panel(g,x+2,y+2,w-4,h-4,0xffb8beca);
    }
    static void drawMachineWell(GuiGraphics g,int x,int y) {
        panel(g,x-3,y-3,24,24,0xff68768f);
        drawSlot(g,x,y);
        g.fill(x+2,y-3,x+16,y-2,0xff5f8fca);
    }
    static void drawCommonPanel(GuiGraphics g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0xff252b38);
        panel(g,x+1,y+1,w-2,h-2,0xff77869d);
        panel(g,x+3,y+3,w-6,h-6,0xffc5c7cd);
    }
    static void drawInsetPanel(GuiGraphics g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0xff454953);g.fill(x+1,y+1,x+w,y+h,0xfff1f1f3);
        g.fill(x+1,y+1,x+w-1,y+h-1,0xff898e99);
    }
    static void drawSlot(GuiGraphics g,int x,int y){drawInsetPanel(g,x,y,18,18);}
    static void drawPlayerInventory(GuiGraphics g,int left,int top,int x,int y,int hotbar) {
        drawWorkPanel(g,left+x-4,top+y-4,170,hotbar-y+24);
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)drawSlot(g,left+x+c*18-1,top+y+r*18-1);
        for(int c=0;c<9;c++)drawSlot(g,left+x+c*18-1,top+hotbar-1);
    }
}
