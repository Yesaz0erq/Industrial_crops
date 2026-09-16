package com.industrialcrops.client.renderer;

import com.industrialcrops.block.entity.CrystalLogisticsBlockEntity;
import com.industrialcrops.block.CrystalLogisticsBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;

/** The contained gateway is visual only; logistics retain their existing channel rules. */
public final class CrystalLogisticsRenderer implements BlockEntityRenderer<CrystalLogisticsBlockEntity> {
    // Detached visual only: never placed or ticked, so it cannot teleport or emit beams.
    private static final TheEndGatewayBlockEntity VISUAL_GATEWAY = new TheEndGatewayBlockEntity(BlockPos.ZERO, Blocks.END_GATEWAY.defaultBlockState()) {
        @Override public boolean isSpawning() { return false; }
        @Override public boolean isCoolingDown() { return false; }
        @Override public boolean shouldRenderFace(Direction side) { return true; }
    };
    private final TheEndGatewayRenderer gatewayRenderer;
    public CrystalLogisticsRenderer(BlockEntityRendererProvider.Context context) {
        // The vanilla gateway renderer does not need context resources.
        gatewayRenderer = new TheEndGatewayRenderer(context);
    }
    public void renderGateway(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(.25, .25, .25);
        pose.scale(.5f, .5f, .5f);
        gatewayRenderer.render(VISUAL_GATEWAY, 0, pose, buffers, light, overlay);
        pose.popPose();
    }
    @Override public void render(CrystalLogisticsBlockEntity machine,float partial,PoseStack pose,
                                 MultiBufferSource buffers,int light,int overlay) {
        var state = machine.getBlockState();
        pose.pushPose();
        pose.translate(.5, .5, .5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - state.getValue(CrystalLogisticsBlock.FACING).toYRot()));
        float tilt = switch (state.getValue(CrystalLogisticsBlock.FACE)) {
            case FLOOR -> 0;
            case WALL -> -90;
            case CEILING -> -180;
        };
        pose.mulPose(Axis.XP.rotationDegrees(tilt));
        pose.translate(-.5, -.5, -.5);
        renderGateway(pose, buffers, light, overlay);
        pose.popPose();
    }
}
