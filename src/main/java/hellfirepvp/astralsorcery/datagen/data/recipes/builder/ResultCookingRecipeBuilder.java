/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.recipes.builder;

import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import hellfirepvp.astralsorcery.common.util.RegistryHelper;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResultCookingRecipeBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 08:11
 */
//ItemStack (item + count) result sensitive version...
public class ResultCookingRecipeBuilder {

    private final ItemStack result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final SimpleCookingSerializer<?> serializer;

    private ResultCookingRecipeBuilder(ItemStack result, Ingredient ingredientIn, float experienceIn, int cookingTimeIn, SimpleCookingSerializer<?> serializer) {
        this.result = result.copy();
        this.ingredient = ingredientIn;
        this.experience = experienceIn;
        this.cookingTime = cookingTimeIn;
        this.serializer = serializer;
    }

    public static ResultCookingRecipeBuilder cooking(Ingredient ingredientIn, ItemStack result, float experienceIn, int cookingTimeIn, SimpleCookingSerializer<?> serializer) {
        return new ResultCookingRecipeBuilder(result, ingredientIn, experienceIn, cookingTimeIn, serializer);
    }

    public static ResultCookingRecipeBuilder blasting(Ingredient ingredientIn, ItemStack result, float experienceIn, int cookingTimeIn) {
        return cooking(ingredientIn, result, experienceIn, cookingTimeIn, RecipeSerializer.BLASTING);
    }

    public static ResultCookingRecipeBuilder smelting(Ingredient ingredientIn, ItemStack result, float experienceIn, int cookingTimeIn) {
        return cooking(ingredientIn, result, experienceIn, cookingTimeIn, RecipeSerializer.SMELTING);
    }

    public void build(Consumer<FinishedRecipe> consumerIn) {
        this.build(consumerIn, BuiltInRegistries.ITEM.getKey(this.result.getItem()));
    }

    public void build(Consumer<FinishedRecipe> consumerIn, String save) {
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(this.result.getItem());
        ResourceLocation saveNameKey = ResourceLocation.parse(save);
        if (saveNameKey.equals(itemKey)) {
            throw new IllegalStateException("Recipe " + saveNameKey + " should remove its 'save' argument");
        } else {
            this.build(consumerIn, saveNameKey);
        }
    }

    public void build(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), RegistryHelper.getKey(this.serializer).getPath() + "/" + id.getPath());
        consumerIn.accept(new Result(id, this.ingredient, this.result, this.experience, this.cookingTime, this.serializer));
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final Ingredient ingredient;
        private final ItemStack result;
        private final float experience;
        private final int cookingTime;
        private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

        public Result(ResourceLocation idIn, Ingredient ingredientIn, ItemStack resultIn, float experienceIn, int cookingTimeIn, RecipeSerializer<? extends AbstractCookingRecipe> serializerIn) {
            this.id = idIn;
            this.ingredient = ingredientIn;
            this.result = resultIn;
            this.experience = experienceIn;
            this.cookingTime = cookingTimeIn;
            this.serializer = serializerIn;
        }

        public void serialize(JsonObject json) {
            JsonObject itemResult = new JsonObject();
            itemResult.addProperty("item", RegistryHelper.getKey(this.result.getItem()).toString());
            itemResult.addProperty("count", this.result.getCount());

            json.add("ingredient", this.ingredient.serialize());
            json.add("result", itemResult);
            json.addProperty("experience", this.experience);
            json.addProperty("cookingtime", this.cookingTime);
        }

        public RecipeSerializer<?> getSerializer() {
            return this.serializer;
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
