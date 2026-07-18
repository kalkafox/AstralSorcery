/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import hellfirepvp.astralsorcery.common.world.StructureGenerationConfig;
import hellfirepvp.astralsorcery.common.world.feature.config.ReplaceBlockConfig;
import hellfirepvp.astralsorcery.common.world.placement.ChancePlacement;
import hellfirepvp.astralsorcery.common.world.placement.RiverbedPlacement;
import hellfirepvp.astralsorcery.common.world.placement.WorldFilteredPlacement;
import hellfirepvp.astralsorcery.common.world.structure.AncientShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.DesertShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.SmallShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureAncientShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureDesertShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureSmallShrineStructure;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Config.*;
import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Features.*;
import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Modifiers.*;
import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Placements.*;
import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Structures.*;

/**
 * Since 1.21 the configured/placed features, structures, structure sets and
 * biome modifiers are datapack entries; the bootstrap methods below are
 * emitted as JSON by datagen ({@code AstralDataGenerator}). Only the codec
 * carriers (feature types, placement modifier types, structure piece types,
 * structure types) are registered at runtime. Biome selection lives in the
 * {@code WorldGenerationAS.Tags} biome tags; the TOML "enabled"/dimension
 * gates are applied at placement time by {@link WorldFilteredPlacement} and
 * for structures by {@code TemplateStructureFeature}.
 *
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryWorldGeneration
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:17
 */
public class RegistryWorldGeneration {

    public static void init() {
        AstralRegistries.register(AstralRegistries.FEATURES, KEY_FEATURE_REPLACE_BLOCK, REPLACE_BLOCK);
        AstralRegistries.register(AstralRegistries.FEATURES, KEY_FEATURE_ROCK_CRYSTAL, ROCK_CRYSTAL);

        CHANCE = registerPlacement(KEY_PLACEMENT_CHANCE, () -> ChancePlacement.CODEC);
        RIVERBED = registerPlacement(KEY_PLACEMENT_RIVERBED, () -> RiverbedPlacement.CODEC);
        WORLD_FILTER = registerPlacement(KEY_PLACEMENT_WORLD_FILTER, () -> WorldFilteredPlacement.CODEC);

        ANCIENT_SHRINE_PIECE = registerStructurePiece(KEY_ANCIENT_SHRINE, AncientShrineStructure::new);
        DESERT_SHRINE_PIECE  = registerStructurePiece(KEY_DESERT_SHRINE,  DesertShrineStructure::new);
        SMALL_SHRINE_PIECE   = registerStructurePiece(KEY_SMALL_SHRINE,   SmallShrineStructure::new);

        TYPE_ANCIENT_SHRINE = registerStructureType(KEY_ANCIENT_SHRINE, () -> FeatureAncientShrineStructure.CODEC);
        TYPE_DESERT_SHRINE  = registerStructureType(KEY_DESERT_SHRINE,  () -> FeatureDesertShrineStructure.CODEC);
        TYPE_SMALL_SHRINE   = registerStructureType(KEY_SMALL_SHRINE,   () -> FeatureSmallShrineStructure.CODEC);
    }

    public static void addConfigEntries(Consumer<ConfigEntry> registrar) {
        registrar.accept(CFG_ANCIENT_SHRINE);
        registrar.accept(CFG_DESERT_SHRINE);
        registrar.accept(CFG_SMALL_SHRINE);

        registrar.accept(CFG_GLOW_FLOWER);
        registrar.accept(CFG_ROCK_CRYSTAL);
        registrar.accept(CFG_AQUAMARINE);
        registrar.accept(CFG_MARBLE);
    }

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        ctx.register(CF_GLOW_FLOWER, new ConfiguredFeature<>(Feature.FLOWER,
                FeatureUtils.simpleRandomPatchConfiguration(12, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(BlocksAS.GLOW_FLOWER))))));
        ctx.register(CF_ROCK_CRYSTAL, new ConfiguredFeature<>(ROCK_CRYSTAL,
                new ReplaceBlockConfig(new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD), BlocksAS.ROCK_CRYSTAL_ORE.defaultBlockState())));
        ctx.register(CF_AQUAMARINE, new ConfiguredFeature<>(REPLACE_BLOCK,
                new ReplaceBlockConfig(new TagMatchTest(BlockTags.SAND), BlocksAS.AQUAMARINE_SAND_ORE.defaultBlockState())));
        ctx.register(CF_MARBLE, new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD), BlocksAS.MARBLE_RAW.defaultBlockState(), 26)));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> features = ctx.lookup(Registries.CONFIGURED_FEATURE);
        ctx.register(PF_GLOW_FLOWER, new PlacedFeature(features.getOrThrow(CF_GLOW_FLOWER), List.of(
                CountPlacement.of(UniformInt.of(0, 6)),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                WorldFilteredPlacement.forConfig(CFG_GLOW_FLOWER),
                BiomeFilter.biome())));
        // 1.16 placed these at y 5..6; keep them hugging the world bottom.
        ctx.register(PF_ROCK_CRYSTAL, new PlacedFeature(features.getOrThrow(CF_ROCK_CRYSTAL), List.of(
                ChancePlacement.withChance(1F / 25F),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(5), VerticalAnchor.aboveBottom(6)),
                WorldFilteredPlacement.forConfig(CFG_ROCK_CRYSTAL),
                BiomeFilter.biome())));
        ctx.register(PF_AQUAMARINE, new PlacedFeature(features.getOrThrow(CF_AQUAMARINE), List.of(
                CountPlacement.of(UniformInt.of(0, 8)),
                new RiverbedPlacement(),
                WorldFilteredPlacement.forConfig(CFG_AQUAMARINE),
                BiomeFilter.biome())));
        ctx.register(PF_MARBLE, new PlacedFeature(features.getOrThrow(CF_MARBLE), List.of(
                CountPlacement.of(UniformInt.of(0, 10)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(96)),
                WorldFilteredPlacement.forConfig(CFG_MARBLE),
                BiomeFilter.biome())));
    }

    public static void bootstrapStructures(BootstrapContext<Structure> ctx) {
        HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
        ctx.register(STRUCTURE_ANCIENT_SHRINE, new FeatureAncientShrineStructure(
                shrineSettings(biomes.getOrThrow(WorldGenerationAS.Tags.HAS_ANCIENT_SHRINE))));
        ctx.register(STRUCTURE_DESERT_SHRINE, new FeatureDesertShrineStructure(
                shrineSettings(biomes.getOrThrow(WorldGenerationAS.Tags.HAS_DESERT_SHRINE))));
        ctx.register(STRUCTURE_SMALL_SHRINE, new FeatureSmallShrineStructure(
                shrineSettings(biomes.getOrThrow(WorldGenerationAS.Tags.HAS_SMALL_SHRINE))));
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> ctx) {
        HolderGetter<Structure> structures = ctx.lookup(Registries.STRUCTURE);
        registerStructureSet(ctx, structures, SET_ANCIENT_SHRINE, STRUCTURE_ANCIENT_SHRINE, CFG_ANCIENT_SHRINE);
        registerStructureSet(ctx, structures, SET_DESERT_SHRINE, STRUCTURE_DESERT_SHRINE, CFG_DESERT_SHRINE);
        registerStructureSet(ctx, structures, SET_SMALL_SHRINE, STRUCTURE_SMALL_SHRINE, CFG_SMALL_SHRINE);
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> ctx) {
        HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> features = ctx.lookup(Registries.PLACED_FEATURE);
        ctx.register(ADD_GLOW_FLOWER, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(WorldGenerationAS.Tags.HAS_GLOW_FLOWER),
                HolderSet.direct(features.getOrThrow(PF_GLOW_FLOWER)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        ctx.register(ADD_ROCK_CRYSTAL, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(WorldGenerationAS.Tags.HAS_ROCK_CRYSTAL),
                HolderSet.direct(features.getOrThrow(PF_ROCK_CRYSTAL)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        ctx.register(ADD_AQUAMARINE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(WorldGenerationAS.Tags.HAS_AQUAMARINE),
                HolderSet.direct(features.getOrThrow(PF_AQUAMARINE)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        ctx.register(ADD_MARBLE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(WorldGenerationAS.Tags.HAS_MARBLE),
                HolderSet.direct(features.getOrThrow(PF_MARBLE)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static Structure.StructureSettings shrineSettings(HolderSet<Biome> biomes) {
        // The shrines were noise-affecting features in 1.16 -> beard terrain adaptation.
        return new Structure.StructureSettings(biomes, Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN);
    }

    private static void registerStructureSet(BootstrapContext<StructureSet> ctx, HolderGetter<Structure> structures,
                                             ResourceKey<StructureSet> setKey, ResourceKey<Structure> structureKey,
                                             StructureGenerationConfig cfg) {
        ctx.register(setKey, new StructureSet(structures.getOrThrow(structureKey),
                new RandomSpreadStructurePlacement(cfg.getSpacing(), cfg.getSeparation(), RandomSpreadType.LINEAR, cfg.getSalt())));
    }

    private static <P extends PlacementModifier> PlacementModifierType<P> registerPlacement(ResourceLocation key, PlacementModifierType<P> type) {
        return AstralRegistries.register(AstralRegistries.PLACEMENT_MODIFIER_TYPES, key, type);
    }

    private static StructurePieceType registerStructurePiece(ResourceLocation key, StructurePieceType.StructureTemplateType type) {
        return AstralRegistries.register(AstralRegistries.STRUCTURE_PIECE_TYPES, key, type);
    }

    private static <S extends Structure> StructureType<S> registerStructureType(ResourceLocation key, StructureType<S> type) {
        return AstralRegistries.register(AstralRegistries.WORLDGEN_STRUCTURE_TYPES, key, type);
    }
}
