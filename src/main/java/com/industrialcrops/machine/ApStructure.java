package com.industrialcrops.machine;

import com.industrialcrops.registry.ModBlocks;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;

/** Shared immutable blueprint for validation, operating bounds and the client projection. */
public final class ApStructure {
   private static final int[] WIDTHS = {16, 32, 32, 48};
   private static final int[] DEPTHS = {16, 16, 32, 48};
   private static final int ALL_CATEGORIES = (1 << Category.values().length) - 1;
   private static final TagKey<Block> GREENHOUSE_GLASS = TagKey.create(
      Registries.BLOCK, new ResourceLocation("industrialcrops", "ap_greenhouse_glass"));
   private static final Map<Key, Layout> CACHE = new LinkedHashMap<>(16, 0.75F, true);

   private ApStructure() { }

   public static Layout layout(BlockPos controller, Direction facing, Type type, int tier) {
      Direction horizontal = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
      Key key = new Key(controller.immutable(), horizontal, type, clampTier(tier));
      synchronized (CACHE) {
         Layout result = CACHE.get(key);
         if (result == null) {
            result = new Layout(key);
            CACHE.put(key, result);
            if (CACHE.size() > 16) CACHE.remove(CACHE.keySet().iterator().next());
         }
         return result;
      }
   }

   private static int clampTier(int tier) { return Math.max(0, Math.min(3, tier)); }

   public enum Type { FARM, RANCH }
   // Keep category ordinals stable for the existing menu/status packet. FOUNDATION and GROUND
   // remain complete without fixed below-ground materials; soil is checked per planting operation.
   public enum Category { FOUNDATION, FRAME, WALLS, ROOF, WATER, GROUND, ENTRANCE, FACILITIES, CLEARANCE }
   public enum Requirement {
      EXACT, GLASS, PLANKS, WOOD_ROOF, WOOD_SLAB, FENCE, FENCE_GATE,
      DOOR_LOWER, DOOR_UPPER, SOURCE_WATER, FARMLAND, PASTURE, CROP_SPACE, AIRSPACE
   }

   public record Part(BlockPos pos, BlockState example, Category category, Requirement requirement) {
      public Part { pos = pos.immutable(); }

      public boolean matches(Level level) {
         if (!level.hasChunkAt(pos)) return false;
         BlockState state = level.getBlockState(pos);
         return switch (requirement) {
            case EXACT -> state.is(example.getBlock());
            case GLASS -> state.is(GREENHOUSE_GLASS);
            case PLANKS -> state.is(BlockTags.PLANKS);
            case WOOD_ROOF -> state.is(BlockTags.PLANKS) || state.is(BlockTags.WOODEN_SLABS) || state.is(BlockTags.WOODEN_STAIRS);
            case WOOD_SLAB -> state.is(BlockTags.WOODEN_SLABS);
            case FENCE -> state.is(BlockTags.FENCES);
            case FENCE_GATE -> state.is(BlockTags.FENCE_GATES);
            case DOOR_LOWER -> state.is(BlockTags.WOODEN_DOORS)
               && state.hasProperty(DoorBlock.HALF) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
            case DOOR_UPPER -> state.is(BlockTags.WOODEN_DOORS)
               && state.hasProperty(DoorBlock.HALF) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
            case SOURCE_WATER -> state.getFluidState().is(FluidTags.WATER) && state.getFluidState().isSource();
            case FARMLAND -> state.is(Blocks.FARMLAND);
            case PASTURE -> state.is(BlockTags.DIRT);
            case CROP_SPACE -> clearSpace(level, state) || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN);
            case AIRSPACE -> clearSpace(level, state);
         };
      }

      private boolean clearSpace(Level level, BlockState state) {
         // Water and its flow are clear space, but waterlogging cannot hide a solid obstruction.
         if (state.getFluidState().is(FluidTags.WATER)) return state.getCollisionShape(level, pos).isEmpty();
         return state.getFluidState().isEmpty()
            && (state.isAir() || state.getBlock() instanceof BushBlock || state.getCollisionShape(level, pos).isEmpty());
      }
   }

   private record Key(BlockPos controller, Direction facing, Type type, int tier) { }

   public static final class Layout {
      private final Type type;
      private final int tier, width, depth;
      private final BlockPos controller;
      private final BlockPos planeOrigin;
      private final Direction facing, right, back;
      private final List<Part> parts;
      private final AABB bounds, workBounds;

      private Layout(Key key) {
         type = key.type(); tier = key.tier(); width = WIDTHS[tier]; depth = DEPTHS[tier];
         controller = key.controller(); facing = key.facing();
         // The ranch controller sits on the fence beside the gate; animals remain at gate height.
         planeOrigin = type == Type.RANCH ? controller.below() : controller;
         right = facing.getClockWise(); back = facing.getOpposite();
         Builder builder = new Builder(this);
         if (type == Type.FARM) builder.farm(); else builder.ranch();
         parts = List.copyOf(builder.parts.values());
         bounds = box(0, 0, 0, width - 1, type == Type.FARM ? 6 : 3, depth - 1);
         workBounds = box(1, 0, 1, width - 2, type == Type.FARM ? 0 : 2, depth - 2);
      }

      public Type type() { return type; }
      public int tier() { return tier; }
      public int width() { return width; }
      public int depth() { return depth; }
      public BlockPos controller() { return controller; }
      public Direction facing() { return facing; }
      public List<Part> parts() { return parts; }
      public AABB bounds() { return bounds; }
      public AABB workBounds() { return workBounds; }
      public BlockPos center() { return pos(width / 2, 0, depth / 2); }

      /** u = right, v = back, h = height above the planting / animal plane. */
      public BlockPos pos(int u, int h, int v) {
         return planeOrigin.relative(right, u - (width / 2 - 1)).relative(back, v).above(h);
      }

      public boolean containsWorkPos(BlockPos pos) {
         return workBounds.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      }

      private AABB box(int minU, int minH, int minV, int maxU, int maxH, int maxV) {
         BlockPos a = pos(minU, minH, minV), b = pos(maxU, maxH, maxV);
         return new AABB(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
            Math.max(a.getX(), b.getX()) + 1, Math.max(a.getY(), b.getY()) + 1, Math.max(a.getZ(), b.getZ()) + 1);
      }

      public Status check(Level level) {
         // Availability is checked before reading blocks: validation never requests neighbouring chunks.
         int firstX = ((int)Math.floor(bounds.minX)) >> 4, lastX = ((int)Math.ceil(bounds.maxX) - 1) >> 4;
         int firstZ = ((int)Math.floor(bounds.minZ)) >> 4, lastZ = ((int)Math.ceil(bounds.maxZ) - 1) >> 4;
         for (int x = firstX; x <= lastX; x++) {
            for (int z = firstZ; z <= lastZ; z++) {
               if (!level.hasChunkAt(new BlockPos(x << 4, controller.getY(), z << 4)))
                  return new Status(0, tier, false, type == Type.RANCH);
            }
         }
         int mask = ALL_CATEGORIES;
         for (Part part : parts) {
            int bit = 1 << part.category().ordinal();
            // Stop reading a failed category until the next one-second controller check.
            if ((mask & bit) != 0 && !part.matches(level)) mask &= ~bit;
         }
         if (!hasSourceWater(level)) mask &= ~(1 << Category.WATER.ordinal());
         return new Status(mask, tier, true, type == Type.RANCH);
      }

      /** One source anywhere in the footprint, from natural soil level through the roof, is enough. */
      public boolean hasSourceWater(Level level) {
         BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
         int top = type == Type.FARM ? 6 : 3;
         for (int u = 0; u < width; u++) {
            for (int v = 0; v < depth; v++) {
               BlockPos column = pos(u, 0, v);
               // The renderer can also query this helper; never request an unloaded neighbouring chunk.
               if (!level.hasChunkAt(column)) continue;
               for (int h = -1; h <= top; h++) {
                  cursor.set(column.getX(), column.getY() + h, column.getZ());
                  var fluid = level.getFluidState(cursor);
                  if (fluid.is(FluidTags.WATER) && fluid.isSource()) return true;
               }
            }
         }
         return false;
      }
   }

   private static final class Builder {
      private final Layout layout;
      private final int w, d;
      private final Map<BlockPos, Part> parts = new LinkedHashMap<>();
      private Builder(Layout layout) { this.layout = layout; w = layout.width(); d = layout.depth(); }

      private void set(int u, int h, int v, BlockState example, Category category, Requirement requirement) {
         BlockPos pos = layout.pos(u, h, v);
         parts.put(pos, new Part(pos, example, category, requirement));
      }
      private void exact(int u, int h, int v, Block block, Category category) {
         set(u, h, v, block.defaultBlockState(), category, Requirement.EXACT);
      }
      private void fillClearance(int u, int h, int v, boolean crop) {
         BlockPos pos = layout.pos(u, h, v);
         parts.putIfAbsent(pos, new Part(pos, Blocks.AIR.defaultBlockState(), Category.CLEARANCE,
            crop ? Requirement.CROP_SPACE : Requirement.AIRSPACE));
      }
      private int roofHeight(int u) { int bay = u % 16; return 3 + Math.min(bay, 15 - bay) / 2; }
      private boolean rib(int v) { return v % 8 == 0 || v == d - 1; }
      private boolean pillar(int u) { return u == 0 || u == w - 1 || (u % 16 == 0 && u != w / 2); }
      private boolean perimeter(int u, int v) { return u == 0 || u == w - 1 || v == 0 || v == d - 1; }
      private void farm() {
         Block frame = ModBlocks.AP_STEEL_FRAME.get();
         // Ground, paths and irrigation are player-designed; only the above-ground greenhouse is fixed.
         int door = w / 2;
         exact(2, 0, d - 3, Blocks.COMPOSTER, Category.FACILITIES);
         exact(3, 0, d - 3, Blocks.CRAFTING_TABLE, Category.FACILITIES);
         // Sixteen-wide stepped gables, AP arches every eight cells and columns at the bay boundaries.
         for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
               int roof = roofHeight(u);
               if (perimeter(u, v)) {
                  for (int h = 0; h < roof; h++)
                     set(u, h, v, Blocks.GLASS.defaultBlockState(), Category.WALLS, Requirement.GLASS);
               }
               if (rib(v) && pillar(u)) {
                  for (int h = 0; h < roof; h++) exact(u, h, v, frame, Category.FRAME);
               }
               if (rib(v)) exact(u, roof, v, frame, Category.FRAME);
               else set(u, roof, v, Blocks.GLASS.defaultBlockState(), Category.ROOF, Requirement.GLASS);
            }
         }
         parts.remove(layout.controller());
         set(door, 0, 0, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, layout.facing())
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), Category.ENTRANCE, Requirement.DOOR_LOWER);
         set(door, 1, 0, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, layout.facing())
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Category.ENTRANCE, Requirement.DOOR_UPPER);
         for (int u = 1; u < w - 1; u++) {
            for (int v = 1; v < d - 1; v++) {
               for (int h = 0; h < roofHeight(u); h++) fillClearance(u, h, v, h == 0);
            }
         }
      }

      private void ranch() {
         for (int u = 0; u < w; u++) {
            for (int v = 0; v < d; v++) {
               if (perimeter(u, v)) {
                  set(u, 0, v, Blocks.OAK_FENCE.defaultBlockState(), Category.WALLS, Requirement.FENCE);
               }
            }
         }
         // The controller occupies h=1 above this fence, to the right of the gate from outside.
         set(w / 2 - 1, 0, 0, Blocks.OAK_FENCE.defaultBlockState(), Category.FRAME, Requirement.FENCE);
         set(w / 2, 0, 0, Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, layout.facing()),
            Category.ENTRANCE, Requirement.FENCE_GATE);
         for (int h = 1; h <= 2; h++) fillClearance(w / 2, h, 0, false);
         // The rear-right seven-by-seven rain shelter leaves the main pasture open-air.
         for (int u = w - 8; u <= w - 2; u++) {
            for (int v = d - 8; v <= d - 2; v++) {
               BlockState roof = v == d - 8
                  ? Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, layout.facing())
                  : Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
               set(u, 3, v, roof, Category.ROOF, Requirement.WOOD_ROOF);
            }
         }
         for (int u : new int[]{w - 8, w - 2}) {
            for (int v : new int[]{d - 8, d - 2}) {
               for (int h = 0; h <= 2; h++) set(u, h, v, Blocks.OAK_FENCE.defaultBlockState(), Category.FRAME, Requirement.FENCE);
            }
         }
         // A slab-rimmed feeding trough surrounds three hay bales beneath the canopy.
         for (int u = w - 8; u <= w - 4; u++) {
            for (int v = d - 7; v <= d - 5; v++) {
               if (u == w - 8 || u == w - 4 || v == d - 7 || v == d - 5)
                  set(u, 0, v, Blocks.OAK_SLAB.defaultBlockState(), Category.FACILITIES, Requirement.WOOD_SLAB);
               else exact(u, 0, v, Blocks.HAY_BLOCK, Category.FACILITIES);
            }
         }
         exact(w - 6, 0, d - 4, Blocks.COMPOSTER, Category.FACILITIES);
         // The drinking supply is a global source-water check, with no prescribed basin or floor.
         for (int u = 1; u < w - 1; u++) {
            for (int v = 1; v < d - 1; v++) {
               for (int h = 0; h <= 2; h++) fillClearance(u, h, v, false);
            }
         }
      }
   }

   public record Status(int mask, int tier, boolean loaded, boolean livestock) {
      public Status { mask &= ALL_CATEGORIES; tier = clampTier(tier); }
      public boolean formed() { return loaded && mask == ALL_CATEGORIES; }
      public boolean complete(Category category) { return (mask & (1 << category.ordinal())) != 0; }
      public int width() { return WIDTHS[tier]; }
      public int depth() { return DEPTHS[tier]; }
      public int completedCategories() { return Integer.bitCount(mask); }
      public int pack() { return mask | tier << 9 | (loaded ? 1 << 11 : 0) | (livestock ? 1 << 12 : 0); }
      public static Status unpack(int packed) {
         return new Status(packed & ALL_CATEGORIES, (packed >>> 9) & 3, (packed & (1 << 11)) != 0, (packed & (1 << 12)) != 0);
      }
      public static Status empty(Type type, int tier) { return new Status(0, tier, false, type == Type.RANCH); }
   }
}
