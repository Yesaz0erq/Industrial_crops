package com.industrialcrops.curios;

import com.industrialcrops.CarroteCurios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import java.util.List;

public final class CarroteCuriosItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CarroteCurios.MOD_ID);
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

    private static RegistryObject<Item> register(String name) {
        return ITEMS.register(name, () -> new Accessory(name));
    }

    private static final class Accessory extends Item {
        private final String name;
        private Accessory(String name) {
            super(new Item.Properties().stacksTo(1));
            this.name = name;
        }

        @Override
        public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> lines, TooltipFlag flag) {
            lines.add(Component.translatable("tooltip.carrote_curios." + name).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.carrote_curios.unique").withStyle(ChatFormatting.DARK_GRAY));
            if (!CarroteCuriosEffects.curiosLoaded()) {
                lines.add(Component.translatable("tooltip.carrote_curios.offhand").withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
