package com.industrialcrops.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Dragon Breath (from the Purple Dragon Carrot): a swirl of Ender Dragon breath surrounds the holder.
 * Like the dragon's breath cloud, it deals 6 magic damage once per second to every living thing
 * inside it, except the holder, other players and the holder's own pets.
 */
public final class DragonBreathMobEffect extends MobEffect {
    public static final double RADIUS = 3.0D;
    private static final float DAMAGE = 6.0F;
    private static final int DAMAGE_INTERVAL = 20;

    public DragonBreathMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB25AE8);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity holder, int amplifier) {
        if (!(holder.level() instanceof ServerLevel level)) return;
        double radius = RADIUS + amplifier;
        int tick = holder.tickCount;

        // Swirling ring of breath at chest height, plus drifting puffs inside it.
        if (tick % 2 == 0) {
            for (int i = 0; i < 6; i++) {
                float angle = (tick * 0.12F) + i * Mth.TWO_PI / 6;
                double r = radius * (0.75D + 0.25D * Mth.sin(tick * 0.2F + i));
                level.sendParticles(ParticleTypes.DRAGON_BREATH,
                        holder.getX() + Mth.cos(angle) * r, holder.getY() + 0.3D + (i % 3) * 0.45D, holder.getZ() + Mth.sin(angle) * r,
                        1, 0.05D, 0.05D, 0.05D, 0.005D);
            }
        }
        if (tick % 6 == 0) {
            level.sendParticles(ParticleTypes.DRAGON_BREATH, holder.getX(), holder.getY() + 0.2D, holder.getZ(),
                    6, radius * 0.5D, 0.15D, radius * 0.5D, 0.01D);
        }

        if (tick % DAMAGE_INTERVAL == 0) {
            AABB area = holder.getBoundingBox().inflate(radius, 1.0D, radius);
            boolean hit = false;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, target -> canHit(holder, target))) {
                if (target.distanceToSqr(holder) > (radius + 0.5D) * (radius + 0.5D)) continue;
                target.hurt(holder.damageSources().indirectMagic(holder, holder), DAMAGE);
                hit = true;
            }
            if (hit) {
                level.playSound(null, holder.getX(), holder.getY(), holder.getZ(), SoundEvents.ENDER_DRAGON_SHOOT,
                        SoundSource.PLAYERS, 0.35F, 1.4F);
            }
        }
        return;
    }

    private static boolean canHit(LivingEntity holder, LivingEntity target) {
        if (target == holder || !target.isAlive() || target.isSpectator()) return false;
        if (target instanceof Player) return false;
        if (holder.isAlliedTo(target)) return false;
        return !(target instanceof OwnableEntity pet && pet.getOwner() == holder);
    }
}
