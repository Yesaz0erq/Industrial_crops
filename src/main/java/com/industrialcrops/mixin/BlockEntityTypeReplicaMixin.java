package com.industrialcrops.mixin;

import com.industrialcrops.registry.CarroteBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Allows a replica shell at runtime without changing other mods' registered block sets. */
@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeReplicaMixin {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void industrialcrops$allowReplicaShell(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // Third-party registration code constructs entities from getValidBlocks().
        // That collection must contain only their real, constructor-compatible blocks.
        if (CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.isBound()
                && state.is(CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())) {
            cir.setReturnValue(true);
        }
    }
}
