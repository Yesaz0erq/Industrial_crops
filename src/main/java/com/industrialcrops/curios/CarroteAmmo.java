package com.industrialcrops.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CarroteAmmo {
    private CarroteAmmo() {}

    public static boolean preserves(LivingEntity shooter, ItemStack weapon, ItemStack ammo) {
        return !ammo.isEmpty() && (weapon.is(Items.BOW) || weapon.is(Items.CROSSBOW))
                && (ammo.is(Items.ARROW) || ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW)
                || weapon.is(Items.CROSSBOW) && ammo.is(Items.FIREWORK_ROCKET))
                && CarroteCuriosEffects.has(shooter, CarroteCuriosItems.AMMO);
    }
}
