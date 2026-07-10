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

## Member-name remap (done)

`tools/port/Remap-McpMembers.ps1` renames MCP method/field names and leftover
class simple names to Mojang official names across `src/main/java`. It joins
`joined.tsrg` (obf -> SRG), the MCP snapshot csvs (SRG -> MCP name), and the
1.16.5 proguard mappings (official -> obf) into name-level MCP -> official
rename maps, then applies them token-wise, skipping strings, comments,
import/package statements, and package-qualified name chains.

Because the map is name-level (not receiver-aware), renames are only applied
when (near-)unambiguous across the mappings; `tools/port/member-overrides.csv`
carries curated picks and identity locks for names that collide with JDK,
library, or already-ported 1.21 identifiers. New collisions found while
compiling should be added there — the script can then be re-run cleanly from a
committed tree (`git checkout -- src/main/java` first; note that any manual
post-remap fixes in the tree would be reverted and must be re-applied).
`build/port-mappings/member-remap-report.txt` lists ambiguous and
low-confidence names for triage.

This pass (plus a small set of hand fixes for JDK/library call sites the
rename clobbered) took `compileJava` from ~17.5k unique errors in 823 files to
~11.5k in 717, with no file that compiled before the pass failing after it.

## Registry layer (done)

The old `InternalRegistryPrimer`/`PrimerEventHandler` machinery has been
replaced with NeoForge `DeferredRegister`s, all owned by
`common.registry.internal.AstralRegistries`:

- One `DeferredRegister` per vanilla/NeoForge registry the mod publishes to
  (blocks, items, fluids, block entities, entities, mob effects, menus,
  sounds, recipe types/serializers, creative tabs, command argument types,
  loot function types, fluid types, entity data serializers, global loot
  modifier serializers) plus the vendored ObserverLib provider registry.
- The mod's twelve custom registries are created through
  `DeferredRegister.makeRegistry`; the `LegacyRegistry` views in
  `RegistriesAS` are assigned from there (the constellations `onAdd`
  callback is preserved).
- Registry values are still constructed eagerly during mod construction
  (`CommonProxy.buildRegistryContent()`, invoked from `attachLifecycle`
  before `AstralRegistries.subscribe(modEventBus)`), so the concrete-typed
  static fields in the `common.lib` classes keep working; the deferred
  registers just hand those instances out when their RegisterEvent fires.
- All registrations must use the `astralsorcery` namespace
  (`AstralRegistries.register` enforces this).

Registry-related follow-ups tracked for later milestones:

1. **Enchantments** are a datapack registry in 1.21;
   `RegistryEnchantments` is now a `BootstrapContext<Enchantment>` bootstrap
   that must be wired into a `DatapackBuiltinEntriesProvider` when datagen is
   ported (until then the two enchantment JSONs could also be committed as
   plain data files).
2. **World generation** (`RegistryWorldGeneration`) is untouched 1.16 code;
   configured/placed features and structures are datapack-driven in 1.21 and
   biome injection happens via biome modifier JSONs. Whole subsystem needs
   its own port.
3. **Loot** (`RegistryLoot`) is rewritten against MapCodec-based
   registration, but the referenced `CODEC` constants on the loot modifier /
   loot function classes still need to be implemented when that subsystem is
   ported.

## Sounds and block-entity types (done)

- `SoundEvent`s are created through `SoundEvent.createVariableRangeEvent`;
  `CategorizedSoundEvent` no longer subclasses `SoundEvent` (its constructor
  is private in 1.21) and instead wraps the registered event plus its default
  `SoundSource`. `SoundHelper` gained explicit `CategorizedSoundEvent`
  overloads in place of the old instanceof sniffing, and the client sound
  instances (`PositionedLoopSound`, `FadeSound`, `FadeLoopSound`) use the
  1.21 `SimpleSoundInstance` constructor.
- `RegistryTileEntities` builds `BlockEntityType`s with
  `BlockEntityType.Builder.of((pos, state) -> ...)`, currently instantiating
  tiles reflectively via a `(BlockPos, BlockState)` constructor lookup — the
  tile classes themselves still need those constructors when the tile
  subsystem is ported. Renderer binding moved from the removed
  `ClientRegistry.bindTileEntityRenderer` to `BlockEntityRenderers.register`;
  `CustomTileEntityRenderer` now implements the `BlockEntityRenderer`
  interface and takes a `BlockEntityRendererProvider.Context`.

## Optional-mod integrations (excluded from the build)

`hellfirepvp/astralsorcery/common/integration/**` (JEI, CraftTweaker,
Curios, Botania) is excluded from `sourceSets.main` in `build.gradle` until
the mod itself compiles and their 1.21 APIs are brought back. The call
sites are stubbed: `CommonProxy` no longer attaches the CraftTweaker/Curios
hooks, `AmuletEnchantmentHelper.getWornAmulet` returns `null` (amulet
lookup is Curios-driven), and `ItemUtils`' Botania flower special-casing is
commented out. Search for "1.21 port:" comments to find the stubs when
re-enabling.

## Mechanical API-residue sweep (done)

A scripted sweep converted the mechanical 1.16 -> 1.21 residue across
`src/main/java`: `new ResourceLocation(ns, path)` ->
`ResourceLocation.fromNamespaceAndPath` (one-arg -> `parse`),
`Vector3f.XP`-style rotation constants -> `com.mojang.math.Axis` (JOML
`Vector3f` for the remaining vector uses), `LivingHurtEvent` ->
`LivingIncomingDamageEvent`, `ForgeRegistries.*` -> `BuiltInRegistries.*`
with `.getValue(` -> `.get(`, and log-driven per-line fixes for Entity
accessors (`.level` -> `.level()`, `xRot`/`yRot` -> getters/setters,
`getPosition()` -> `position()`, `sendMessage(c, uuid)` ->
`sendSystemMessage(c)`, `getRegistryName()` -> `RegistryHelper.getKey`,
BlockPos `.add` -> `.offset`, missing `ChatFormatting` imports).
`ReaderVanillaAttribute` now takes a vanilla `Holder<Attribute>`
(`Attributes.*` / `NeoForgeMod.SWIM_SPEED`; the old `ForgeMod.REACH_DISTANCE`
maps to vanilla `Attributes.BLOCK_INTERACTION_RANGE`).

Caveat: `BuiltInRegistries` defaulted registries return their default entry
instead of `null` for unknown keys — call sites that relied on Forge's
null-returning `getValue` may need `getOptional`/containsKey checks when
those subsystems are runtime-tested.

## Networking (done)

The payload layer (`PacketChannel` envelope over `CustomPacketPayload` +
`StreamCodec`, `PacketContext` over `IPayloadContext`, `SimpleSendChannel`
over `PacketDistributor`) was already ported; this pass finished the packet
classes: handler signatures now take `PacketContext` instead of
`NetworkEvent.Context`, entity spawn-packet overrides
(`NetworkHooks.getEntitySpawningPacket`) are deleted (the 1.21 default
handles them), `NetworkHooks.openGui` -> `ServerPlayer#openMenu`,
`IContainerFactory` comes from `net.neoforged.neoforge.network`, and
`LogicalSidedProvider.INSTANCE` (removed) -> `ServerLifecycleHooks.getCurrentServer()`.
`PktSyncStepAssist` writes the `Attributes.STEP_HEIGHT` base value now —
worth revisiting; step assist may not need a sync packet at all since
attributes auto-sync.

## Capabilities and data attachments (done)

- The chunk-fluid capability is a NeoForge **data attachment**
  (`AstralRegistries.ATTACHMENT_TYPES`, registered in
  `RegistryCapabilities.init()` during `buildRegistryContent`); readers use
  `chunk.getData(CapabilitiesAS.CHUNK_FLUID)`. `ChunkFluidEntry` implements
  the 1.21 `INBTSerializable` signatures (with `HolderLookup.Provider`).
- Tile item/fluid handlers: the `getCapability` overrides on the six
  exposing tiles (Well, Chalice, Fountain, Infuser, RitualPedestal,
  SpectralRelay) became plain `getExposedItemHandler`/`getExposedFluidHandler`
  accessors, registered against `Capabilities.ItemHandler.BLOCK` /
  `FluidHandler.BLOCK` in `RegistryCapabilities.attachCapabilities`
  (mod-bus `RegisterCapabilitiesEvent`, wired in `CommonProxy.attachLifecycle`).
- Consumers use `level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side)`
  and `player.getCapability(Capabilities.ItemHandler.ENTITY)`
  (`ItemUtils.getPlayerInventoryHandler`). `LazyOptional` is gone.
- The liquid starlight bucket registers its `FluidBucketWrapper` in
  `attachCapabilities` — NeoForge only auto-registers exact `BucketItem`
  instances, not subclasses.

## Items and tags (done)

- Tags are `TagKey<T>` everywhere (`TagsAS`); iterate values via
  `common.util.TagHelper` (registry `getTagOrEmpty`), test membership with
  `state.is`/`stack.is`. String/JSON tag lookups now `TagKey.create` —
  unlike the old `getAllTags().get(...)` this cannot detect an unknown tag,
  so config entries referencing nonexistent tags silently match nothing.
- `Item.Properties`: `stacksTo`/`durability`/`craftRemainder`; `group()` is
  gone — the three creative tabs are registered via builders in
  `CommonProxy` (main tab currently lists every registered item;
  papers/crystals tab contents still need re-curation, and per-item
  `fillItemCategory` overrides are dead code to clean up).
- `ArmorMaterial` is a registered record (`AstralRegistries.ARMOR_MATERIALS`);
  `ItemMantle` passes `Holder.direct` because items are built before
  registry events fire, plus explicit `durability(486)`.
- `ToolType` is gone; harvest logic is tag-driven
  (`BlockTags.MINEABLE_WITH_*`, `ItemTags.PICKAXES`). The marble/ore block
  tag files (`mineable/pickaxe` etc.) still need to be added as data.
- Custom `Rarity.create` values (`RARITY_CELESTIAL` etc.) still use the
  removed enum-extension API — open item-cluster follow-up.

## Current compile boundary

The registration mechanism itself compiles clean. After the member-name
remap, `gradlew compileJava` (configured with `-Xmaxerrs 10000`) stops at
structural 1.16 -> 1.21 API changes rather than naming: `Recipe<Container>`
vs the new `RecipeInput` bound, creative tab construction, fluid
attributes/`ForgeFlowingFluid`, the entity/block model class rewrite
(`addBox`, `setRotationPoint`, `addChild`), vertex formats and
`VertexConsumer` (`vertex` -> `addVertex`), and the systems built on those
(world generation, rendering, items/creative tabs, datagen). Roughly 4,700
errors across ~630 files remain at this point.
