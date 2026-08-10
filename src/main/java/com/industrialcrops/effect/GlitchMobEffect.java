package com.industrialcrops.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class GlitchMobEffect extends MobEffect {
    public GlitchMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xD44CFF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isAlive() && entity.getHealth() > 1.0F) entity.setHealth(1.0F);
        if (entity.isAlive()) entity.setAbsorptionAmount(0.0F);
        return true;
    }
}
