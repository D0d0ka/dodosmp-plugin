# DodoSMP Plugin — Agent Instructions

Server-side Fabric mod for Minecraft 1.21.11. **Mojang official mappings** (not Yarn).

## Build

**Requires Java 25** — Fabric Loom 1.18-SNAPSHOT mandates JVM 25+, even though the compilation target is Java 21.

```bash
# Install Java 25 (Temurin recommended), then:
./gradlew build
# Output: build/libs/dodosmp-plugin-1.0.0.jar
```

Other useful tasks:
```bash
./gradlew compileJava   # compile only, faster feedback
./gradlew runServer     # start a test server (if configured)
```

If the build fails with `Dependency requires at least JVM runtime version 25`, the active JVM is not 25. Set `JAVA_HOME` or use a Gradle toolchain.

## Project Overview

| File | Purpose |
|---|---|
| [gradle.properties](gradle.properties) | Minecraft/Fabric versions |
| [src/main/resources/fabric.mod.json](src/main/resources/fabric.mod.json) | Mod metadata, `"environment": "server"` |
| [src/main/resources/dodosmp-plugin.mixins.json](src/main/resources/dodosmp-plugin.mixins.json) | Mixin list (add new mixins here) |
| [src/main/java/dodo/dodosmpplugin/DodosmpPlugin.java](src/main/java/dodo/dodosmpplugin/DodosmpPlugin.java) | Entry point, FEATURES list |
| [src/main/java/dodo/dodosmpplugin/DodoFeature.java](src/main/java/dodo/dodosmpplugin/DodoFeature.java) | Feature interface |
| [src/main/java/dodo/dodosmpplugin/DodoConfig.java](src/main/java/dodo/dodosmpplugin/DodoConfig.java) | JSON config loader/saver |

## Adding a New Feature

1. Create `src/main/java/dodo/dodosmpplugin/feature/MyFeature.java` implementing `DodoFeature`
2. If it has a crafting recipe, add `src/main/resources/data/dodosmp-plugin/recipe/my_feature.json`
3. Add to `shouldKeepRecipe()` switch in `RecipeManagerMixin.java` (for config toggle support)
4. Register in `DodosmpPlugin.java` FEATURES list — one line

Recipe toggle takes effect on `/reload`; potion/brewing changes require a server restart.

## Key API Facts (Mojang Mappings, 1.21.11)

These differ significantly from older Fabric tutorials (Yarn-mapped):

| What | Mojang 1.21.11 name |
|---|---|
| Identifier | `net.minecraft.resources.Identifier` (was `ResourceLocation`) |
| Effect fields | `MobEffects.NAUSEA` (not `CONFUSION`) → `Holder<MobEffect>` |
| Potion constructor | `new Potion(String name, MobEffectInstance...)` |
| Brewing API | `FabricBrewingRecipeRegistryBuilder` in `net.fabricmc.fabric.api.registry` (module `fabric-content-registries-v0`; `fabric-brewing-recipe-registry-v1` was removed); `registerRecipes(Ingredient, Holder<Potion>)` — wraps `Item` with `Ingredient.of(item)` |
| ResourceKey method | `resourceKey.identifier()` returns the `Identifier` — NOT `.location()` (that method doesn't exist in 1.21.11) |
| Command source | `CommandSourceStack` (not `ServerCommandSource`) |
| Permission check | `new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)` — no `hasPermission(int)` |
| Recipe reload | `RecipeManager.apply(RecipeMap, ...)` — `RecipeMap` (not `Map<ResourceLocation, JsonElement>`) |
| Recipe folder | `data/<modid>/recipe/` (not `recipes/`); ingredient format is a plain string `"minecraft:item_id"` (not `{"item":"..."}`) |

## Config

Runtime config at `config/dodosmp.json`:
```json
{ "features": { "vodka": true, "cheaper_golden_apple": true, "craftable_god_apple": true } }
```
Missing keys default to `true`. Admin command: `/dodosmpplugin list|enable|disable <id>` (op level 2+).

## Architecture Notes

- **`"environment": "server"`** in `fabric.mod.json` — vanilla clients can join without the mod.
- Crafting recipes are JSON files loaded by the data pack system; `RecipeManagerMixin` removes disabled ones after each `apply()` (supports `/reload`).
- Brewing/potion registration happens via `PotionBrewingMixin` (injected into `PotionBrewing.mix/hasMix/isIngredient`) — **no custom Potion registry entry**, so vanilla clients can connect. `BuiltInRegistries.POTION` entries are synced to clients and will block vanilla connections.
