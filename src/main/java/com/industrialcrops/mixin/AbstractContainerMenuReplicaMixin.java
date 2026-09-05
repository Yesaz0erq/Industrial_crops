package com.industrialcrops.mixin;

import com.industrialcrops.replication.UniversalReplicaData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes vanilla-style menu validation recognize a copied machine through the
 * permanent universal-device shell at the same position.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuReplicaMixin {
    @Inject(
            method = "stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void industrialcrops$validateReplicaBlock(
            ContainerLevelAccess access, Player player, Block expectedBlock,
            CallbackInfoReturnable<Boolean> cir) {
        boolean replicaMatches = access.evaluate((level, pos) -> {
            if (!level.getBlockState(pos).is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())
                    || player.distanceToSqr(
                            pos.getX() + 0.5D,
                            pos.getY() + 0.5D,
                            pos.getZ() + 0.5D) > 64.0D) {
                return false;
            }
            BlockEntity entity = level.getBlockEntity(pos);
            return entity != null && UniversalReplicaData.getMirroredState(level, entity)
                    .map(state -> state.is(expectedBlock))
                    .orElse(false);
        }, false);
        if (replicaMatches) {
            cir.setReturnValue(true);
        }
    }
}
