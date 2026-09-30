package com.industrialcrops.curios.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface CarroteCombatAccess {
    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> carrote$healthKey() { throw new AssertionError(); }
    @Accessor("lastHurt") float carrote$getLastHurt();
    @Accessor("lastHurt") void carrote$setLastHurt(float value);
    @Accessor("lastHurtByPlayer") void carrote$setLastHurtByPlayer(Player player);
    @Accessor("lastHurtByPlayerTime") void carrote$setLastHurtByPlayerTime(int ticks);
    @Accessor("lastDamageSource") void carrote$setLastDamageSource(DamageSource source);
    @Accessor("lastDamageStamp") void carrote$setLastDamageStamp(long time);
    @Invoker("hurtArmor") void carrote$hurtArmor(DamageSource source, float amount);
    @Invoker("checkTotemDeathProtection") boolean carrote$tryTotem(DamageSource source);
    @Invoker("playHurtSound") void carrote$playHurtSound(DamageSource source);
}
