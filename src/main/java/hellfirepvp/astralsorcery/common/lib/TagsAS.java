/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.base.Mods;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static hellfirepvp.astralsorcery.common.base.Mods.ASTRAL_SORCERY;
import static hellfirepvp.astralsorcery.common.base.Mods.CURIOS;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TagsAS
 * Created by HellFirePvP
 * Date: 01.06.2019 / 17:08
 */
public class TagsAS {

    private TagsAS() {}

    public static class Blocks {

        public static final TagKey<Block> MARBLE = blockTagForge("marble");
        public static final TagKey<Block> ORES = blockTagForge("ores");

    }

    public static class Items {

        public static final TagKey<Item> CURIOS_NECKLACE = itemTag(CURIOS, "necklace");

        public static final TagKey<Item> FORGE_GEM_AQUAMARINE = itemTagForge("gems/aquamarine");

        public static final TagKey<Item> DUSTS_STARDUST = itemTag(ASTRAL_SORCERY, "stardust");
        public static final TagKey<Item> INGOTS_STARMETAL = itemTag(ASTRAL_SORCERY, "starmetal");
        public static final TagKey<Item> COLORED_LENS = itemTag(ASTRAL_SORCERY, "colored_lens");

    }

    private static TagKey<Block> blockTagForge(String name) {
        return blockTag(Mods.FORGE, name);
    }

    private static TagKey<Block> blockTag(Mods mod, String name) {
        return TagKey.create(Registries.BLOCK, mod.key(name));
    }

    private static TagKey<Item> itemTagForge(String name) {
        return itemTag(Mods.FORGE, name);
    }

    private static TagKey<Item> itemTag(Mods mod, String name) {
        return TagKey.create(Registries.ITEM, mod.key(name));
    }
}
