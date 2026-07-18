/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.tags;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Populates the biome tags gating the mod's worldgen - the 1.21 equivalent
 * of the 1.16 Biome.Category TOML lists (ancient shrine / glow flower:
 * ICY + EXTREME_HILLS, desert shrine: MESA + DESERT + SAVANNA, small shrine:
 * FOREST + PLAINS, ores: every overworld biome).
 *
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AstralBiomeTagsProvider
 * Created by HellFirePvP
 * Date: 06.03.2020 / 21:23
 */
public class AstralBiomeTagsProvider extends TagsProvider<Biome> {

    public AstralBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BIOME, lookupProvider, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(WorldGenerationAS.Tags.HAS_ANCIENT_SHRINE)
                .addTag(Tags.Biomes.IS_SNOWY)
                .addTag(BiomeTags.IS_MOUNTAIN);
        this.tag(WorldGenerationAS.Tags.HAS_DESERT_SHRINE)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(Tags.Biomes.IS_DESERT)
                .addTag(BiomeTags.IS_SAVANNA);
        this.tag(WorldGenerationAS.Tags.HAS_SMALL_SHRINE)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(Tags.Biomes.IS_PLAINS);

        this.tag(WorldGenerationAS.Tags.HAS_GLOW_FLOWER)
                .addTag(Tags.Biomes.IS_SNOWY)
                .addTag(BiomeTags.IS_MOUNTAIN);
        this.tag(WorldGenerationAS.Tags.HAS_ROCK_CRYSTAL)
                .addTag(BiomeTags.IS_OVERWORLD);
        this.tag(WorldGenerationAS.Tags.HAS_AQUAMARINE)
                .addTag(BiomeTags.IS_OVERWORLD);
        this.tag(WorldGenerationAS.Tags.HAS_MARBLE)
                .addTag(BiomeTags.IS_OVERWORLD);
    }
}
