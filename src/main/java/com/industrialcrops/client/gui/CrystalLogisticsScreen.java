package com.industrialcrops.client.gui;

import com.industrialcrops.network.payload.LogisticsConfigPayload;
import com.industrialcrops.screen.CrystalLogisticsMenu;
import com.industrialcrops.screen.CrystalLogisticsLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;

public final class CrystalLogisticsScreen extends IndustrialContainerScreen<CrystalLogisticsMenu> {
    private EditBox channel;
    private Button mode;
    private boolean output;
    public CrystalLogisticsScreen(CrystalLogisticsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth=CrystalLogisticsLayout.WIDTH; imageHeight=CrystalLogisticsLayout.HEIGHT;
    }
    private Component text(String key) { return Component.translatable("gui.industrialcrops.logistics."+key); }
    @Override protected void init() {
        super.init(); output=menu.machine().output();
        channel=new EditBox(font, leftPos+45, topPos+29, 116, 16, text("channel"));
        channel.setMaxLength(32); channel.setValue(menu.machine().channel()); addRenderableWidget(channel);
        addRenderableWidget(new CrystalButton(leftPos+167,topPos+27,46,20,text("apply"), b->save()));
        mode=addRenderableWidget(new CrystalButton(leftPos+13,topPos+51,162,20,text(output ? "output" : "input"), b->{ output=!output; mode.setMessage(text(output ? "output" : "input")); save(); }));
        mode.setTooltip(Tooltip.create(text("mode_help")));
    }
    private void save() { PacketDistributor.sendToServer(new LogisticsConfigPayload(menu.containerId,channel.getValue(),output)); channel.setFocused(false); }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (channel.isFocused()) {
            if (key==257 || key==335) { save(); return true; }
            if (key!=256) { channel.keyPressed(key,scan,modifiers); return true; }
        }
        return super.keyPressed(key,scan,modifiers);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        int x=leftPos,y=topPos;
        g.blit(IndustrialGuiStyle.containerTexture("infinite_logistics_transfer_device"),x,y,0,0,imageWidth,imageHeight,imageWidth,imageHeight);
        g.fill(x+12,y+80,x+14,y+97, menu.machine().connections()>0 ? 0xff54aade : 0xff777b87);
        IndustrialGuiStyle.drawVerticalMeter(g,x+CrystalLogisticsLayout.METER_X,y+CrystalLogisticsLayout.METER_Y,CrystalLogisticsLayout.METER_HEIGHT,menu.machine().tank().getFluidAmount(),16000,0xff4c9ed0,false);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my) {
        IndustrialGuiStyle.drawFittedString(g,font,title.getString(),8,titleLabelY,imageWidth-16,IndustrialGuiStyle.TEXT,true);
        IndustrialGuiStyle.drawFittedString(g,font,playerInventoryTitle.getString(),CrystalLogisticsLayout.GRID_X,CrystalLogisticsLayout.INVENTORY_LABEL_Y,170,IndustrialGuiStyle.MUTED_TEXT,false);
        g.drawString(font,text("channel"),12,32,IndustrialGuiStyle.TEXT,false);
        String status=menu.machine().channel().isEmpty() ? text("inactive").getString() : Component.translatable("gui.industrialcrops.logistics.connected",menu.machine().connections()).getString();
        IndustrialGuiStyle.drawFittedString(g,font,status,19,80,157,IndustrialGuiStyle.TEXT,false);
        IndustrialGuiStyle.drawFittedString(g,font,text("range"+menu.machine().tier()).getString(),19,91,157,IndustrialGuiStyle.MUTED_TEXT,false);
        IndustrialGuiStyle.drawFittedString(g,font,text("buffer").getString(),CrystalLogisticsLayout.GRID_X,CrystalLogisticsLayout.BUFFER_LABEL_Y,170,IndustrialGuiStyle.MUTED_TEXT,false);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g,mx,my,partial); super.render(g,mx,my,partial); renderTooltip(g,mx,my);
        if(mx>=leftPos+194&&mx<leftPos+220&&my>=topPos+78&&my<topPos+142) {
            var fluid=menu.machine().tank().getFluid();
            g.renderComponentTooltip(font,List.of(fluid.isEmpty()?Component.translatable("gui.industrialcrops.empty_tank"):fluid.getHoverName(),Component.literal(fluid.getAmount()+" / 16000 mB")),mx,my);
        }
        if(mx>=leftPos+186&&mx<leftPos+214&&my>=topPos+48&&my<topPos+74&&!menu.getSlot(9).hasItem()) g.renderTooltip(font,text("upgrade_help"),mx,my);
    }
}
