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

## Rendering infrastructure (done)

The low-level vertex/buffer/render-type layer is on 1.21 APIs:

- `RenderingUtils.draw(mode, format, fn)` keeps its immediate-mode shape but
  now sets a core shader chosen by vertex format (`shaderFor`), begins via
  `Tesselator.getInstance().begin`, and uploads the built `MeshData` with
  `BufferUploader.drawWithShader` (or `RenderType.draw`). `refreshDrawing`
  returns a fresh `BufferBuilder` — callers still ignoring the return value
  compile but must be fixed when their files are ported.
- `BufferContext` no longer extends `BufferBuilder`; it wraps a persistent
  `ByteBufferBuilder` and a per-`begin()` `BufferBuilder`, implementing
  `VertexConsumer`. Manual `sortVertexData` is gone — translucency sorting
  must come from `sortOnUpload`/`MeshData.sortQuads` (runtime caveat for
  `BatchRenderContext`, which previously distance-sorted quads per frame).
- `RenderStateBuilder` wraps `RenderType.CompositeState.CompositeStateBuilder`.
  Alpha-test, shade-model, and diffuse-lighting builder calls are no-ops
  (removed fixed-function state; shaders handle these — alpha thresholds are
  a runtime caveat). `RegistryRenderTypes.createType` injects a
  shader-by-format state when none is set; `POSITION_COLOR_TEX` usages became
  vanilla `POSITION_TEX_COLOR`, `ENTITY` -> `NEW_ENTITY`, and the custom
  `POSITION_COLOR_TEX_NORMAL` aliases vanilla `POSITION_TEX_COLOR_NORMAL`
  (its normal attribute is unused by the chosen shader). The depth-projection
  type approximates the old GL texture-matrix/texgen trick with
  `RenderSystem.setTextureMatrix` only.
- `BatchedVertexList` uploads `MeshData` into a
  `VertexBuffer(Usage.STATIC)` and renders via `drawWithShader`.
- Resource layer: `AssetLibrary`/`AssetPreLoader` implement
  `ResourceManagerReloadListener` (selective `IResourceType` reloads are
  gone). `TextureManager.register/release` replace `loadTexture`/
  `deleteTexture`; binds go through `RenderSystem.setShaderTexture`;
  `NativeImage.Format`/`upload` replace `PixelFormat`/`uploadTextureSub`.
- Mechanical sweep landed repo-wide: `VertexConsumer` chains ending in
  `endVertex()` renamed (`vertex`->`addVertex`, `color`->`setColor`,
  `tex`/`uv`->`setUv`, packed `uv2`/`lightmap`->`setLight`, two-arg ->
  `setUv2`, `overlay`->`setOverlay`, `normal`->`setNormal`, `endVertex`
  dropped); `GL11.GL_*` draw modes -> `VertexFormat.Mode.*` (plain GL lines
  map to `DEBUG_LINES`/`DEBUG_LINE_STRIP`); removed `RenderSystem`
  fixed-function toggles deleted; `color4f` -> `setShaderColor`;
  `MultiBufferSource.Impl/getImpl/finish` -> `BufferSource/immediate/endBatch`;
  `getStringPropertyWidth` -> `Font.width`; `LOCATION_BLOCKS_TEXTURE` ->
  `LOCATION_BLOCKS`.
- Item/block model rendering in `RenderingUtils` uses `ClientHooks
  .handleCameraTransforms`, `ItemDisplayContext`, `IClientItemExtensions`
  (custom renderer, font), NeoForge `getRenderPasses`, `putBulkData`,
  `renderBatched`/`tesselateBlock` with `RandomSource`/`ModelData`, and
  `IClientFluidTypeExtensions` for fluid still textures. `ItemEntity`
  age/bob offset are set via `ReflectionHelper` (fields are private now).

## Model classes (done)

`client/model/builtin` and `client/model/armor` are on the 1.21 baked-part
API: each model builds a static `LayerDefinition`
(`MeshDefinition`/`CubeListBuilder`/`PartPose`) and bakes it in its
constructor (`bakeRoot()` + `getChild`) — no `RegisterLayerDefinitions`
event needed since these are hand-rendered `Model`s, not entity layers.
`ModelPart.render` takes a packed ARGB color now; `CustomModel` keeps the
old float-color `render(...)` entry point and bridges via
`FastColor.ARGB32.colorFromFloat` (`packColor` helper). `ModelArmorMantle`
can no longer swap `HumanoidModel`'s final body/arm/head fields for
replacement parts; the humanoid skeleton is built with empty (cube-less)
parts at vanilla pivots and the mantle geometry attached as children, with
visibility flags handled in `renderToBuffer`. `ModelRefractionTable`'s 20
frame parts are grouped under one `frame` parent part.

## Entity renderers and sky (done)

- Entity renderers take `EntityRendererProvider.Context`; the `IRenderFactory`
  inner classes are gone and registration happens in
  `EntityRenderersEvent.RegisterRenderers` (`RegistryEntities.initClient(event)`,
  wired from `ClientProxy.onRegisterRenderers` on the mod bus, along with the
  existing `BlockEntityRenderers.register` calls).
- The starry player layers are added in `EntityRenderersEvent.AddLayers`
  (iterating `PlayerSkin.Model`), replacing the old skin-map mutation in
  client setup. `StarryLayerRenderer` builds its `PlayerModel`s from baked
  `LayerDefinition`s, passes the `ModelManager` to the 1.21
  `HumanoidArmorLayer` constructor, and renders with a packed ARGB color.
- **Sky rendering**: `ISkyRenderHandler`/`setSkyRenderHandler` no longer exist.
  `ChainingSkyRenderer` was deleted; `SkyRenderEventHandler.onRender` now
  listens to `RenderLevelStageEvent` at `Stage.AFTER_SKY` and draws the astral
  sky *on top of* the vanilla sky (runtime caveat: vanilla sun/moon/stars are
  not suppressed — full replacement needs a custom `DimensionSpecialEffects`
  registration). Fog tint uses `ViewportEvent.ComputeFogColor`.
  `AstralSkyRenderer` no longer implements a handler interface; removed
  `RenderSystem.enable/disableFog` (shader-driven now), `getSunriseColor`,
  `getRainLevel`, `getSunAngle`, `getEyePosition`, and texture binds via
  `RenderSystem.setShaderTexture`. `AstralSkyRendererSetup` no longer calls
  `begin` (the `BatchedVertexList` provides an already-begun builder).
- `ClientProxy` also lost the selective-reload language check (perk text
  caches now clear on every resource reload).
- Tile renderers: `ItemRenderer.renderStatic` (was `render`),
  `ItemBlockRenderTypes.getRenderType(state, false)` (was `func_239221_b_`),
  fluid tint via `IClientFluidTypeExtensions.getTintColor`.

## Accessor and package residue sweep (done)

- Removed stale `net.minecraft.util.text.*` imports and added explicit 1.21
  imports for chat, interaction, block-state, position, and direction types.
- Mechanical accessors now use the 1.21 names: equipment `getItemBySlot`,
  player-list `getPlayer(UUID)`, entity `onGround`/`level`, player-event
  `getEntity`, `ResourceKey.location`, dragon phase `getPhase`, and compound
  tag `getUUID`/`hasUUID`.
- Item/block contexts use `getClickedPos`, `getClickedFace`, and
  `getItemInHand`; dispenser `BlockSource` uses `level`, `pos`, and
  `blockEntity`.
- `FluidStack.getType()` call sites now use `getFluid()`. Old fluid-attribute
  and component/NBT APIs remain for their structural subsystem ports.

## Current compile boundary

The registration mechanism itself compiles clean. After the member-name
remap and the rendering-infrastructure port, `gradlew compileJava`
(configured with `-Xmaxerrs 10000`) stops at structural 1.16 -> 1.21 API
changes rather than naming: the entity/block model class rewrite to
`LayerDefinition` (`addBox`, `setRotationPoint`, `addChild`), screens on
`GuiGraphics` (`setBlitOffset`, tooltip/font helpers), BER render
signatures and the sky renderer (`ISkyRenderHandler` ->
`DimensionSpecialEffects`), `Recipe<Container>` vs the new `RecipeInput`
bound, fluid attributes/`ForgeFlowingFluid`, world generation, loot/datagen
packages, and `.normal(Matrix3f, ...)` chain calls that now take a
`PoseStack.Pose`. Roughly 3,027 errors across 525 files remain at this point.
