package com.industrialcrops.curios;
import java.util.ArrayList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
public final class CarroteAdvancedEffects {
   public static final String ABSORPTION_READY = "carrote_curios:absorption_ready";
   public static int armor(LivingEntity var0, int var1) {
      long var2 = (long)var1 + (CarroteCuriosEffects.has(var0, CarroteCuriosItems.HARD) ? 4L : 0L);
      if (CarroteCuriosEffects.has(var0, CarroteCuriosItems.UNBREAKABLE)) {
         var2 *= 2L;
      }
      return (int)Math.min(2147483647L, var2);
   }
   @SubscribeEvent
   public static void tick(TickEvent.PlayerTickEvent var0) {
      if (var0.phase != TickEvent.Phase.END) return;
      if (var0.player instanceof ServerPlayer var1 && var1.isAlive() && CarroteCuriosEffects.has(var1, CarroteCuriosItems.RESISTANCE)) {
         long var5 = var1.level().getGameTime();
         if (var1.getPersistentData().getLong("carrote_curios:absorption_ready") > var5) {
            return;
         }
         var1.getPersistentData().putLong("carrote_curios:absorption_ready", var5 + 600L);
         MobEffectInstance var4 = var1.getEffect(MobEffects.ABSORPTION);
         if (var4 == null || var4.getAmplifier() <= 4) {
            var1.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 4, true, false, true));
            var1.setAbsorptionAmount(Math.max(var1.getAbsorptionAmount(), 20.0F));
         }
         return;
      }
   }
   @SubscribeEvent
   public static void expel(net.minecraftforge.event.entity.living.LivingDamageEvent var0) {
      LivingEntity var1 = var0.getEntity();
      if (!var1.level().isClientSide() && !(var0.getAmount() <= 0.0F) && !var1.getActiveEffects().isEmpty()) {
         if (!(var0.getSource().getEntity() instanceof LivingEntity var2) || var2 == var1 || !CarroteCuriosEffects.has(var2, CarroteCuriosItems.EXPEL)) {
            return;
         }
         if (var1.removeAllEffects()) {
            if (var1.level() instanceof ServerLevel var5) {
               var5.sendParticles(ParticleTypes.WAX_OFF, var1.getX(), var1.getY() + (double)var1.getBbHeight() * 0.6, var1.getZ(), 12, 0.35, 0.4, 0.35, 0.02);
               var5.playSound(null, var1.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6F, 1.6F);
            }
         }
      }
   }
   public static void stealEquipment(LivingEntity var0, DamageSource var1) {
      if (var1.getEntity() instanceof ServerPlayer var2 && var2 != var0 && CarroteCuriosEffects.has(var2, CarroteCuriosItems.THIEF)) {
         long var10 = var0.level().getGameTime();
         if (var0.getPersistentData().contains("carrote_curios:stolen_at") && var0.getPersistentData().getLong("carrote_curios:stolen_at") == var10) {
            return;
         }
         var0.getPersistentData().putLong("carrote_curios:stolen_at", var10);
         ArrayList var5 = new ArrayList();
         for (EquipmentSlot var9 : EquipmentSlot.values()) {
            if (!var0.getItemBySlot(var9).isEmpty()) {
               var5.add(var9);
            }
         }
         if (var5.isEmpty()) {
            return;
         }
         EquipmentSlot var11 = (EquipmentSlot)var5.get(var2.getRandom().nextInt(var5.size()));
         ItemStack var12 = var0.getItemBySlot(var11);
         ItemStack var13 = var12.split(1);
         var0.setItemSlot(var11, var12);
         if (!var2.getInventory().add(var13)) {
            var2.drop(var13, false);
         }
         return;
      }
   }
}
