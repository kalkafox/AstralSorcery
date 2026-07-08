# NeoForge 1.21.1 port

This branch is being migrated from Forge 1.16.5 to NeoForge 1.21.1.

ObserverLib is no longer vendored: its ported sources were extracted to the
ObserverLib repo (`D:\git\ObserverLib`, branch `port/1.21.1`) and are consumed
from mavenLocal as `hellfirepvp.observerlib:observerlib` — publish there with
`gradlew publishToMavenLocal` after changes. References below to the "vendored"
copy are historical.

## Completed baseline

- Replaced ForgeGradle 4 with NeoForge ModDevGradle.
- Added a Gradle 9.2.1 wrapper and Java 21 toolchain.
- Added generated `neoforge.mods.toml` metadata.
- Updated the resource pack format and mixin Java compatibility level.
- Migrated Forge package names to NeoForge package names.
- Composed the original MCP mappings with Mojang mappings and remapped the
  majority of Minecraft classes to their 1.21.1 names.
- Migrated the mod entry point to constructor-injected `IEventBus` and
  `ModContainer`.
- Migrated config specs and basic text components to the 1.21.1 APIs.
- Added a local registry-name compatibility helper for old
  `getRegistryName`/`IForgeRegistryEntry`-style code paths.
- Migrated common packet/NBT registry serialization away from Forge's removed
  `RegistryManager` and updated the cached event bus wrapper for NeoForge's
  current bus API.

The mapping helper is in `tools/port/Remap-McpClasses.ps1`. It consumes local
mapping artifacts under `build/port-mappings`; those artifacts are generated
for porting only and are not committed.

## Current compile boundary

`gradlew compileJava` reaches the source compiler and currently stops at these
large API removals:

1. Forge's old `ForgeRegistryEntry`, `IForgeRegistry`, and registry lookup
   helpers that still remain in datagen/recipe builder code.
2. Removed common-side 1.16 APIs: enchantment categories, tool types,
   `LazyOptional` capabilities, and old network context classes.
3. ObserverLib, which has no 1.21.1 artifact and must be ported or replaced.
4. Systems built on top of those foundations, followed by networking,
   capabilities/data attachments, world generation, rendering, and optional
   integrations.

The next implementation milestone is replacing `InternalRegistryPrimer` and
`PrimerEventHandler` with NeoForge `DeferredRegister`, `DeferredHolder`, and
custom registry keys, while preserving the existing static fields used
throughout the mod.
