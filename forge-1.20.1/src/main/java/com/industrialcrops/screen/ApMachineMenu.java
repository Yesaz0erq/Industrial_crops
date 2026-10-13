package com.industrialcrops.screen;
import com.industrialcrops.machine.ApStructure;
import net.minecraftforge.items.IItemHandler;
public interface ApMachineMenu {
   int UPGRADE_X = -84;
   int UPGRADE_Y = 48;
   int BUTTON_PROJECTION = 20;
   IItemHandler upgrades();
   ApStructure.Status structure();
   boolean projection();
   void setUpgradesVisible(boolean var1);
}
