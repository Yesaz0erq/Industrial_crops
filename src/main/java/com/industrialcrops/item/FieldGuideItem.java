package com.industrialcrops.item;

import com.industrialcrops.client.FieldGuideClient;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

public final class FieldGuideItem extends Item {
   public FieldGuideItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide()) {
         FieldGuideClient.open();
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("tooltip.industrialcrops.field_guide").withStyle(ChatFormatting.GRAY));
      tooltip.add(Component.translatable("tooltip.industrialcrops.field_guide.recipe").withStyle(ChatFormatting.DARK_GRAY));
   }
}
