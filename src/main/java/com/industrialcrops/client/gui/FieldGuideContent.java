package com.industrialcrops.client.gui;

import java.util.List;

final class FieldGuideContent {
   static final List<FieldGuideContent.Chapter> CHAPTERS = List.of(
      new FieldGuideContent.Chapter(
         "start",
         "guide.industrialcrops.chapter.start",
         "industrialcrops:field_guide",
         -5871035,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.start.welcome.title",
               "guide.industrialcrops.start.welcome.body",
               "industrialcrops:field_guide",
               List.of("industrialcrops:industrial_carrot", "industrialcrops:copper_device_casing", "industrialcrops:crystal_ingot", "carrote:carrote")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.start.roadmap.title",
               "guide.industrialcrops.start.roadmap.body",
               null,
               List.of(
                  "industrialcrops:copper_device_casing",
                  "industrialcrops:iron_device_casing",
                  "industrialcrops:component_substrate_integrated_gold_device_casing",
                  "industrialcrops:crystal_steel_casing",
                  "carrote:paradox_steel_device_casing"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.start.tips.title",
               "guide.industrialcrops.start.tips.body",
               null,
               List.of("industrialcrops:redstone_bonemeal", "industrialcrops:remote_access_device")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "crops",
         "guide.industrialcrops.chapter.crops",
         "industrialcrops:industrial_carrot",
         -10577348,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.crops.first_tools.title",
               "guide.industrialcrops.crops.first_tools.body",
               "industrialcrops:redstone_bonemeal",
               List.of("industrialcrops:empty_bag", "industrialcrops:copper_device_casing", "minecraft:copper_ingot")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crops.conversion.title",
               "guide.industrialcrops.crops.conversion.body",
               "industrialcrops:basic_crop_conversion_device",
               List.of(
                  "minecraft:carrot",
                  "industrialcrops:bagged_industrial_carrot",
                  "industrialcrops:industrial_wheat",
                  "industrialcrops:bagged_industrial_wheat_seeds",
                  "industrialcrops:industrial_melon"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crops.growing.title",
               "guide.industrialcrops.crops.growing.body",
               "industrialcrops:industrial_carrot",
               List.of(
                  "industrialcrops:bagged_industrial_potato",
                  "industrialcrops:bagged_industrial_wheat_seeds",
                  "industrialcrops:empty_bag",
                  "industrialcrops:rapid_growth_fertilizer_bag"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crops.compress.title",
               "guide.industrialcrops.crops.compress.body",
               "industrialcrops:crop_compressor",
               List.of(
                  "industrialcrops:industrial_carrot_block",
                  "industrialcrops:industrial_wheat_block",
                  "industrialcrops:fertile_soil_fertilizer_bag",
                  "industrialcrops:fertile_soil"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crops.mixer.title",
               "guide.industrialcrops.crops.mixer.body",
               "industrialcrops:mixer",
               List.of(
                  "industrialcrops:rapid_growth_fertilizer_bag",
                  "industrialcrops:fertile_soil_fertilizer_bag",
                  "industrialcrops:normal_feed_bag",
                  "industrialcrops:rapid_breeding_feed_bag",
                  "industrialcrops:resistance_boost_feed_bag"
               )
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "genetics",
         "guide.industrialcrops.chapter.genetics",
         "industrialcrops:crop_analysis_device",
         -3696082,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.genetics.quality.title",
               "guide.industrialcrops.genetics.quality.body",
               "industrialcrops:crop_analysis_device",
               List.of("industrialcrops:industrial_potato", "industrialcrops:bagged_industrial_carrot")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.genetics.breeding.title",
               "guide.industrialcrops.genetics.breeding.body",
               null,
               List.of(
                  "industrialcrops:industrial_melon",
                  "industrialcrops:industrial_pumpkin",
                  "industrialcrops:bagged_industrial_melon_seeds",
                  "industrialcrops:bagged_industrial_pumpkin_seeds"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.genetics.fusion.title",
               "guide.industrialcrops.genetics.fusion.body",
               "industrialcrops:melon_modification_device",
               List.of("industrialcrops:fusion_melon", "industrialcrops:fusion_ingot", "industrialcrops:fusion_ingot_pickaxe")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.genetics.cultivator.title",
               "guide.industrialcrops.genetics.cultivator.body",
               "industrialcrops:black_nebula_carrot",
               List.of("industrialcrops:super_crop_cultivator", "industrialcrops:black_nebula_carrot", "industrialcrops:purple_dragon_carrot", "industrialcrops:omni_carrot", "industrialcrops:comet_essence", "industrialcrops:prism_pod", "minecraft:dragon_breath", "industrialcrops:neonbulb")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "exotic",
         "guide.industrialcrops.chapter.exotic",
         "industrialcrops:comet_sapling",
         -7709756,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.exotic.exotic_seeds.title",
               "guide.industrialcrops.exotic.exotic_seeds.body",
               "industrialcrops:bagged_prism_pod_seeds",
               List.of(
                  "industrialcrops:prism_pod_seeds",
                  "industrialcrops:embercoil_seeds",
                  "industrialcrops:starbloom_seeds",
                  "industrialcrops:neonbulb_seeds",
                  "industrialcrops:fluxstalk_seeds"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.exotic.strange_fertilizer.title",
               "guide.industrialcrops.exotic.strange_fertilizer.body",
               "industrialcrops:strange_fertilizer_bag",
               List.of("industrialcrops:comet_soil", "industrialcrops:comet_sapling", "minecraft:bone_meal")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.exotic.comet_tree.title",
               "guide.industrialcrops.exotic.comet_tree.body",
               "industrialcrops:comet_planks",
               List.of("industrialcrops:comet_log", "industrialcrops:comet_leaves", "industrialcrops:comet_essence", "industrialcrops:comet_sapling")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "logistics",
         "guide.industrialcrops.chapter.logistics",
         "industrialcrops:basic_control_device",
         -8615274,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.logistics.crop_storage.title",
               "guide.industrialcrops.logistics.crop_storage.body",
               "industrialcrops:basic_control_device",
               List.of("industrialcrops:basic_storage_component")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.logistics.pipes.title",
               "guide.industrialcrops.logistics.pipes.body",
               "industrialcrops:basic_pipe",
               List.of("industrialcrops:basic_output_pipe", "industrialcrops:basic_input_pipe", "industrialcrops:pipe_sorter")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.logistics.manipulator.title",
               "guide.industrialcrops.logistics.manipulator.body",
               "industrialcrops:basic_manipulation_device",
               List.of(
                  "industrialcrops:iron_device_casing",
                  "industrialcrops:reinforced_control_device",
                  "industrialcrops:reinforced_storage_component",
                  "industrialcrops:reinforced_pipe"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.logistics.advanced_manipulator.title",
               "guide.industrialcrops.logistics.advanced_manipulator.body",
               "industrialcrops:gold_upgrade_kit",
               List.of(
                  "industrialcrops:upgrade_kit_substrate",
                  "industrialcrops:advanced_manipulation_device",
                  "industrialcrops:advanced_pipe",
                  "industrialcrops:gold_fluid_pipe",
                  "industrialcrops:advanced_industrial_storage_device"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.logistics.components.title",
               "guide.industrialcrops.logistics.components.body",
               "industrialcrops:component_programming_device",
               List.of(
                  "industrialcrops:component_substrate",
                  "industrialcrops:industrial_storage_component_1",
                  "industrialcrops:speed_component_1",
                  "industrialcrops:automatic_component",
                  "industrialcrops:dimension_upgrade_component"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.logistics.network_storage.title",
               "guide.industrialcrops.logistics.network_storage.body",
               "industrialcrops:remote_access_device",
               List.of(
                  "industrialcrops:advanced_industrial_storage_device",
                  "industrialcrops:industrial_storage_component_4",
                  "industrialcrops:reinforced_control_device"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.logistics.automation.title",
               "guide.industrialcrops.logistics.automation.body",
               "industrialcrops:automatic_sowing_device",
               List.of("industrialcrops:explosive_potato_launcher", "industrialcrops:explosive_potato", "industrialcrops:guidance_component")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "power",
         "guide.industrialcrops.chapter.power",
         "industrialcrops:bio_energy_generation_device",
         -2513365,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.power.gold_casing.title",
               "guide.industrialcrops.power.gold_casing.body",
               "industrialcrops:component_substrate_integrated_gold_device_casing",
               List.of("industrialcrops:component_substrate", "minecraft:gold_ingot")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.power.bio_energy.title",
               "guide.industrialcrops.power.bio_energy.body",
               "industrialcrops:bio_energy_generation_device",
               List.of("industrialcrops:incinerator", "industrialcrops:industrial_wheat_block", "industrialcrops:industrial_carrot")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.power.grid.title",
               "guide.industrialcrops.power.grid.body",
               "industrialcrops:gold_energy_storage_cabinet",
               List.of("industrialcrops:basic_energy_cable", "industrialcrops:advanced_energy_cable", "industrialcrops:electric_furnace")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "crystal",
         "guide.industrialcrops.chapter.crystal",
         "industrialcrops:crystal_ingot",
         -10514486,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.plasma.title",
               "guide.industrialcrops.crystal.plasma.body",
               "industrialcrops:extraction_device",
               List.of(
                  "industrialcrops:plasma_fruit",
                  "industrialcrops:plasma_juice_bucket",
                  "industrialcrops:copper_fluid_storage_cabinet",
                  "industrialcrops:gold_fluid_pipe"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.energy_crystal.title",
               "guide.industrialcrops.crystal.energy_crystal.body",
               "industrialcrops:crystal_ingot",
               List.of("industrialcrops:energy_crystal", "minecraft:diamond", "industrialcrops:crystal_steel_casing")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.workbench.title",
               "guide.industrialcrops.crystal.workbench.body",
               "industrialcrops:crystal_steel_workbench",
               List.of("industrialcrops:comet_essence", "industrialcrops:crystal_steel_casing", "industrialcrops:copper_fluid_storage_cabinet")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.omni.title",
               "guide.industrialcrops.crystal.omni.body",
               "industrialcrops:omni_bucket",
               List.of("industrialcrops:omni_bucket", "industrialcrops:omni_potion", "industrialcrops:omni_carrot", "minecraft:cauldron")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.crystal_machines.title",
               "guide.industrialcrops.crystal.crystal_machines.body",
               "industrialcrops:wireless_logistics_transfer_device",
               List.of(
                  "industrialcrops:crafting_processing_device",
                  "industrialcrops:terrain_processing_device",
                  "industrialcrops:automatic_animal_breeder",
                  "industrialcrops:wireless_energy_transfer_device"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.launch_site.title",
               "guide.industrialcrops.crystal.launch_site.body",
               "industrialcrops:rocket_launch_site",
               List.of(
                  "industrialcrops:launch_pad",
                  "industrialcrops:launch_gantry_frame",
                  "industrialcrops:small_satellite_rocket",
                  "industrialcrops:medium_satellite_rocket",
                  "industrialcrops:large_satellite_rocket",
                  "industrialcrops:satellite_receiver"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.space_mining.title",
               "guide.industrialcrops.crystal.space_mining.body",
               "industrialcrops:satellite_control_device",
               List.of(
                  "industrialcrops:satellite_receiver",
                  "industrialcrops:item_network_management_terminal",
                  "industrialcrops:green_iron_ore",
                  "industrialcrops:inv_ore",
                  "industrialcrops:tycon_ore",
                  "industrialcrops:green_iron_ingot",
                  "industrialcrops:inv_ingot",
                  "industrialcrops:tycon_ingot"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.crystal.reaction.title",
               "guide.industrialcrops.crystal.reaction.body",
               "industrialcrops:crystal_steel_upgrade_kit",
               List.of("industrialcrops:bio_energy_reaction_device", "industrialcrops:bio_energy_generation_device")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "matter",
         "guide.industrialcrops.chapter.matter",
         "industrialcrops:matter_digitization_device",
         -12673382,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.matter.digitize.title",
               "guide.industrialcrops.matter.digitize.body",
               "industrialcrops:matter_digitization_device",
               List.of("industrialcrops:item_network_management_terminal")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.matter.copy_rebuild.title",
               "guide.industrialcrops.matter.copy_rebuild.body",
               "industrialcrops:data_copying_device",
               List.of("industrialcrops:matter_reconstruction_device", "industrialcrops:automatic_component", "minecraft:diamond")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.matter.forest.title",
               "guide.industrialcrops.matter.forest.body",
               "industrialcrops:digital_miniature_forest",
               List.of("industrialcrops:speed_component_1", "industrialcrops:speed_component_4", "minecraft:oak_sapling")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.matter.slimes.title",
               "guide.industrialcrops.matter.slimes.body",
               "industrialcrops:slime_conversion_device",
               List.of("industrialcrops:slime_cultivation_device", "minecraft:slime_ball", "minecraft:iron_ore", "industrialcrops:component_substrate")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "carrote",
         "guide.industrialcrops.chapter.carrote",
         "carrote:carrote",
         -3194796,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.carrote.carrote.title",
               "guide.industrialcrops.carrote.carrote.body",
               null,
               List.of("carrote:carrote", "industrialcrops:industrial_carrot", "industrialcrops:matter_digitization_device")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.carrote.forge.title",
               "guide.industrialcrops.carrote.forge.body",
               null,
               List.of("carrote:paradox_steel_forge", "carrote:paradox_steel_ingot", "minecraft:netherite_ingot", "minecraft:lava_bucket")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.carrote.paradox_devices.title",
               "guide.industrialcrops.carrote.paradox_devices.body",
               "carrote:paradox_steel_device_casing",
               List.of("carrote:material_hardening_device", "carrote:universal_replication_device", "carrote:mimic_block")
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.carrote.stable_matter.title",
               "guide.industrialcrops.carrote.stable_matter.body",
               "industrialcrops:fusion_ingot",
               List.of("carrote:stable_matter_ingot", "carrote:paradox_steel_ingot", "industrialcrops:fusion_melon")
            )
         )
      ),
      new FieldGuideContent.Chapter(
         "ap", "guide.industrialcrops.chapter.ap", "industrialcrops:ap_agricultural_complex", -1529542,
         List.of(
            new FieldGuideContent.Page("guide.industrialcrops.ap.sizes.title", "guide.industrialcrops.ap.sizes.body", "industrialcrops:ap_expansion_component_1", List.of("industrialcrops:ap_expansion_component_1", "industrialcrops:ap_expansion_component_2", "industrialcrops:ap_expansion_component_3")),
            new FieldGuideContent.Page("guide.industrialcrops.ap.farm.title", "guide.industrialcrops.ap.farm.body", "industrialcrops:ap_agricultural_complex", List.of("industrialcrops:ap_steel_casing", "industrialcrops:ap_steel_frame", "minecraft:glass", "minecraft:water_bucket", "minecraft:composter")),
            new FieldGuideContent.Page("guide.industrialcrops.ap.ranch.title", "guide.industrialcrops.ap.ranch.body", "industrialcrops:ap_livestock_ranch", List.of("minecraft:oak_fence", "minecraft:oak_planks", "minecraft:oak_slab", "minecraft:hay_block", "minecraft:water_bucket")),
            new FieldGuideContent.Page("guide.industrialcrops.ap.projection.title", "guide.industrialcrops.ap.projection.body", null, List.of("industrialcrops:field_guide", "industrialcrops:dimension_upgrade_component"))
         )
      ),
      new FieldGuideContent.Chapter(
         "curios",
         "guide.industrialcrops.chapter.curios",
         "carrote_curios:helmet_carrote",
         -2065624,
         List.of(
            new FieldGuideContent.Page(
               "guide.industrialcrops.curios.finding.title",
               "guide.industrialcrops.curios.finding.body",
               null,
               List.of(
                  "carrote_curios:strength_carrote",
                  "carrote_curios:flight_carrote",
                  "carrote_curios:steel_carrote",
                  "carrote_curios:peace_carrote",
                  "carrote_curios:greed_carrote",
                  "carrote_curios:substitute_carrote"
               )
            ),
            new FieldGuideContent.Page(
               "guide.industrialcrops.curios.equipping.title",
               "guide.industrialcrops.curios.equipping.body",
               null,
               List.of("carrote_curios:helmet_carrote", "carrote_curios:ultimate_carrote")
            )
         )
      )
   );

   private FieldGuideContent() {
   }

   static record Chapter(String id, String titleKey, String icon, int accent, List<FieldGuideContent.Page> pages) {
   }

   static record Page(String titleKey, String bodyKey, String recipe, List<String> items) {
   }
}
