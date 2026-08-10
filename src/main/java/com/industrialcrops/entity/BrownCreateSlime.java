package com.industrialcrops.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public final class BrownCreateSlime extends IndustrialGearSlime {
    private static final DustParticleOptions JUMP_PARTICLE =
            new DustParticleOptions(new Vector3f(0.48F, 0.25F, 0.10F), 1.15F);

    public BrownCreateSlime(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level, JUMP_PARTICLE, "copper_twin_gear_slime");
    }
}
