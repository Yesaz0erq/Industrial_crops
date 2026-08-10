package com.industrialcrops.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public final class GrayGearSlime extends IndustrialGearSlime {
    private static final DustParticleOptions JUMP_PARTICLE =
            new DustParticleOptions(new Vector3f(0.42F, 0.46F, 0.50F), 1.15F);

    public GrayGearSlime(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level, JUMP_PARTICLE, "gray_gear_slime");
    }
}
