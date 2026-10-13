package com.industrialcrops.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import org.jetbrains.annotations.Nullable;

public final class SatelliteNetworkData extends SavedData {
   private static final String NAME = "industrialcrops_satellites";
   public static final int[] DEFAULT_DURABILITY = new int[]{0, 1811, 3592, 10223};
   public static final int[] REACH = new int[]{0, 1, 2, 3};
   public static final int AREAS = 3;
   private final int[] launched = new int[4];
   private final List<SatelliteNetworkData.Satellite> satellites = new ArrayList<>();
   private int nextId = 1;
   private int destroyed;
   private static final Factory<SatelliteNetworkData> FACTORY = new Factory<>(SatelliteNetworkData::new, SatelliteNetworkData::load, null);

   public static SatelliteNetworkData get(MinecraftServer server) {
      ServerLevel overworld = server.overworld();
      return (SatelliteNetworkData)overworld.getDataStorage().computeIfAbsent(FACTORY, "industrialcrops_satellites");
   }

   public static int total(MinecraftServer server) {
      return get(server).total();
   }

   public static int defaultDurability(int tier) {
      return tier >= 1 && tier <= 3 ? DEFAULT_DURABILITY[tier] : 0;
   }

   public static int reach(int tier) {
      return tier >= 1 && tier <= 3 ? REACH[tier] : 0;
   }

   public void launch(int tier, int durability, int maxDurability) {
      if (tier >= 1 && tier <= 3) {
         this.launched[tier]++;
         if (durability > 0) {
            this.satellites.add(new SatelliteNetworkData.Satellite(this.nextId++, tier, durability, maxDurability));
         }

         this.setDirty();
      }
   }

   public List<SatelliteNetworkData.Satellite> satellites() {
      return Collections.unmodifiableList(this.satellites);
   }

   public int launchedCount(int tier) {
      return tier >= 1 && tier <= 3 ? this.launched[tier] : 0;
   }

   public int count(int tier) {
      int n = 0;

      for (SatelliteNetworkData.Satellite s : this.satellites) {
         if (s.tier == tier) {
            n++;
         }
      }

      return n;
   }

   public int total() {
      return this.satellites.size();
   }

   public int destroyedCount() {
      return this.destroyed;
   }

   public long durability(int tier) {
      long sum = 0L;

      for (SatelliteNetworkData.Satellite s : this.satellites) {
         if (s.tier == tier) {
            sum += (long)s.durability;
         }
      }

      return sum;
   }

   public long maxDurability(int tier) {
      long sum = 0L;

      for (SatelliteNetworkData.Satellite s : this.satellites) {
         if (s.tier == tier) {
            sum += (long)s.maxDurability;
         }
      }

      return sum;
   }

   public int maxReach() {
      int best = 0;

      for (SatelliteNetworkData.Satellite s : this.satellites) {
         best = Math.max(best, reach(s.tier));
      }

      return best;
   }

   @Nullable
   public SatelliteNetworkData.Satellite assign(int area) {
      SatelliteNetworkData.Satellite best = null;

      for (SatelliteNetworkData.Satellite s : this.satellites) {
         if (reach(s.tier) >= area && s.durability > 0 && (best == null || s.tier < best.tier || s.tier == best.tier && s.durability < best.durability)) {
            best = s;
         }
      }

      return best;
   }

   public boolean wear(SatelliteNetworkData.Satellite satellite, int amount) {
      satellite.durability = Math.max(0, satellite.durability - amount);
      boolean gone = satellite.durability <= 0;
      if (gone) {
         this.satellites.remove(satellite);
         this.destroyed++;
      }

      this.setDirty();
      return gone;
   }

   private static SatelliteNetworkData load(CompoundTag tag, Provider registries) {
      SatelliteNetworkData data = new SatelliteNetworkData();
      data.launched[1] = tag.getInt("Small");
      data.launched[2] = tag.getInt("Medium");
      data.launched[3] = tag.getInt("Large");
      data.destroyed = tag.getInt("Destroyed");
      data.nextId = Math.max(1, tag.getInt("NextId"));
      if (tag.contains("Satellites", 9)) {
         ListTag list = tag.getList("Satellites", 10);

         for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int tier = entry.getInt("Tier");
            if (tier >= 1 && tier <= 3) {
               data.satellites.add(new SatelliteNetworkData.Satellite(entry.getInt("Id"), tier, entry.getInt("Durability"), entry.getInt("Max")));
            }
         }
      } else {
         for (int tier = 1; tier <= 3; tier++) {
            for (int ix = 0; ix < data.launched[tier]; ix++) {
               int full = defaultDurability(tier);
               data.satellites.add(new SatelliteNetworkData.Satellite(data.nextId++, tier, full, full));
            }
         }
      }

      return data;
   }

   public CompoundTag save(CompoundTag tag, Provider registries) {
      tag.putInt("Small", this.launched[1]);
      tag.putInt("Medium", this.launched[2]);
      tag.putInt("Large", this.launched[3]);
      tag.putInt("Destroyed", this.destroyed);
      tag.putInt("NextId", this.nextId);
      ListTag list = new ListTag();

      for (SatelliteNetworkData.Satellite s : this.satellites) {
         CompoundTag entry = new CompoundTag();
         entry.putInt("Id", s.id);
         entry.putInt("Tier", s.tier);
         entry.putInt("Durability", s.durability);
         entry.putInt("Max", s.maxDurability);
         list.add(entry);
      }

      tag.put("Satellites", list);
      return tag;
   }

   public static final class Satellite {
      public final int id;
      public final int tier;
      public int durability;
      public final int maxDurability;

      Satellite(int id, int tier, int durability, int maxDurability) {
         this.id = id;
         this.tier = tier;
         this.maxDurability = Math.max(1, maxDurability);
         this.durability = Math.max(0, Math.min(this.maxDurability, durability));
      }
   }
}
