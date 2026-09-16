package com.industrialcrops.curios.mixin;

import com.industrialcrops.effect.CropCombatEvents;
import com.industrialcrops.registry.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class PulseKnockbackMixin {
    @Inject(method = "modifyKnockback", at = @At("RETURN"), cancellable = true)
    private static void crop$pulse(ServerLevel level, ItemStack weapon, Entity victim, DamageSource source,
                                   float base, CallbackInfoReturnable<Float> cir) {
        if (source.getEntity() instanceof Player attacker) {
            cir.setReturnValue(cir.getReturnValue() + CropCombatEvents.level(attacker, ModEffects.PULSE));
        }
    }
}
