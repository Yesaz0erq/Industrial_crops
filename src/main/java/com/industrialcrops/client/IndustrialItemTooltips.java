package com.industrialcrops.client;

import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.crop.CropQuality;
import com.industrialcrops.item.RemoteAccessDeviceItem;
import com.industrialcrops.machine.UpgradeCompatibility;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(
   modid = "industrialcrops",
   value = {Dist.CLIENT}
)
public final class IndustrialItemTooltips {
   private IndustrialItemTooltips() {
   }

   @SubscribeEvent
   public static void addFunctionalDescription(ItemTooltipEvent event) {
      addCropQuality(event);
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
      if ("industrialcrops".equals(id.getNamespace()) || "carrote".equals(id.getNamespace())) {
         if (id.getPath().equals("remote_access_device")) {
            addRemoteBindingDetails(event);
         } else {
            boolean functionalBlock = event.getItemStack().getItem() instanceof BlockItem;
            boolean fusionPickaxe = id.getPath().equals("fusion_ingot_pickaxe");
            boolean compressedPotato = id.getPath().equals("compressed_potato_10k") || id.getPath().equals("black_nebula_carrot")
               || id.getPath().equals("purple_dragon_carrot")
               || id.getPath().equals("omni_carrot") || id.getPath().equals("omni_potion") || id.getPath().equals("omni_bucket");
            boolean component = UpgradeCompatibility.hasComponentInfo(event.getItemStack().getItem());
            if (functionalBlock || fusionPickaxe || compressedPotato || component) {
               String key = "tooltip." + id.getNamespace() + ".function." + id.getPath();
               if (I18n.exists(key)) {
                  event.getToolTip().add(Component.empty());
                  if (Screen.hasShiftDown()) {
                     event.getToolTip().add(Component.translatable("tooltip.industrialcrops.function").withStyle(ChatFormatting.GOLD));
                     event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
                     if (id.getPath().equals("matter_digitization_device")) {
                        event.getToolTip().add(rainbow(Component.translatable("tooltip.industrialcrops.matter_digitization_device.hint").getString()));
                     }
                  } else {
                     event.getToolTip().add(Component.translatable("tooltip.industrialcrops.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
                  }
               }
            }
         }
      }
   }

   private static void addCropQuality(ItemTooltipEvent event) {
      CropGenetics.Genes genes = CropGenetics.read(event.getItemStack());
      if (genes != null) {
         CropQuality quality = genes.dominantQuality();
         MutableComponent qualityName = quality == CropQuality.SUPER
            ? rainbow(Component.translatable(quality.translationKey()).getString())
            : Component.translatable(quality.translationKey()).withStyle(style -> style.withColor(quality.color()));
         event.getToolTip().add(1, Component.translatable("tooltip.industrialcrops.crop_quality").withStyle(ChatFormatting.GRAY).append(qualityName));
         if (quality == CropQuality.SUPER && !event.getToolTip().isEmpty()) {
            event.getToolTip().set(0, rainbow(((Component)event.getToolTip().get(0)).getString()));
         }
      }
   }

   private static MutableComponent rainbow(String text) {
      MutableComponent result = Component.empty();
      int[] characters = text.codePoints().toArray();
      float baseHue = (float)(System.currentTimeMillis() % 4000L) / 4000.0F;

      for (int index = 0; index < characters.length; index++) {
         float hue = (baseHue + (float)index * 0.11F) % 1.0F;
         int color = Mth.hsvToRgb(hue, 0.78F, 1.0F);
         result.append(Component.literal(new String(Character.toChars(characters[index]))).withStyle(style -> style.withColor(color)));
      }

      return result;
   }

   private static void addRemoteBindingDetails(ItemTooltipEvent event) {
      BlockPos bound = RemoteAccessDeviceItem.getBoundPosition(event.getItemStack());
      if (bound != null) {
         event.getToolTip().add(Component.empty());
         if (Screen.hasShiftDown()) {
            event.getToolTip()
               .add(
                  Component.translatable("tooltip.industrialcrops.remote_access.coordinates", new Object[]{bound.getX(), bound.getY(), bound.getZ()})
                     .withStyle(ChatFormatting.GRAY)
               );
         } else {
            event.getToolTip().add(Component.translatable("tooltip.industrialcrops.remote_access.show_coordinates").withStyle(ChatFormatting.DARK_GRAY));
         }
      }
   }
}
