package com.industrialcrops.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A copied machine may have a dynamic block-entity renderer. Suppress it at a
 * replica position so the visible block remains the original Carrote-steel
 * universal-device model.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void industrialcrops$keepReplicaShellTexture(
            E entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, CallbackInfo ci) {
        Level level = entity.getLevel();
        if (level != null && level.getBlockState(entity.getBlockPos())
                .is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())) {
            ci.cancel();
        }
    }
}
