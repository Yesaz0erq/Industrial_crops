package com.industrialcrops.curios;

import com.industrialcrops.CarroteCurios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.List;

public final class CarroteCuriosItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CarroteCurios.MOD_ID);
    public static final DeferredItem<Item> STRENGTH = register("strength_carrote");
    public static final DeferredItem<Item> AIRBORNE = register("airborne_carrote");
    public static final DeferredItem<Item> GREED = register("greed_carrote");
    public static final DeferredItem<Item> LUCK = register("luck_carrote");
    public static final DeferredItem<Item> ENCHANTING = register("enchanting_carrote");
    public static final DeferredItem<Item> SMELTING = register("smelting_carrote");
    public static final DeferredItem<Item> TASTY = register("tasty_carrote");
    public static final DeferredItem<Item> FLIGHT = register("flight_carrote");
    public static final DeferredItem<Item> ARCANE = register("arcane_carrote");
    public static final DeferredItem<Item> STEEL = register("steel_carrote");

    private static DeferredItem<Item> register(String name) {
        return ITEMS.register(name, () -> new Accessory(name));
    }

    private static final class Accessory extends Item {
        private final String name;
        private Accessory(String name) {
            super(new Item.Properties().stacksTo(1));
            this.name = name;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
            lines.add(Component.translatable("tooltip.carrote_curios." + name).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.carrote_curios.unique").withStyle(ChatFormatting.DARK_GRAY));
            if (!CarroteCuriosEffects.curiosLoaded()) {
                lines.add(Component.translatable("tooltip.carrote_curios.offhand").withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
