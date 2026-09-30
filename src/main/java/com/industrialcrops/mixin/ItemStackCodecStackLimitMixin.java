package com.industrialcrops.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemStack.class)
public abstract class ItemStackCodecStackLimitMixin {
    @ModifyArg(
            method = "lambda$static$3",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ExtraCodecs;intRange(II)Lcom/mojang/serialization/Codec;"),
            index = 1,
            require = 1,
            remap = false
    )
    private static int industrialcrops$raiseSerializedStackLimit(int original) {
        return Math.max(original, 999);
    }
}
