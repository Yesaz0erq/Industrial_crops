package com.industrialcrops.recipe;
import com.industrialcrops.registry.ModItems;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.IItemHandler;
public final class UltraHighPressureInvForgeRecipes {
   private static List<UltraHighPressureInvForgeRecipes.Recipe> recipes;
   private UltraHighPressureInvForgeRecipes() {
   }
   public static List<UltraHighPressureInvForgeRecipes.Recipe> all() {
      if (recipes == null) {
         ItemStack var0 = new ItemStack((ItemLike)ModItems.TYCON_INGOT.get());
         ItemStack var1 = new ItemStack((ItemLike)ModItems.INV_INGOT.get());
         ItemStack var2 = new ItemStack((ItemLike)ModItems.GREEN_IRON_INGOT.get());
         ItemStack var3 = new ItemStack((ItemLike)ModItems.TYCON_PRESSURIZED_INGOT.get());
         recipes = List.of(
            new UltraHighPressureInvForgeRecipes.Recipe(
               "ap_steel", List.of(var0.copyWithCount(1), var1.copyWithCount(1), var2.copyWithCount(1)), var3.copyWithCount(1), 160, 80
            ),
            new UltraHighPressureInvForgeRecipes.Recipe(
               "ap_steel_bulk",
               List.of(var0.copyWithCount(2), var1.copyWithCount(2), var2.copyWithCount(2), new ItemStack((ItemLike)ModItems.CRYSTAL_INGOT.get())),
               var3.copyWithCount(4),
               240,
               120
            ),
            new UltraHighPressureInvForgeRecipes.Recipe(
               "ap_steel_iron", List.of(var0.copyWithCount(1), var2.copyWithCount(1), new ItemStack(Items.IRON_INGOT, 2)), var3.copyWithCount(1), 240, 80
            ),
            new UltraHighPressureInvForgeRecipes.Recipe(
               "ap_steel_plate",
               List.of(var3.copyWithCount(2), var2.copyWithCount(1)),
               new ItemStack((ItemLike)ModItems.AP_STRUCTURAL_STEEL_PLATE.get(), 2),
               120,
               100
            )
         );
      }
      return recipes;
   }
   public static int count() {
      return all().size();
   }
   public static UltraHighPressureInvForgeRecipes.Recipe get(int var0) {
      List var1 = all();
      return (UltraHighPressureInvForgeRecipes.Recipe)var1.get(Math.floorMod(var0, var1.size()));
   }
   public static boolean isIngredient(ItemStack var0) {
      if (var0.isEmpty()) {
         return false;
      } else {
         for (UltraHighPressureInvForgeRecipes.Recipe var2 : all()) {
            for (ItemStack var4 : var2.inputs()) {
               if (ItemStack.isSameItem(var0, var4)) {
                  return true;
               }
            }
         }
         return false;
      }
   }
   public static int count(IItemHandler var0, int var1, int var2, ItemStack var3) {
      int var4 = 0;
      for (int var5 = var1; var5 < var1 + var2; var5++) {
         ItemStack var6 = var0.getStackInSlot(var5);
         if (ItemStack.isSameItem(var6, var3)) {
            var4 += var6.getCount();
         }
      }
      return var4;
   }
   public static int count(Container var0, int var1, int var2, ItemStack var3) {
      int var4 = 0;
      for (int var5 = var1; var5 < var1 + var2; var5++) {
         ItemStack var6 = var0.getItem(var5);
         if (ItemStack.isSameItem(var6, var3)) {
            var4 += var6.getCount();
         }
      }
      return var4;
   }
   public static record Recipe(String id, List<ItemStack> inputs, ItemStack output, int ticks, int energyPerTick) {
      public boolean matches(IItemHandler var1, int var2, int var3) {
         for (ItemStack var5 : this.inputs) {
            if (UltraHighPressureInvForgeRecipes.count(var1, var2, var3, var5) < var5.getCount()) {
               return false;
            }
         }
         return true;
      }
      public void consume(IItemHandler var1, int var2, int var3) {
         for (ItemStack var5 : this.inputs) {
            int var6 = var5.getCount();
            for (int var7 = var2; var7 < var2 + var3 && var6 > 0; var7++) {
               ItemStack var8 = var1.getStackInSlot(var7);
               if (ItemStack.isSameItem(var8, var5)) {
                  var6 -= var1.extractItem(var7, Math.min(var6, var8.getCount()), false).getCount();
               }
            }
         }
      }
   }
}
