/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.recipes.builder;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleShapelessRecipeBuilder
 * Created by HellFirePvP
 * Date: 30.11.2020 / 20:28
 */
public class SimpleShapelessRecipeBuilder {

    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();

    private String subDirectory = null;

    public SimpleShapelessRecipeBuilder(ItemLike result, int count) {
        this.result = result.asItem();
        this.count = count;
    }

    public static SimpleShapelessRecipeBuilder shapelessRecipe(ItemLike result) {
        return shapelessRecipe(result, 1);
    }

    public static SimpleShapelessRecipeBuilder shapelessRecipe(ItemLike result, int count) {
        return new SimpleShapelessRecipeBuilder(result, count);
    }

    public SimpleShapelessRecipeBuilder addIngredient(Tag<Item> tagIn) {
        return this.addIngredient(Ingredient.fromTag(tagIn));
    }

    public SimpleShapelessRecipeBuilder addIngredient(ItemLike itemIn) {
        return this.addIngredient(itemIn, 1);
    }
    public SimpleShapelessRecipeBuilder addIngredient(ItemLike itemIn, int quantity) {
        for(int i = 0; i < quantity; ++i) {
            this.addIngredient(Ingredient.valueFromJson(itemIn));
        }

        return this;
    }

    public SimpleShapelessRecipeBuilder addIngredient(Ingredient ingredientIn) {
        return this.addIngredient(ingredientIn, 1);
    }

    public SimpleShapelessRecipeBuilder addIngredient(Ingredient ingredientIn, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            this.ingredients.add(ingredientIn);
        }
        return this;
    }

    public SimpleShapelessRecipeBuilder subDirectory(String dir) {
        this.subDirectory = dir;
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumerIn) {
        this.build(consumerIn, BuiltInRegistries.ITEM.getKey(this.result.getItem()));
    }

    public void build(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
        String path = id.getPath();
        if (this.subDirectory != null && !this.subDirectory.isEmpty()) {
            path = this.subDirectory + "/" + path;
        }
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "shapeless/" + path);
        consumerIn.accept(new Result(id, this.result, this.count, this.ingredients));
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation key;
        private final Item result;
        private final int count;
        private final List<Ingredient> ingredients;

        public Result(ResourceLocation key, Item result, int resultCount, List<Ingredient> ingredients) {
            this.key = key;
            this.result = result;
            this.count = resultCount;
            this.ingredients = ingredients;
        }

        @Override
        public void serialize(JsonObject json) {
            JsonArray map = new JsonArray();
            for (Ingredient ingredient : this.ingredients) {
                map.add(ingredient.serialize());
            }
            json.add("ingredients", map);

            JsonObject result = new JsonObject();
            result.addProperty("item", Registry.ITEM.getKey(this.result).toString());
            if (this.count > 1) {
                result.addProperty("count", this.count);
            }

            json.add("result", result);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        @Override
        public ResourceLocation getID() {
            return this.key;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementID() {
            return ResourceLocation.parse("");
        }
    }
}
