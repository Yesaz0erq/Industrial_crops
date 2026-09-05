package com.industrialcrops.curios;

import com.industrialcrops.CarroteCurios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import java.util.List;

public final class CarroteCuriosItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.ITEMS, CarroteCurios.MOD_ID);
    public static final RegistryObject<Item> STRENGTH = register("strength_carrote");
    public static final RegistryObject<Item> AIRBORNE = register("airborne_carrote");
    public static final RegistryObject<Item> GREED = register("greed_carrote");
    public static final RegistryObject<Item> LUCK = register("luck_carrote");
    public static final RegistryObject<Item> ENCHANTING = register("enchanting_carrote");
    public static final RegistryObject<Item> SMELTING = register("smelting_carrote");
    public static final RegistryObject<Item> TASTY = register("tasty_carrote");
    public static final RegistryObject<Item> FLIGHT = register("flight_carrote");
    public static final RegistryObject<Item> ARCANE = register("arcane_carrote");
    public static final RegistryObject<Item> STEEL = register("steel_carrote");
    public static final RegistryObject<Item> POWER = register("power_carrote");
    public static final RegistryObject<Item> PEACE = register("peace_carrote");
    public static final RegistryObject<Item> SUBSTITUTE = register("substitute_carrote");
    public static final RegistryObject<Item> NIGHT_VISION = register("night_vision_carrote");
    public static final RegistryObject<Item> BLAST = register("blast_carrote");
    public static final RegistryObject<Item> FALL = register("fall_carrote");
    public static final RegistryObject<Item> HELMET = ITEMS.register("helmet_carrote", HelmetAccessory::new);

    public static int defaultQuality(Item item) {
        if (item == HELMET.get()) return 3;
        if (item == POWER.get() || item == BLAST.get() || item == FLIGHT.get() || item == GREED.get()) return 2;
        if (item == SUBSTITUTE.get() || item == FALL.get() || item == ENCHANTING.get()
                || item == STEEL.get() || item == ARCANE.get()) return 1;
        return 0;
    }

    public static boolean isAccessory(ItemStack stack) {
        return stack.getItem() instanceof Accessory;
    }

    private static RegistryObject<Item> register(String name) {
        return ITEMS.register(name, () -> new Accessory(name));
    }

    private static class Accessory extends Item {
        private final String name;
        private Accessory(String name) {
            super(new Item.Properties().stacksTo(1));
            this.name = name;
        }

        @Override
        public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> lines, TooltipFlag flag) {
            var quality = CarroteQuality.get(stack);
            int color = quality == com.industrialcrops.crop.CropQuality.SUPER
                    ? net.minecraft.util.Mth.hsvToRgb((net.minecraft.Util.getMillis() % 4000L) / 4000.0F, 0.78F, 1.0F)
                    : quality.color();
            lines.add(Component.translatable("tooltip.carrote_curios.quality",
                    Component.translatable(quality.translationKey()).withStyle(style -> style.withColor(color)))
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.carrote_curios." + name).withStyle(ChatFormatting.GRAY));
            if (this instanceof HelmetAccessory) {
                CarroteTooltip.addCopyStatus(stack, lines);
            }
            if (CarroteCuriosEffects.isSpent(stack)) {
                lines.add(Component.translatable("tooltip.carrote_curios.spent").withStyle(ChatFormatting.RED));
            }
            if (CarroteTooltip.isShiftDown()) {
                lines.add(Component.translatable("tooltip.carrote_curios." + (this instanceof HelmetAccessory ? "helmet_usage" : "unique"))
                        .withStyle(ChatFormatting.DARK_GRAY));
                if (!CarroteCuriosEffects.curiosLoaded()) {
                    lines.add(Component.translatable("tooltip.carrote_curios.offhand").withStyle(ChatFormatting.YELLOW));
                }
            } else {
                lines.add(Component.translatable("tooltip.carrote_curios.shift").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static final class HelmetAccessory extends Accessory implements net.minecraft.world.item.Equipable {
        private HelmetAccessory() { super("helmet_carrote"); }

        @Override
        public net.minecraft.world.entity.EquipmentSlot getEquipmentSlot() {
            return net.minecraft.world.entity.EquipmentSlot.HEAD;
        }

        @Override
        public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,
                net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
            return swapWithEquipmentSlot(this, level, player, hand);
        }
    }
}
