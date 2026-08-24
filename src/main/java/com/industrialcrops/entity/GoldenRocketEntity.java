package com.industrialcrops.entity;

import com.industrialcrops.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A coordinate-guided rocket that follows a deterministic ballistic parabola. */
public final class GoldenRocketEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(
            GoldenRocketEntity.class,
            EntityDataSerializers.BLOCK_POS
    );
    private static final double HORIZONTAL_SPEED = 0.68D;
    private static final int MIN_FLIGHT_TICKS = 24;
    private static final double MIN_ARC_HEIGHT = 12.0D;
    private static final double MAX_ARC_HEIGHT = 96.0D;
    private static final float BASE_EXPLOSION_POWER = 3.5F;

    private BlockPos launchOrigin = BlockPos.ZERO;
    private int flightTicks;
    private float explosionPower = BASE_EXPLOSION_POWER;
    private boolean detonateOnBlockHit;

    public GoldenRocketEntity(EntityType<GoldenRocketEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public GoldenRocketEntity(Level level, BlockPos launchOrigin, BlockPos target) {
        this(level, launchOrigin, target, 1);
    }

    public GoldenRocketEntity(Level level, BlockPos launchOrigin, BlockPos target, int powerMultiplier) {
        this(level, launchOrigin, target, powerMultiplier, false);
    }

    public GoldenRocketEntity(
            Level level,
            BlockPos launchOrigin,
            BlockPos target,
            int powerMultiplier,
            boolean detonateOnBlockHit
    ) {
        this(ModEntities.GOLDEN_ROCKET.get(), level);
        this.launchOrigin = launchOrigin;
        entityData.set(TARGET, target);
        explosionPower = BASE_EXPLOSION_POWER * Mth.clamp(powerMultiplier, 1, 16);
        this.detonateOnBlockHit = detonateOnBlockHit;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TARGET, BlockPos.ZERO);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            Vec3 motion = getDeltaMovement();
            if (motion.lengthSqr() > 1.0E-6D) {
                move(MoverType.SELF, motion);
                updateRotation(motion);
            }
            spawnTrailParticles(motion);
            return;
        }

        flightTicks++;
        Vec3 launchPoint = new Vec3(
                launchOrigin.getX() + 0.5D,
                launchOrigin.getY() + 1.2D,
                launchOrigin.getZ() + 0.5D
        );
        Vec3 targetCenter = Vec3.atCenterOf(getTarget());
        double deltaX = targetCenter.x - launchPoint.x;
        double deltaZ = targetCenter.z - launchPoint.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        int totalFlightTicks = Math.max(MIN_FLIGHT_TICKS, (int) Math.ceil(horizontalDistance / HORIZONTAL_SPEED));
        double progress = Math.min(1.0D, flightTicks / (double) totalFlightTicks);
        double arcHeight = Mth.clamp(
                MIN_ARC_HEIGHT + horizontalDistance * 0.25D,
                MIN_ARC_HEIGHT,
                MAX_ARC_HEIGHT
        );

        double nextX = Mth.lerp(progress, launchPoint.x, targetCenter.x);
        double nextZ = Mth.lerp(progress, launchPoint.z, targetCenter.z);
        double linearY = Mth.lerp(progress, launchPoint.y, targetCenter.y);
        double nextY = linearY + 4.0D * arcHeight * progress * (1.0D - progress);
        Vec3 nextPosition = new Vec3(nextX, nextY, nextZ);
        if (detonateOnBlockHit && flightTicks > totalFlightTicks) {
            nextPosition = new Vec3(targetCenter.x, getY() - HORIZONTAL_SPEED, targetCenter.z);
        }
        Vec3 motion = nextPosition.subtract(position());

        int nextChunkX = Mth.floor(nextX) >> 4;
        int nextChunkZ = Mth.floor(nextZ) >> 4;
        if (!level().hasChunk(nextChunkX, nextChunkZ)) {
            discard();
            return;
        }

        if (detonateOnBlockHit) {
            BlockHitResult blockHit = level().clip(new ClipContext(
                    position(),
                    nextPosition,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this
            ));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                Vec3 hitLocation = blockHit.getLocation();
                setDeltaMovement(motion);
                setPos(hitLocation.x, hitLocation.y, hitLocation.z);
                updateRotation(motion);
                detonate();
                return;
            }
            if (nextY < level().getMinBuildHeight()) {
                discard();
                return;
            }
        }

        setDeltaMovement(motion);
        setPos(nextPosition.x, nextPosition.y, nextPosition.z);
        hasImpulse = true;
        updateRotation(motion);
        if (progress >= 1.0D && !detonateOnBlockHit) {
            detonate();
        }
    }

    public BlockPos getTarget() {
        return entityData.get(TARGET);
    }

    private void updateRotation(Vec3 motion) {
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        setYRot((float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI)));
        setXRot((float) -(Mth.atan2(motion.y, horizontal) * (180.0D / Math.PI)));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    private void spawnTrailParticles(Vec3 motion) {
        if (motion.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 trailDirection = motion.normalize().scale(-0.22D);
        for (int index = 0; index < 3; index++) {
            double distance = index + 0.5D;
            double jitterX = (random.nextDouble() - 0.5D) * 0.045D;
            double jitterY = (random.nextDouble() - 0.5D) * 0.045D;
            double jitterZ = (random.nextDouble() - 0.5D) * 0.045D;
            double particleX = getX() + trailDirection.x * distance + jitterX;
            double particleY = getY() + trailDirection.y * distance - 0.08D + jitterY;
            double particleZ = getZ() + trailDirection.z * distance + jitterZ;
            level().addParticle(
                    ParticleTypes.FLAME,
                    particleX,
                    particleY,
                    particleZ,
                    trailDirection.x * 0.05D,
                    trailDirection.y * 0.05D,
                    trailDirection.z * 0.05D
            );
        }

        level().addParticle(
                tickCount % 3 == 0 ? ParticleTypes.CAMPFIRE_COSY_SMOKE : ParticleTypes.SMOKE,
                getX() + trailDirection.x * 1.6D,
                getY() + trailDirection.y * 1.6D - 0.08D,
                getZ() + trailDirection.z * 1.6D,
                trailDirection.x * 0.025D,
                trailDirection.y * 0.025D + 0.008D,
                trailDirection.z * 0.025D
        );
    }

    private void detonate() {
        level().explode(this, getX(), getY(), getZ(), explosionPower, ExplosionInteraction.TNT);
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        launchOrigin = new BlockPos(tag.getInt("LaunchX"), tag.getInt("LaunchY"), tag.getInt("LaunchZ"));
        entityData.set(TARGET, new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ")));
        flightTicks = tag.getInt("FlightTicks");
        explosionPower = tag.contains("ExplosionPower") ? tag.getFloat("ExplosionPower") : BASE_EXPLOSION_POWER;
        detonateOnBlockHit = tag.getBoolean("DetonateOnBlockHit");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LaunchX", launchOrigin.getX());
        tag.putInt("LaunchY", launchOrigin.getY());
        tag.putInt("LaunchZ", launchOrigin.getZ());
        tag.putInt("TargetX", getTarget().getX());
        tag.putInt("TargetY", getTarget().getY());
        tag.putInt("TargetZ", getTarget().getZ());
        tag.putInt("FlightTicks", flightTicks);
        tag.putFloat("ExplosionPower", explosionPower);
        tag.putBoolean("DetonateOnBlockHit", detonateOnBlockHit);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
