/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.recipes.builder;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

import java.util.List;

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

    public SimpleShapelessRecipeBuilder addIngredient(TagKey<Item> tagIn) {
        return this.addIngredient(Ingredient.of(tagIn));
    }

    public SimpleShapelessRecipeBuilder addIngredient(ItemLike itemIn) {
        return this.addIngredient(itemIn, 1);
    }
    public SimpleShapelessRecipeBuilder addIngredient(ItemLike itemIn, int quantity) {
        for(int i = 0; i < quantity; ++i) {
            this.addIngredient(Ingredient.of(itemIn));
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

    public void build(RecipeOutput output) {
        this.build(output, BuiltInRegistries.ITEM.getKey(this.result));
    }

    public void build(RecipeOutput output, ResourceLocation id) {
        String path = id.getPath();
        if (this.subDirectory != null && !this.subDirectory.isEmpty()) {
            path = this.subDirectory + "/" + path;
        }
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "shapeless/" + path);

        NonNullList<Ingredient> recipeIngredients = NonNullList.createWithCapacity(this.ingredients.size());
        recipeIngredients.addAll(this.ingredients);
        output.accept(id, new ShapelessRecipe("", CraftingBookCategory.MISC, new ItemStack(this.result, this.count), recipeIngredients), null);
    }
}
