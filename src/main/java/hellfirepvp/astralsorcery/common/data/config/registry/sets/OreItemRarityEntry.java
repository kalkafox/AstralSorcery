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
import net.minecraft.world.item.Item;
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
 * Class: OreItemRarityEntry
 * Created by HellFirePvP
 * Date: 31.08.2019 / 23:46
 */
public class OreItemRarityEntry implements ConfigDataSet {

    private final TagKey<Item> itemTag;
    private final ResourceLocation key;
    private final int weight;

    public OreItemRarityEntry(TagKey<Item> itemTag, ResourceLocation key, int weight) {
        this.itemTag = itemTag;
        this.key = key;
        this.weight = weight;
    }

    public OreItemRarityEntry(TagKey<Item> itemTag, int weight) {
        this(itemTag, itemTag.location(), weight);
    }

    public int getWeight() {
        return weight;
    }

    @Nullable
    public Item getRandomItem(Random random) {
        return MiscUtils.getRandomEntry(TagHelper.getItems(this.itemTag)
                .filter(item -> !GeneralConfig.CONFIG.modidOreBlacklist.get().contains(RegistryHelper.getKey(item).getNamespace()))
                .collect(Collectors.toList()), random);
    }

    @Nullable
    public static OreItemRarityEntry deserialize(String str) throws IllegalArgumentException {
        String[] split = str.split(";");
        if (split.length != 2) {
            return null;
        }
        ResourceLocation keyItemTag = ResourceLocation.parse(split[0]);
        TagKey<Item> itemTag = TagKey.create(Registries.ITEM, keyItemTag);
        if (itemTag == null) {
            return null;
        }
        String strWeight = split[1];
        int weight;
        try {
            weight = Integer.parseInt(strWeight);
        } catch (NumberFormatException exc) {
            return null;
        }
        return new OreItemRarityEntry(itemTag, keyItemTag, weight);
    }

    @Nonnull
    @Override
    public String serialize() {
        return String.format("%s;%s", key.toString(), weight);
    }
}
