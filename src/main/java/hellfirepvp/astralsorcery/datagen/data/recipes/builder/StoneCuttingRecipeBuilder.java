/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.recipes.builder;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StoneCuttingRecipeBuilder
 * Created by HellFirePvP
 * Date: 22.08.2020 / 16:00
 */
public class StoneCuttingRecipeBuilder {

    private final Ingredient from;
    private final ItemLike output;
    private final int count;

    private StoneCuttingRecipeBuilder(Ingredient from, ItemLike output, int count) {
        this.from = from;
        this.output = output;
        this.count = count;
    }

    public static StoneCuttingRecipeBuilder stoneCuttingRecipe(Ingredient from, ItemLike output) {
        return stoneCuttingRecipe(from, output, 1);
    }

    public static StoneCuttingRecipeBuilder stoneCuttingRecipe(Ingredient from, ItemLike output, int count) {
        return new StoneCuttingRecipeBuilder(from, output, count);
    }

    public void build(Consumer<FinishedRecipe> consumerIn) {
        this.build(consumerIn, BuiltInRegistries.ITEM.getKey(this.output.asItem()));
    }

    public void build(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
        id = NameUtil.prefixPath(id, "stonecutting/");
        consumerIn.accept(new Result(id, this.from, this.output.asItem(), this.count));
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final Ingredient ingredient;
        private final Item result;
        private final int count;

        public Result(ResourceLocation id, Ingredient from, Item output, int count) {
            this.id = id;
            this.ingredient = from;
            this.result = output;
            this.count = count;
        }

        @Override
        public void serialize(JsonObject jsonObject) {
            jsonObject.add("ingredient", this.ingredient.serialize());
            jsonObject.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
            jsonObject.addProperty("count", this.count);
        }

        @Override
        public ResourceLocation getID() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return RecipeSerializer.STONECUTTING;
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
