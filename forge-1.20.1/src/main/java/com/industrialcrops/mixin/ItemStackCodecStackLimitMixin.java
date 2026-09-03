package com.industrialcrops.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemStack.class)
public abstract class ItemStackCodecStackLimitMixin {
    @ModifyConstant(
            method = "lambda$static$3",
            constant = @Constant(intValue = 99),
            require = 1,
            remap = false
    )
    private static int industrialcrops$raiseSerializedStackLimit(int original) {
        return 999;
    }
}
