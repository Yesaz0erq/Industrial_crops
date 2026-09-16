package com.industrialcrops.client.gui;

import com.industrialcrops.screen.AnimalBreederMenu;
import com.industrialcrops.block.entity.AnimalBreederBlockEntity;
import java.util.List;
import com.industrialcrops.network.payload.BreederConfigPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public final class AnimalBreederScreen extends AbstractContainerScreen<AnimalBreederMenu> {
    private EditBox limit;private boolean wide,enabled,harvest,slaughter,error,initialized,dirty;
    public AnimalBreederScreen(AnimalBreederMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=288;imageHeight=320;}
    private Component text(String key){return Component.translatable("gui.industrialcrops.breeder."+key);}
    private Button area,on,crops,kill;
    @Override protected void init(){super.init();initialized=false;
        area=addRenderableWidget(new CrystalButton(leftPos+12,topPos+25,100,20,text("one"),b->{dirty=true;wide=!wide;labels();send();}));
        on=addRenderableWidget(new CrystalButton(leftPos+118,topPos+25,100,20,text("on"),b->{dirty=true;enabled=!enabled;labels();send();}));
        limit=new EditBox(font,leftPos+83,topPos+50,64,18,text("limit"));limit.setMaxLength(4);limit.setFilter(s->s.matches("[0-9]*"));limit.setResponder(s->dirty=true);addRenderableWidget(limit);
        addRenderableWidget(new CrystalButton(leftPos+155,topPos+49,63,20,text("apply"),b->send()));
        crops=addRenderableWidget(new CrystalButton(leftPos+12,topPos+73,100,20,text("harvest_on"),b->{dirty=true;harvest=!harvest;labels();send();}));
        kill=addRenderableWidget(new CrystalButton(leftPos+118,topPos+73,100,20,text("kill_on"),b->{dirty=true;slaughter=!slaughter;labels();send();}));
        area.setTooltip(Tooltip.create(text("range_help")));crops.setTooltip(Tooltip.create(text("food_help")));kill.setTooltip(Tooltip.create(text("kill_help")));
    }
    @Override protected void containerTick(){super.containerTick();if(!initialized||!dirty){var m=menu.machine();wide=m.wide();enabled=m.enabled();harvest=m.harvest();slaughter=m.slaughter();limit.setValue(Integer.toString(m.limit()));labels();initialized=true;dirty=false;}}
    private void labels(){area.setMessage(text(wide?"nine":"one"));on.setMessage(text(enabled?"on":"off"));crops.setMessage(text(harvest?"harvest_on":"harvest_off"));kill.setMessage(text(slaughter?"kill_on":"kill_off"));}
    private void send(){try{int n=Integer.parseInt(limit.getValue());if(n<2||n>4096)throw new NumberFormatException();com.industrialcrops.network.ModNetworking.sendToServer(new BreederConfigPayload(menu.containerId,n,wide,enabled,harvest,slaughter));error=false;}catch(NumberFormatException ex){error=true;}}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(limit.isFocused()&&key!=256){if(key==257||key==335){send();limit.setFocused(false);return true;}limit.keyPressed(key,scan,modifiers);return true;}return super.keyPressed(key,scan,modifiers);}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;CrystalGuiStyle.drawContainer(g,x,y,230,imageHeight);
        CrystalGuiStyle.drawWorkPanel(g,x+234,y+25,54,160);
        IndustrialGuiStyle.drawVerticalMeter(g,x+240,y+49,128,energy(),AnimalBreederBlockEntity.ENERGY_CAPACITY,IndustrialGuiStyle.ENERGY_RED,false);
        IndustrialGuiStyle.drawVerticalMeter(g,x+264,y+49,128,menu.machine().data().get(2),AnimalBreederBlockEntity.TANK_CAPACITY,0xff8e72bf,false);
        for(int top:new int[]{147,187}){CrystalGuiStyle.drawWorkPanel(g,x+23,y+top,184,26);for(int c=0;c<9;c++)CrystalGuiStyle.drawSlot(g,x+29+c*19,y+top+4);}
        CrystalGuiStyle.drawWorkPanel(g,x+23,y+229,184,62);CrystalGuiStyle.drawWorkPanel(g,x+23,y+293,184,25);
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)CrystalGuiStyle.drawSlot(g,x+29+c*19,y+233+r*19);
        for(int c=0;c<9;c++)CrystalGuiStyle.drawSlot(g,x+29+c*19,y+295);
    }
    private int energy(){var d=menu.machine().data();return (d.get(0)&65535)|(d.get(1)<<16);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        IndustrialGuiStyle.drawFittedString(g,font,title.getString(),9,7,212,IndustrialGuiStyle.TEXT,true);
        g.drawString(font,text("limit"),12,55,IndustrialGuiStyle.TEXT,false);
        var d=menu.machine().data();String count=Component.translatable("gui.industrialcrops.breeder.count",d.get(4),d.get(5),d.get(3)).getString();
        IndustrialGuiStyle.drawFittedString(g,font,count,12,98,206,IndustrialGuiStyle.TEXT,false);
        g.drawString(font,"FE",242,33,IndustrialGuiStyle.TEXT,false);
        IndustrialGuiStyle.drawFittedString(g,font,text("plasma").getString(),263,33,21,IndustrialGuiStyle.TEXT,true);
        g.drawString(font,text("food"),30,134,IndustrialGuiStyle.MUTED_TEXT,false);
        g.drawString(font,text("drops"),30,174,IndustrialGuiStyle.MUTED_TEXT,false);
        g.drawString(font,playerInventoryTitle,30,217,IndustrialGuiStyle.MUTED_TEXT,false);
        IndustrialGuiStyle.drawFittedString(g,font,text(error?"invalid":"status_"+d.get(10)).getString(),12,114,206,IndustrialGuiStyle.TEXT,false);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);super.render(g,mx,my,partial);renderTooltip(g,mx,my);
        if(my>=topPos+49&&my<topPos+177){
            if(mx>=leftPos+240&&mx<leftPos+258)g.renderComponentTooltip(font,List.of(
                    Component.translatable("gui.industrialcrops.energy",energy(),AnimalBreederBlockEntity.ENERGY_CAPACITY),
                    Component.literal(AnimalBreederBlockEntity.ENERGY_PER_BIRTH+" FE/"+text("birth").getString())),mx,my);
            else if(mx>=leftPos+264&&mx<leftPos+282)g.renderComponentTooltip(font,List.of(text("plasma"),
                    Component.translatable("gui.industrialcrops.fluid_amount",menu.machine().data().get(2),AnimalBreederBlockEntity.TANK_CAPACITY),text("plasma_help")),mx,my);
        }
    }
}
