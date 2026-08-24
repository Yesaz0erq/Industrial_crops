package com.industrialcrops.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class AnimalFeedBagItem extends Item {
    public enum Mode {
        BASIC,
        HEALING,
        GROWTH,
        FAST_BREEDING,
        RESISTANCE
    }

    private final Mode mode;

    public AnimalFeedBagItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
            LivingEntity target, InteractionHand hand) {
        boolean applied = switch (mode) {
            case BASIC -> setLove(target, player, false);
            case FAST_BREEDING -> setLove(target, player, true);
            case HEALING -> heal(target);
            case GROWTH -> grow(target);
            case RESISTANCE -> resistance(target);
        };
        if (!applied) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide()) {
            BagItemHelper.consumeAndReturnEmptyBag(player, stack);
            player.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    private static boolean setLove(LivingEntity target, Player player, boolean resetCooldown) {
        if (!(target instanceof Animal animal) || animal.isBaby()) {
            return false;
        }
        if (resetCooldown) {
            animal.setAge(0);
            animal.setInLoveTime(0);
        }
        animal.setInLove(player);
        return true;
    }

    private static boolean heal(LivingEntity target) {
        if (target.getHealth() >= target.getMaxHealth()) {
            return false;
        }
        target.heal(target.getMaxHealth());
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
        return true;
    }

    private static boolean grow(LivingEntity target) {
        if (!(target instanceof AgeableMob ageable) || !ageable.isBaby()) {
            return false;
        }
        ageable.ageUp(-ageable.getAge());
        return true;
    }

    private static boolean resistance(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                MobEffectInstance.INFINITE_DURATION, 2));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.industrialcrops.feed_bag." + mode.name().toLowerCase()));
    }
}
