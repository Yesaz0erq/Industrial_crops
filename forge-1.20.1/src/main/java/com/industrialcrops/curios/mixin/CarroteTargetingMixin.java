package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Modify the ray cast's input directly instead of depending on the renderer's call site. */
@Mixin(ProjectileUtil.class)
public abstract class CarroteTargetingMixin {
    @ModifyVariable(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Predicate<Entity> carrote$targetProtectedEntities(Predicate<Entity> original,
            Entity viewer, Vec3 start, Vec3 end, AABB bounds, Predicate<Entity> supplied, double rangeSquared) {
        return CarroteCombat.targetingPredicate(viewer, Minecraft.getInstance().player, original);
    }
}
