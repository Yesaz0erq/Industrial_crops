package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;

@Mixin(LootParams.Builder.class)
public abstract class FortuneLootMixin {
    @Shadow @Final private Map<LootContextParam<?>, Object> params;

    @Inject(method = "create", at = @At("HEAD"))
    private void carroteCurios$fortune(LootContextParamSet set, CallbackInfoReturnable<LootParams> cir) {
        if (params.containsKey(LootContextParams.BLOCK_STATE)
                && params.get(LootContextParams.THIS_ENTITY) instanceof LivingEntity player
                && params.get(LootContextParams.TOOL) instanceof ItemStack tool) {
            params.put(LootContextParams.TOOL, CarroteCuriosEffects.fortuneTool(tool, player));
        }
    }
}
