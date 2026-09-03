package com.industrialcrops.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Container.class)
public interface ContainerStackLimitMixin {
    @ModifyConstant(method = "getMaxStackSize", constant = @Constant(intValue = 64), require = 1)
    private int industrialcrops$raiseContainerStackLimit(int original) {
        return 999;
    }
}
