package com.industrialcrops.recipe;

import com.industrialcrops.item.SatelliteRocketItem;
import com.industrialcrops.registry.ModFluids;
import com.industrialcrops.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CrystalWorkbenchRecipes {
   private static final List<CrystalWorkbenchRecipes.Recipe> RECIPES = List.of(
      wireless(),
      logistics(),
      processor(),
      terrain(),
      breeder(),
      satelliteReceiver(),
      launchSite(),
      smallRocket(),
      mediumRocket(),
      largeRocket(),
      satelliteControl(),
      superCompressor(),
      superCropCultivator(),
      omniBucket(),
      integratedSmeltingFurnace()
   );

   public static ItemStack assemble(CrystalWorkbenchRecipes.Recipe recipe, Container grid) {
      ItemStack out = recipe.output().copy();
      if (!(out.getItem() instanceof SatelliteRocketItem)) {
         return out;
      } else {
         int durability = 0;
         int max = 0;

         for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack stack = grid.getItem(i);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
               durability += stack.getMaxDamage() - stack.getDamageValue();
               max += stack.getMaxDamage();
            }
         }

         if (max > 0) {
            SatelliteRocketItem.setDurability(out, durability, max);
         }

         return out;
      }
   }

   private static ItemStack spaceKey(char key) {
      return switch (key) {
         case 'B' -> new ItemStack(Items.IRON_BLOCK);
         case 'C' -> new ItemStack((ItemLike)ModItems.CRYSTAL_STEEL_DEVICE_CASING.get());
         default -> ItemStack.EMPTY;
         case 'E' -> new ItemStack(Items.ENDER_PEARL);
         case 'F' -> new ItemStack(Items.FIREWORK_ROCKET);
         case 'G' -> new ItemStack((ItemLike)ModItems.GUIDANCE_COMPONENT.get());
         case 'I' -> new ItemStack((ItemLike)ModItems.CRYSTAL_INGOT.get());
         case 'K' -> new ItemStack((ItemLike)ModItems.COMET_FRUIT.get());
         case 'L' -> new ItemStack(Items.DAYLIGHT_DETECTOR);
         case 'M' -> new ItemStack((ItemLike)ModItems.MINING_UPGRADE_COMPONENT.get());
         case 'N' -> new ItemStack(Items.NETHER_STAR);
         case 'P' -> new ItemStack((ItemLike)ModItems.COMPONENT_SUBSTRATE.get());
         case 'R' -> new ItemStack(Items.COMPARATOR);
         case 'S' -> new ItemStack(Items.REDSTONE);
         case 'T' -> new ItemStack(Items.BLAST_FURNACE);
         case 'U' -> new ItemStack((ItemLike)ModItems.AUTOMATIC_COMPONENT.get());
         case 'X' -> new ItemStack((ItemLike)ModItems.DIMENSION_UPGRADE_COMPONENT.get());
         case 'Y' -> new ItemStack((ItemLike)ModItems.ENERGY_CRYSTAL.get());
         case 'a' -> new ItemStack(Items.IRON_PICKAXE);
         case 'd' -> new ItemStack(Items.DIAMOND_PICKAXE);
         case 'f' -> new ItemStack((ItemLike)ModItems.FUSION_INGOT_PICKAXE.get());
         case 'n' -> new ItemStack(Items.NETHERITE_PICKAXE);
      };
   }

   private static CrystalWorkbenchRecipes.Recipe space(String pattern, Item result, FluidStack fluid) {
      ArrayList<ItemStack> inputs = new ArrayList<>();

      for (char key : pattern.toCharArray()) {
         inputs.add(spaceKey(key));
      }

      return new CrystalWorkbenchRecipes.Recipe(List.copyOf(inputs), new ItemStack(result), fluid);
   }

   private static CrystalWorkbenchRecipes.Recipe satelliteReceiver() {
      return space(" EYE I L I IPI ISCSIIIRII", (Item)ModItems.SATELLITE_RECEIVER.get(), FluidStack.EMPTY);
   }

   private static CrystalWorkbenchRecipes.Recipe launchSite() {
      return space("IGKGIIRPRIISCSIIBUBIIITII", (Item)ModItems.LAUNCH_SITE.get(), FluidStack.EMPTY);
   }

   private static CrystalWorkbenchRecipes.Recipe smallRocket() {
      return space("  I   ILI  IPI aIGIdF I F", (Item)ModItems.SMALL_SATELLITE_ROCKET.get(), FluidStack.EMPTY);
   }

   private static CrystalWorkbenchRecipes.Recipe mediumRocket() {
      return space("  I   ILI LIPILdIGInFIYIF", (Item)ModItems.MEDIUM_SATELLITE_ROCKET.get(), FluidStack.EMPTY);
   }

   private static CrystalWorkbenchRecipes.Recipe largeRocket() {
      return space(
         " IYI LILILLIXILnIGIfFINIF", (Item)ModItems.LARGE_SATELLITE_ROCKET.get(), new FluidStack((Fluid)ModFluids.CONCENTRATED_PLASMA_JUICE.get(), 1000)
      );
   }

   private static CrystalWorkbenchRecipes.Recipe satelliteControl() {
      return space(" EYE IRLRIIMCMIIGPGIIIUII", (Item)ModItems.SATELLITE_CONTROL_DEVICE.get(), FluidStack.EMPTY);
   }

   /**
    * Super Compressor, built mostly from AP Steel (AP Structural Steel ingots):
    * <pre>
    * A A A A A
    * A P C P A
    * A C K C A     A = AP Structural Steel, P = Piston, C = Crystal Steel Device Casing,
    * A P C P A     K = Crop Compressor
    * A A A A A
    * </pre>
    */
   private static CrystalWorkbenchRecipes.Recipe superCompressor() {
      String pattern = "AAAAAAPCPAACKCAAPCPAAAAAA";
      ArrayList<ItemStack> inputs = new ArrayList<>();

      for (char key : pattern.toCharArray()) {
         inputs.add(switch (key) {
            case 'A' -> new ItemStack((ItemLike)ModItems.TYCON_PRESSURIZED_INGOT.get());
            case 'C' -> new ItemStack((ItemLike)ModItems.CRYSTAL_STEEL_DEVICE_CASING.get());
            case 'K' -> new ItemStack((ItemLike)ModItems.CROP_COMPRESSOR.get());
            case 'P' -> new ItemStack(Items.PISTON);
            default -> ItemStack.EMPTY;
         });
      }

      return new CrystalWorkbenchRecipes.Recipe(List.copyOf(inputs), new ItemStack((ItemLike)ModItems.SUPER_COMPRESSOR.get()), FluidStack.EMPTY);
   }

   /** Super Crop Cultivator: a glass dome over fertile soil, fed by comet essence and prism pods. */
   private static CrystalWorkbenchRecipes.Recipe superCropCultivator() {
      String pattern = "GGGGGGKPKGIFCFIIAMAIIIIII";
      ArrayList<ItemStack> inputs = new ArrayList<>();

      for (char key : pattern.toCharArray()) {
         inputs.add(switch (key) {
            case 'G' -> new ItemStack(Items.GLASS);
            case 'K' -> new ItemStack((ItemLike)ModItems.COMET_FRUIT.get());
            case 'P' -> new ItemStack((ItemLike)ModItems.PRISM_POD.get());
            case 'I' -> new ItemStack((ItemLike)ModItems.CRYSTAL_INGOT.get());
            case 'F' -> new ItemStack((ItemLike)ModItems.FERTILE_FARMLAND.get());
            case 'C' -> new ItemStack((ItemLike)ModItems.CRYSTAL_STEEL_DEVICE_CASING.get());
            case 'A' -> new ItemStack((ItemLike)ModItems.ENERGY_CRYSTAL.get());
            case 'M' -> new ItemStack((ItemLike)ModItems.MELON_MODIFICATION_DEVICE.get());
            default -> ItemStack.EMPTY;
         });
      }

      return new CrystalWorkbenchRecipes.Recipe(
         List.copyOf(inputs),
         new ItemStack((ItemLike)ModItems.SUPER_CROP_CULTIVATOR.get()),
         new FluidStack((Fluid)ModFluids.CONCENTRATED_PLASMA_JUICE.get(), 1000)
      );
   }

   /**
    * Omni Bucket: a bucket in the centre of a diamond of every buff source.
    *   . . A . .     A Beacon
    *   . H F W .     H Healing, F Fire Resistance, W Water Breathing
    *   . I B N .     I Invisibility, B Bucket, N Night Vision
    *   . S G T .     S Slow Falling, G Golden Apple, T Suspicious Stew (Saturation)
    *   . . C . .     C Conduit
    * Potions match by effect, so long and strong variants work too.
    */
   private static CrystalWorkbenchRecipes.Recipe omniBucket() {
      String pattern = "  A   HFW  IBN  SGT   C  ";
      ArrayList<ItemStack> inputs = new ArrayList<>();

      for (char key : pattern.toCharArray()) {
         inputs.add(switch (key) {
            case 'A' -> new ItemStack(Items.BEACON);
            case 'C' -> new ItemStack(Items.CONDUIT);
            case 'H' -> PotionContents.createItemStack(Items.POTION, Potions.HEALING);
            case 'F' -> PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE);
            case 'W' -> PotionContents.createItemStack(Items.POTION, Potions.WATER_BREATHING);
            case 'I' -> PotionContents.createItemStack(Items.POTION, Potions.INVISIBILITY);
            case 'N' -> PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION);
            case 'S' -> PotionContents.createItemStack(Items.POTION, Potions.SLOW_FALLING);
            case 'G' -> new ItemStack(Items.GOLDEN_APPLE);
            case 'T' -> saturationStew();
            case 'B' -> new ItemStack(Items.BUCKET);
            default -> ItemStack.EMPTY;
         });
      }

      return new CrystalWorkbenchRecipes.Recipe(List.copyOf(inputs), new ItemStack((ItemLike)ModItems.OMNI_BUCKET.get()), FluidStack.EMPTY);
   }

   /** Integrated Smelting Furnace: AP steel shell around blast furnaces, electric furnaces and a chest. */
   private static CrystalWorkbenchRecipes.Recipe integratedSmeltingFurnace() {
      String pattern = "AAAAAAFEFAACKCAAFEFAAAAAA";
      ArrayList<ItemStack> inputs = new ArrayList<>();

      for (char key : pattern.toCharArray()) {
         inputs.add(switch (key) {
            case 'A' -> new ItemStack((ItemLike)ModItems.TYCON_PRESSURIZED_INGOT.get());
            case 'F' -> new ItemStack(Items.BLAST_FURNACE);
            case 'E' -> new ItemStack((ItemLike)ModItems.ELECTRIC_FURNACE.get());
            case 'C' -> new ItemStack((ItemLike)ModItems.AP_STEEL_CASING.get());
            case 'K' -> new ItemStack(Items.CHEST);
            default -> ItemStack.EMPTY;
         });
      }

      return new CrystalWorkbenchRecipes.Recipe(List.copyOf(inputs), new ItemStack((ItemLike)ModItems.INTEGRATED_SMELTING_FURNACE.get()), FluidStack.EMPTY);
   }

   private static ItemStack saturationStew() {
      ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
      stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS,
         new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(MobEffects.SATURATION, 7))));
      return stew;
   }

   /** Item match, plus effect match for potions and suspicious stew (any duration or strength). */
   public static boolean matchesStack(ItemStack expected, ItemStack actual) {
      if (!actual.is(expected.getItem())) {
         return false;
      }
      PotionContents wanted = expected.get(DataComponents.POTION_CONTENTS);
      if (wanted != null) {
         PotionContents have = actual.get(DataComponents.POTION_CONTENTS);
         if (have == null) return false;
         for (MobEffectInstance need : wanted.getAllEffects()) {
            boolean found = false;
            for (MobEffectInstance got : have.getAllEffects()) {
               if (got.getEffect().equals(need.getEffect())) found = true;
            }
            if (!found) return false;
         }
      }
      SuspiciousStewEffects wantedStew = expected.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
      if (wantedStew != null) {
         SuspiciousStewEffects haveStew = actual.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
         if (haveStew == null) return false;
         for (SuspiciousStewEffects.Entry need : wantedStew.effects()) {
            boolean found = false;
            for (SuspiciousStewEffects.Entry got : haveStew.effects()) {
               if (got.effect().equals(need.effect())) found = true;
            }
            if (!found) return false;
         }
      }
      return true;
   }

   private static CrystalWorkbenchRecipes.Recipe wireless() {
      String pattern = " C C C E C C C  IBI IIDII";
      ArrayList<ItemStack> ingredients = new ArrayList<>();

      for (char key : pattern.toCharArray()) {
         ingredients.add(switch (key) {
            case 'B' -> new ItemStack(Items.BEACON);
            case 'C' -> new ItemStack((ItemLike)ModItems.COMET_FRUIT.get());
            case 'D' -> new ItemStack((ItemLike)ModItems.AUTOMATIC_COMPONENT.get());
            case 'E' -> new ItemStack(Items.ENDER_PEARL);
            default -> ItemStack.EMPTY;
            case 'I' -> new ItemStack((ItemLike)ModItems.CRYSTAL_INGOT.get());
         });
      }

      return new CrystalWorkbenchRecipes.Recipe(
         List.copyOf(ingredients),
         new ItemStack((ItemLike)ModItems.WIRELESS_ENERGY_TRANSMITTER.get()),
         new FluidStack((Fluid)ModFluids.CONCENTRATED_PLASMA_JUICE.get(), 1000)
      );
   }

   private static CrystalWorkbenchRecipes.Recipe logistics() {
      return dry("      IEI  ECE  IEI IIKII", (Item)ModItems.CRYSTAL_LOGISTICS.get());
   }

   private static CrystalWorkbenchRecipes.Recipe breeder() {
      return dry(" III ISSSIIKOKIIRGRI III ", (Item)ModItems.ANIMAL_BREEDER.get());
   }

   private static CrystalWorkbenchRecipes.Recipe terrain() {
      return dry(" III ISDSIIRPRIIKCKI III ", (Item)ModItems.TERRAIN_PROCESSOR.get());
   }

   private static CrystalWorkbenchRecipes.Recipe processor() {
      return dry("IIIIIIKWKIIKHKIIKCKIIRSRI", (Item)ModItems.CRAFTING_PROCESSOR.get());
   }

   private static CrystalWorkbenchRecipes.Recipe dry(String pattern, Item result) {
      ArrayList<ItemStack> inputs = new ArrayList<>();

      for (char key : pattern.toCharArray()) {
         inputs.add(switch (key) {
            case 'C' -> new ItemStack((ItemLike)ModItems.CRYSTAL_STEEL_DEVICE_CASING.get());
            case 'D' -> new ItemStack(Items.DIAMOND_PICKAXE);
            case 'E' -> new ItemStack(Items.ENDER_PEARL);
            default -> ItemStack.EMPTY;
            case 'G' -> new ItemStack((ItemLike)ModItems.GUIDANCE_COMPONENT.get());
            case 'H' -> new ItemStack(Items.HOPPER);
            case 'I' -> new ItemStack((ItemLike)ModItems.CRYSTAL_INGOT.get());
            case 'K' -> new ItemStack((ItemLike)ModItems.COMET_FRUIT.get());
            case 'O' -> new ItemStack(Items.DROPPER);
            case 'P' -> new ItemStack((ItemLike)ModItems.COMPONENT_SUBSTRATE.get());
            case 'R' -> new ItemStack(Items.COMPARATOR);
            case 'S' -> new ItemStack(Items.REDSTONE);
            case 'W' -> new ItemStack(Items.CRAFTER);
         });
      }

      return new CrystalWorkbenchRecipes.Recipe(List.copyOf(inputs), new ItemStack(result), FluidStack.EMPTY);
   }

   public static List<CrystalWorkbenchRecipes.Recipe> all() {
      return RECIPES;
   }

   public static CrystalWorkbenchRecipes.Recipe find(Container grid) {
      return RECIPES.stream().filter(r -> r.matchesItems(grid)).findFirst().orElse(null);
   }

   public static record Recipe(List<ItemStack> inputs, ItemStack output, FluidStack fluid) {
      public boolean matchesItems(Container grid) {
         if (grid.getContainerSize() != 25) {
            return false;
         } else {
            for (int i = 0; i < 25; i++) {
               ItemStack expected = this.inputs.get(i);
               ItemStack actual = grid.getItem(i);
               if (expected.isEmpty() ? !actual.isEmpty() : !CrystalWorkbenchRecipes.matchesStack(expected, actual)) {
                  return false;
               }
            }

            return true;
         }
      }

      public boolean hasFluid(FluidStack stored) {
         return this.fluid.isEmpty() || FluidStack.isSameFluidSameComponents(stored, this.fluid) && stored.getAmount() >= this.fluid.getAmount();
      }
   }
}
