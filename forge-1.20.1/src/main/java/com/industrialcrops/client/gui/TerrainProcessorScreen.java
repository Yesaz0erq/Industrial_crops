package com.industrialcrops.client.gui;

import com.industrialcrops.network.payload.TerrainConfigPayload;
import com.industrialcrops.screen.TerrainProcessorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public final class TerrainProcessorScreen extends UpgradeableMachineScreen<TerrainProcessorMenu> {
    private EditBox chunkX,chunkZ,startY,endY;
    private boolean fill,replaceOnly,error,autoSupply;
    private Button mode,replace,source;
    public TerrainProcessorScreen(TerrainProcessorMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);imageWidth=230;imageHeight=299;inventoryLabelY=181;
    }
    private Component text(String key){return Component.translatable("gui.industrialcrops.terrain."+key);}
    private EditBox number(int x,int y,int value){
        EditBox field=new EditBox(font,leftPos+x,topPos+y,62,16,Component.empty());
        field.setMaxLength(9);field.setFilter(s->s.matches("-?[0-9]*"));field.setValue(Integer.toString(value));
        addRenderableWidget(field);return field;
    }
    @Override protected void init(){
        super.init();var m=menu.machine();fill=m.fill();replaceOnly=m.replaceOnly();autoSupply=m.autoSupply();
        chunkX=number(47,27,m.chunkX());chunkZ=number(150,27,m.chunkZ());
        startY=number(47,49,m.startY());endY=number(150,49,m.endY());
        mode=addRenderableWidget(new CrystalButton(leftPos+50,topPos+74,129,20,text(fill?"fill":"dig"),b->{fill=!fill;mode.setMessage(text(fill?"fill":"dig"));}));
        addRenderableWidget(new CrystalButton(leftPos+12,topPos+99,64,20,text("start"),b->send(0)));
        addRenderableWidget(new CrystalButton(leftPos+83,topPos+99,64,20,text("pause"),b->send(1)));
        addRenderableWidget(new CrystalButton(leftPos+154,topPos+99,64,20,text("defaults"),b->{
            send(2);var p=menu.machine().getBlockPos();
            chunkX.setValue(Integer.toString(p.getX()>>4));chunkZ.setValue(Integer.toString(p.getZ()>>4));
            startY.setValue(Integer.toString(Math.max(minecraft.level.getMinBuildHeight(),p.getY()-1)));
            endY.setValue(Integer.toString(minecraft.level.getMinBuildHeight()));
            fill=false;replaceOnly=true;autoSupply=true;source.setMessage(text("source_auto"));mode.setMessage(text("dig"));replace.setMessage(text("replace_only"));
        }));
        replace=addRenderableWidget(new CrystalButton(leftPos+12,topPos+122,206,18,text(replaceOnly?"replace_only":"replace_all"),b->{replaceOnly=!replaceOnly;replace.setMessage(text(replaceOnly?"replace_only":"replace_all"));}));
        source=addRenderableWidget(new CrystalButton(leftPos+12,topPos+143,206,18,text(autoSupply?"source_auto":"source_slot"),b->{
            autoSupply=!autoSupply;source.setMessage(text(autoSupply?"source_auto":"source_slot"));send(autoSupply?4:3);
        }));
        source.setTooltip(Tooltip.create(text("source_help")));
        replace.setTooltip(Tooltip.create(text("help")));
        chunkX.setTooltip(Tooltip.create(text("chunk_help")));chunkZ.setTooltip(Tooltip.create(text("chunk_help")));
    }
    private void send(int action){
        try{
            int cx=action==0?Integer.parseInt(chunkX.getValue()):0,cz=action==0?Integer.parseInt(chunkZ.getValue()):0;
            int y1=action==0?Integer.parseInt(startY.getValue()):0,y2=action==0?Integer.parseInt(endY.getValue()):0;
            com.industrialcrops.network.ModNetworking.sendToServer(new TerrainConfigPayload(menu.containerId,cx,cz,y1,y2,fill,replaceOnly,action));error=false;
        }catch(NumberFormatException e){error=true;}
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        for(EditBox field:new EditBox[]{chunkX,chunkZ,startY,endY})if(field.isFocused()&&key!=256){
            if(key==257||key==335){field.setFocused(false);return true;}
            field.keyPressed(key,scan,modifiers);return true;
        }
        return super.keyPressed(key,scan,modifiers);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        drawUpgradeDrawer(g,-58,24);
        CrystalGuiStyle.drawContainer(g,x,y,imageWidth,imageHeight);
        CrystalGuiStyle.drawSlot(g,x+18,y+76);CrystalGuiStyle.drawSlot(g,x+190,y+76);
        CrystalGuiStyle.drawWorkPanel(g,x+21,y+163,188,33);
        for(int c=0;c<9;c++)CrystalGuiStyle.drawSlot(g,x+26+c*19,y+175);
        CrystalGuiStyle.drawWorkPanel(g,x+21,y+210,188,61);
        CrystalGuiStyle.drawWorkPanel(g,x+21,y+271,188,23);
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)CrystalGuiStyle.drawSlot(g,x+26+c*19,y+213+r*19);
        for(int c=0;c<9;c++)CrystalGuiStyle.drawSlot(g,x+26+c*19,y+273);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        IndustrialGuiStyle.drawFittedString(g,font,title.getString(),8,titleLabelY,imageWidth-16,IndustrialGuiStyle.TEXT,true);
        IndustrialGuiStyle.drawFittedString(g,font,text("chunk_x").getString(),12,31,32,IndustrialGuiStyle.TEXT,false);IndustrialGuiStyle.drawFittedString(g,font,text("chunk_z").getString(),115,31,32,IndustrialGuiStyle.TEXT,false);
        IndustrialGuiStyle.drawFittedString(g,font,text("from_y").getString(),12,53,32,IndustrialGuiStyle.TEXT,false);IndustrialGuiStyle.drawFittedString(g,font,text("to_y").getString(),115,53,32,IndustrialGuiStyle.TEXT,false);
        g.drawString(font,text("sample"),12,66,IndustrialGuiStyle.MUTED_TEXT,false);g.drawString(font,text("supply"),184,66,IndustrialGuiStyle.MUTED_TEXT,false);
        g.drawString(font,text("drops"),27,165,IndustrialGuiStyle.MUTED_TEXT,false);
        var m=menu.machine();String state=text(error?"invalid":m.status()).getString();
        IndustrialGuiStyle.drawFittedString(g,font,state+"  "+m.changed()+" / "+m.total(),12,199,206,IndustrialGuiStyle.TEXT,false);
        // Progress shares one line; the inventory title is omitted to keep the slots clear.
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);super.render(g,mx,my,partial);renderTooltip(g,mx,my);renderUpgradeTab(g,mx,my);
        if(menu.getSlot(11).isActive()&&mx>=leftPos-62&&mx<leftPos-20&&my>=topPos+20&&my<topPos+62)g.renderTooltip(font,text("upgrades_help"),mx,my);
        if(mx>=leftPos+18&&mx<leftPos+37&&my>=topPos+76&&my<topPos+95)g.renderTooltip(font,text("sample_help"),mx,my);
    }
}
