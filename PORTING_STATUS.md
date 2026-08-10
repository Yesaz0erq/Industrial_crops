# NeoForge 1.21.1 Port Status

This branch targets Minecraft 1.21.1 and NeoForge 21.1.227.

Current status:

- Gradle project scaffolded for Minecraft 1.21.1 and NeoForge 21.1.227.
- Version normalized to `1.0-K1`.
- Mod metadata moved from `fabric.mod.json` to `META-INF/neoforge.mods.toml`.
- Resource assets, recipes, loot tables, block states, models, and language files were copied from the Fabric branch.
- Original Fabric/Yarn Java sources were moved to `fabric-yarn-reference/` so they remain available as porting reference without participating in compilation.
- A NeoForge/Mojang-mapped registration layer now registers the core blocks, items, industrial crops, basic_pipes, machines, and creative tab.
- The basic crop conversion device, basic crop storage array, and reinforced industrial storage array now have NeoForge block entities.
- The crop conversion device has server-side progress logic for potato/carrot blocks plus redstone bonemeal.
- The two storage arrays save block entity data, and their block items can carry storage data when broken and placed again.
- The crop conversion device now has a NeoForge menu and client screen.
- The basic control device now opens a NeoForge menu and client screen for the adjacent basic crop storage array.
- The reinforced control device now opens a NeoForge large-storage menu and client screen with page controls for connected reinforced storage arrays.
- The basic manipulator now opens a NeoForge menu and client screen and can craft its manipulator recipe list from player inventory plus an adjacent basic crop storage array.
- Basic and reinforced basic_pipes now have NeoForge block entities and server-side transfer logic using item handler capabilities.
- The crop conversion device, basic crop storage array, and reinforced industrial storage array expose NeoForge item handler capabilities for hopper/modded basic_basic_pipe style automation.
- JEI 1.21.1 NeoForge optional integration is present for the basic manipulator recipe list.
- Chinese and English language files are present.
- `gradlew build` succeeds with JDK 21 and produces `industrial-crops-neoforge-1.0-K1.jar`.
- Industrial crop textures were regenerated in a consistent industrial pixel-art style via `tools/gen_textures.py` (deterministic; safe to re-run). Covers all five crops' growth-stage textures (stages 0-3), harvest item icons, compressed crop block faces, plus `carrot_mechanical_core` and `slime_converter_front`.
- Industrial crop blockstates now map ages 0-7 to four distinct growth-stage models instead of showing the mature model at every age.
- `slime_converter_front.png` (with baked-in translucent hatch glass for the block entity renderer) is now a normal source texture; the `generateIncubatorTexture` task was removed from `build.gradle`.
- The recipe file `carrot_mechanical_core.json` was renamed to `redstone_bonemeal.json` to match its actual output.
- Pre-regeneration textures are backed up in `backup_textures_20260707/`.

Maintenance notes:

- NeoForge datagen providers remain a future maintenance task; the current hand-authored data files are kept compatible with the 1.21.1 runtime.
- `carrot_mechanical_core` is a legacy asset name retained for reference and is not a registered item; the active item is `redstone_bonemeal`.
- All registered energy, digital-processing, forestry, incinerator, forge, and anomaly items now have explicit 1.21.1 item model definitions.
- Machine inventories defensively migrate legacy slot counts when loading saved block entities.
- Matter-machine and digital-forest terminal lookups are memoized for the current server tick only; network topology is still re-evaluated on the next tick.
