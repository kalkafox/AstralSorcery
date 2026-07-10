/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigDataSet;
import hellfirepvp.astralsorcery.common.data.config.entry.GeneralConfig;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RegistryHelper;
import hellfirepvp.astralsorcery.common.util.TagHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.tags.TagKey;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: OreBlockRarityEntry
 * Created by HellFirePvP
 * Date: 01.09.2019 / 00:10
 */
public class OreBlockRarityEntry implements ConfigDataSet {

    private final TagKey<Block> blockTag;
    private final ResourceLocation key;
    private final int weight;

    public OreBlockRarityEntry(TagKey<Block> blockTag, ResourceLocation key, int weight) {
        this.blockTag = blockTag;
        this.key = key;
        this.weight = weight;
    }

    public OreBlockRarityEntry(TagKey<Block> blockTag, int weight) {
        this(blockTag, blockTag.location(), weight);
    }

    public int getWeight() {
        return weight;
    }

    @Nullable
    public Block getRandomBlock(Random random) {
        return MiscUtils.getRandomEntry(TagHelper.getBlocks(this.blockTag)
                .filter(item -> !GeneralConfig.CONFIG.modidOreBlacklist.get().contains(RegistryHelper.getKey(item).getNamespace()))
                .collect(Collectors.toList()), random);
    }

    @Nullable
    public static OreBlockRarityEntry deserialize(String str) throws IllegalArgumentException {
        String[] split = str.split(";");
        if (split.length != 2) {
            return null;
        }
        ResourceLocation keyBlockTag = ResourceLocation.parse(split[0]);
        TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, keyBlockTag);
        if (blockTag == null) {
            return null;
        }
        String strWeight = split[1];
        int weight;
        try {
            weight = Integer.parseInt(strWeight);
        } catch (NumberFormatException exc) {
            return null;
        }
        return new OreBlockRarityEntry(blockTag, keyBlockTag, weight);
    }

    @Nonnull
    @Override
    public String serialize() {
        return String.format("%s;%s", key.toString(), weight);
    }
}
