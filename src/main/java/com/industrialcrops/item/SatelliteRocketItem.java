package com.industrialcrops.item;

import com.industrialcrops.block.SatelliteRocketBlock;
import com.industrialcrops.world.SatelliteNetworkData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

public final class SatelliteRocketItem extends BlockItem {
   public static final String DURABILITY = "SatelliteDurability";
   public static final String MAX_DURABILITY = "SatelliteMaxDurability";

   public SatelliteRocketItem(Block block, Properties properties) {
      super(block, properties);
   }

   public int tier() {
      return this.getBlock() instanceof SatelliteRocketBlock rocket ? rocket.tier() : 0;
   }

   public static int durability(ItemStack stack) {
      CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
      if (tag.contains("SatelliteDurability")) {
         return Math.max(0, tag.getInt("SatelliteDurability"));
      } else {
         return stack.getItem() instanceof SatelliteRocketItem item ? SatelliteNetworkData.defaultDurability(item.tier()) : 0;
      }
   }

   public static int maxDurability(ItemStack stack) {
      CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
      if (tag.contains("SatelliteMaxDurability")) {
         return Math.max(1, tag.getInt("SatelliteMaxDurability"));
      } else {
         return stack.getItem() instanceof SatelliteRocketItem item ? Math.max(1, SatelliteNetworkData.defaultDurability(item.tier())) : 1;
      }
   }

   public static void setDurability(ItemStack stack, int durability, int max) {
      CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
         tag.putInt("SatelliteDurability", Math.max(0, durability));
         tag.putInt("SatelliteMaxDurability", Math.max(1, max));
      });
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(
         Component.translatable(
               "tooltip.industrialcrops.satellite.durability",
               new Object[]{String.format("%,d", durability(stack)), String.format("%,d", maxDurability(stack))}
            )
            .withStyle(ChatFormatting.AQUA)
      );
      tooltip.add(
         Component.translatable("tooltip.industrialcrops.satellite.reach", new Object[]{SatelliteNetworkData.reach(this.tier())})
            .withStyle(ChatFormatting.GRAY)
      );
   }
}
