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

    public static List<Holder<MobEffect>> vanillaBeneficial() {
        List<Holder.Reference<MobEffect>> refs = new ArrayList<>();
        BuiltInRegistries.MOB_EFFECT.holders().forEach(holder -> {
            if (holder.key().location().getNamespace().equals("minecraft")
                    && holder.value().getCategory() == MobEffectCategory.BENEFICIAL) {
                refs.add(holder);
            }
        });
        refs.sort((a, b) -> a.key().location().getPath().compareTo(b.key().location().getPath()));
        return new ArrayList<>(refs);
    }

    /** Applies every beneficial vanilla effect: instant ones (Instant Health, Saturation) right away, the rest for {@code ticks}. */
    public static void apply(LivingEntity entity, int ticks) {
        if (!(entity.level() instanceof ServerLevel)) return;
        for (Holder<MobEffect> effect : vanillaBeneficial()) {
            if (effect.value().isInstantenous()) {
                effect.value().applyInstantenousEffect(entity, entity, entity, 0, 1.0D);
            } else {
                entity.addEffect(new MobEffectInstance(effect, ticks, 0));
            }
        }
    }
}
