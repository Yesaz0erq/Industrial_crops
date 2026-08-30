package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.effect.ElectrocutionMobEffect;
import com.industrialcrops.effect.GlitchMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, IndustrialCrops.MOD_ID);
    public static final RegistryObject<MobEffect> GLITCH =
            EFFECTS.register("glitch", GlitchMobEffect::new);
    public static final RegistryObject<MobEffect> ELECTROCUTION =
            EFFECTS.register("electrocution", ElectrocutionMobEffect::new);

    private ModEffects() { }
}
