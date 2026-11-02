package com.industrialcrops.entity;

import com.industrialcrops.item.FluxOreGelItem;
import com.industrialcrops.machine.FluxOreIndex;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * A slime imprinted with Flux Ore Gel. Feed it raw ore or ore blocks of a metal it has scan data for and it
 * digests them one by one into the matching ingot. Metals without a natural ore are never produced.
 */
public final class FluxSlime extends IndustrialGearSlime {
   public static final int DIGEST_TICKS = 40;
   public static final int DIGEST_CAPACITY = 64;
   private static final DustParticleOptions FLUX_PARTICLE = new DustParticleOptions(new Vector3f(0.70F, 0.36F, 0.98F), 1.1F);
   private static final DustParticleOptions SPARK_PARTICLE = new DustParticleOptions(new Vector3f(0.42F, 0.98F, 0.92F), 0.8F);
   private static final String TAG_DIGEST = "FluxDigest";
   private static final String TAG_DIGEST_TICKS = "FluxDigestTicks";

   private final Set<ResourceLocation> knownIngots = new LinkedHashSet<>();
   private ItemStack digest = ItemStack.EMPTY;
   private int digestTicks;

   public FluxSlime(EntityType<? extends Slime> type, Level level) {
      super(type, level, FLUX_PARTICLE, "flux_slime");
   }

   public Set<ResourceLocation> knownIngots() {
      return knownIngots;
   }

   public boolean knows(Item ingot) {
      return ingot != null && knownIngots.contains(BuiltInRegistries.ITEM.getKey(ingot));
   }

   /** Adds scan data; returns how many metals were new to this slime. */
   public int learn(Set<ResourceLocation> ingots) {
      int before = knownIngots.size();
      knownIngots.addAll(ingots);
      return knownIngots.size() - before;
   }

   @Override
   protected void inheritFrom(FriendlyIndustrialSlime parent) {
      if (parent instanceof FluxSlime flux) knownIngots.addAll(flux.knownIngots);
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   @Override
   public boolean shouldDespawnInPeaceful() {
      return false;
   }

   @Override
   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      boolean client = level().isClientSide();
      if (held.getItem() instanceof FluxOreGelItem) {
         if (!client) {
            int added = learn(FluxOreGelItem.ingots(held));
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.displayClientMessage(Component.translatable("message.industrialcrops.flux_slime.learned", added, knownIngots.size()), true);
            level().playSound(null, blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.NEUTRAL, 1.0F, 1.3F);
            burst(16);
         }
         return InteractionResult.sidedSuccess(client);
      }
      if (hand == InteractionHand.MAIN_HAND && held.isEmpty() && player.isShiftKeyDown()) {
         if (!client) player.displayClientMessage(describe(), false);
         return InteractionResult.sidedSuccess(client);
      }
      Item ingot = client ? null : FluxOreIndex.ingotFor(level(), held);
      if (client && FluxOreIndex.isOreInput(held)) return InteractionResult.SUCCESS;
      if (ingot != null) {
         if (!knows(ingot)) {
            player.displayClientMessage(Component.translatable("message.industrialcrops.flux_slime.unknown",
               ingot.getDescription()).withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
         }
         if (!digest.isEmpty() && !ItemStack.isSameItemSameComponents(digest, held)) {
            player.displayClientMessage(Component.translatable("message.industrialcrops.flux_slime.busy",
               digest.getHoverName()).withStyle(ChatFormatting.GOLD), true);
            return InteractionResult.CONSUME;
         }
         int room = DIGEST_CAPACITY - digest.getCount();
         int moved = Math.min(room, held.getCount());
         if (moved <= 0) {
            player.displayClientMessage(Component.translatable("message.industrialcrops.flux_slime.full"), true);
            return InteractionResult.CONSUME;
         }
         if (digest.isEmpty()) digest = held.copyWithCount(moved);
         else digest.grow(moved);
         if (!player.getAbilities().instabuild) held.shrink(moved);
         level().playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 1.0F, 0.8F);
         burst(10);
         return InteractionResult.CONSUME;
      }
      return super.mobInteract(player, hand);
   }

   private MutableComponent describe() {
      MutableComponent text = Component.translatable("message.industrialcrops.flux_slime.knows", knownIngots.size())
         .withStyle(ChatFormatting.LIGHT_PURPLE);
      boolean first = true;
      for (ResourceLocation id : knownIngots) {
         Item item = FluxOreIndex.item(id);
         text.append(Component.literal(first ? " " : ", ").withStyle(ChatFormatting.GRAY));
         text.append(item.getDescription().copy().withStyle(ChatFormatting.AQUA));
         first = false;
      }
      if (!digest.isEmpty()) {
         text.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
         text.append(Component.translatable("message.industrialcrops.flux_slime.digesting", digest.getCount(), digest.getHoverName())
            .withStyle(ChatFormatting.GOLD));
      }
      return text;
   }

   @Override
   public void tick() {
      super.tick();
      if (!(level() instanceof ServerLevel server) || digest.isEmpty()) return;
      if (++digestTicks % 8 == 0) {
         server.sendParticles(SPARK_PARTICLE, getX(), getY() + 0.5D, getZ(), 2, 0.25D, 0.25D, 0.25D, 0.0D);
      }
      if (digestTicks < DIGEST_TICKS) return;
      digestTicks = 0;
      Item ingot = FluxOreIndex.ingotFor(server, digest);
      ItemStack eaten = digest.split(1);
      if (ingot == null || !knows(ingot)) {
         // Data or recipes changed since feeding: hand the ore back untouched.
         spit(eaten);
         return;
      }
      spit(new ItemStack(ingot));
      server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eaten), getX(), getY() + 0.5D, getZ(), 6, 0.2D, 0.2D, 0.2D, 0.05D);
      level().playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 0.8F, 1.4F);
   }

   private void spit(ItemStack stack) {
      ItemEntity item = new ItemEntity(level(), getX(), getY() + 0.6D, getZ(), stack);
      item.setDeltaMovement((random.nextDouble() - 0.5D) * 0.2D, 0.28D, (random.nextDouble() - 0.5D) * 0.2D);
      item.setDefaultPickUpDelay();
      level().addFreshEntity(item);
   }

   private void burst(int count) {
      if (level() instanceof ServerLevel server) {
         server.sendParticles(FLUX_PARTICLE, getX(), getY() + 0.5D, getZ(), count, 0.35D, 0.35D, 0.35D, 0.0D);
         server.sendParticles(SPARK_PARTICLE, getX(), getY() + 0.5D, getZ(), count / 2, 0.4D, 0.4D, 0.4D, 0.0D);
      }
   }

   /** AP Slime Farm hook: the bulk conversion visibly pulses through the slime. */
   public void pulse() {
      burst(6);
   }

   @Override
   protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
      super.dropCustomDeathLoot(level, source, recentlyHit);
      if (!digest.isEmpty()) {
         spawnAtLocation(digest.copy());
         digest = ItemStack.EMPTY;
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.put(FluxOreIndex.TAG_ORES, FluxOreIndex.writeIds(knownIngots));
      if (!digest.isEmpty()) tag.put(TAG_DIGEST, digest.save(registryAccess()));
      tag.putInt(TAG_DIGEST_TICKS, digestTicks);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      knownIngots.clear();
      knownIngots.addAll(FluxOreIndex.readIds(tag));
      digest = tag.contains(TAG_DIGEST) ? ItemStack.parseOptional(registryAccess(), tag.getCompound(TAG_DIGEST)) : ItemStack.EMPTY;
      digestTicks = tag.getInt(TAG_DIGEST_TICKS);
   }
}
