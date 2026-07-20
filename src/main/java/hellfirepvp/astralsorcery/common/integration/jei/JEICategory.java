/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.common.crafting.helper.CustomMatcherRecipe;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEICategory
 * Created by HellFirePvP
 * Date: 05.09.2020 / 12:38
 */
public abstract class JEICategory<T extends CustomMatcherRecipe> implements IRecipeCategory<T> {

    private final RecipeType<T> recipeType;
    private final Component title;

    public JEICategory(RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        ResourceLocation uid = recipeType.getUid();
        this.title = Component.translatable(String.format("jei.category.%s.%s", uid.getNamespace(), uid.getPath()));
    }

    protected static List<ItemStack> ingredientStacks(Ingredient ingredient) {
        return Arrays.asList(ingredient.getItems());
    }

    public abstract List<T> getRecipes();

    @Override
    public RecipeType<T> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }
}
