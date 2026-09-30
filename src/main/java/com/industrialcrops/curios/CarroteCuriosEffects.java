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

    private record AccessoryRef(ItemStack stack, java.util.function.Consumer<ItemStack> save) {
        private void commit() { save.accept(stack); }
    }

    private static AccessoryRef direct(ItemStack stack) { return new AccessoryRef(stack, ignored -> {}); }

    private static java.util.List<ItemStack> activeBags(LivingEntity entity) {
        var bags = new java.util.ArrayList<ItemStack>();
        if (entity == null) return bags;
        if (entity.getOffhandItem().is(CarroteCuriosItems.ULTIMATE.get())) bags.add(entity.getOffhandItem());
        if (curiosLoaded()) for (var stack : CuriosIntegration.stacks(entity, false)) {
            if (stack.is(CarroteCuriosItems.ULTIMATE.get()) && !bags.contains(stack)) bags.add(stack);
        }
        return bags;
    }

    private static void addContents(java.util.List<AccessoryRef> result, ItemStack bag, LivingEntity entity) {
        var contents = UltimateCarroteStorage.contents(bag, entity.registryAccess());
        for (int i = 0; i < contents.size(); i++) {
            ItemStack stack = contents.get(i);
            if (!UltimateCarroteStorage.accepts(stack)) continue;
            int index = i;
            result.add(new AccessoryRef(stack, changed -> UltimateCarroteStorage.replace(bag, index, changed, entity.registryAccess())));
        }
    }

    private static java.util.List<AccessoryRef> ultimateSources(LivingEntity entity) {
        var result = new java.util.ArrayList<AccessoryRef>();
        var bags = activeBags(entity);
        if (bags.isEmpty()) return result;
        for (var bag : bags) addContents(result, bag, entity);
        if (entity instanceof Player player) for (var stack : player.getInventory().items) {
            if (UltimateCarroteStorage.accepts(stack)) result.add(direct(stack));
        }
        return result;
    }

    private static AccessoryRef helmetRef(LivingEntity entity) {
        if (entity == null) return direct(ItemStack.EMPTY);
        var worn = entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        if (worn.is(CarroteCuriosItems.HELMET.get())) return direct(worn);
        return ultimateSources(entity).stream().filter(ref -> ref.stack().is(CarroteCuriosItems.HELMET.get()))
                .findFirst().orElse(direct(ItemStack.EMPTY));
    }

    public static ItemStack activeHelmet(LivingEntity entity) { return helmetRef(entity).stack(); }

    public static ItemStack copiedCarrote(LivingEntity entity) {
        if (activeHelmet(entity).isEmpty()) return ItemStack.EMPTY;
        if (curiosLoaded()) {
            for (var stack : CuriosIntegration.stacks(entity, true)) {
                if (copyable(stack)) return effectiveCopy(stack);
            }
        }
        var offhand = effectiveCopy(entity.getOffhandItem());
        if (!offhand.isEmpty() || !ultimateActive(entity)) return offhand;
        return ultimateSources(entity).stream().map(ref -> effectiveCopy(ref.stack()))
                .filter(s -> !s.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
    }

    private static boolean copyable(ItemStack stack) {
        return UltimateCarroteStorage.accepts(stack) && !stack.is(CarroteCuriosItems.HELMET.get());
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

    public static boolean ultimateActive(LivingEntity entity) { return !activeBags(entity).isEmpty(); }

    private static boolean normallyEquipped(LivingEntity entity, Item accessory) {
        if (accessory == CarroteCuriosItems.ULTIMATE.get()) return ultimateActive(entity);
        return directlyEquipped(entity, accessory) || ultimateSources(entity).stream().anyMatch(ref -> ref.stack().is(accessory));
    }

    private static boolean directlyEquipped(LivingEntity entity, Item accessory) {
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

    private static java.util.List<AccessoryRef> activeRefs(LivingEntity entity) {
        var refs = new java.util.ArrayList<AccessoryRef>();
        if (entity == null) return refs;
        refs.add(direct(entity.getOffhandItem()));
        refs.add(direct(entity.getMainHandItem()));
        if (curiosLoaded()) for (var stack : CuriosIntegration.stacks(entity, false)) refs.add(direct(stack));
        refs.addAll(ultimateSources(entity));
        return refs;
    }

    public static java.util.List<ItemStack> activeStacks(LivingEntity entity) {
        return activeRefs(entity).stream().map(AccessoryRef::stack).toList();
    }

    private static java.util.List<AccessoryRef> carriedRefs(Player player) {
        var refs = new java.util.ArrayList<>(activeRefs(player));
        var bags = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            refs.add(direct(stack));
            if (stack.is(CarroteCuriosItems.ULTIMATE.get())) bags.add(stack);
        }
        if (curiosLoaded()) for (var stack : CuriosIntegration.stacks(player, false)) {
            if (stack.is(CarroteCuriosItems.ULTIMATE.get()) && !bags.contains(stack)) bags.add(stack);
        }
        for (var bag : bags) addContents(refs, bag, player);
        return refs;
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
        AccessoryRef original = activeRefs(player).stream().filter(ref -> ref.stack().is(CarroteCuriosItems.SUBSTITUTE.get()))
                .findFirst().orElse(direct(ItemStack.EMPTY));
        AccessoryRef helmet = helmetRef(player);
        AccessoryRef charge = !original.stack().isEmpty() && !isSpent(original.stack()) ? original
                : copiedCarrote(player).is(CarroteCuriosItems.SUBSTITUTE.get()) && !isSpent(helmet.stack()) ? helmet : direct(ItemStack.EMPTY);
        if (charge.stack().isEmpty()) return false;
        spent(charge.stack(), true);
        charge.commit();
        player.getInventory().setChanged();
        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.fallDistance = 0;
        player.invulnerableTime = 20;
        updateSubstituteCooldowns(player);
        return true;
    }

    public static void resetCarriedCharges(Player player) {
        for (var ref : carriedRefs(player)) {
            ItemStack stack = ref.stack();
            if ((stack.is(CarroteCuriosItems.SUBSTITUTE.get()) || stack.is(CarroteCuriosItems.HELMET.get())) && isSpent(stack)) {
                spent(stack, false);
                ref.commit();
            }
        }
        player.getInventory().setChanged();
        player.getCooldowns().removeCooldown(CarroteCuriosItems.SUBSTITUTE.get());
        player.getCooldowns().removeCooldown(CarroteCuriosItems.HELMET.get());
    }

    /** Keep the vanilla pearl-style overlay full while the carried item's persistent charge is spent. */
    public static void updateSubstituteCooldowns(Player player) {
        var carried = carriedRefs(player).stream().map(AccessoryRef::stack).toList();
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
