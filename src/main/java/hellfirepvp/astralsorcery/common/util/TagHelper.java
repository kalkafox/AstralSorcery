/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TagHelper
 * Created by HellFirePvP
 * Date: 10.07.2026 / 12:00
 */
public final class TagHelper {

    private TagHelper() {}

    public static Stream<Block> getBlocks(TagKey<Block> tag) {
        return StreamSupport.stream(BuiltInRegistries.BLOCK.getTagOrEmpty(tag).spliterator(), false)
                .map(Holder::value);
    }

    public static Stream<Item> getItems(TagKey<Item> tag) {
        return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false)
                .map(Holder::value);
    }

    public static Stream<Fluid> getFluids(TagKey<Fluid> tag) {
        return StreamSupport.stream(BuiltInRegistries.FLUID.getTagOrEmpty(tag).spliterator(), false)
                .map(Holder::value);
    }
}
