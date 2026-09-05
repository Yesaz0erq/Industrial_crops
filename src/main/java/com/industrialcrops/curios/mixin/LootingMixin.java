package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class LootingMixin {
    @Inject(method = "getEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void carroteCurios$looting(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (enchantment.is(Enchantments.LOOTING) && CarroteCuriosEffects.has(entity, CarroteCuriosItems.GREED)) {
            cir.setReturnValue(Math.max(3, cir.getReturnValue()));
        }
    }
}
