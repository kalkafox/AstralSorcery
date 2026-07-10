# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Astral Sorcery is a Minecraft magic mod (mod id `astralsorcery`) originally built for Forge 1.16.5.
The `port/1.21.1` branch (current branch) is an in-progress migration to **NeoForge 1.21.1** using
NeoForge ModDevGradle, Java 21, and Parchment mappings. `1.16-indev` is the main/upstream branch for
the original 1.16.5 codebase — do not assume 1.16-era Forge APIs apply on this branch.

Read `PORTING.md` before doing porting work — it tracks what's been migrated, what's still on old
1.16 APIs, and the exact current compile boundary (structural API changes still to fix: SoundEvent
construction, TileEntityType.Builder, Recipe<Container>/RecipeInput, creative tabs, fluid attributes,
capabilities, networking, model/vertex format changes, world generation, etc.).

## Build

`JAVA_HOME` is not set by default in this environment and `java` is not on PATH. Set it to a JDK 21
before invoking Gradle:

```powershell
$env:JAVA_HOME = "C:\Users\kalka\.jdks\ms-21.0.11"; .\gradlew compileJava --console=plain
```

- `build.gradle` sets `-Xmaxerrs 10000` on `JavaCompile` (porting aid to see every error, not just
  javac's default first 100). Compile output during the port can be huge — redirect to a file and
  strip ANSI color codes before grepping/reading it.
- `./gradlew runClient` / `runServer` / `runGameTestServer` — NeoForge run configs (see `build.gradle`
  `neoForge.runs`).
- `./gradlew runData` — datagen, outputs to `src/generated/resources/`, existing input from
  `src/main/resources/`.
- No test suite currently exists in this repo.

## Architecture

- Root package: `hellfirepvp.astralsorcery`. `AstralSorcery.java` is the `@Mod` entry point
  (constructor-injected `IEventBus`/`ModContainer`, NeoForge style). It delegates to a `CommonProxy`
  (or `ClientProxy` on `Dist.CLIENT`), which does `initialize()` -> `attachLifecycle(modEventBus)` ->
  `attachEventHandlers(NeoForge.EVENT_BUS)`.
- `common/` vs `client/` mirrors the usual Forge/NeoForge server-safe vs client-only split; keep
  client-only code (rendering, screens, sky rendering) out of `common/`.
- **Registries**: all registration goes through `common.registry.internal.AstralRegistries`, which
  owns one NeoForge `DeferredRegister` per vanilla/NeoForge registry (blocks, items, fluids, block
  entities, entities, mob effects, menus, sounds, recipe types/serializers, creative tabs, command
  argument types, loot function types, fluid types, entity data serializers) plus the mod's own
  custom registries (constellations, perks, crystal properties, structure types, etc.) created via
  `DeferredRegister.makeRegistry`. Registrations must use the `astralsorcery` namespace —
  `AstralRegistries.register` enforces this. Concrete registry content classes live in
  `common.registry` (e.g. `RegistryBlocks`, `RegistryItems`, `RegistryConstellations`); values are
  still constructed eagerly in `CommonProxy.buildRegistryContent()` before
  `AstralRegistries.subscribe(modEventBus)` runs, so the static fields on `common.lib.*` classes keep
  their concrete types. `common.lib.RegistriesAS` exposes `LegacyRegistry` views over these.
- ObserverLib (a HellFirePvP library this mod depends on) is consumed from mavenLocal as
  `hellfirepvp.observerlib:observerlib` (version via `observerlib_version` in the local, gitignored
  `gradle.properties`). Its 1.21.1 port lives in `D:\git\ObserverLib` on branch `port/1.21.1`;
  publish changes with `gradlew publishToMavenLocal` there.
- `tools/port/` holds one-off PowerShell porting aids, not part of the mod runtime:
  - `Remap-McpClasses.ps1` — remaps MCP-mapped 1.16.5 class names to Mojang's 1.21.1 official names,
    consuming mapping artifacts generated under `build/port-mappings` (not committed).
  - `Remap-McpMembers.ps1` — renames MCP method/field names and leftover class simple names to
    Mojang official names across `src/main/java`, by joining `joined.tsrg`, MCP snapshot CSVs, and
    1.16.5 proguard mappings into name-level rename maps. `member-overrides.csv` carries curated
    picks/identity locks for names that collide with JDK/library/already-ported identifiers — add new
    collisions there when found, then re-run from a clean tree (`git checkout -- src/main/java`
    first; this reverts any manual post-remap fixes, which must be reapplied). Its report
    (`build/port-mappings/member-remap-report.txt`) lists ambiguous/low-confidence renames to triage.
