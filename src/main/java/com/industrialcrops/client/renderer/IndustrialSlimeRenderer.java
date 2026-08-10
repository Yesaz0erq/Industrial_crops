package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.IndustrialSlimeModel;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IndustrialSlimeRenderer<T extends Entity & GeoAnimatable> extends DynamicGeoEntityRenderer<T> {
    private final Map<ResourceLocation, RenderType> shellTypes = new ConcurrentHashMap<>();
    private final String shellRenderTypeName;
    private final boolean shellDepthTest;

    protected IndustrialSlimeRenderer(
            EntityRendererProvider.Context context,
            IndustrialSlimeModel<T> model,
            float shadowRadius,
            String shellRenderTypeName,
            boolean shellDepthTest
    ) {
        super(context, model);
        this.shadowRadius = shadowRadius;
        this.shellRenderTypeName = shellRenderTypeName;
        this.shellDepthTest = shellDepthTest;
    }

    @Override
    protected @Nullable RenderType getRenderTypeOverrideForBone(
            GeoBone bone,
            T animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        if ("body".equals(bone.getName())) {
            if (ContainedSlimeRenderContext.isActive()) {
                return RenderType.entityTranslucent(texture);
            }
            return shellTypes.computeIfAbsent(texture, this::shellRenderType);
        }
        RenderType extra = getRenderTypeOverrideForExtraBone(bone, texture);
        if (extra != null) {
            return extra;
        }
        return RenderType.entityCutoutNoCull(texture);
    }

    protected @Nullable RenderType getRenderTypeOverrideForExtraBone(GeoBone bone, ResourceLocation texture) {
        return null;
    }

    protected RenderType shellRenderType(ResourceLocation texture) {
        RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE);
        if (shellDepthTest) {
            builder.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST);
        }
        return RenderType.create(
                shellRenderTypeName,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                true,
                true,
                builder.createCompositeState(true)
        );
    }
}
