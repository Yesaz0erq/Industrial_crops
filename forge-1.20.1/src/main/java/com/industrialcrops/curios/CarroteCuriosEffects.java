package com.industrialcrops.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.fml.ModList;
import java.util.function.Supplier;

public final class CarroteCuriosEffects {
    public static final String STEEL_READY = "carrote_curios:steel_ready";

    private CarroteCuriosEffects() {}

    public static boolean curiosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    public static boolean has(LivingEntity entity, Supplier<? extends Item> item) {
        if (entity == null) return false;
        Item accessory = item.get();
        if (entity.getMainHandItem().is(accessory) || entity.getOffhandItem().is(accessory)) return true;
        return curiosLoaded() && CuriosIntegration.isEquipped(entity, accessory);
    }

    public static float damageMultiplier(LivingEntity attacker) {
        float multiplier = has(attacker, CarroteCuriosItems.STRENGTH) ? 1.5F : 1F;
        if (has(attacker, CarroteCuriosItems.AIRBORNE)) multiplier *= attacker.onGround() ? 0.5F : 2F;
        return multiplier;
    }

    public static boolean blockAttack(Player player) {
        if (player.level().isClientSide || !has(player, CarroteCuriosItems.STEEL)) return false;
        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong(STEEL_READY) > now) return false;
        player.getPersistentData().putLong(STEEL_READY, now + 200L);
        // ServerItemCooldowns sends the same vanilla packet used by ender pearls.
        player.getCooldowns().addCooldown(CarroteCuriosItems.STEEL.get(), 200);
        return true;
    }

    /** Restore the visual timer after login/respawn without resetting it every tick. */
    public static void syncCooldowns(Player player) {
        if (player.level().isClientSide) return;
        long remaining = player.getPersistentData().getLong(STEEL_READY) - player.level().getGameTime();
        if (remaining > 0) {
            player.getCooldowns().addCooldown(CarroteCuriosItems.STEEL.get(), (int) Math.min(200, remaining));
        }
    }

    /** The actual held tool is never enchanted or mutated. Silk Touch retains vanilla precedence. */
    public static ItemStack fortuneTool(ItemStack tool, LivingEntity player) {
        if (!has(player, CarroteCuriosItems.GREED)) return tool;
        ItemStack copy = tool.copy();
        // Empty hand still receives Fortune through a temporary loot-only tool.
        if (copy.isEmpty()) copy = new ItemStack(net.minecraft.world.item.Items.STICK);
        var fortune = Enchantments.BLOCK_FORTUNE;
        if (copy.getEnchantmentLevel(fortune) < 3) copy.enchant(fortune, 3);
        return copy;
    }

    public static void boostEnchantments(ItemStack stack) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) return;
        enchantments.replaceAll((enchantment, level) -> Math.min(255, level + 1));
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }
}
