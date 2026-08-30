package com.industrialcrops.mechanic;

import com.industrialcrops.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class PlasmaTransmutation {
    private PlasmaTransmutation() {
    }

    public static boolean tryConvertDiamond(Level level, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (!(level instanceof ServerLevel serverLevel) || !stack.is(Items.DIAMOND)) {
            return false;
        }

        itemEntity.setItem(stack.transmuteCopy(ModItems.ENERGY_CRYSTAL.get(), stack.getCount()));
        itemEntity.setPickUpDelay(10);

        double x = itemEntity.getX();
        double y = itemEntity.getY() + 0.2D;
        double z = itemEntity.getZ();
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                24, 0.28D, 0.22D, 0.28D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z,
                8, 0.18D, 0.16D, 0.18D, 0.025D);
        serverLevel.sendParticles(ParticleTypes.FLASH, x, y, z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        return true;
    }
}
