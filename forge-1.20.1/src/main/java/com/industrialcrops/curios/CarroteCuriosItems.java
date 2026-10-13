package com.industrialcrops.curios;
import net.minecraftforge.registries.RegistryObject;
import com.industrialcrops.crop.CropQuality;
import com.industrialcrops.screen.UltimateCarroteMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.DeferredRegister;
public final class CarroteCuriosItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, "carrote_curios");
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
   public static final RegistryObject<Item> POWER = register("powerful_carrote");
   public static final RegistryObject<Item> PEACE = register("peace_carrote");
   public static final RegistryObject<Item> SUBSTITUTE = register("substitute_carrote");
   public static final RegistryObject<Item> NIGHT_VISION = register("night_vision_carrote");
   public static final RegistryObject<Item> BLAST = register("blast_protection_carrote");
   public static final RegistryObject<Item> FALL = register("fall_protection_carrote");
   public static final RegistryObject<Item> HELMET = ITEMS.register("helmet_carrote", CarroteCuriosItems.HelmetAccessory::new);
   public static final RegistryObject<Item> HARD = register("hard_carrote");
   public static final RegistryObject<Item> UNBREAKABLE = register("unbreakable_carrote");
   public static final RegistryObject<Item> RESISTANCE = register("resistance_carrote");
   public static final RegistryObject<Item> THIEF = register("thief_carrote");
   public static final RegistryObject<Item> AMMO = register("ammo_carrote");
   public static final RegistryObject<Item> BREACH = register("breach_carrote");
   public static final RegistryObject<Item> ARMOR_PIERCING = register("armor_piercing_carrote");
   public static final RegistryObject<Item> EXPEL = register("expel_carrote");
   public static final RegistryObject<Item> ULTIMATE = ITEMS.register("ultimate_carrote", CarroteCuriosItems.UltimateAccessory::new);
   public static int defaultQuality(Item var0) {
      if (var0 == ULTIMATE.get()) {
         return 5;
      } else if (var0 == THIEF.get() || var0 == ARMOR_PIERCING.get() || var0 == EXPEL.get()) {
         return 4;
      } else if (var0 == AMMO.get() || var0 == BREACH.get()) {
         return 3;
      } else if (var0 == UNBREAKABLE.get() || var0 == RESISTANCE.get()) {
         return 2;
      } else if (var0 == HELMET.get()) {
         return 3;
      } else if (var0 == POWER.get() || var0 == BLAST.get() || var0 == FLIGHT.get() || var0 == GREED.get()) {
         return 2;
      } else {
         return var0 != SUBSTITUTE.get() && var0 != FALL.get() && var0 != ENCHANTING.get() && var0 != STEEL.get() && var0 != ARCANE.get() ? 0 : 1;
      }
   }
   public static boolean isAccessory(ItemStack var0) {
      return var0.getItem() instanceof CarroteCuriosItems.Accessory;
   }
   private static RegistryObject<Item> register(String var0) {
      return ITEMS.register(var0, () -> new CarroteCuriosItems.Accessory(var0));
   }
   private static class Accessory extends Item {
      private final String name;
      private Accessory(String var1) {
         super(new Properties().stacksTo(1));
         this.name = var1;
      }
      public Component getName(ItemStack var1) {
         Component var2 = super.getName(var1);
         if (!this.name.equals("ultimate_carrote")) {
            return var2;
         } else {
            MutableComponent var3 = Component.empty();
            String var4 = var2.getString();
            float var5 = (float)(Util.getMillis() % 4000L) / 4000.0F;
            for (int var6 = 0; var6 < var4.length(); var6++) {
               int var7 = Mth.hsvToRgb((var5 + (float)var6 / (float)Math.max(1, var4.length())) % 1.0F, 0.8F, 1.0F);
               var3.append(Component.literal(var4.substring(var6, var6 + 1)).withStyle(var1x -> var1x.withColor(var7)));
            }
            return var3;
         }
      }
      public void appendHoverText(ItemStack var1, net.minecraft.world.level.Level var2, List<Component> var3, TooltipFlag var4) {
         CropQuality var5 = CarroteQuality.get(var1);
         int var6 = var5 == CropQuality.SUPER ? Mth.hsvToRgb((float)(Util.getMillis() % 4000L) / 4000.0F, 0.78F, 1.0F) : var5.color();
         var3.add(
            Component.translatable(
                  "tooltip.carrote_curios.quality", new Object[]{Component.translatable(var5.translationKey()).withStyle(var1x -> var1x.withColor(var6))}
               )
               .withStyle(ChatFormatting.GRAY)
         );
         var3.add(Component.translatable("tooltip.carrote_curios." + this.name).withStyle(ChatFormatting.GRAY));
         if (this instanceof CarroteCuriosItems.HelmetAccessory) {
            CarroteTooltip.addCopyStatus(var1, var3);
         }
         if (CarroteCuriosEffects.isSpent(var1)) {
            var3.add(Component.translatable("tooltip.carrote_curios.spent").withStyle(ChatFormatting.RED));
         }
         if (CarroteTooltip.isShiftDown()) {
            var3.add(
               Component.translatable(
                     "tooltip.carrote_curios."
                        + (
                           this instanceof CarroteCuriosItems.HelmetAccessory
                              ? "helmet_usage"
                              : (this instanceof CarroteCuriosItems.UltimateAccessory ? "ultimate_usage" : "unique")
                        )
                  )
                  .withStyle(ChatFormatting.DARK_GRAY)
            );
            if (!CarroteCuriosEffects.curiosLoaded()) {
               var3.add(Component.translatable("tooltip.carrote_curios.offhand").withStyle(ChatFormatting.YELLOW));
            }
         } else {
            var3.add(Component.translatable("tooltip.carrote_curios.shift").withStyle(ChatFormatting.DARK_GRAY));
         }
      }
   }
   private static final class HelmetAccessory extends CarroteCuriosItems.Accessory implements Equipable {
      private HelmetAccessory() {
         super("helmet_carrote");
      }
      public EquipmentSlot getEquipmentSlot() {
         return EquipmentSlot.HEAD;
      }
      public InteractionResultHolder<ItemStack> use(Level var1, Player var2, InteractionHand var3) {
         return this.swapWithEquipmentSlot(this, var1, var2, var3);
      }
   }
   private static final class UltimateAccessory extends CarroteCuriosItems.Accessory {
      private UltimateAccessory() {
         super("ultimate_carrote");
      }
      public InteractionResultHolder<ItemStack> use(Level var1, Player var2, InteractionHand var3) {
         ItemStack var4 = var2.getItemInHand(var3);
         int var5 = var3 == InteractionHand.OFF_HAND ? 40 : var2.getInventory().selected;
         if (!var1.isClientSide) {
            net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer)var2,
               new SimpleMenuProvider((var1x, var2x, var3x) -> new UltimateCarroteMenu(var1x, var2x, var5), var4.getHoverName()),
               var1x -> var1x.writeVarInt(var5)
            );
         }
         return InteractionResultHolder.sidedSuccess(var4, var1.isClientSide);
      }
      public boolean canFitInsideContainerItems() {
         return false;
      }
   }
}
