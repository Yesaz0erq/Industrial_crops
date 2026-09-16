package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.effect.ElectrocutionMobEffect;
import com.industrialcrops.effect.GlitchMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, IndustrialCrops.MOD_ID);
    public static final DeferredHolder<MobEffect, MobEffect> GLITCH =
            EFFECTS.register("glitch", GlitchMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> ELECTROCUTION =
            EFFECTS.register("electrocution", ElectrocutionMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> FLAME = EFFECTS.register("flame", () -> new com.industrialcrops.effect.CropCombatEffect(0xF08027));
    public static final DeferredHolder<MobEffect, MobEffect> REFRACTION = EFFECTS.register("refraction", () -> new com.industrialcrops.effect.CropCombatEffect(0x608EEB));
    public static final DeferredHolder<MobEffect, MobEffect> PULSE = EFFECTS.register("pulse", () -> new com.industrialcrops.effect.CropCombatEffect(0x3ADACD));

    private ModEffects() { }
}
