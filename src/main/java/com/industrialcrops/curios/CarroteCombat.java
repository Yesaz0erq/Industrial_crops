package com.industrialcrops.curios;

import com.industrialcrops.curios.mixin.CarroteCombatAccess;
import com.industrialcrops.curios.mixin.CarroteServerPlayerAccess;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gameevent.GameEvent;

/** Applies an attributed hit without entering shield or scripted invulnerability gates. */
public final class CarroteCombat {
    private static final ThreadLocal<LivingEntity> FORCED_DEATH = new ThreadLocal<>();

    private CarroteCombat() {}

    public static boolean handles(LivingEntity victim, DamageSource source) {
        return source.getEntity() instanceof LivingEntity attacker && attacker != victim
                && (CarroteCuriosEffects.has(attacker, CarroteCuriosItems.BREACH)
                || CarroteCuriosEffects.has(attacker, CarroteCuriosItems.ARMOR_PIERCING));
    }

    public static boolean attack(Entity target, DamageSource source, float amount) {
        return target instanceof LivingEntity victim && handles(victim, source)
                ? hurt(victim, source, amount) : target.hurt(source, amount);
    }

    public static boolean forcedDeath(LivingEntity victim) {
        return FORCED_DEATH.get() == victim;
    }

    public static boolean hurt(LivingEntity victim, DamageSource source, float amount) {
        if (victim.level().isClientSide || victim.isRemoved() || victim.getHealth() <= 0
                || !Float.isFinite(amount) || amount <= 0) return false;
        LivingEntity attacker = (LivingEntity) source.getEntity();
        if (victim instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return false;
            if (attacker instanceof Player other && (!other.canHarmPlayer(player)
                    || !victim.getServer().isPvpAllowed())) return false;
        }
        if (victim instanceof ServerPlayer player && (player.isChangingDimension()
                || (((CarroteServerPlayerAccess) player).carrote$spawnProtection() > 0
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)))) return false;
        boolean piercing = CarroteCuriosEffects.has(attacker, CarroteCuriosItems.ARMOR_PIERCING);
        var access = (CarroteCombatAccess) victim;
        float original = amount;
        if (!piercing) {
            if (source.is(DamageTypeTags.IS_FIRE) && victim.hasEffect(MobEffects.FIRE_RESISTANCE)) return false;
            if (victim.invulnerableTime > 10 && !source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
                amount -= access.carrote$getLastHurt();
                if (amount <= 0) return false;
            }
        }

        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            access.carrote$hurtArmor(source, amount);
            float armor = victim.getArmorValue() * (piercing ? 0.5F : 1.0F);
            amount = CombatRules.getDamageAfterAbsorb(victim, amount, source, armor,
                    (float) victim.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }
        if (!piercing && !source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            var resistance = victim.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (resistance != null && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                amount *= Math.max(0.0F, 1.0F - (resistance.getAmplifier() + 1) * 0.2F);
            }
            if (!source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) && victim.level() instanceof ServerLevel level) {
                amount = CombatRules.getDamageAfterMagicAbsorb(amount,
                        EnchantmentHelper.getDamageProtection(level, victim, source));
            }
        }
        amount = CarroteCuriosEffects.finalDamage(attacker, amount);
        if (!Float.isFinite(amount) || amount <= 0) return false;
        // Absorption remains a health pool rather than a resistance modifier.
        float absorbed = Math.min(victim.getAbsorptionAmount(), amount);
        victim.setAbsorptionAmount(victim.getAbsorptionAmount() - absorbed);
        amount -= absorbed;
        access.carrote$setLastHurt(original);
        access.carrote$setLastDamageSource(source);
        access.carrote$setLastDamageStamp(victim.level().getGameTime());
        victim.invulnerableTime = 20;
        victim.hurtDuration = 10;
        victim.hurtTime = 10;
        victim.setLastHurtByMob(attacker);
        if (attacker instanceof Player player) {
            access.carrote$setLastHurtByPlayer(player);
            access.carrote$setLastHurtByPlayerTime(100);
        }
        if (victim.isSleeping()) victim.stopSleeping();
        victim.getCombatTracker().recordDamage(source, amount);
        setHealth(victim, victim.getHealth() - amount);
        victim.level().broadcastDamageEvent(victim, source);
        victim.gameEvent(GameEvent.ENTITY_DAMAGE);
        victim.hurtMarked = true;
        if (!source.is(DamageTypeTags.NO_KNOCKBACK)) {
            var position = source.getSourcePosition();
            if (position != null) victim.knockback(.4, position.x - victim.getX(), position.z - victim.getZ());
        }
        if (victim instanceof ServerPlayer player) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(player, source, original, amount, false);
        }
        if (attacker instanceof ServerPlayer player) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(player, victim, source, original, amount, false);
        }
        if (victim.getHealth() <= 0) {
            if (!piercing) {
                if (!access.carrote$tryTotem(source)) victim.die(source);
                return true;
            }
            LivingEntity previous = FORCED_DEATH.get();
            try {
                FORCED_DEATH.set(victim);
                victim.die(source);
                setHealth(victim, 0);
            } finally {
                if (previous == null) FORCED_DEATH.remove(); else FORCED_DEATH.set(previous);
            }
        } else access.carrote$playHurtSound(source);
        return true;
    }

    private static void setHealth(LivingEntity victim, float health) {
        victim.getEntityData().set(CarroteCombatAccess.carrote$healthKey(), Mth.clamp(health, 0, victim.getMaxHealth()));
    }
}
