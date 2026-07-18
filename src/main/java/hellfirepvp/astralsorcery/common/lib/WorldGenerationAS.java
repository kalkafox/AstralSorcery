/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.world.FeatureGenerationConfig;
import hellfirepvp.astralsorcery.common.world.StructureGenerationConfig;
import hellfirepvp.astralsorcery.common.world.feature.ReplaceBlockFeature;
import hellfirepvp.astralsorcery.common.world.feature.RockCrystalFeature;
import hellfirepvp.astralsorcery.common.world.placement.ChancePlacement;
import hellfirepvp.astralsorcery.common.world.placement.RiverbedPlacement;
import hellfirepvp.astralsorcery.common.world.placement.WorldFilteredPlacement;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureAncientShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureDesertShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureSmallShrineStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collections;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldGenerationAS
 * Created by HellFirePvP
 * Date: 23.07.2019 / 20:22
 */
public class WorldGenerationAS {

    public static class Structures {

        public static final ResourceLocation KEY_ANCIENT_SHRINE = AstralSorcery.key("ancient_shrine");
        public static final ResourceLocation KEY_DESERT_SHRINE = AstralSorcery.key("desert_shrine");
        public static final ResourceLocation KEY_SMALL_SHRINE = AstralSorcery.key("small_shrine");

        // Datapack registry keys (structure + structure-set JSONs are emitted by datagen)
        public static final ResourceKey<Structure> STRUCTURE_ANCIENT_SHRINE = ResourceKey.create(Registries.STRUCTURE, KEY_ANCIENT_SHRINE);
        public static final ResourceKey<Structure> STRUCTURE_DESERT_SHRINE = ResourceKey.create(Registries.STRUCTURE, KEY_DESERT_SHRINE);
        public static final ResourceKey<Structure> STRUCTURE_SMALL_SHRINE = ResourceKey.create(Registries.STRUCTURE, KEY_SMALL_SHRINE);

        public static final ResourceKey<StructureSet> SET_ANCIENT_SHRINE = ResourceKey.create(Registries.STRUCTURE_SET, KEY_ANCIENT_SHRINE);
        public static final ResourceKey<StructureSet> SET_DESERT_SHRINE = ResourceKey.create(Registries.STRUCTURE_SET, KEY_DESERT_SHRINE);
        public static final ResourceKey<StructureSet> SET_SMALL_SHRINE = ResourceKey.create(Registries.STRUCTURE_SET, KEY_SMALL_SHRINE);

        public static StructurePieceType ANCIENT_SHRINE_PIECE;
        public static StructurePieceType DESERT_SHRINE_PIECE;
        public static StructurePieceType SMALL_SHRINE_PIECE;

        public static StructureType<FeatureAncientShrineStructure> TYPE_ANCIENT_SHRINE;
        public static StructureType<FeatureDesertShrineStructure> TYPE_DESERT_SHRINE;
        public static StructureType<FeatureSmallShrineStructure> TYPE_SMALL_SHRINE;

    }

    public static class Features {

        public static final ResourceLocation KEY_GLOW_FLOWER = AstralSorcery.key("glow_flower");
        public static final ResourceLocation KEY_ROCK_CRYSTAL = AstralSorcery.key("rock_crystal");
        public static final ResourceLocation KEY_AQUAMARINE = AstralSorcery.key("aquamarine");
        public static final ResourceLocation KEY_MARBLE = AstralSorcery.key("marble");

        public static final ResourceLocation KEY_FEATURE_REPLACE_BLOCK = AstralSorcery.key("replace_block");
        public static final ResourceLocation KEY_FEATURE_ROCK_CRYSTAL = AstralSorcery.key("rock_crystal");

        public static final ReplaceBlockFeature REPLACE_BLOCK = new ReplaceBlockFeature();
        public static final RockCrystalFeature ROCK_CRYSTAL = new RockCrystalFeature();

        // Datapack registry keys (configured/placed feature JSONs are emitted by datagen)
        public static final ResourceKey<ConfiguredFeature<?, ?>> CF_GLOW_FLOWER = ResourceKey.create(Registries.CONFIGURED_FEATURE, KEY_GLOW_FLOWER);
        public static final ResourceKey<ConfiguredFeature<?, ?>> CF_ROCK_CRYSTAL = ResourceKey.create(Registries.CONFIGURED_FEATURE, KEY_ROCK_CRYSTAL);
        public static final ResourceKey<ConfiguredFeature<?, ?>> CF_AQUAMARINE = ResourceKey.create(Registries.CONFIGURED_FEATURE, KEY_AQUAMARINE);
        public static final ResourceKey<ConfiguredFeature<?, ?>> CF_MARBLE = ResourceKey.create(Registries.CONFIGURED_FEATURE, KEY_MARBLE);

        public static final ResourceKey<PlacedFeature> PF_GLOW_FLOWER = ResourceKey.create(Registries.PLACED_FEATURE, KEY_GLOW_FLOWER);
        public static final ResourceKey<PlacedFeature> PF_ROCK_CRYSTAL = ResourceKey.create(Registries.PLACED_FEATURE, KEY_ROCK_CRYSTAL);
        public static final ResourceKey<PlacedFeature> PF_AQUAMARINE = ResourceKey.create(Registries.PLACED_FEATURE, KEY_AQUAMARINE);
        public static final ResourceKey<PlacedFeature> PF_MARBLE = ResourceKey.create(Registries.PLACED_FEATURE, KEY_MARBLE);

    }

    public static class Modifiers {

        // NeoForge biome modifier JSONs adding the placed features to their biome tags
        public static final ResourceKey<BiomeModifier> ADD_GLOW_FLOWER = modifier(Features.KEY_GLOW_FLOWER);
        public static final ResourceKey<BiomeModifier> ADD_ROCK_CRYSTAL = modifier(Features.KEY_ROCK_CRYSTAL);
        public static final ResourceKey<BiomeModifier> ADD_AQUAMARINE = modifier(Features.KEY_AQUAMARINE);
        public static final ResourceKey<BiomeModifier> ADD_MARBLE = modifier(Features.KEY_MARBLE);

        private static ResourceKey<BiomeModifier> modifier(ResourceLocation key) {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, key);
        }

    }

    public static class Placements {

        public static final ResourceLocation KEY_PLACEMENT_CHANCE = AstralSorcery.key("chance");
        public static final ResourceLocation KEY_PLACEMENT_RIVERBED = AstralSorcery.key("riverbed");
        public static final ResourceLocation KEY_PLACEMENT_WORLD_FILTER = AstralSorcery.key("world_filter");

        public static PlacementModifierType<ChancePlacement> CHANCE;
        public static PlacementModifierType<RiverbedPlacement> RIVERBED;
        public static PlacementModifierType<WorldFilteredPlacement> WORLD_FILTER;

    }

    public static class Tags {

        // Biome membership for the mod's worldgen; populated by datagen, and the
        // 1.21 replacement for the old Biome.Category TOML lists.
        public static final TagKey<Biome> HAS_ANCIENT_SHRINE = biomeTag("has_structure/ancient_shrine");
        public static final TagKey<Biome> HAS_DESERT_SHRINE = biomeTag("has_structure/desert_shrine");
        public static final TagKey<Biome> HAS_SMALL_SHRINE = biomeTag("has_structure/small_shrine");

        public static final TagKey<Biome> HAS_GLOW_FLOWER = biomeTag("has_feature/glow_flower");
        public static final TagKey<Biome> HAS_ROCK_CRYSTAL = biomeTag("has_feature/rock_crystal");
        public static final TagKey<Biome> HAS_AQUAMARINE = biomeTag("has_feature/aquamarine");
        public static final TagKey<Biome> HAS_MARBLE = biomeTag("has_feature/marble");

        private static TagKey<Biome> biomeTag(String path) {
            return TagKey.create(Registries.BIOME, AstralSorcery.key(path));
        }

    }

    public static class Config {

        public static StructureGenerationConfig CFG_ANCIENT_SHRINE =
                new StructureGenerationConfig(Structures.KEY_ANCIENT_SHRINE, 18, 4);
        public static StructureGenerationConfig CFG_DESERT_SHRINE =
                new StructureGenerationConfig(Structures.KEY_DESERT_SHRINE, 18, 4);
        public static StructureGenerationConfig CFG_SMALL_SHRINE =
                new StructureGenerationConfig(Structures.KEY_SMALL_SHRINE, 18, 4);

        public static FeatureGenerationConfig CFG_GLOW_FLOWER =
                new FeatureGenerationConfig(Features.KEY_GLOW_FLOWER)
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));
        public static FeatureGenerationConfig CFG_ROCK_CRYSTAL =
                new FeatureGenerationConfig(Features.KEY_ROCK_CRYSTAL)
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));
        public static FeatureGenerationConfig CFG_AQUAMARINE =
                new FeatureGenerationConfig(Features.KEY_AQUAMARINE)
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));
        public static FeatureGenerationConfig CFG_MARBLE =
                new FeatureGenerationConfig(Features.KEY_MARBLE)
                        .setGenerateEveryWorld();
    }
}
