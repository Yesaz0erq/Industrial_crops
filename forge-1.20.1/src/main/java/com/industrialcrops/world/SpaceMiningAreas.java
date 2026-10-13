package com.industrialcrops.world;
import com.industrialcrops.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
public final class SpaceMiningAreas {
   public static final int COUNT = 3;
   private static final int[] CYCLE_TICKS = new int[]{0, 100, 120, 160};
   private SpaceMiningAreas() {
   }
   public static int cycleTicks(int area) {
      return area >= 1 && area <= 3 ? CYCLE_TICKS[area] : 100;
   }
   public static ItemStack roll(int area, RandomSource random) {
      int r = random.nextInt(100);
      Item item = switch (area) {
         case 1 -> r < 75 ? (Item)ModItems.GREEN_IRON_ORE.get() : (Item)ModItems.INV_ORE.get();
         case 2 -> r < 50 ? (Item)ModItems.INV_ORE.get() : (r < 80 ? (Item)ModItems.GREEN_IRON_ORE.get() : (Item)ModItems.TYCON_ORE.get());
         default -> r < 60 ? (Item)ModItems.TYCON_ORE.get() : (r < 85 ? (Item)ModItems.INV_ORE.get() : (Item)ModItems.GREEN_IRON_ORE.get());
      };
      int count = random.nextInt(100) < 25 ? 2 : 1;
      return new ItemStack(item, count);
   }
}
