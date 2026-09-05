package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Shadow private float exhaustionLevel;

    @Inject(method = "tick", at = @At("HEAD"))
    private void carroteCurios$noHunger(Player player, CallbackInfo ci) {
        if (CarroteCuriosEffects.has(player, CarroteCuriosItems.TASTY)) exhaustionLevel = 0F;
    }
}
