package com.industrialcrops.effect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** Every beneficial vanilla status effect, as granted by the Omni Potion and the Omni Carrot. */
public final class OmniEffects {
    private OmniEffects() {
    }

    public static List<MobEffect> vanillaBeneficial() {
        List<MobEffect> effects = new ArrayList<>();
        for (MobEffect effect : BuiltInRegistries.MOB_EFFECT) {
            var id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (id != null && id.getNamespace().equals("minecraft") && effect.getCategory() == MobEffectCategory.BENEFICIAL) effects.add(effect);
        }
        effects.sort(java.util.Comparator.comparing(effect -> BuiltInRegistries.MOB_EFFECT.getKey(effect).getPath()));
        return effects;
    }

    /** Applies every beneficial vanilla effect: instant ones (Instant Health, Saturation) right away, the rest for {@code ticks}. */
    public static void apply(LivingEntity entity, int ticks) {
        if (!(entity.level() instanceof ServerLevel)) return;
        for (MobEffect effect : vanillaBeneficial()) {
            if (effect.isInstantenous()) {
                effect.applyInstantenousEffect(entity, entity, entity, 0, 1.0D);
            } else {
                entity.addEffect(new MobEffectInstance(effect, ticks, 0));
            }
        }
    }
}
