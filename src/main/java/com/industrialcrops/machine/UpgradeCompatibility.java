package com.industrialcrops.machine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Devices where components have a function, including crafting-only materials. */
public final class UpgradeCompatibility {
   private static final String[] SPEED = {
      "crop_compressor", "super_compressor", "basic_crop_conversion_device", "electric_furnace",
      "slime_conversion_device", "slime_cultivation_device", "component_programming_device",
      "bio_energy_generation_device", "bio_energy_reaction_device", "digital_miniature_forest", "terrain_processing_device",
      "matter_digitization_device", "data_copying_device", "matter_reconstruction_device",
      "ap_agricultural_complex", "ap_livestock_ranch", "integrated_smelting_furnace"
   };
   private static final String[] AP = {"ap_agricultural_complex", "ap_livestock_ranch"};
   private static final String[] DIMENSION = {
      "basic_control_device", "reinforced_control_device", "advanced_industrial_storage_device",
      "expandable_storage_device", "wireless_logistics_transfer_device",
      "ap_agricultural_complex", "ap_livestock_ranch"
   };
   private static final Map<String, String[]> MACHINES = new LinkedHashMap<>();

   static {
      for (int tier = 1; tier <= 4; tier++) {
         MACHINES.put("speed_component_" + tier, SPEED);
      }
      MACHINES.put("power_component", concat(new String[]{"explosive_potato_launcher"}, AP));
      MACHINES.put("rapid_fire_component", new String[]{"explosive_potato_launcher"});
      MACHINES.put("automatic_component", concat(new String[]{"data_copying_device"}, AP));
      MACHINES.put("dimension_upgrade_component", DIMENSION);
      MACHINES.put("infinite_dimension_upgrade_component", DIMENSION);
      MACHINES.put("ap_expansion_component_1", AP);
      MACHINES.put("ap_expansion_component_2", AP);
      MACHINES.put("ap_expansion_component_3", AP);
      MACHINES.put("mining_upgrade_component", new String[]{"terrain_processing_device"});
      MACHINES.put("gold_upgrade_kit", new String[]{"basic_manipulation_device"});
      MACHINES.put("crystal_steel_upgrade_kit", new String[]{"bio_energy_generation_device"});
      for (int tier = 1; tier <= 4; tier++) {
         MACHINES.put("industrial_storage_component_" + tier,
            new String[]{"reinforced_control_device", "advanced_industrial_storage_device", "integrated_smelting_furnace"});
      }
      MACHINES.put("storage_component_10k", new String[]{"expandable_storage_device"});
      MACHINES.put("basic_storage_component", new String[]{"basic_control_device"});
      MACHINES.put("reinforced_storage_component", new String[]{"reinforced_control_device"});
      MACHINES.put("component_substrate", new String[]{"component_programming_device"});
      MACHINES.put("upgrade_kit_substrate", new String[0]);
      MACHINES.put("guidance_component", new String[0]);
   }

   private UpgradeCompatibility() {
   }

   private static String[] concat(String[]... parts) {
      List<String> all = new ArrayList<>();
      for (String[] part : parts) {
         all.addAll(List.of(part));
      }
      return all.toArray(String[]::new);
   }

   public static List<String> componentPaths() {
      return List.copyOf(MACHINES.keySet());
   }

   public static boolean hasComponentInfo(Item item) {
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
      return "industrialcrops".equals(id.getNamespace()) && MACHINES.containsKey(id.getPath());
   }

   public static ItemStack componentStack(String path) {
      Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("industrialcrops", path));
      return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
   }

   public static List<ItemStack> compatibleMachines(Item upgrade) {
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(upgrade);
      String[] machines = "industrialcrops".equals(id.getNamespace()) ? MACHINES.get(id.getPath()) : null;
      if (machines == null) {
         return List.of();
      }
      List<ItemStack> stacks = new ArrayList<>(machines.length);
      for (String machine : machines) {
         Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("industrialcrops", machine));
         if (item != Items.AIR) {
            stacks.add(new ItemStack(item));
         }
      }
      return stacks;
   }
}
