package com.industrialcrops.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Container.class)
public interface ContainerStackLimitMixin {
    /**
     * @author OFFSET Inc.
     * @reason Match the industrial stack capacity on the default container implementation.
     */
    @Overwrite
    default int getMaxStackSize() {
        return 999;
    }
}
