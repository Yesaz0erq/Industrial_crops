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
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;


public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, IndustrialCrops.MOD_ID);
    public record Family(RegistryObject<Potion> base, RegistryObject<Potion> extended, RegistryObject<Potion> strong) {}
    public static final Family FLAME = family("flame", ModEffects.FLAME);
    public static final Family REFRACTION = family("refraction", ModEffects.REFRACTION);
    public static final Family PULSE = family("pulse", ModEffects.PULSE);

    private static Family family(String name, java.util.function.Supplier<MobEffect> effect) {
        return new Family(register(name, name, effect, 3600, 0), register("long_" + name, name, effect, 9600, 0),
                register("strong_" + name, name, effect, 1800, 1));
    }
    private static RegistryObject<Potion> register(String id, String name, java.util.function.Supplier<MobEffect> effect, int ticks, int amplifier) {
        return POTIONS.register(id, () -> new Potion(name, new MobEffectInstance(effect.get(), ticks, amplifier)));
    }
    public static void brewing() {
        brew(FLAME, ModItems.EMBERCOIL.get());
        brew(REFRACTION, ModItems.PRISM_POD.get());
        brew(PULSE, ModItems.STARBLOOM.get());
    }
    private static void brew(Family family, Item ingredient) {
        mix(Potions.AWKWARD, ingredient, family.base().get());
        mix(family.base().get(), Items.REDSTONE, family.extended().get());
        mix(family.base().get(), Items.GLOWSTONE_DUST, family.strong().get());
    }
    private static void mix(Potion input, Item ingredient, Potion output) {
        net.minecraftforge.common.brewing.BrewingRecipeRegistry.addRecipe(new net.minecraftforge.common.brewing.IBrewingRecipe() {
            public boolean isInput(net.minecraft.world.item.ItemStack stack) {
                return (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION))
                        && net.minecraft.world.item.alchemy.PotionUtils.getPotion(stack) == input;
            }
            public boolean isIngredient(net.minecraft.world.item.ItemStack stack) { return stack.is(ingredient); }
            public net.minecraft.world.item.ItemStack getOutput(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemStack reagent) {
                return isInput(stack) && isIngredient(reagent) ? net.minecraft.world.item.alchemy.PotionUtils.setPotion(new net.minecraft.world.item.ItemStack(stack.getItem()), output) : net.minecraft.world.item.ItemStack.EMPTY;
            }
        });
    }
}
