package com.industrialcrops.curios;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.fml.ModList;
import java.util.function.Supplier;

public final class CarroteCuriosEffects {
    public static final String STEEL_READY = "carrote_curios:steel_ready";
    public static final String HELMET_STEEL_READY = "carrote_curios:helmet_steel_ready";
    public static final String SPENT = "CarroteSubstituteSpent";

    private CarroteCuriosEffects() {}

    public static boolean curiosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    public static boolean has(LivingEntity entity, Supplier<? extends Item> item) {
        return count(entity, item) > 0;
    }

    public static ItemStack copiedCarrote(LivingEntity entity) {
        if (entity == null || !entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(CarroteCuriosItems.HELMET))
            return ItemStack.EMPTY;
        if (curiosLoaded()) {
            for (var stack : CuriosIntegration.stacks(entity, true)) {
                if (copyable(stack)) return effectiveCopy(stack);
            }
        }
        return effectiveCopy(entity.getOffhandItem());
    }

    private static boolean copyable(ItemStack stack) {
        return CarroteCuriosItems.isAccessory(stack) && !stack.is(CarroteCuriosItems.HELMET);
    }

    /** Only abilities with an additional numeric effect or independent charge can be copied.
     * Resolve slot priority first: an ineffective preferred target must not silently copy the offhand instead.
     */
    private static ItemStack effectiveCopy(ItemStack stack) {
        return stack.is(CarroteCuriosItems.STRENGTH) || stack.is(CarroteCuriosItems.AIRBORNE)
                || stack.is(CarroteCuriosItems.POWER) || stack.is(CarroteCuriosItems.GREED)
                || stack.is(CarroteCuriosItems.LUCK) || stack.is(CarroteCuriosItems.ARCANE)
                || stack.is(CarroteCuriosItems.STEEL) || stack.is(CarroteCuriosItems.SUBSTITUTE)
                ? stack : ItemStack.EMPTY;
    }

    public static int count(LivingEntity entity, Supplier<? extends Item> item) {
        if (entity == null) return 0;
        return (normallyEquipped(entity, item.get()) ? 1 : 0) + (copiedCarrote(entity).is(item.get()) ? 1 : 0);
    }

    private static boolean normallyEquipped(LivingEntity entity, Item accessory) {
        if (entity == null) return false;
        if (entity.getMainHandItem().is(accessory) || entity.getOffhandItem().is(accessory)) return true;
        return curiosLoaded() && CuriosIntegration.isEquipped(entity, accessory);
    }

    public static float finalDamage(LivingEntity attacker, float damage) {
        if (damage <= 0) return damage;
        double adjusted = damage + 4 * count(attacker, CarroteCuriosItems.STRENGTH)
                + (attacker.onGround() ? -2 : 6) * count(attacker, CarroteCuriosItems.AIRBORNE);
        return (float) Math.min(Float.MAX_VALUE, Math.max(0, adjusted)
                * Math.pow(1.5, count(attacker, CarroteCuriosItems.POWER)));
    }

    public static boolean blockAttack(Player player) {
        if (player.level().isClientSide || !has(player, CarroteCuriosItems.STEEL)) return false;
        long now = player.level().getGameTime();
        if (normallyEquipped(player, CarroteCuriosItems.STEEL.get()) && player.getPersistentData().getLong(STEEL_READY) <= now) {
            player.getPersistentData().putLong(STEEL_READY, now + 200L);
            player.getCooldowns().addCooldown(CarroteCuriosItems.STEEL.get(), 200);
            return true;
        }
        if (copiedCarrote(player).is(CarroteCuriosItems.STEEL) && player.getPersistentData().getLong(HELMET_STEEL_READY) <= now) {
            player.getPersistentData().putLong(HELMET_STEEL_READY, now + 200L);
            player.getCooldowns().addCooldown(CarroteCuriosItems.HELMET.get(), 200);
            return true;
        }
        return false;
    }

    public static java.util.List<ItemStack> activeStacks(LivingEntity entity) {
        var stacks = new java.util.ArrayList<ItemStack>();
        stacks.add(entity.getOffhandItem());
        stacks.add(entity.getMainHandItem());
        if (curiosLoaded()) stacks.addAll(CuriosIntegration.stacks(entity, false));
        return stacks;
    }

    public static boolean isSpent(ItemStack stack) {
        var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(SPENT);
    }

    private static void spent(ItemStack stack, boolean value) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                stack, tag -> { if (value) tag.putBoolean(SPENT, true); else tag.remove(SPENT); });
    }

    public static boolean saveFromDeath(Player player) {
        if (player.level().isClientSide) return false;
        // A same-kind accessory is one source; the helmet has its own independent charge.
        ItemStack original = activeStacks(player).stream().filter(s -> s.is(CarroteCuriosItems.SUBSTITUTE)).findFirst().orElse(ItemStack.EMPTY);
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        ItemStack charge = !original.isEmpty() && !isSpent(original) ? original
                : copiedCarrote(player).is(CarroteCuriosItems.SUBSTITUTE) && !isSpent(helmet) ? helmet : ItemStack.EMPTY;
        if (charge.isEmpty()) return false;
        spent(charge, true);
        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.fallDistance = 0;
        player.invulnerableTime = 20;
        updateSubstituteCooldowns(player);
        return true;
    }

    public static void resetCarriedCharges(Player player) {
        var carried = new java.util.ArrayList<>(activeStacks(player));
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) carried.add(player.getInventory().getItem(i));
        for (var stack : carried) {
            if ((stack.is(CarroteCuriosItems.SUBSTITUTE) || stack.is(CarroteCuriosItems.HELMET)) && isSpent(stack)) spent(stack, false);
        }
        player.getCooldowns().removeCooldown(CarroteCuriosItems.SUBSTITUTE.get());
        player.getCooldowns().removeCooldown(CarroteCuriosItems.HELMET.get());
    }

    /** Keep the vanilla pearl-style overlay full while the carried item's persistent charge is spent. */
    public static void updateSubstituteCooldowns(Player player) {
        var carried = new java.util.ArrayList<>(activeStacks(player));
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) carried.add(player.getInventory().getItem(i));
        for (var item : java.util.List.of(CarroteCuriosItems.SUBSTITUTE.get(), CarroteCuriosItems.HELMET.get())) {
            boolean spent = carried.stream().anyMatch(s -> s.is(item) && isSpent(s));
            if (spent) {
                if (player.getCooldowns().getCooldownPercent(item, 0) < 0.99F) player.getCooldowns().addCooldown(item, 1_000_000);
            } else if (item == CarroteCuriosItems.SUBSTITUTE.get()
                    || player.getPersistentData().getLong(HELMET_STEEL_READY) <= player.level().getGameTime()) {
                if (player.getCooldowns().isOnCooldown(item)) player.getCooldowns().removeCooldown(item);
            }
        }
    }

    /** Restore the visual timer after login/respawn without resetting it every tick. */
    public static void syncCooldowns(Player player) {
        if (player.level().isClientSide) return;
        long remaining = player.getPersistentData().getLong(STEEL_READY) - player.level().getGameTime();
        if (remaining > 0) {
            player.getCooldowns().addCooldown(CarroteCuriosItems.STEEL.get(), (int) Math.min(200, remaining));
        }
        long helmetRemaining = player.getPersistentData().getLong(HELMET_STEEL_READY) - player.level().getGameTime();
        if (helmetRemaining > 0) player.getCooldowns().addCooldown(CarroteCuriosItems.HELMET.get(), (int) Math.min(200, helmetRemaining));
        updateSubstituteCooldowns(player);
    }

    /** The actual held tool is never enchanted or mutated. Silk Touch retains vanilla precedence. */
    public static ItemStack fortuneTool(ItemStack tool, LivingEntity player) {
        if (!has(player, CarroteCuriosItems.GREED)) return tool;
        ItemStack copy = tool.copy();
        // Empty hand still receives Fortune through a temporary loot-only tool.
        if (copy.isEmpty()) copy = new ItemStack(net.minecraft.world.item.Items.STICK);
        var fortune = player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FORTUNE);
        int level = 3 * count(player, CarroteCuriosItems.GREED);
        if (copy.getEnchantmentLevel(fortune) < level) copy.enchant(fortune, level);
        return copy;
    }

    public static void boostEnchantments(ItemStack stack, int levels) {
        ItemEnchantments original = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (original.isEmpty()) return;
        ItemEnchantments.Mutable boosted = new ItemEnchantments.Mutable(original);
        original.entrySet().forEach(entry -> boosted.set(entry.getKey(), Math.min(255, entry.getIntValue() + levels)));
        EnchantmentHelper.setEnchantments(stack, boosted.toImmutable());
    }
}
