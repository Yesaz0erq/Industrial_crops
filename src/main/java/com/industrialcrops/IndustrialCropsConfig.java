package com.industrialcrops;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public final class IndustrialCropsConfig {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue GIVE_FIELD_GUIDE_ON_FIRST_JOIN;

   private IndustrialCropsConfig() {
   }

   static {
      Builder builder = new Builder();
      builder.push("field_guide");
      GIVE_FIELD_GUIDE_ON_FIRST_JOIN = builder.comment("Give every player one Industrial Crops Field Guide the first time they join a world.")
         .define("giveOnFirstJoin", true);
      builder.pop();
      SPEC = builder.build();
   }
}
