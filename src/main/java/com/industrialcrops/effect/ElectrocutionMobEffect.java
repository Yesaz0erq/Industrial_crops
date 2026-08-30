package com.industrialcrops.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public final class ElectrocutionMobEffect extends MobEffect {
    private static final int DAMAGE_INTERVAL_TICKS = 40;
    private static final float DAMAGE_PER_PULSE = 2.0F;

    public ElectrocutionMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x45E8F4);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // The first pulse occurs after roughly two seconds, then every two seconds.
        return duration % DAMAGE_INTERVAL_TICKS == 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player
                && player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
            player.hurt(player.damageSources().magic(), DAMAGE_PER_PULSE);
        }
        return true;
    }
}
