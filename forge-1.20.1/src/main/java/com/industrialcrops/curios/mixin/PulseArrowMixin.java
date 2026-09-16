package com.industrialcrops.curios.mixin;

import com.industrialcrops.effect.CropCombatEvents;
import com.industrialcrops.registry.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public abstract class PulseArrowMixin {
    @Shadow private int knockback;

    @Redirect(method = {"onHitEntity", "getKnockback"}, at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;knockback:I", opcode = 180))
    private int crop$pulse(AbstractArrow arrow) {
        return knockback + (arrow.getOwner() instanceof Player player
                ? CropCombatEvents.level(player, ModEffects.PULSE) : 0);
    }
}
