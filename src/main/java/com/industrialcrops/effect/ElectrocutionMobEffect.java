package com.industrialcrops.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public final class ElectrocutionMobEffect extends MobEffect {
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation("industrialcrops", "electrocution"));
    private static final int DAMAGE_INTERVAL_TICKS = 40;
    private static final float DAMAGE_PER_PULSE = 2.0F;

    public ElectrocutionMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x45E8F4);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // The first pulse occurs after roughly two seconds, then every two seconds.
        return duration % DAMAGE_INTERVAL_TICKS == 1;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player
                && player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
            player.hurt(new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DAMAGE_TYPE)), DAMAGE_PER_PULSE);
        }
    }
}
