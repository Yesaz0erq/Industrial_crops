package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.DigitalMiniatureForestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class DigitalMiniatureForestScreen extends IndustrialContainerScreen<DigitalMiniatureForestMenu>{
    private Button upgradeTab;private boolean upgradesOpen;
    public DigitalMiniatureForestScreen(DigitalMiniatureForestMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=166;inventoryLabelY=72;}
    @Override protected void init(){super.init();upgradeTab=addRenderableWidget(Button.builder(Component.empty(),b->{upgradesOpen=!upgradesOpen;menu.setUpgradeSlotsVisible(upgradesOpen);}).bounds(leftPos-20,topPos+20,20,20).build());menu.setUpgradeSlotsVisible(false);}
    @Override protected void renderBg(GuiGraphics g,float tick,int mx,int my){IndustrialGuiStyle.drawContainer(g,leftPos,topPos,imageWidth,imageHeight);IndustrialGuiStyle.drawInsetPanel(g,leftPos+22,topPos+22,132,38);IndustrialGuiStyle.drawMekanismBar(g,leftPos+75,topPos+63,Math.min(25,menu.progress()*25/Math.max(1,menu.maxProgress())));IndustrialGuiStyle.drawPlayerInventory(g,leftPos,topPos,8,84,142);IndustrialGuiStyle.drawVerticalMeter(g,leftPos+178,topPos+18,58,menu.energy(),DigitalMiniatureForestBlockEntity.ENERGY_CAPACITY,IndustrialGuiStyle.ENERGY_RED,false);if(upgradesOpen){IndustrialGuiStyle.drawCommonPanel(g,leftPos-80,topPos+20,60,52);for(int i=0;i<4;i++)IndustrialGuiStyle.drawSlot(g,leftPos+DigitalMiniatureForestMenu.UPGRADE_X-1+i%2*22,topPos+DigitalMiniatureForestMenu.UPGRADE_Y-1+i/2*22);}}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){IndustrialGuiStyle.drawFittedString(g,font,title.getString(),8,6,160,IndustrialGuiStyle.TEXT,true);Component status=Component.translatable(menu.connected()?(menu.treeId()==0?"gui.industrialcrops.digital_forest.waiting":"gui.industrialcrops.digital_forest.growing"):"gui.industrialcrops.storage.disconnected");IndustrialGuiStyle.drawFittedString(g,font,status.getString(),26,30,124,menu.connected()?IndustrialGuiStyle.TEXT:IndustrialGuiStyle.WARNING,true);if(menu.treeId()!=0)IndustrialGuiStyle.drawFittedString(g,font,Component.translatable("gui.industrialcrops.digital_forest.tree."+menu.treeId()).getString(),26,44,124,IndustrialGuiStyle.MUTED_TEXT,true);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,IndustrialGuiStyle.MUTED_TEXT,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float tick){renderBackground(g,mx,my,tick);super.render(g,mx,my,tick);if(upgradeTab!=null){g.renderItem(new ItemStack(ModItems.SPEED_COMPONENT_1.get()),upgradeTab.getX()+2,upgradeTab.getY()+2);if(upgradesOpen){g.fill(upgradeTab.getX(),upgradeTab.getY(),upgradeTab.getX()+20,upgradeTab.getY()+1,IndustrialGuiStyle.ACTIVE);g.fill(upgradeTab.getX(),upgradeTab.getY(),upgradeTab.getX()+1,upgradeTab.getY()+20,IndustrialGuiStyle.ACTIVE);}if(upgradeTab.isMouseOver(mx,my))g.renderTooltip(font,Component.translatable("gui.industrialcrops.upgrade_slots"),mx,my);}if(mx>=leftPos+178&&mx<leftPos+196&&my>=topPos+18&&my<topPos+76)g.renderTooltip(font,Component.translatable("gui.industrialcrops.energy",menu.energy(),DigitalMiniatureForestBlockEntity.ENERGY_CAPACITY),mx,my);renderTooltip(g,mx,my);}
    @Override public void removed(){menu.setUpgradeSlotsVisible(false);super.removed();}
}
