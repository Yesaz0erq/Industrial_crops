package com.industrialcrops.client;
import com.industrialcrops.client.gui.FieldGuideScreen;
import net.minecraft.client.Minecraft;
public final class FieldGuideClient {
   private FieldGuideClient() {
   }
   public static void open() {
      Minecraft.getInstance().setScreen(new FieldGuideScreen());
   }
}
