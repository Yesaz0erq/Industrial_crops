package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.IndustrialSlimeModel;
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
        return RenderType.entityTranslucent(texture);
    }
}
