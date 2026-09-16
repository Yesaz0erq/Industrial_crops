package com.industrialcrops.client.gui;

import com.industrialcrops.screen.CraftingProcessorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class CraftingProcessorScreen extends IndustrialContainerScreen<CraftingProcessorMenu> {
    public CraftingProcessorScreen(CraftingProcessorMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title); imageWidth=226; imageHeight=244; inventoryLabelX=26; inventoryLabelY=149;
    }
    private Component text(String key){return Component.translatable("gui.industrialcrops.crafting_processor."+key);}
    private void action(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private void button(String key,int id,int x,int y,int w){addRenderableWidget(new CrystalButton(leftPos+x,topPos+y,w,18,text(key),b->action(id)));}
    @Override protected void init(){
        super.init(); button("inspect",0,18,66,89);button("craft",1,115,66,92);
        button("minus",2,48,42,18);button("plus",3,111,42,18);button("one",4,133,42,20);button("stack",5,156,42,25);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        CrystalGuiStyle.drawContainer(g,x,y,imageWidth,imageHeight);
        CrystalGuiStyle.drawWorkPanel(g,x+8,y+23,210,63);
        CrystalGuiStyle.drawSlot(g,x+18,y+41); CrystalGuiStyle.drawSlot(g,x+190,y+41);
        CrystalGuiStyle.drawInsetPanel(g,x+10,y+89,206,19);
        CrystalGuiStyle.drawWorkPanel(g,x+23,y+119,170,25);
        for(int i=0;i<9;i++)CrystalGuiStyle.drawSlot(g,x+26+18*i,y+122);
        CrystalGuiStyle.drawPlayerInventory(g,x,y,27,161,219);
        if(menu.getSlot(0).getItem().isEmpty()&&!menu.machine().target().isEmpty()){
            g.renderItem(menu.machine().target(),x+19,y+42);
            g.fill(x+19,y+42,x+35,y+58,0x407fc8ff);
        }
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        super.renderLabels(g,mx,my);
        IndustrialGuiStyle.drawFittedString(g,font,text("sample").getString(),14,29,36,IndustrialGuiStyle.TEXT,false);
        IndustrialGuiStyle.drawFittedString(g,font,text("link").getString(),180,29,32,IndustrialGuiStyle.TEXT,false);
        IndustrialGuiStyle.drawFittedString(g,font,text("quantity").getString(),55,29,112,IndustrialGuiStyle.TEXT,true);
        g.drawCenteredString(font,Integer.toString(menu.machine().requested()),88,47,0xff404040);
        IndustrialGuiStyle.drawFittedString(g,font,text(menu.machine().status()).getString(),15,95,196,IndustrialGuiStyle.TEXT,false);
        String binding=menu.machine().binding();
        int separator=binding.indexOf('|');
        if(separator>0) binding=Component.translatable(binding.substring(0,separator)).getString()+" · "+binding.substring(separator+1);
        IndustrialGuiStyle.drawFittedString(g,font,binding.isEmpty()?text("auto").getString():binding,14,110,198,IndustrialGuiStyle.MUTED_TEXT,false);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);super.render(g,mx,my,partial);renderTooltip(g,mx,my);
        if(mx>=leftPos+18&&mx<leftPos+38&&my>=topPos+27&&my<topPos+61)g.renderTooltip(font,text("sample_help"),mx,my);
        if(mx>=leftPos+187&&mx<leftPos+211&&my>=topPos+27&&my<topPos+61)g.renderTooltip(font,text("link_help"),mx,my);
        if(mx>=leftPos+10&&mx<leftPos+216&&my>=topPos+89&&my<topPos+109)
            g.renderComponentTooltip(font,java.util.List.of(text(menu.machine().status()),Component.translatable("gui.industrialcrops.crafting_processor.operations",menu.machine().operations()),text("scope")),mx,my);
    }
}
