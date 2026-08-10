package com.industrialcrops.mixin;

import com.industrialcrops.replication.UniversalReplicaData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Presents the copied machine's state only while its own code is executing.
 * The actual world state always remains the universal replication device.
 */
@Mixin(Level.class)
public abstract class LevelUniversalReplicaMixin {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void industrialcrops$getReplicaState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        UniversalReplicaData.virtualState((Level) (Object) this, pos).ifPresent(cir::setReturnValue);
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    private void industrialcrops$captureReplicaState(BlockPos pos, BlockState state, int flags,
            int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (UniversalReplicaData.captureStateChange((Level) (Object) this, pos, state)) {
            cir.setReturnValue(true);
        }
    }
}
