package com.industrialcrops.curios.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface CarroteServerPlayerAccess {
    @Accessor("spawnInvulnerableTime") int carrote$spawnProtection();
    @Accessor("spawnInvulnerableTime") void carrote$spawnProtection(int ticks);
}
