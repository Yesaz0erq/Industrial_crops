package com.industrialcrops.guide;

import com.industrialcrops.IndustrialCropsConfig;
import com.industrialcrops.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

@EventBusSubscriber(
   modid = "industrialcrops"
)
public final class FieldGuideEvents {
   private static final String RECEIVED_TAG = "industrialcrops:received_field_guide";

   private FieldGuideEvents() {
   }

   @SubscribeEvent
   public static void giveGuideOnFirstJoin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if ((Boolean)IndustrialCropsConfig.GIVE_FIELD_GUIDE_ON_FIRST_JOIN.get()) {
            CompoundTag root = player.getPersistentData();
            CompoundTag persisted = root.getCompound("PlayerPersisted");
            if (!persisted.getBoolean("industrialcrops:received_field_guide")) {
               ItemHandlerHelper.giveItemToPlayer(player, new ItemStack((ItemLike)ModItems.FIELD_GUIDE.get()));
               persisted.putBoolean("industrialcrops:received_field_guide", true);
               root.put("PlayerPersisted", persisted);
               player.sendSystemMessage(Component.translatable("message.industrialcrops.field_guide.received").withStyle(ChatFormatting.GOLD));
            }
         }
      }
   }
}
