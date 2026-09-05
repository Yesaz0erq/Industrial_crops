package com.industrialcrops.client;

import com.industrialcrops.Carrote;
import com.industrialcrops.block.MimicBlock;
import com.industrialcrops.block.UniversalReplicationDeviceBlock;
import com.industrialcrops.client.renderer.WorldVolumeOutlineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Highlights the right-side target used by mimic and universal-copy placement. */
@EventBusSubscriber(modid = Carrote.MOD_ID, value = Dist.CLIENT)
public final class UniversalReplicationDevicePreviewRenderer {
    private UniversalReplicationDevicePreviewRenderer() {}

    @SubscribeEvent
    public static void renderSourcePreview(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        ItemStack held = minecraft.player.getMainHandItem();
        boolean mimicHeld = held.is(com.industrialcrops.registry.CarroteItems.MIMIC_BLOCK.get());
        boolean universalHeld = held.is(com.industrialcrops.registry.CarroteItems.UNIVERSAL_REPLICATION_DEVICE.get());
        if (!mimicHeld && !universalHeld) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }

        BlockPlaceContext context = new BlockPlaceContext(
                minecraft.player,
                InteractionHand.MAIN_HAND,
                held,
                blockHit
        );
        BlockPos devicePos = context.getClickedPos();
        Direction front = context.getHorizontalDirection().getOpposite();
        BlockPos sourcePos = mimicHeld
                ? MimicBlock.targetPosition(devicePos, front)
                : UniversalReplicationDeviceBlock.sourcePosition(devicePos, front);
        boolean valid = mimicHeld
                ? MimicBlock.canMimic(minecraft.level, sourcePos)
                : UniversalReplicationDeviceBlock.canCopy(minecraft.level, sourcePos);

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        WorldVolumeOutlineRenderer.renderBlock(
                poseStack,
                buffers,
                sourcePos,
                valid
                        ? WorldVolumeOutlineRenderer.Status.NORMAL
                        : WorldVolumeOutlineRenderer.Status.INVALID
        );
        poseStack.popPose();
        buffers.endBatch(WorldVolumeOutlineRenderer.renderType());
    }
}
