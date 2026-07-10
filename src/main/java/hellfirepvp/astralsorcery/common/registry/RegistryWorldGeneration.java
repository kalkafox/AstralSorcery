/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.world.FeatureGenerationConfig;
import hellfirepvp.astralsorcery.common.world.StructureGenerationConfig;
import hellfirepvp.astralsorcery.common.world.TemplateStructureFeature;
import hellfirepvp.astralsorcery.common.world.feature.config.ReplaceBlockConfig;
import hellfirepvp.astralsorcery.common.world.structure.AncientShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.DesertShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.SmallShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureAncientShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureDesertShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureSmallShrineStructure;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.event.world.BiomeLoadingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Config.*;
import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Features.*;
import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Placements.*;
import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Structures.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryWorldGeneration
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:17
 */
public class RegistryWorldGeneration {

    private static final Map<ConfiguredStructureFeature<?, ?>, StructureGenerationConfig> STRUCTURE_FEATURES = new HashMap<>();
    private static final Map<ConfiguredFeature<?, ?>, FeatureGenerationConfig> FEATURES = new HashMap<>();
    private static final Map<ConfiguredFeature<?, ?>, GenerationStep.Decoration> FEATURE_STAGE = new HashMap<>();

    public static void init() {
        registerFeature(KEY_FEATURE_REPLACE_BLOCK, REPLACE_BLOCK);
        registerFeature(KEY_FEATURE_ROCK_CRYSTAL, ROCK_CRYSTAL);

        registerPlacement(KEY_PLACEMENT_CHANCE, CHANCE);
        registerPlacement(KEY_PLACEMENT_RIVERBED, RIVERBED);
        registerPlacement(KEY_PLACEMENT_WORLD_FILTER, WORLD_FILTER);

        ANCIENT_SHRINE_PIECE = registerStructurePiece(KEY_ANCIENT_SHRINE, AncientShrineStructure::new);
        DESERT_SHRINE_PIECE  = registerStructurePiece(KEY_DESERT_SHRINE,  DesertShrineStructure::new);
        SMALL_SHRINE_PIECE   = registerStructurePiece(KEY_SMALL_SHRINE,   SmallShrineStructure::new);

        STRUCTURE_ANCIENT_SHRINE = registerStructure(KEY_ANCIENT_SHRINE, CFG_ANCIENT_SHRINE, new FeatureAncientShrineStructure());
        STRUCTURE_DESERT_SHRINE  = registerStructure(KEY_DESERT_SHRINE, CFG_DESERT_SHRINE, new FeatureDesertShrineStructure());
        STRUCTURE_SMALL_SHRINE   = registerStructure(KEY_SMALL_SHRINE, CFG_SMALL_SHRINE, new FeatureSmallShrineStructure());

        GEN_GLOW_FLOWER = registerConfiguredFeature(KEY_GLOW_FLOWER, GenerationStep.Decoration.VEGETAL_DECORATION, CFG_GLOW_FLOWER,
                Feature.FLOWER.withConfiguration(new RandomPatchConfiguration.Builder(new SimpleStateProvider(BlocksAS.GLOW_FLOWER.defaultBlockState()), SimpleBlockPlacer.INSTANCE)
                        .tries(12)
                        .build())
                        .countRandom(6)
                        .decorated(Features.Placements.ADD_32)
                        .decorated(Features.Placements.HEIGHTMAP_SQUARE)
                        .decorated(WORLD_FILTER.configured(CFG_GLOW_FLOWER.worldFilterConfig())));
        GEN_ROCK_CRYSTAL = registerConfiguredFeature(KEY_ROCK_CRYSTAL, GenerationStep.Decoration.UNDERGROUND_ORES, CFG_ROCK_CRYSTAL,
                ROCK_CRYSTAL.withConfiguration(new ReplaceBlockConfig(OreConfiguration.FillerBlockType.BASE_STONE_OVERWORLD, BlocksAS.ROCK_CRYSTAL_ORE.defaultBlockState()))
                        .decorated(FeatureDecorator.RANGE.configured(new RangeDecoratorConfiguration(5, 0, 2)))
                        .decorated(CHANCE.withChance(1F / 25F))
                        .decorated(WORLD_FILTER.configured(CFG_ROCK_CRYSTAL.worldFilterConfig())));
        GEN_AQUAMARINE = registerConfiguredFeature(KEY_AQUAMARINE, GenerationStep.Decoration.UNDERGROUND_ORES, CFG_AQUAMARINE,
                REPLACE_BLOCK.withConfiguration(new ReplaceBlockConfig(new TagMatchTest(BlockTags.SAND), BlocksAS.AQUAMARINE_SAND_ORE.defaultBlockState()))
                        .decorated(RIVERBED.configured(NoneDecoratorConfiguration.INSTANCE))
                        .countRandom(8)
                        .decorated(WORLD_FILTER.configured(CFG_AQUAMARINE.worldFilterConfig())));
        GEN_MARBLE = registerConfiguredFeature(KEY_MARBLE, GenerationStep.Decoration.UNDERGROUND_ORES, CFG_MARBLE,
                Feature.ORE.withConfiguration(new OreConfiguration(OreConfiguration.FillerBlockType.BASE_STONE_OVERWORLD, BlocksAS.MARBLE_RAW.defaultBlockState(), 26))
                        .range(96)
                        .square()
                        .countRandom(10)
                        .decorated(WORLD_FILTER.configured(CFG_MARBLE.worldFilterConfig())));
    }

    public static void registerStructureGeneration() {
        List<Map<Structure<?>, StructureFeatureConfiguration>> structureSettings = new ArrayList<>();
        structureSettings.add(NoiseGeneratorSettings.BUILTIN_OVERWORLD.getStructures().structureConfig());
        BuiltinRegistries.NOISE_GENERATOR_SETTINGS.forEach(settings -> structureSettings.add(settings.getStructures().structureConfig()));

        ImmutableMap.Builder<Structure<?>, StructureFeatureConfiguration> builder = ImmutableMap.builder();
        builder.putAll(StructureSettings.DEFAULTS);
        STRUCTURE_FEATURES.forEach((structureFeature, cfg) -> {
            if (cfg.isEnabled()) {
                StructureFeatureConfiguration settings = cfg.createSettings();
                builder.put(structureFeature.feature, settings);
                structureSettings.forEach(noiseStructureSettings -> noiseStructureSettings.put(structureFeature.feature, settings));
            }
        });
        StructureSettings.DEFAULTS = builder.build();

        Structure.NOISE_AFFECTING_FEATURES = ImmutableList.<Structure<?>>builder()
                .addAll(Structure.NOISE_AFFECTING_FEATURES)
                .add(STRUCTURE_ANCIENT_SHRINE, STRUCTURE_DESERT_SHRINE, STRUCTURE_SMALL_SHRINE)
                .build();
    }

    public static void loadBiomeFeatures(BiomeLoadingEvent event) {
        BiomeGenerationSettingsBuilder gen = event.getGeneration();
        STRUCTURE_FEATURES.forEach((structureFeature, cfg) -> {
            if (cfg.isEnabled() && cfg.canGenerateIn(event.getCategory())) {
                gen.withStructure(structureFeature);
            }
        });
        FEATURES.forEach((feature, cfg) -> {
            if (cfg.isEnabled() && cfg.canGenerateIn(event.getCategory())) {
                GenerationStep.Decoration stage = FEATURE_STAGE.get(feature);
                if (stage == null) {
                    ResourceLocation key = BuiltinRegistries.CONFIGURED_FEATURE.getOptionalKey(feature)
                            .map(ResourceKey::getLocation)
                            .orElse(ResourceLocation.parse("not_registered"));
                    throw new IllegalArgumentException("Unknown generation stage for feature " + key + "!");
                }
                gen.addCarver(stage, feature);
            }
        });
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

    private static ConfiguredFeature<?, ?> registerConfiguredFeature(ResourceLocation key, GenerationStep.Decoration stage, FeatureGenerationConfig cfg, ConfiguredFeature<?, ?> feature) {
        FEATURE_STAGE.put(feature, stage);
        FEATURES.put(feature, cfg);
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, key, feature);
    }

    private static void registerFeature(ResourceLocation key, Feature<?> feature) {
        AstralSorcery.getProxy().getRegistryPrimer().register(feature.setRegistryName(key));
    }

    private static void registerPlacement(ResourceLocation key, FeatureDecorator<?> placement) {
        AstralSorcery.getProxy().getRegistryPrimer().register(placement.setRegistryName(key));
    }

    private static <T extends StructurePieceType> T registerStructurePiece(ResourceLocation key, T type) {
        return Registry.register(Registry.STRUCTURE_PIECE, key, type);
    }

    private static <S extends TemplateStructureFeature> S registerStructure(ResourceLocation key, StructureGenerationConfig cfg, S structure) {
        AstralSorcery.getProxy().getRegistryPrimer().register(structure.setRegistryName(key));
        Structure.STRUCTURES_REGISTRY.put(structure.getFeatureName(), structure);
        ConfiguredStructureFeature<?, ?> structureFeature = structure.withConfiguration(FeatureConfiguration.NONE);
        STRUCTURE_FEATURES.put(structureFeature, cfg);
        BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, key, structureFeature);
        return structure;
    }
}
