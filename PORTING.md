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

1. **Enchantments** (done): `RegistryEnchantments` is a
   `BootstrapContext<Enchantment>` bootstrap wired into the
   `DatapackBuiltinEntriesProvider` in `AstralDataGenerator`; datagen emits
   the enchantment definition JSONs.
2. **World generation** (`RegistryWorldGeneration`) is untouched 1.16 code;
   configured/placed features and structures are datapack-driven in 1.21 and
   biome injection happens via biome modifier JSONs. Whole subsystem needs
   its own port.
3. **Loot** (done): `RegistryLoot` registers MapCodec-based loot function
   types and global loot modifiers; the `CODEC` constants are implemented on
   the loot function/modifier classes (`common/loot/**`).

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

## Recipe subsystem (done)

`common/crafting/helper`, `common/crafting/recipe/**` (including `altar/*`),
`common/crafting/serializer/*`, `common/crafting/custom/RecipeDyeableChangeColor`,
and the recipe-type/serializer registration in `common/registry` are ported
to the 1.21 `Recipe`/`RecipeSerializer` API. The `common/crafting/builder/**`
(datagen recipe builders) tree and the custom-ingredient subsystem
(`common/crafting/helper/ingredient/*` as `ICustomIngredient`s with
`IngredientType` registration, `RegistryIngredientTypes`,
`IngredientSerializersAS`) are now ported as part of the datagen port;
call sites obtain vanilla `Ingredient`s via `ICustomIngredient.toVanilla()`
and detect them via `Ingredient.getCustomIngredient()`. `common/crafting/nojson/**`
is partially ported (starlight/freezing recipes compile; the rest follows
its own subsystem port).

## Datagen (done)

All providers under `datagen/**` compile against the 1.21 datagen APIs:
`PackOutput` + `CompletableFuture<HolderLookup.Provider>` constructors, tag
providers on NeoForge's `BlockTagsProvider`/vanilla `ItemTagsProvider`,
advancements on NeoForge's `AdvancementProvider` with codec-based custom
criteria (see `common/advancement/**`: `SimpleCriterionTrigger` subclasses
with record `SimpleInstance`s, registered via the `TRIGGER_TYPE` deferred
register), recipes on `RecipeOutput` (custom builders construct real vanilla
`Recipe` objects), the perk tree provider on `CachedOutput`/
`DataProvider.saveStable`, and the blockstate provider on the 1.21 NeoForge
`BlockStateProvider`. `AstralDataGenerator` wires everything through
`GatherDataEvent` including a `DatapackBuiltinEntriesProvider` for the
enchantment datapack registry. The liquid starlight fluid is ported to
`BaseFlowingFluid`/`FluidType` (client textures via
`RegisterClientExtensionsEvent` in `ClientProxy`). `runData` has not been
executed yet - do that once `compileJava` passes to regenerate
`src/generated/resources`.

- **`RecipeInput` bound.** `Recipe<T>` now requires `T extends RecipeInput`
  instead of `Container`. None of these recipes are ever matched in a
  vanilla crafting grid - `IHandlerRecipe<I extends IItemHandler>` real
  matching always went through `matches(I handler, Level)` with the
  `Container` overload hardwired to return `false` - so `IHandlerRecipe` now
  extends `Recipe<IHandlerRecipe.NoopInput>`, a nested zero-size marker
  `RecipeInput` (`getItem` always `ItemStack.EMPTY`, `size()` always `0`).
  `CustomMatcherRecipe.assemble`/`getResultItem` were updated to the new
  `(NoopInput, HolderLookup.Provider)` / `(HolderLookup.Provider)`
  signatures (still always returning `ItemStack.EMPTY` - actual output is
  produced through each recipe's own application-specific methods, e.g.
  `SimpleAltarRecipe.getOutputs(TileAltar)`).
- **Recipes no longer self-report their own id.** `Recipe.getId()` is gone;
  `RecipeManager` now attaches a `ResourceLocation` externally by wrapping
  the decoded recipe in a `RecipeHolder<T>`, and neither
  `RecipeSerializer.codec()` nor `.streamCodec()` are told that id during
  decode. `BaseHandlerRecipe` keeps its `recipeId` field/constructor (no
  longer `@Override`-ing anything) purely as this object's own identity for
  the lifetime of the decoded instance - `CustomRecipeSerializer.generateDynamicId()`
  synthesizes a `astralsorcery:dynamic/<uuid>` placeholder for every
  JSON/network-decoded recipe. **Caveat:** this means the NBT-persisted
  "recipe in progress" round-trip in `ActiveSimpleAltarRecipe.serialize()`/
  `deserialize()` only resolves back to the same recipe object within the
  RecipeManager reload cycle it was crafted in (a `/reload` or datapack swap
  while a craft is in progress will fail to find the recipe again). Fixing
  this properly would mean threading `RecipeHolder<T>` through
  `ResolvingRecipeType`/`ActiveSimpleAltarRecipe` instead of raw recipe
  instances - left as a follow-up.
- **Codec/stream codec wrapping.** `RecipeSerializer<T>` dropped
  `read(ResourceLocation, JsonObject)` / `read(ResourceLocation, FriendlyByteBuf)`
  / `write(FriendlyByteBuf, T)` for `codec()` (`MapCodec<T>`) and
  `streamCodec()` (`StreamCodec<RegistryFriendlyByteBuf, T>`). Rather than
  rewrite the hand-written imperative `GsonHelper`-based parsers as codec
  combinator chains, `CustomRecipeSerializer` wraps each subclass's existing
  `read(JsonObject)`/`write(JsonObject, T)` pair via
  `LegacyRecipeCodecs.ofLegacyJson(...)` (round-trips through
  `JsonOps.INSTANCE` internally, `MapCodec.assumeMapUnsafe`-wrapped) and each
  `read(RegistryFriendlyByteBuf)`/`write(RegistryFriendlyByteBuf, T)` pair
  via `StreamCodec.of(...)`. Buffer parameters throughout the recipe classes
  were widened from `FriendlyByteBuf` to `RegistryFriendlyByteBuf` since
  `Ingredient.CONTENTS_STREAM_CODEC` (see below) requires it specifically.
- **`Ingredient` lost `serialize()`/`deserialize()`/`read()`/`write()`.**
  It's `Ingredient.CODEC` (JSON) and `Ingredient.CONTENTS_STREAM_CODEC`
  (network) now. `common/crafting/helper/IngredientIO` is a small adapter
  back to the old four-method call shape so the many hand-written recipe
  (de)serializers didn't each need their own codec plumbing.
  `Ingredient.fromTag`/`valueFromJson`/`fromStacks`/`deserialize` calls
  became `Ingredient.of(...)` overloads + `IngredientIO`.
- **`assemble`/`getResultItem` gained a `HolderLookup.Provider registries`
  parameter**; updated on every recipe/serializer touched here.
- **`FluidStack` lost its raw-NBT constructor** (`FluidStack(Fluid, int,
  CompoundTag)` -> data components replaced free-form NBT tags on stacks).
  `LiquidInteraction`'s optional per-reactant `reactant1Tag`/`reactant2Tag`
  JSON fields are now parsed-and-discarded - reactant matching is
  fluid+amount only until/unless this is redesigned around
  `DataComponentPatch`.
- **`CustomRecipeBuilder` (datagen-facing)** now hands recipes to a
  `RecipeOutput` instead of building a `FinishedRecipe`/`IFinishedRecipe`
  shim - `RecipeOutput.accept(id, recipe, advancement)` serializes through
  the recipe's own `RecipeSerializer.codec()` automatically, so the old
  per-recipe `write(JsonObject)` plumbing datagen used to invoke by hand is
  no longer needed for that path (still used for the JSON codec above). The
  existing recipe datagen providers under `datagen/data/recipes/**` were
  **not** touched - they're on much older 1.16 datagen APIs
  (`net.minecraft.data.IFinishedRecipe`, a `DataGenerator`-constructor
  `RecipeProvider`) that are broken independent of anything in this pass and
  are out of scope for this subsystem.
- **`RecipeDyeableChangeColor`** is the one real vanilla 3x3-grid
  `CustomRecipe` in this package (not one of the handler-based recipes
  above). Ported to `CraftingInput`/`CraftingBookCategory` (constructor no
  longer takes an id) and `SimpleCraftingRecipeSerializer<T>` (vanilla's
  1.21 replacement for the old `SpecialRecipeSerializer`).
- **Registration.** `RegistryRecipeSerializers`/`RegistryRecipeTypes` and
  the `AstralRegistries.RECIPE_TYPES`/`RECIPE_SERIALIZERS` `DeferredRegister`
  declarations needed no bound changes - they were already
  `DeferredRegister<RecipeType<?>>`/`DeferredRegister<RecipeSerializer<?>>`.
  `ResolvingRecipeType`'s constructor used to double-register directly
  against the (now-removed) static `Registry.RECIPE_TYPE`; that call was
  dropped since `RegistryRecipeTypes.register()` already routes through
  `AstralRegistries.RECIPE_TYPES`. `ResolvingRecipeType.getAllRecipes()` now
  reads `RecipeManager.getAllRecipesFor(type)` (`List<RecipeHolder<T>>`,
  unwrapped via `RecipeHolder::value`) instead of the removed
  `getRecipes(RecipeType)`.
- **Out-of-scope call-site fix:** `ActiveSimpleAltarRecipe.deserialize()`
  used `RecipeManager.getRecipe(ResourceLocation)`, which no longer exists;
  changed to `RecipeManager.byKey(...)` (`Optional<RecipeHolder<?>>`,
  unwrapped via `.value()`). This file still has unrelated pre-existing
  errors (`FluidActionResult.shouldSwing()`/`getObject()`, and its use of
  the still-unported `FluidIngredient`) that are out of scope here.
- **Mis-remapped calls fixed in-line** (pre-existing corruption from the
  automated MCP->Mojmap member remap, not new porting work, but they were
  blocking compilation of files in scope): several `JSONUtils.hasField(json, key)`
  calls had been mangled into `GsonHelper.convertToInt(json, key)` /
  `convertToDouble(json, key)` (int/double compared as a `boolean`) across
  `SimpleAltarRecipeSerializer`, `BlockTransmutationSerializer`, and four
  `altar/builtin/*` recipes - restored to `json.has(key)` (or
  `GsonHelper.isArrayNode` where the original used `isJsonArray`).
  `GsonHelper.getAsString(JsonElement, String)`/`GsonHelper.toString(JsonElement)`
  don't exist in 1.21's `GsonHelper` (only the `JsonObject`-keyed overload
  does) - restored to `GsonHelper.convertToString(JsonElement, String)` /
  plain `toString()`. `AltarRecipeGrid`'s pattern-padding calls were
  garbled from `StringUtils.repeat` (still imported, just unused) into a
  nonexistent `StringUtil.zoom` - restored. `LiquidInteraction`'s
  `Strings.checkExceptions(...)` was similarly garbled from `Strings.join(...)`.
- **Deliberately left alone / follow-ups:**
  - `common/crafting/helper/ingredient/{CrystalIngredient,FluidIngredient,*Serializer}`
    subclass `Ingredient`, which became `final` in 1.21; custom ingredient
    behavior now goes through NeoForge's `ICustomIngredient`/`IngredientType`
    (registered against `NeoForgeRegistries.Keys.INGREDIENT_TYPES`) instead
    of subclassing. This is its own subsystem port and was left broken
    (unchanged from before this pass). `AltarRecipeGrid.Builder`'s
    `key(Character, Fluid)` convenience overload (the only in-scope caller
    of `FluidIngredient`) was removed rather than worked around, since
    nothing else in scope used it.
  - `common/crafting/builder/*` (non-datagen recipe builders used by
    `AstralRecipeBuilder`/datagen) and `common/crafting/nojson/**` were not
    touched; both were already broken before this pass for unrelated
    reasons (old `RegistryHelper`/`FluidStack`/world-gen APIs) and remain so.
  - `common/crafting/recipe/interaction/**` (`InteractionResult`,
    `ResultDropItem`, `ResultSpawnEntity`, and the whole `interaction/jei/*`
    subpackage) fail to compile because the JEI (`mezz.jei.*`) dependency
    isn't wired into this build at all - unrelated to the `Recipe`/
    `RecipeSerializer` API and out of scope here. `LiquidInteraction` itself
    (which references `InteractionResult`) compiles fine.

## Screens and GUI (done)

The `client/screen/**` tree compiles against the 1.21 screen API. Vanilla's
render entry points now take `GuiGraphics` instead of `PoseStack`; since the
mod's screens draw exclusively through their own PoseStack-based helpers
(`RenderingGuiUtils`/`RenderingDrawUtils`/`RenderingUtils`), the port bridges
at the mod's base classes instead of rewriting every screen:

- **`InputScreen`** (root of all non-container screens) overrides
  `render(GuiGraphics, ...)`, stashes the `GuiGraphics` in a field
  (accessible via `getCurrentGraphics()` during the render pass), and calls a
  mod-side `render(PoseStack, ...)` that all subclasses keep overriding. The
  PoseStack IS `graphics.pose()`, so transforms carry through. The default
  PoseStack body forwards to vanilla `super.render` (background + widgets).
- **Blit offset**: `Screen.get/setBlitOffset` are gone in vanilla; the mod's
  own z-layered draw helpers still consume it, so it's kept as a plain field
  on `InputScreen` and `ScreenCustomContainer` with the old accessor names.
- **Container screens** (`ScreenCustomContainer`, dead-but-compiling
  `ContainerBaseScreen`): vanilla's abstract `renderBg(GuiGraphics, ...)` and
  `renderLabels(GuiGraphics, ...)` are implemented as bridges that call the
  1.16-shaped `drawGuiContainerBackgroundLayer(PoseStack, ...)` /
  `renderLabels(PoseStack, ...)` hooks the altar/tome screens override.
  `renderHoveredTooltip` -> `renderTooltip(GuiGraphics, x, y)`;
  `tick()` (now final) -> `containerTick()`; `xSize/ySize` ->
  `imageWidth/imageHeight`; `this.container` -> `this.getMenu()`.
- **Screen registration** moved from `ScreenManager.registerFactory` (in
  `FMLClientSetupEvent`) to `RegisterMenuScreensEvent` on the mod bus
  (`RegistryContainerTypes.initClient(event)`); `MenuType` construction now
  needs `FeatureFlags.DEFAULT_FLAGS`.
- **Tooltip/text APIs**: `stack.getTooltipLines(player, flag)` gained a
  leading `Item.TooltipContext.of(level)`; `TooltipFlag.TooltipFlags.*` ->
  `TooltipFlag.*`; `Screen.getTooltipFromItem` is static and takes the
  `Minecraft` instance; item-stack custom fonts go through
  `IClientItemExtensions.of(stack).getFont(stack, FontContext.TOOLTIP)`;
  `I18n.format` -> `I18n.get`; `Language.getInstance().func_230503_a_` ->
  `getOrDefault`; `font.func_243245_a` -> `font.width`.
- **Misc renames**: `mouseScrolled` gained a `scrollX` parameter;
  `Screen.init(Minecraft, w, h)` is final (re-init hooks moved into
  `init()`); `options.renderDebug` ->
  `Minecraft.getDebugOverlay().showDebugScreen()`;
  `keyboardHandler.setClipboardString` -> `setClipboard`;
  `options.setPointOfView` -> `setCameraType`; `player.getPitch(pt)` ->
  `getViewXRot(pt)`; `player.onClose()` -> `closeContainer()`;
  `Lighting.turnBackOn/turnOff` -> `setupFor3DItems/setupForFlatItems`;
  `Random.initNoise` was a bad remap of `setSeed`;
  `getRainStrength` -> `getRainLevel` (repo-wide);
  `Screen.fill(PoseStack, ...)` -> `getCurrentGraphics().fill(...)`;
  fluid display names via `fluid.getFluidType().getDescription(stack)`.
- **Journal recipe pages** now flow `RecipeHolder<?>` (which carries the id
  vanished from `Recipe.getId()`) through `JournalPageRecipe` ->
  `RenderPageRecipe.fromRecipe`; lookups use `RecipeManager.byKey` /
  `getAllRecipesFor`. `RecipeHelper.findSmeltingResult` was ported to
  `SingleRecipeInput`/`getRecipeFor`.
- **`RenderPageStructure`**: ObserverLib's client `StructureRenderer` isn't
  part of the vendored copy; a compatibility shell
  (`hellfirepvp.observerlib.api.client.StructureRenderer`) keeps the page
  compiling with the 3D structure slice rendering stubbed out (restored in a
  later client-rendering pass, same approach as `StructurePreview`).

## Block classes (done)

All of `common/block/**` (plus `BlockLiquidStarlight`) compiles against the
1.21 block API:

- `BaseEntityBlock` requires a `codec()` override; Astral Sorcery blocks are
  never codec-constructed, so they all return the shared
  `common/block/base/UnsupportedBlockCodec.unsupported()` placeholder (throws
  if ever invoked).
- Properties: `Properties.create(Material, ...)` -> `Properties.of()` +
  `.mapColor(...)` (`Material` is gone; barrier-like blocks now set
  `pushReaction(PushReaction.BLOCK)` / `isValidSpawn((s, l, p, t) -> false)`
  explicitly). Two systematic bad remaps were corrected across the package:
  `hardnessAndResistance` -> `strength`, and `isRedstoneConductor(state -> N)`
  which was actually 1.16 `setLightLevel` -> `lightLevel(state -> N)`.
- `Block#getOffsetType()` is no longer overridable ->
  `Properties.offsetType(OffsetType.XZ)` (crystal clusters).
- `animateTick` (and the mod's `showBreakingParticles` helper) take
  `RandomSource`; the static `RANDOM` fields feeding `Mth.nextInt` were
  replaced with `level.getRandom()`.
- Client hooks `addDestroyEffects`/`addHitEffects` moved off `Block` onto
  `IClientBlockExtensions`, registered per-block in
  `ClientProxy.onRegisterClientExtensions`: flare light (suppress vanilla
  particles), telescope (also play particles for the structural top half),
  structural dummies (redirect particles to the supported block).
- `getExpDrop` is now `(state, LevelAccessor, pos, blockEntity, breaker,
  tool)`; fortune/silk-touch are read off the tool via
  `EnchantmentHelper.getItemEnchantmentLevel` with registry `Holder`s.
- `IPlantable`/`PlantType` are gone; `BlockFoliageTemplate` soil checks use
  `BlockState#canSustainPlant` returning NeoForge's `TriState` (falling back
  to the old material checks on `TriState.DEFAULT`).
- `CommonHooks.isCorrectToolForDrops(state, player)` no longer exists ->
  vanilla `player.hasCorrectToolForDrops(state)` (custom `getDestroyProgress`
  overrides on the gateway/collector crystal).
- Misc renames hit here: `getAiPathNodeType` -> `getBlockPathType` returning
  `PathType`; `IStringSerializable.getString` -> `getSerializedName`;
  `getStateContainer` -> `getStateDefinition`; `state.get` -> `getValue`;
  waterlogging via `level.scheduleTick(...)` + `Fluids.WATER.getSource(false)`;
  `hasSolidSideOnTop` -> `canSupportRigidBlock`; `Block.fillItemCategory`
  (bogus remap) -> `Block.isFaceFull`; 2-arg `Level.setBlock` gained
  `Block.UPDATE_ALL`; `FluidActionResult.shouldSwing/getObject` ->
  `isSuccess/getResult`; `Inventory.add` restored where the member remap
  garbled it into `hurtArmor`/`getArmor`; collision-context entity access
  needs an `EntityCollisionContext` instanceof-check; `SoundType.PLANT` ->
  `SoundType.GRASS`.
- The blocks' `newBlockEntity(pos, state)` overrides construct their tiles
  directly, so `TileAltar`, `TileInfuser`, `TileWell`, `TileTreeBeacon`, and
  `TileRitualPedestal` were given `(BlockPos, BlockState)` constructors
  (their base classes already had the `(type, pos, state)` shape). The rest
  of the tile subsystem is still unported.

## Tile subsystem (done)

`common/tile/**` compiles clean. The base classes (`TileEntitySynchronized`,
`TileEntityTick`, `TileReceiverBase`) were already on the 1.21 shapes
(`(BlockEntityType, BlockPos, BlockState)` constructors,
`loadAdditional`/`saveAdditional` bridging to the mod's
`readCustomNBT`/`writeCustomNBT(CompoundTag, HolderLookup.Provider)`); this
pass caught the remaining subclasses up:

- `readCustomNBT`/`writeCustomNBT` overrides gained the
  `HolderLookup.Provider` parameter; `TileWell`/`TileTreeBeacon`/
  `TileRitualPedestal` got `(BlockPos, BlockState)` constructors.
- Bare `pos` field accesses (the 1.16 `TileEntity.pos`) -> `getBlockPos()`;
  `BlockEntity#remove()` -> `setRemoved()`.
- `markForUpdate`'s garbled `Level.findNearestBiome` call (bad remap of
  `sendBlockUpdated`) restored, ditto in `TilePrism.onDataReceived`. Other
  remap garbage fixed: `Set.add` mangled into `Set.offset`
  (`TileAltar.nearbyRelays`, `TileAttunementAltar.getConstellationPositions`),
  a local `colorIndex` int named `random` shadowing the RNG field
  (`TileRefractionTable.playEngravingEffects`), `Component.Serializer.getPos`
  (= `toJson`) in `TileCelestialGateway`, and the fluid tanks'
  `fillDefaultJigsawNBT` (vanilla jigsaw name clobbered the tank's NBT writer)
  renamed to `save()`.
- `RecipeManager.getRecipes(type).get(id)` (via a 1.16 AT) ->
  `byKey(id).map(RecipeHolder::value).orElse(null)` in `TileAltar`/`TileInfuser`
  client craft-finish handlers.
- `Level.setBlock(pos, state)` -> `setBlockAndUpdate` (or explicit
  `Block.UPDATE_ALL` flags); `BlockPos.add` -> `offset`;
  `BlockPos.offset(dir, n)` -> `relative(dir, n)`; `distSqr(Vec3, false)` ->
  plain `distSqr(Vec3i)`; `Vec3.copy/copyCentered` ->
  `atLowerCornerOf`/`atCenterOf`; `AABB(BlockPos, BlockPos)` ctor is gone ->
  `AABB(Vec3, Vec3)`; `AABB.offset(BlockPos)` -> `move`;
  `getEntitiesWithinAABB` -> `getEntitiesOfClass`; `Level.addEntity` ->
  `addFreshEntity`; `Entity.setPositionAndRotation` -> `moveTo`;
  `Level.getLight` -> `getMaxLocalRawBrightness`; `getLightFor` ->
  `getBrightness`; `Tag.getString()` -> `getAsString()`;
  `new FluidStack(stack, amount)` -> `stack.copyWithAmount(amount)`;
  `FluidAttributes.getColor` -> `IClientFluidTypeExtensions.getTintColor`;
  `ItemStack.attemptDamageItem` -> `hurtAndBreak(dmg, ServerLevel, player,
  onBreak)` (break callback replaces the boolean return).
- `TileEntityTick.refreshMatcher` resolves the observer provider's registry
  name via `RegistryProviders.getRegistry().getKey(...)` (ObserverLib's
  provider no longer self-reports it).
- **Tree beacon**: `SaplingGrowTreeEvent` -> NeoForge's
  `BlockGrowFeatureEvent` (`getPos`/`getRandom`; `Event.Result.DENY` ->
  `setCanceled(true)`). `TreeType` is ported with it: generators take
  `RandomSource`, `SaplingBlock.treeGrower` is exposed through a new
  1.21-format AT entry (the old SRG-named AT entries are inert - the whole
  `accesstransformer.cfg` needs a re-audit, tracked separately), and
  `BlockSnapshot` uses `getCurrentState()`/`getPos()`.
- **Render bounding boxes moved off the tile**: NeoForge 21.1 replaced
  `IForgeTileEntity.getRenderBoundingBox` with
  `BlockEntityRenderer#getRenderBoundingBox(T)`;
  `TileAttunementAltar`'s expanded culling box now lives in
  `RenderAttunementAltar`.

## Starlight network and world data (done)

`common/starlight/**`, `common/data/**`, and the AS side of ObserverLib's
world-cache framework compile clean:

- **World-cache framework**: the vendored ObserverLib framework was already
  redesigned around Codecs (`SectionWorldData<T, S extends WorldSection>`
  with a per-section `Codec<S>`, instance data through
  `SaveKey.getInstanceCodec()`, extra files via
  `writeAdditionalData`/`readAdditionalData`). The four AS data classes now
  follow `StructureMatchingBuffer`'s pattern: a `CODEC` built from
  `SaveKey.CODEC` plus a `CompoundTag.CODEC` field bridging to the classes'
  existing hand-written NBT I/O (kept as plain `save`/`readFromNBT`-style
  methods rather than rewritten as codec combinators). `RegistryData` passes
  the codecs to `createSaveKey`. `GlobalWorldData` gained default no-op
  additional-data hooks. **On-disk format/layout changed with the framework;
  1.16 world data is not migrated.**
- **The framework no longer ticks its data.** `updateTick` survives only on
  `LightNetworkBuffer` (queued chunk cleanup + source-refresh upkeep),
  driven by a new `NETWORK_TICK_HANDLER` (`ITickHandler`, WORLD/END)
  registered in `CommonProxy.attachTickListeners`; it only runs when the
  buffer is already loaded (new `WorldCacheDomain#getDataIfLoaded`). The
  empty `updateTick`s on the other three were deleted.
- `GatewayCache`/`TileCelestialGateway` display names serialize via
  `Component.Serializer.toJson/fromJson(..., RegistryAccess.EMPTY)`.
- `WorldEvent.Load/Unload` -> `LevelEvent.Load/Unload`;
  `ChunkEvent#getChunk().getPos()` (was garbled to `getBlockPos`).
- Remap garbage fixed: `Deque.push/pop` mangled to `pushPose/popPose`
  (`TransmissionChain`), `File.delete` to `deleteText` (`ResearchHelper`),
  shadowed loop variables re-separated (`TransmissionWorldHandler` - javac
  rejects what 1.16's remap collapsed into duplicate `pos` locals),
  `BlockPos.of(long)` garbled to `BlockPos.subtract` (sync/client).
- Misc: `NbtIo.read/write` take `Path` now; `CompoundTag.keySet` ->
  `getAllKeys`; `Tag.getString` -> `getAsString`;
  `Registry.DIMENSION_REGISTRY` -> `Registries.DIMENSION`;
  `ChunkPos.asBlockPos` -> `getWorldPosition`; corner-distance
  `distSqr(Vec3, false)` -> `distSqr(Vec3i)`; `withinDistance` ->
  `closerThan`; `Level.getPlayers` -> `players()`;
  `PlayerList.getPlayerByUsername` -> `getPlayerByName`;
  `CommandSource.sendMessage(c, uuid)` -> `sendSystemMessage(c)`;
  `Entity.writeWithoutTypeId/read/removeEntity` ->
  `saveWithoutId/load/discard`; journal pages use
  `getAllRecipesFor` + `RecipeHolder.value()`.

## Entities (done)

`common/entity/**` compiles clean. Highlights, since this pack of changes is
large and easy to re-derive incorrectly from memory of the 1.16 API:

- **Synced data changed shape.** `Entity#defineSynchedData()` is now
  `defineSynchedData(SynchedEntityData.Builder builder)`; register keys via
  `builder.define(ACCESSOR, default)` instead of
  `this.entityData.register(...)`, and always call
  `super.defineSynchedData(builder)` first when overriding a non-`Entity`
  base. `SynchedEntityData.createKey` -> `defineId`.
- **`EntityType.IFactory` -> `EntityType.EntityFactory`.**
- **Positioning**: `setPosition(x,y,z)` -> `setPos(x,y,z)`; no combined
  "set position and mark old position" helper exists anymore -> call
  `setPos(...)` then `setOldPosAndRot()` explicitly
  (`EntityObservatoryHelper`). `Entity#level` field is private -> `level()`.
- **Removal**: `remove()` takes an `Entity.RemovalReason` argument now
  (`KILLED`/`DISCARDED` covers everything ported here).
- **Targeting**: `Mob#getAttackTarget`/`setAttackTarget` -> `getTarget`/
  `setTarget`. `getDistance(Entity)` -> `distanceTo(Entity)`.
- **Gravity**: `Entity#getGravity()` is `final` now; override
  `getDefaultGravity()` instead (`EntityGrapplingHook`'s pull-mode gravity
  toggle).
- **`Vec3` has no `getX/getY/getZ()` getters** - its `x`/`y`/`z` fields are
  public final; use those directly. `Vec3#mul` was renamed `multiply`.
  `Vector3` (the mod's own vector helper class) keeps its `getX()`-style
  getters and is unaffected - don't conflate the two when porting motion
  math.
- **`AABB#grow` -> `inflate`; `AABB#offset(Vec3i)` -> `move`.**
  `getEntitiesWithinAABB` -> `getEntitiesOfClass`. `Direction` no longer
  implements `Vec3i`, so `BlockPos#offset(Direction)` doesn't resolve
  anymore - use `BlockPos#relative(Direction)`.
- **`ItemEntity`**: no bare `(Level, x, y, z)` constructor remains (only the
  `ItemStack`-carrying overloads); the 1.16 `timeout` field was actually a
  bad member-remap of `lifespan` (restored across
  `EntityItemHighlighted`/`EntityStarmetal`/`EntityCrystal`/
  `EntityDazzlingGem`). `age`/`pickupDelay` became private with only a
  getter (`getAge()`) or boolean check (`hasPickUpDelay()`) exposed - reading
  the exact `pickupDelay` value (to detect ItemEntity's "fake item" marker,
  `Short.MAX_VALUE`) and writing `age` now go through two small reflection
  helpers added to `ReflectionHelper`
  (`getItemEntityPickupDelay`/`setItemEntityAge`, the latter already existed).
  `getSize(Pose)` -> `getDimensions(Pose)`; `isOnGround()` -> `onGround()`.
- **Damage**: `DamageSource.FALL` and other static constants are gone
  (damage sources are data-driven) - build them via
  `level().damageSources().fall()` etc. `DamageSource#isExplosion()` ->
  `source.is(DamageTypeTags.IS_EXPLOSION)`. `ItemStack#damageItem(amount,
  LivingEntity, Consumer<LivingEntity>)` -> `hurtAndBreak(amount,
  LivingEntity, EquipmentSlot)`. `EnchantmentHelper.getEnchantmentLevel(Enchantment,
  ItemStack)` -> `getItemEnchantmentLevel(Holder<Enchantment>, ItemStack)`,
  the `Holder` resolved via
  `level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(...)`.
- **Removed without a direct replacement**: `Entity#isMovementNoisy()` -
  deleted the two dead overrides (`EntitySpectralTool`,
  `EntityObservatoryHelper`) rather than guess at a new hook.
  `Entity#isGlowing()` -> `isCurrentlyGlowing()`.
- **Spawn-data interface**: NeoForge's `IEntityAdditionalSpawnData` ->
  `IEntityWithComplexSpawn`, using `RegistryFriendlyByteBuf` instead of
  `FriendlyByteBuf`; the manual `getAddEntityPacket()` ->
  `NetworkHooks.getEntitySpawningPacket(this)` override is deleted (1.21's
  default entity spawn packet handles it, consistent with the networking
  pass's caveat in the Networking section above).
- **`BlockPlaceContext`/`UseOnContext`**: `getBlockPos()`/`getFace()` ->
  `getClickedPos()`/`getClickedFace()`.
- **`MiscUtils`** gained `RandomSource` overloads of `getRandomEntry`/
  `applyRandomOffset` alongside the existing `java.util.Random` ones, since
  `Entity#random` is `RandomSource` and isn't assignment-compatible with
  `java.util.Random`.

## Item classes (done)

All of `common/item/**` now compiles against the 1.21 item API. The adjacent
item-side portions of `RegistryItems` (color registration, dispenser behavior,
item model predicates, and creative variants) were ported with it:

- Tooltip hooks take `Item.TooltipContext`; item display names use `getName`;
  use animations use `startUsingItem`, `finishUsingItem`, `onUseTick`, and the
  entity-aware `getUseDuration` overload. Old `fillItemCategory` hooks are gone.
- The main creative tab now adds the configured stacks those hooks used to
  produce: constellation papers, knowledge share, both resonator tiers,
  attuned crystals, constellation mantles, collector crystals, cluster growth
  stages, and creative lens/prism attributes. The papers/crystals tab split is
  still awaiting the broader creative-tab re-curation already tracked in
  `CommonProxy`.
- Block-item placement uses `getPlacementState`; stack damage reads/writes use
  `getDamageValue`/`setDamageValue`. Custom item entities copy data through
  `saveWithoutId`/`load`, and entity removal/damage/fire calls use the new
  removal reasons and level damage sources.
- Wand and dust interactions were moved to the current hit-result, position,
  teleport, swing, block-update, dispenser, and `EventHooks` APIs. Colored
  lenses now use `RandomSource`, `Block.BLOCK_STATE_REGISTRY`, data-driven fire
  damage, and `igniteForSeconds`.
- `ItemMantle` supplies its custom model through `IClientItemExtensions`.
  Item/block tint handlers use `RegisterColorHandlersEvent`; model predicates
  use `ItemProperties.register`; dispenser behaviors use
  `DispenserBlock.registerBehavior`.

## Current compile boundary

The registration mechanism, the recipe/serializer subsystem, rendering
infrastructure, models, renderers, screens, the block classes, the tile
subsystem, the starlight network, the world-data layer, entities, and item
classes now
compile clean. `gradlew compileJava` (configured with `-Xmaxerrs 10000`)
stops at structural 1.16 -> 1.21 API changes rather than naming; the biggest
remaining clusters are perks (`common/perk/**`), `client/util`, world
generation (`RegistryWorldGeneration` +
`common/world/**`), constellation effects/mantle effects, the remaining
`common/registry` content classes, and `crafting/nojson`. 912 compiler errors
remain, measured off a full `gradlew compileJava` run.

### `common/util/**` leaf helpers (done)

`EntityUtils`, `BlockUtils`, `MiscUtils`, `CollisionManager`/`CollisionHelper`,
`FluidContainerDispenseBehavior`, `TimeStopZone`, `CelestialStrike`,
`TestBlockUseContext`, `NBTHelper`, and `LootUtil` now compile clean. Notable
API-shape changes hit in this pass, for the next session's reference:

- `ForgeHooks`/`ForgeEventFactory` -> NeoForge's `net.neoforged.neoforge.event.EventHooks`
  (`getPotentialSpawns`, `checkSpawnPosition`, `finalizeMobSpawn`); the old
  `Event.Result` tri-state spawn checks collapse into plain `boolean`s.
- `LootContext.Builder` no longer builds contexts directly; drops/loot now go
  through `LootParams.Builder` (`.withLuck` moved there), and
  `LootTable.getRandomItems(LootParams, RandomSource)` skips manually
  constructing a `LootContext` for the common case.
  `LivingEntity#getLootTable()` returns `ResourceKey<LootTable>`, resolved via
  `MinecraftServer#reloadableRegistries().getLootTable(key)`.
- `BlockEvent.BreakEvent` no longer carries an editable xp-to-drop field; xp
  is dropped internally by the block's own loot-table experience function via
  `Block#playerDestroy`, same as vanilla's `ServerPlayerGameMode#destroyBlock`.
- `IItemExtension#canPlayerBreakBlockWhileHolding`/`ItemStack#onBlockStartBreak`
  were removed from NeoForge with no replacement found; the pre-checks they
  gated were dropped from `BlockUtils#breakBlockWithoutPlayer`.
- `Level#tickableBlockEntities` (the public list Forge/vanilla used to expose
  for per-tick block-entity ticking) is gone; block-entity ticking is now
  driven by a private `LevelChunk` map with no public add/remove accessor.
  `TimeStopZone` now freezes/resumes ticking by toggling
  `BlockEntity#setRemoved()`/`clearRemoved()` directly, since the chunk's
  ticker wrapper gates on `!isRemoved()` - this is the least-invasive option
  available without reflection, but note `setRemoved()`/`clearRemoved()` each
  call `invalidateCapabilities()`, so capability providers (hoppers, item/
  energy pipes) will briefly see the frozen block entity as capability-less.
- `net.minecraft.util.math.shapes.VoxelShapeSpliterator` (the 1.16.5 class
  `CollisionManager`/`CollisionHelper` iterated collision shapes through, and
  that `mixin/MixinVoxelShapeSpliterator.java` mixed into) no longer exists in
  1.21.1 - vanilla's entity-collision code was restructured and no longer
  routes through a dedicated Spliterator class. Added a small local
  `common/util/collision/CollisionSpliterator` POJO (entity + query AABB) so
  `CollisionManager`/`CollisionHelper` keep compiling with the same shape of
  data the old mixin used to hand them; `MixinVoxelShapeSpliterator` itself is
  unrelated to this file list and remains broken/inert (no real mixin target
  exists for it anymore) - out of scope for this pass, needs a redesign of the
  custom-collision hook for 1.21.1 (e.g. via a different vanilla extension
  point) in a later session.
- Misc renames worth remembering: `AABB#grow` -> `#inflate`; `Shapes#compare`
  -> `#joinIsNotEmpty`; `Shapes#getAllowedOffset` -> `VoxelShape#collide`;
  `Level#isOutsideBuildHeight` is now an instance method (needs a level/
  `LevelHeightAccessor`); `BlockState#getMaterial()` is gone, use
  `#blocksMotion()`/other direct predicates; `BlockState#get`/`getBlock get` ->
  `#getValue`; `LivingEntity#isPotionActive`/`getActivePotionEffect` ->
  `#hasEffect`/`#getEffect` (taking `Holder<MobEffect>`);
  `EnchantmentHelper#getEfficiencyModifier`/`#hasAquaAffinity`/
  `#getMaxEnchantmentLevel` are gone, use
  `EnchantmentHelper#getEnchantmentLevel(Holder<Enchantment>, LivingEntity)`
  with a `Holder<Enchantment>` looked up via
  `level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.X)`;
  `LevelAccessor#getEntitiesWithinAABB` -> `Level#getEntities(EntityTypeTest.forClass(...), AABB, Predicate)`;
  `ChunkSource#isChunkLoaded(ChunkPos)` -> `#hasChunk(int x, int z)`;
  `ClientLevel#getPlayerByUuid` -> `#getPlayerByUUID`; `ServerPlayer#teleport`
  -> `#teleportTo`; `Player#canAttackPlayer` -> `#canHarmPlayer`;
  `ServerPlayerGameMode#setLevel(pos)` (bogus remap of a break helper) ->
  `#destroyBlock(pos)`; `BlockState#isCorrectToolForDrops(level,pos,player)`
  -> `#canHarvestBlock(...)`; `BlockState#removedByPlayer` ->
  `#onDestroyedByPlayer`; `ItemStack#onBlockDestroyed` -> `#mineBlock`;
  `ItemStack#write`/`ItemStack.read` (NBT) -> `ItemStack#saveOptional(HolderLookup.Provider)`/
  `ItemStack.parseOptional(HolderLookup.Provider, CompoundTag)` (ditto
  `FluidStack#save`/`.parseOptional`); `Entity#areEyesInFluid` ->
  `#isEyeInFluid`; `LivingEntity#animationSpeed`/`animationSpeedOld` are gone,
  folded into the encapsulated `Entity#walkAnimation`
  (`WalkAnimationState`); `Entity#swingProgress` -> `#attackAnim`.
