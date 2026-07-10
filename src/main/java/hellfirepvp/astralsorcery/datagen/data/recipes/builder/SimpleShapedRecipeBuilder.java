/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.recipes.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.tags.TagKey;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleShapedRecipeBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 09:45
 */
public class SimpleShapedRecipeBuilder {

    private final ItemStack result;
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();

    private String subDirectory = null;

    private SimpleShapedRecipeBuilder(ItemLike result, int count) {
        this(new ItemStack(result.asItem(), count));
    }

    private SimpleShapedRecipeBuilder(ItemStack result) {
        this.result = result.copy();
    }

    public static SimpleShapedRecipeBuilder shapedRecipe(ItemLike result) {
        return shapedRecipe(result, 1);
    }

    public static SimpleShapedRecipeBuilder shapedRecipe(ItemLike result, int count) {
        return new SimpleShapedRecipeBuilder(result, count);
    }

    public SimpleShapedRecipeBuilder key(Character symbol, TagKey<Item> tag) {
        return this.key(symbol, Ingredient.fromTag(tag));
    }

    public SimpleShapedRecipeBuilder key(Character symbol, ItemLike item) {
        return this.key(symbol, Ingredient.valueFromJson(item));
    }

    public SimpleShapedRecipeBuilder key(Character symbol, Ingredient ingredientIn) {
        if (this.key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(symbol, ingredientIn);
            return this;
        }
    }

    public SimpleShapedRecipeBuilder patternLine(String patternIn) {
        if (!this.pattern.isEmpty() && patternIn.length() != this.pattern.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.pattern.add(patternIn);
            return this;
        }
    }

    public SimpleShapedRecipeBuilder subDirectory(String dir) {
        this.subDirectory = dir;
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumerIn) {
        this.build(consumerIn, BuiltInRegistries.ITEM.getKey(this.result.getItem()));
    }

    public void build(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
        this.validate(id);
        String path = id.getPath();
        if (this.subDirectory != null && !this.subDirectory.isEmpty()) {
            path = this.subDirectory + "/" + path;
        }
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "shaped/" + path);
        consumerIn.accept(new Result(id, this.result, this.pattern, this.key));
    }

    private void validate(ResourceLocation id) {
        if (this.pattern.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
        } else {
            Set<Character> set = Sets.newHashSet(this.key.keySet());
            set.remove(' ');

            for(String s : this.pattern) {
                for(int i = 0; i < s.length(); ++i) {
                    char c0 = s.charAt(i);
                    if (!this.key.containsKey(c0) && c0 != ' ') {
                        throw new IllegalStateException("Pattern in recipe " + id + " uses undefined symbol '" + c0 + "'");
                    }

                    set.remove(c0);
                }
            }

            if (!set.isEmpty()) {
                throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + id);
            } else if (this.pattern.size() == 1 && this.pattern.get(0).length() == 1) {
                throw new IllegalStateException("Shaped recipe " + id + " only takes in a single item - should it be a shapeless recipe instead?");
            }
        }
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final ItemStack result;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;

        public Result(ResourceLocation idIn, ItemStack resultIn, List<String> patternIn, Map<Character, Ingredient> keyIn) {
            this.id = idIn;
            this.result = resultIn;
            this.pattern = patternIn;
            this.key = keyIn;
        }

        public void serialize(JsonObject json) {
            JsonArray jsonarray = new JsonArray();
            for(String s : this.pattern) {
                jsonarray.add(s);
            }
            json.add("pattern", jsonarray);

            JsonObject keys = new JsonObject();
            for(Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                keys.add(String.valueOf(entry.getKey()), entry.getValue().serialize());
            }
            json.add("key", keys);

            json.add("result", JsonHelper.serializeItemStack(this.result));
        }

        public RecipeSerializer<?> getSerializer() {
            return RecipeSerializer.SHAPED_RECIPE;
        }

        public ResourceLocation getID() {
            return this.id;
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        public ResourceLocation getAdvancementID() {
            return ResourceLocation.parse("");
        }
    }
}
