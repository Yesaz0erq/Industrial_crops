package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, IndustrialCrops.MOD_ID);
    public record Family(DeferredHolder<Potion, Potion> base, DeferredHolder<Potion, Potion> extended, DeferredHolder<Potion, Potion> strong) {}
    public static final Family FLAME = family("flame", ModEffects.FLAME);
    public static final Family REFRACTION = family("refraction", ModEffects.REFRACTION);
    public static final Family PULSE = family("pulse", ModEffects.PULSE);

    private static Family family(String name, Holder<MobEffect> effect) {
        return new Family(register(name, name, effect, 3600, 0), register("long_" + name, name, effect, 9600, 0),
                register("strong_" + name, name, effect, 1800, 1));
    }
    private static DeferredHolder<Potion, Potion> register(String id, String name, Holder<MobEffect> effect, int ticks, int amplifier) {
        return POTIONS.register(id, () -> new Potion(name, new MobEffectInstance(effect, ticks, amplifier)));
    }
    public static void brewing(RegisterBrewingRecipesEvent event) {
        brew(event, FLAME, ModItems.EMBERCOIL.get());
        brew(event, REFRACTION, ModItems.PRISM_POD.get());
        brew(event, PULSE, ModItems.STARBLOOM.get());
    }
    private static void brew(RegisterBrewingRecipesEvent event, Family family, Item ingredient) {
        var builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, ingredient, family.base());
        builder.addMix(family.base(), Items.REDSTONE, family.extended());
        builder.addMix(family.base(), Items.GLOWSTONE_DUST, family.strong());
    }
}
