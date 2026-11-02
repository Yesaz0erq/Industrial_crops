package com.industrialcrops.item;

import com.industrialcrops.entity.FluxSlime;
import com.industrialcrops.machine.FluxOreIndex;
import com.industrialcrops.registry.ModEntities;
import com.industrialcrops.registry.ModItems;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.industrialcrops.compat.LegacyItemData.CustomData;

/** Slimeball imprinted with the natural metal ores of one world; turns a slime into a Flux Slime. */
public final class FluxOreGelItem extends Item {
   private static final int TOOLTIP_LIMIT = 10;

   public FluxOreGelItem(Properties properties) {
      super(properties);
   }

   public static ItemStack create(List<ResourceLocation> ingots, ResourceLocation world) {
      ItemStack stack = new ItemStack(ModItems.FLUX_ORE_GEL.get());
      com.industrialcrops.util.ItemStackNbt.update(stack, tag -> {
         tag.put(FluxOreIndex.TAG_ORES, FluxOreIndex.writeIds(ingots));
         tag.putString(FluxOreIndex.TAG_WORLD, world.toString());
      });
      return stack;
   }

   public static Set<ResourceLocation> ingots(ItemStack stack) {
      return FluxOreIndex.readIds(com.industrialcrops.compat.LegacyItemData.getOrEmpty(stack).copyTag());
   }

   public static String world(ItemStack stack) {
      return com.industrialcrops.compat.LegacyItemData.getOrEmpty(stack).copyTag().getString(FluxOreIndex.TAG_WORLD);
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return !ingots(stack).isEmpty();
   }

   @Override
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
      if (target.getType() != EntityType.SLIME || !(target instanceof Slime slime)) {
         return InteractionResult.PASS;
      }
      if (ingots(stack).isEmpty()) {
         if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.translatable("message.industrialcrops.flux_ore_gel.blank").withStyle(ChatFormatting.RED), true);
         }
         return InteractionResult.FAIL;
      }
      if (player.level() instanceof ServerLevel server) {
         FluxSlime flux = ModEntities.FLUX_SLIME.get().create(server);
         if (flux == null) return InteractionResult.FAIL;
         flux.moveTo(slime.getX(), slime.getY(), slime.getZ(), slime.getYRot(), 0.0F);
         flux.finalizeSpawn(server, server.getCurrentDifficultyAt(slime.blockPosition()), MobSpawnType.CONVERSION, null, null);
         flux.learn(ingots(stack));
         if (slime.hasCustomName()) flux.setCustomName(slime.getCustomName());
         slime.discard();
         server.addFreshEntity(flux);
         server.sendParticles(ParticleTypes.WITCH, flux.getX(), flux.getY() + 0.5D, flux.getZ(), 24, 0.4D, 0.4D, 0.4D, 0.05D);
         server.playSound(null, flux.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.NEUTRAL, 0.6F, 1.6F);
         if (!player.getAbilities().instabuild) stack.shrink(1);
      }
      return InteractionResult.sidedSuccess(player.level().isClientSide());
   }

   @Override
   public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      Set<ResourceLocation> ids = ingots(stack);
      if (ids.isEmpty()) {
         tooltip.add(Component.translatable("tooltip.industrialcrops.flux_ore_gel.blank").withStyle(ChatFormatting.GRAY));
         return;
      }
      String world = world(stack);
      if (!world.isEmpty()) {
         tooltip.add(Component.translatable("tooltip.industrialcrops.flux_ore_gel.world", world).withStyle(ChatFormatting.DARK_PURPLE));
      }
      tooltip.add(Component.translatable("tooltip.industrialcrops.flux_ore_gel.count", ids.size()).withStyle(ChatFormatting.LIGHT_PURPLE));
      int shown = 0;
      for (ResourceLocation id : ids) {
         if (shown++ >= TOOLTIP_LIMIT) {
            tooltip.add(Component.translatable("tooltip.industrialcrops.flux_ore_gel.more", ids.size() - TOOLTIP_LIMIT).withStyle(ChatFormatting.DARK_GRAY));
            break;
         }
         tooltip.add(Component.literal(" • ").withStyle(ChatFormatting.DARK_AQUA).append(FluxOreIndex.item(id).getDescription().copy().withStyle(ChatFormatting.AQUA)));
      }
      tooltip.add(Component.translatable("tooltip.industrialcrops.flux_ore_gel.use").withStyle(ChatFormatting.GRAY));
   }
}
