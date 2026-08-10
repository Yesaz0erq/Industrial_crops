package com.industrialcrops.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class IndustrialGearSlime extends FriendlyIndustrialSlime implements GeoEntity {
    private final DustParticleOptions jumpParticle;
    private final RawAnimation idleAnimation;
    private final RawAnimation jumpAnimation;
    private final RawAnimation landAnimation;

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private boolean wasOnGround;
    private int landingAnimationTicks;

    protected IndustrialGearSlime(
            EntityType<? extends Slime> entityType,
            Level level,
            DustParticleOptions jumpParticle,
            String animationPrefix
    ) {
        super(entityType, level);
        this.jumpParticle = jumpParticle;
        this.idleAnimation = RawAnimation.begin().thenLoop("animation." + animationPrefix + ".idle");
        this.jumpAnimation = RawAnimation.begin().thenPlayAndHold("animation." + animationPrefix + ".jump");
        this.landAnimation = RawAnimation.begin().thenPlay("animation." + animationPrefix + ".land");
        setSize(1, true);
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(1, false);
        AttributeInstance maxHealth = getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(10.0D);
        }
        if (resetHealth) {
            setHealth(10.0F);
        }
    }

    @Override
    protected ParticleOptions getParticleType() {
        return jumpParticle;
    }

    @Override
    public void jumpFromGround() {
        if (!consumeJumpPermission()) {
            return;
        }
        super.jumpFromGround();
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    jumpParticle,
                    getX(),
                    getY() + 0.08D,
                    getZ(),
                    12,
                    0.38D,
                    0.04D,
                    0.38D,
                    0.035D
            );
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            boolean onGround = onGround();
            if (onGround && !wasOnGround) {
                landingAnimationTicks = 12;
            } else if (landingAnimationTicks > 0) {
                landingAnimationTicks--;
            }
            wasOnGround = onGround;
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnData
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        setSize(1, true);
        return result;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 2, this::selectAnimation));
    }

    private PlayState selectAnimation(AnimationState<IndustrialGearSlime> state) {
        if (!onGround()) {
            return state.setAndContinue(jumpAnimation);
        }
        if (landingAnimationTicks > 0) {
            return state.setAndContinue(landAnimation);
        }
        return state.setAndContinue(idleAnimation);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
