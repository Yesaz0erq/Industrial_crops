package com.industrialcrops.client.glitch;

import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Swaps the name line of every Carrote item tooltip for the animated glitch renderer. */
@EventBusSubscriber(modid = "industrialcrops", value = Dist.CLIENT)
public final class CarroteGlitchNames {
   private CarroteGlitchNames() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void glitchName(RenderTooltipEvent.GatherComponents event) {
      ItemStack stack = event.getItemStack();
      if (!GlitchText.isCarrote(stack)) return;
      List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
      if (elements.isEmpty()) return;
      elements.get(0).ifLeft(text -> {
         Component name = text instanceof Component component ? component : Component.literal(text.getString());
         elements.set(0, Either.right(new GlitchNameTooltip(name, GlitchText.seed(stack))));
      });
   }

   @EventBusSubscriber(modid = "industrialcrops", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
   public static final class Registration {
      private Registration() {
      }

      @SubscribeEvent
      public static void registerFactories(RegisterClientTooltipComponentFactoriesEvent event) {
         event.register(GlitchNameTooltip.class, ClientGlitchNameTooltip::new);
      }
   }
}
