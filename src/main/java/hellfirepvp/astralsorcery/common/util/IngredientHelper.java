/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import net.minecraft.tags.TagKey;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IngredientHelper
 * Created by HellFirePvP
 * Date: 11.10.2019 / 22:18
 */
public class IngredientHelper {

    @OnlyIn(Dist.CLIENT)
    public static ItemStack getRandomVisibleStack(Ingredient ingredient) {
        return getRandomVisibleStack(ingredient, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemStack getRandomVisibleStack(Ingredient ingredient, long tick) {
        List<ItemStack> applicable = getVisibleItemStacks(ingredient);
        if (applicable.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int mod = (int) ((tick / 20L) % applicable.size());
        return applicable.get(Mth.clamp(mod, 0, applicable.size() - 1));
    }

    @OnlyIn(Dist.CLIENT)
    public static List<ItemStack> getVisibleItemStacks(Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(ingredient.getItems());
    }

    @Nullable
    public static TagKey<Item> guessTag(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) {
            return null;
        }
        List<TagKey<Item>> applicableTags = new ArrayList<>();
        ItemStack first = stacks[0];
        first.getTags().forEach(tagKey -> {
            boolean containsAllItems = TagHelper.getItems(tagKey)
                    .allMatch(itemInTag -> ingredient.test(new ItemStack(itemInTag)));
            if (containsAllItems) {
                applicableTags.add(tagKey);
            }
        });

        return applicableTags.stream()
                .max(Comparator.comparingLong(tag -> TagHelper.getItems(tag).count()))
                .orElse(null);
    }

}
