package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.ElectricFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ElectricFurnaceScreen extends IndustrialContainerScreen<ElectricFurnaceMenu> {
    private Button upgradeTab; private boolean upgradesOpen;
    public ElectricFurnaceScreen(ElectricFurnaceMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=192;inventoryLabelY=98;}
    @Override protected void init(){super.init();upgradeTab=addRenderableWidget(Button.builder(Component.empty(),b->{upgradesOpen=!upgradesOpen;menu.setUpgradeSlotsVisible(upgradesOpen);}).bounds(leftPos-20,topPos+20,20,20).build());menu.setUpgradeSlotsVisible(false);}
    @Override protected void renderBg(GuiGraphics g,float tick,int mx,int my){
        IndustrialGuiStyle.drawContainer(g,leftPos,topPos,imageWidth,imageHeight);
        for(int row=0;row<3;row++){
            IndustrialGuiStyle.drawSlot(g,leftPos+28,topPos+23+row*24);IndustrialGuiStyle.drawSlot(g,leftPos+128,topPos+23+row*24);
            IndustrialGuiStyle.drawMekanismSmallRight(g,leftPos+72,topPos+28+row*24,Math.min(28,menu.progress(row)*28/Math.max(1,menu.maxProgress())));
        }
        IndustrialGuiStyle.drawPlayerInventory(g,leftPos,topPos,8,110,168);
        IndustrialGuiStyle.drawVerticalMeter(g,leftPos+imageWidth+2,topPos+20,58,menu.energy(),ElectricFurnaceBlockEntity.ENERGY_CAPACITY,IndustrialGuiStyle.ENERGY_RED,false);
        if(upgradesOpen){IndustrialGuiStyle.drawCommonPanel(g,leftPos-80,topPos+20,60,52);for(int i=0;i<4;i++)IndustrialGuiStyle.drawSlot(g,leftPos+ElectricFurnaceMenu.UPGRADE_X-1+i%2*22,topPos+ElectricFurnaceMenu.UPGRADE_Y-1+i/2*22);}
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){IndustrialGuiStyle.drawFittedString(g,font,title.getString(),8,6,160,IndustrialGuiStyle.TEXT,true);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,IndustrialGuiStyle.MUTED_TEXT,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float tick){renderBackground(g);super.render(g,mx,my,tick);drawUpgradeTab(g,mx,my);if(mx>=leftPos+178&&mx<leftPos+196&&my>=topPos+20&&my<topPos+78)g.renderTooltip(font,Component.translatable("gui.industrialcrops.energy",menu.energy(),ElectricFurnaceBlockEntity.ENERGY_CAPACITY),mx,my);renderTooltip(g,mx,my);}
    private void drawUpgradeTab(GuiGraphics g,int mx,int my){if(upgradeTab==null)return;g.renderItem(new ItemStack(ModItems.SPEED_COMPONENT_1.get()),upgradeTab.getX()+2,upgradeTab.getY()+2);if(upgradesOpen){g.fill(upgradeTab.getX(),upgradeTab.getY(),upgradeTab.getX()+20,upgradeTab.getY()+1,IndustrialGuiStyle.ACTIVE);g.fill(upgradeTab.getX(),upgradeTab.getY(),upgradeTab.getX()+1,upgradeTab.getY()+20,IndustrialGuiStyle.ACTIVE);}if(upgradeTab.isMouseOver(mx,my))g.renderTooltip(font,Component.translatable("gui.industrialcrops.upgrade_slots"),mx,my);}
    @Override public void removed(){menu.setUpgradeSlotsVisible(false);super.removed();}
}
