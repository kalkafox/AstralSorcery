/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.recipes.builder;

import hellfirepvp.astralsorcery.common.util.RegistryHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;

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
    private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;
    private final AbstractCookingRecipe.Factory<? extends AbstractCookingRecipe> factory;

    private ResultCookingRecipeBuilder(ItemStack result, Ingredient ingredientIn, float experienceIn, int cookingTimeIn,
                                       RecipeSerializer<? extends AbstractCookingRecipe> serializer,
                                       AbstractCookingRecipe.Factory<? extends AbstractCookingRecipe> factory) {
        this.result = result.copy();
        this.ingredient = ingredientIn;
        this.experience = experienceIn;
        this.cookingTime = cookingTimeIn;
        this.serializer = serializer;
        this.factory = factory;
    }

    public static ResultCookingRecipeBuilder blasting(Ingredient ingredientIn, ItemStack result, float experienceIn, int cookingTimeIn) {
        return new ResultCookingRecipeBuilder(result, ingredientIn, experienceIn, cookingTimeIn, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new);
    }

    public static ResultCookingRecipeBuilder smelting(Ingredient ingredientIn, ItemStack result, float experienceIn, int cookingTimeIn) {
        return new ResultCookingRecipeBuilder(result, ingredientIn, experienceIn, cookingTimeIn, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new);
    }

    public void build(RecipeOutput output) {
        this.build(output, BuiltInRegistries.ITEM.getKey(this.result.getItem()));
    }

    public void build(RecipeOutput output, String save) {
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(this.result.getItem());
        ResourceLocation saveNameKey = ResourceLocation.parse(save);
        if (saveNameKey.equals(itemKey)) {
            throw new IllegalStateException("Recipe " + saveNameKey + " should remove its 'save' argument");
        } else {
            this.build(output, saveNameKey);
        }
    }

    public void build(RecipeOutput output, ResourceLocation id) {
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), RegistryHelper.getKey(this.serializer).getPath() + "/" + id.getPath());
        output.accept(id, this.factory.create("", CookingBookCategory.MISC, this.ingredient, this.result, this.experience, this.cookingTime), null);
    }
}
