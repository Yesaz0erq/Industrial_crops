package com.industrialcrops.client.tooltip;

import com.industrialcrops.machine.UpgradeCompatibility;
import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Components: hold Ctrl to see compatible devices, or whether an item is crafting-only. */
@EventBusSubscriber(modid = "industrialcrops", value = Dist.CLIENT)
public final class UpgradeComponentTooltips {
   private UpgradeComponentTooltips() {
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void addText(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (!UpgradeCompatibility.hasComponentInfo(stack.getItem())) {
         return;
      }
      List<ItemStack> machines = UpgradeCompatibility.compatibleMachines(stack.getItem());
      if (Screen.hasControlDown()) {
         if (machines.isEmpty()) {
            event.getToolTip().add(Component.translatable("tooltip.industrialcrops.upgrade.no_devices")
               .withStyle(ChatFormatting.DARK_GRAY));
         } else {
            event.getToolTip().add(Component.translatable("tooltip.industrialcrops.upgrade.compatible", machines.size())
               .withStyle(ChatFormatting.AQUA));
         }
      } else {
         event.getToolTip().add(Component.translatable("tooltip.industrialcrops.upgrade.hold_ctrl").withStyle(ChatFormatting.DARK_GRAY));
      }
   }

   @SubscribeEvent
   public static void addMachineIcons(RenderTooltipEvent.GatherComponents event) {
      ItemStack stack = event.getItemStack();
      if (stack.isEmpty() || !Screen.hasControlDown() || !UpgradeCompatibility.hasComponentInfo(stack.getItem())) {
         return;
      }
      List<ItemStack> machines = UpgradeCompatibility.compatibleMachines(stack.getItem());
      if (!machines.isEmpty()) {
         event.getTooltipElements().add(Either.right(new CompatibleMachinesTooltip(machines)));
      }
   }

   @EventBusSubscriber(modid = "industrialcrops", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
   public static final class Registration {
      private Registration() {
      }

      @SubscribeEvent
      public static void registerFactories(RegisterClientTooltipComponentFactoriesEvent event) {
         event.register(CompatibleMachinesTooltip.class, ClientCompatibleMachinesTooltip::new);
      }
   }
}
