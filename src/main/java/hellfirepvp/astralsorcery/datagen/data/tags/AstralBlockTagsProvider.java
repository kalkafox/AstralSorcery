/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.tags;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static hellfirepvp.astralsorcery.common.lib.TagsAS.Blocks.MARBLE;
import static hellfirepvp.astralsorcery.common.lib.TagsAS.Blocks.ORES;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AstralBlockTagsProvider
 * Created by HellFirePvP
 * Date: 06.03.2020 / 21:23
 */
public class AstralBlockTagsProvider extends BlockTagsProvider {

    public AstralBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(BlocksAS.STARMETAL);

        this.tag(MARBLE)
                .add(BlocksAS.MARBLE_RAW)
                .add(BlocksAS.MARBLE_ARCH)
                .add(BlocksAS.MARBLE_BRICKS)
                .add(BlocksAS.MARBLE_CHISELED)
                .add(BlocksAS.MARBLE_ENGRAVED)
                .add(BlocksAS.MARBLE_PILLAR)
                .add(BlocksAS.MARBLE_RUNED);

        this.tag(ORES)
                .add(BlocksAS.STARMETAL_ORE)
                .add(BlocksAS.AQUAMARINE_SAND_ORE)
                .add(BlocksAS.ROCK_CRYSTAL_ORE);
    }
}
