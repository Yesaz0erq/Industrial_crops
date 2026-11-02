package com.industrialcrops.machine;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.Tags;

/**
 * Flux ore logic shared by the Ore Vein Data Collector, the Flux Slime and the AP Slime Farm.
 *
 * <p>An "ore type" is a metal that the current dimension generates naturally: an ore block placed by a
 * world-generation feature of one of the dimension's biomes, whose smelting result is an ingot. Ingots
 * with no natural ore (alloys, crafted metals) never appear in the index and so can never be produced.
 */
public final class FluxOreIndex {
   public static final String TAG_ORES = "FluxOres";
   public static final String TAG_WORLD = "FluxWorld";

   /** One scanned metal: the ingot it yields and the natural ore blocks that carry it. */
   public record OreType(Item ingot, List<Item> ores) {
      public Item icon() { return ores.isEmpty() ? ingot : ores.get(0); }
      public ResourceLocation ingotId() { return BuiltInRegistries.ITEM.getKey(ingot); }
   }

   private static final Map<ServerLevel, List<OreType>> CACHE = new WeakHashMap<>();
   private static final Map<Item, Item> SMELT_CACHE = new IdentityHashMap<>();
   private static RecipeManager smeltCacheOwner;

   private FluxOreIndex() { }

   /** The natural metal ores of this dimension, computed once per loaded level. */
   public static synchronized List<OreType> types(ServerLevel level) {
      List<OreType> cached = CACHE.get(level);
      if (cached == null) {
         cached = scan(level);
         CACHE.put(level, cached);
      }
      return cached;
   }

   private static List<OreType> scan(ServerLevel level) {
      Set<Block> found = new LinkedHashSet<>();
      Set<ConfiguredFeature<?, ?>> seen = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
      for (Holder<Biome> biome : level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()) {
         for (HolderSet<PlacedFeature> step : biome.value().getGenerationSettings().features()) {
            for (Holder<PlacedFeature> placed : step) {
               try {
                  placed.value().feature().value().getFeatures().forEach(feature -> {
                     if (seen.add(feature)) collectBlocks(feature.config(), found);
                  });
               } catch (RuntimeException ignored) {
                  // A broken third-party feature must not stop the scan of every other ore.
               }
            }
         }
      }
      Map<Item, List<Item>> byIngot = new LinkedHashMap<>();
      for (Block block : found) {
         if (!block.defaultBlockState().is(Tags.Blocks.ORES)) continue;
         Item ore = block.asItem();
         if (ore == Items.AIR) continue;
         Item ingot = smeltToIngot(level, new ItemStack(ore));
         if (ingot == null) continue;
         List<Item> ores = byIngot.computeIfAbsent(ingot, key -> new ArrayList<>());
         if (!ores.contains(ore)) ores.add(ore);
      }
      List<OreType> result = new ArrayList<>();
      byIngot.forEach((ingot, ores) -> {
         ores.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString().contains("deepslate") ? 1 : 0));
         result.add(new OreType(ingot, List.copyOf(ores)));
      });
      // Vanilla metals first, then every mod metal alphabetically.
      result.sort(Comparator.<OreType, Integer>comparing(type -> "minecraft".equals(type.ingotId().getNamespace()) ? 0 : 1)
         .thenComparing(type -> type.ingotId().toString()));
      return List.copyOf(result);
   }

   private static void collectBlocks(FeatureConfiguration config, Set<Block> out) {
      if (config instanceof OreConfiguration ore) {
         for (OreConfiguration.TargetBlockState target : ore.targetStates) out.add(target.state.getBlock());
         return;
      }
      // Mod ore features use their own configurations (resizable veins, layered ores...). Walk their
      // fields for block states; the ore tag filter afterwards discards stone, deepslate and the like.
      walk(config, out, java.util.Collections.newSetFromMap(new IdentityHashMap<>()), 0);
   }

   private static void walk(Object value, Set<Block> out, Set<Object> visited, int depth) {
      if (value == null || depth > 6 || !visited.add(value)) return;
      if (value instanceof BlockState state) { out.add(state.getBlock()); return; }
      if (value instanceof Block block) { if (block != Blocks.AIR) out.add(block); return; }
      if (value instanceof OreConfiguration.TargetBlockState target) { out.add(target.state.getBlock()); return; }
      if (value instanceof Holder<?> holder) {
         if (holder.isBound()) walk(holder.value(), out, visited, depth + 1);
         return;
      }
      if (value instanceof Optional<?> optional) { optional.ifPresent(inner -> walk(inner, out, visited, depth + 1)); return; }
      if (value instanceof Map<?, ?> map) {
         for (Map.Entry<?, ?> entry : map.entrySet()) {
            walk(entry.getKey(), out, visited, depth + 1);
            walk(entry.getValue(), out, visited, depth + 1);
         }
         return;
      }
      if (value instanceof Collection<?> collection) {
         for (Object inner : collection) walk(inner, out, visited, depth + 1);
         return;
      }
      if (value instanceof Iterable<?> iterable && !(value instanceof HolderSet<?>)) {
         for (Object inner : iterable) walk(inner, out, visited, depth + 1);
         return;
      }
      Class<?> type = value.getClass();
      if (type.isArray()) {
         if (type.getComponentType().isPrimitive()) return;
         for (int index = 0; index < Array.getLength(value); index++) walk(Array.get(value, index), out, visited, depth + 1);
         return;
      }
      if (type.isPrimitive() || type.isEnum() || value instanceof CharSequence || value instanceof Number
         || value instanceof ConfiguredFeature<?, ?> || value instanceof PlacedFeature) return;
      String name = type.getName();
      if (name.startsWith("java.") || name.startsWith("com.mojang.") || name.startsWith("it.unimi.")) return;
      for (Class<?> cls = type; cls != null && cls != Object.class; cls = cls.getSuperclass()) {
         for (Field field : cls.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
            try {
               field.setAccessible(true);
               walk(field.get(value), out, visited, depth + 1);
            } catch (Throwable ignored) {
               // Inaccessible fields simply contribute nothing.
            }
         }
      }
   }

   /** Smelting (then blasting) result of an item if, and only if, that result is an ingot. */
   public static synchronized Item smeltToIngot(Level level, ItemStack input) {
      if (input.isEmpty()) return null;
      RecipeManager manager = level.getRecipeManager();
      if (manager != smeltCacheOwner) {
         SMELT_CACHE.clear();
         smeltCacheOwner = manager;
      }
      Item key = input.getItem();
      if (SMELT_CACHE.containsKey(key)) {
         Item cached = SMELT_CACHE.get(key);
         return cached == Items.AIR ? null : cached;
      }
      SingleRecipeInput recipeInput = new SingleRecipeInput(new ItemStack(key));
      ItemStack result = manager.getRecipeFor(RecipeType.SMELTING, recipeInput, level)
         .map(holder -> holder.value().getResultItem(level.registryAccess()))
         .or(() -> manager.getRecipeFor(RecipeType.BLASTING, recipeInput, level)
            .map(holder -> holder.value().getResultItem(level.registryAccess())))
         .orElse(ItemStack.EMPTY);
      Item ingot = !result.isEmpty() && result.is(Tags.Items.INGOTS) ? result.getItem() : null;
      SMELT_CACHE.put(key, ingot == null ? Items.AIR : ingot);
      return ingot;
   }

   /** Raw ore or unprocessed ore: an ore block item or a raw material. */
   public static boolean isOreInput(ItemStack stack) {
      if (stack.isEmpty()) return false;
      if (stack.is(Tags.Items.ORES) || stack.is(Tags.Items.RAW_MATERIALS)) return true;
      return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().is(Tags.Blocks.ORES);
   }

   /** The ingot a flux slime would make from this input, or null when it is no natural metal ore. */
   public static Item ingotFor(Level level, ItemStack input) {
      return isOreInput(input) ? smeltToIngot(level, input) : null;
   }

   // ---- ore data carried by Flux Ore Gel and Flux Slimes -------------------------------------------

   public static ListTag writeIds(Collection<ResourceLocation> ids) {
      ListTag list = new ListTag();
      for (ResourceLocation id : ids) list.add(StringTag.valueOf(id.toString()));
      return list;
   }

   public static Set<ResourceLocation> readIds(CompoundTag tag) {
      Set<ResourceLocation> ids = new LinkedHashSet<>();
      if (tag == null || !tag.contains(TAG_ORES, Tag.TAG_LIST)) return ids;
      ListTag list = tag.getList(TAG_ORES, Tag.TAG_STRING);
      for (int index = 0; index < list.size(); index++) {
         ResourceLocation id = ResourceLocation.tryParse(list.getString(index));
         if (id != null) ids.add(id);
      }
      return ids;
   }

   public static List<ResourceLocation> ingotIds(List<OreType> types) {
      List<ResourceLocation> ids = new ArrayList<>(types.size());
      for (OreType type : types) ids.add(type.ingotId());
      return ids;
   }

   public static Item item(ResourceLocation id) {
      return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
   }
}
