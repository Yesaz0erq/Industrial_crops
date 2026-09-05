package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.IndustrialSlimeModel;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.DynamicGeoEntityRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IndustrialSlimeRenderer<T extends Entity & GeoAnimatable> extends DynamicGeoEntityRenderer<T> {
    private final Map<ResourceLocation, RenderType> shellTypes = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, RenderType> containedShellTypes = new ConcurrentHashMap<>();
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
                // Do not let the transparent shell write depth before the face and internal core.
                return containedShellTypes.computeIfAbsent(texture, key -> shellRenderType(key, true));
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
        return shellRenderType(texture, false);
    }

    private RenderType shellRenderType(ResourceLocation texture, boolean contained) {
        return ShellRenderState.create(shellRenderTypeName, texture, contained, shellDepthTest);
    }

    private static final class ShellRenderState extends RenderStateShard {
        private ShellRenderState() { super("industrialcrops_shell", () -> {}, () -> {}); }

        private static RenderType create(String shellRenderTypeName, ResourceLocation texture, boolean contained, boolean shellDepthTest) {
            RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE);
            if (contained || shellDepthTest) {
                builder.setDepthTestState(LEQUAL_DEPTH_TEST);
            }
            return RenderType.create(
                    shellRenderTypeName + (contained ? "_contained" : ""),
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    1536,
                    true,
                    true,
                    builder.createCompositeState(true)
            );
        }
}
}
