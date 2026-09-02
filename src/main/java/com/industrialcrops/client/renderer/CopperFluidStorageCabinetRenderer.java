package com.industrialcrops.client.renderer;

import com.industrialcrops.block.entity.CopperFluidStorageCabinetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CopperFluidStorageCabinetRenderer
        implements BlockEntityRenderer<CopperFluidStorageCabinetBlockEntity> {
    private static final float MIN = 3.2F / 16.0F;
    private static final float MAX = 12.8F / 16.0F;
    private static final float BOTTOM = 3.2F / 16.0F;
    private static final float TOP = 12.8F / 16.0F;

    public CopperFluidStorageCabinetRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CopperFluidStorageCabinetBlockEntity cabinet, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        FluidStack fluid = cabinet.getTank().getFluid();
        if (fluid.isEmpty()) {
            return;
        }

        float fill = Math.min(1.0F, fluid.getAmount() / (float) cabinet.getTank().getCapacity());
        float surface = BOTTOM + (TOP - BOTTOM) * fill;
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation texture = extensions.getStillTexture(fluid);
        if (texture == null) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(texture);
        int tint = extensions.getTintColor(fluid);
        int alpha = tint >>> 24;
        if (alpha == 0) alpha = 255;
        int red = tint >> 16 & 255;
        int green = tint >> 8 & 255;
        int blue = tint & 255;

        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        quad(vertices, pose, MIN, surface, MIN, MIN, surface, MAX, MAX, surface, MAX, MAX, surface, MIN,
                u0, v0, u1, v1, red, green, blue, alpha, packedLight, packedOverlay, 0, 1, 0);
        quad(vertices, pose, MAX, BOTTOM, MIN, MIN, BOTTOM, MIN, MIN, surface, MIN, MAX, surface, MIN,
                u0, v1, u1, v0, red, green, blue, alpha, packedLight, packedOverlay, 0, 0, -1);
        quad(vertices, pose, MIN, BOTTOM, MAX, MAX, BOTTOM, MAX, MAX, surface, MAX, MIN, surface, MAX,
                u0, v1, u1, v0, red, green, blue, alpha, packedLight, packedOverlay, 0, 0, 1);
        quad(vertices, pose, MIN, BOTTOM, MIN, MIN, BOTTOM, MAX, MIN, surface, MAX, MIN, surface, MIN,
                u0, v1, u1, v0, red, green, blue, alpha, packedLight, packedOverlay, -1, 0, 0);
        quad(vertices, pose, MAX, BOTTOM, MAX, MAX, BOTTOM, MIN, MAX, surface, MIN, MAX, surface, MAX,
                u0, v1, u1, v0, red, green, blue, alpha, packedLight, packedOverlay, 1, 0, 0);
        quad(vertices, pose, MIN, BOTTOM, MAX, MIN, BOTTOM, MIN, MAX, BOTTOM, MIN, MAX, BOTTOM, MAX,
                u0, v0, u1, v1, red, green, blue, alpha, packedLight, packedOverlay, 0, -1, 0);
    }

    private static void quad(VertexConsumer vertices, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float u0, float v0, float u1, float v1,
                             int red, int green, int blue, int alpha, int light, int overlay,
                             float normalX, float normalY, float normalZ) {
        vertex(vertices, pose, x1, y1, z1, u0, v1, red, green, blue, alpha, light, overlay, normalX, normalY, normalZ);
        vertex(vertices, pose, x2, y2, z2, u1, v1, red, green, blue, alpha, light, overlay, normalX, normalY, normalZ);
        vertex(vertices, pose, x3, y3, z3, u1, v0, red, green, blue, alpha, light, overlay, normalX, normalY, normalZ);
        vertex(vertices, pose, x4, y4, z4, u0, v0, red, green, blue, alpha, light, overlay, normalX, normalY, normalZ);
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, int red, int green, int blue, int alpha,
                               int light, int overlay, float normalX, float normalY, float normalZ) {
        vertices.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, normalX, normalY, normalZ);
    }
}
