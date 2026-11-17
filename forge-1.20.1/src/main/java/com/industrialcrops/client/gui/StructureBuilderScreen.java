package com.industrialcrops.client.gui;

import com.industrialcrops.screen.StructureBuilderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class StructureBuilderScreen extends AbstractContainerScreen<StructureBuilderMenu> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("industrialcrops","textures/gui/structure_builder.png");
    private final boolean creative;
    private Button target,tier,start,stop;
    public StructureBuilderScreen(StructureBuilderMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title); imageWidth=300; imageHeight=236; creative=inventory.player.getAbilities().instabuild;
    }
    private Component text(String name) { return Component.translatable("gui.industrialcrops.builder."+name); }
    private void send(int id) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id); }
    private Button button(int x,int y,int width,Component label,int id) {
        return addRenderableWidget(Button.builder(label,b->send(id)).bounds(leftPos+x,topPos+y,width,18).build());
    }
    @Override protected void init() {
        super.init();
        target=button(12,28,106,text("type.0"),0);
        tier=button(122,28,54,Component.literal("16×16"),1);
        start=button(12,50,77,text("start"),2);
        stop=button(93,50,47,text("stop"),3);
        button(144,50,15,Component.literal("‹"),4);
        button(161,50,15,Component.literal("›"),5);
        button(188,200,100,text("refresh"),6);
    }
    @Override protected void containerTick() {
        super.containerTick();
        target.setMessage(text("type."+menu.type()));
        int[] widths={16,32,32,48},depths={16,16,32,48};
        tier.setMessage(Component.literal(widths[menu.tier()]+"×"+depths[menu.tier()]));
        target.active=tier.active=start.active=!menu.running(); stop.active=menu.running();
    }
    @Override protected void renderBg(GuiGraphics g,float partialTick,int mouseX,int mouseY) {
        g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight,300,236);
        int progress=menu.total()==0 ? 0 : Math.min(276,menu.progress()*276/menu.total());
        g.fill(leftPos+12,topPos+229,leftPos+12+progress,topPos+232,0xFF55D4BE);
    }
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY) {
        g.drawString(font,title,12,10,0xDDEBF0,false);
        g.drawString(font,Component.translatable("gui.industrialcrops.builder.storage",menu.page()+1),12,69,0x46586A,false);
        g.drawString(font,text("materials"),188,31,0xB5E5EA,false);
        for (int row=0; row<10; row++) {
            ItemStack material=menu.material(row);
            if (material.isEmpty()) continue;
            int y=46+row*12,need=menu.needed(row),stock=menu.stock(material);
            g.pose().pushPose(); g.pose().translate(188,y,0); g.pose().scale(0.75F,0.75F,1);
            g.renderItem(material,0,0); g.pose().popPose();
            g.drawString(font,font.plainSubstrByWidth(material.getHoverName().getString(),32),202,y+2,0xD5DEE5,false);
            String counts=(creative ? "∞" : Integer.toString(stock))+"/"+need;
            g.drawString(font,counts,288-font.width(counts),y+2,creative || stock>=need ? 0x74DFBD : 0xFFAB72,false);
        }
        g.drawString(font,Component.translatable("gui.industrialcrops.builder.speed",menu.speed()),188,166,0xB5E5EA,false);
        Component status=text("status."+menu.status());
        g.drawString(font,font.plainSubstrByWidth(status.getString(),164),12,137,menu.status()>=3 ? 0xAD442A : 0x2B6E68,false);
        g.drawString(font,creative ? text("creative") : Component.literal(menu.progress()+" / "+menu.total()),188,220,0x92B6C2,false);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick) {
        renderBackground(g);
        super.render(g,mouseX,mouseY,partialTick); renderTooltip(g,mouseX,mouseY);
        int x=mouseX-leftPos,y=mouseY-topPos;
        if (x>=188 && x<288 && y>=166 && y<176) g.renderTooltip(font,text("speed_help"),mouseX,mouseY);
        else if (x>=12 && x<176 && y>=134 && y<149) g.renderTooltip(font,text("status."+menu.status()),mouseX,mouseY);
        else if (x>=188 && x<288 && y>=46 && y<166) {
            ItemStack material=menu.material((y-46)/12);
            if (!material.isEmpty()) g.renderTooltip(font,material.getHoverName(),mouseX,mouseY);
        } else if (x>=12 && x<176 && y>=28 && y<46) g.renderTooltip(font,text("help"),mouseX,mouseY);
    }
}
