/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResolvingRecipeType
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:52
 */
public class ResolvingRecipeType<C extends IItemHandler, T extends IHandlerRecipe<C>, R extends RecipeCraftingContext<T, C>> {

    private final ResourceLocation id;
    private final Class<T> baseClass;
    private final BiPredicate<T, R> matchFct;
    private final RecipeType<T> type;

    public ResolvingRecipeType(String name, Class<T> baseClass, BiPredicate<T, R> matchFct) {
        this(AstralSorcery.key(name), baseClass, matchFct);
    }

    public ResolvingRecipeType(ResourceLocation id, Class<T> baseClass, BiPredicate<T, R> matchFct) {
        this.id = id;
        this.baseClass = baseClass;
        this.matchFct = matchFct;
        this.type = new RecipeType<T>() {
            @Override
            public String toString() {
                return ResolvingRecipeType.this.id.getPath();
            }
        };
        // Actual registration happens through AstralRegistries.RECIPE_TYPES, driven by
        // RegistryRecipeTypes.register() - this constructor used to double-register directly
        // against the (now-removed) static Registry.RECIPE_TYPE.
    }

    @Nonnull
    public List<T> getAllRecipes() {
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return Collections.emptyList();
        }
        List<RecipeHolder<T>> recipeSet = mgr.getAllRecipesFor(this.type);
        List<T> recipes = new ArrayList<>(recipeSet.size());
        for (RecipeHolder<T> rec : recipeSet) {
            recipes.add(rec.value());
        }
        return recipes;
    }

    @Nonnull
    public List<T> getRecipes(Predicate<T> test) {
        return this.getAllRecipes().stream()
                .filter(test)
                .collect(Collectors.toList());
    }

    public final Class<T> getBaseClass() {
        return baseClass;
    }

    public RecipeType<T> getType() {
        return type;
    }

    public ResourceLocation getRegistryName() {
        return id;
    }

    @Nullable
    public T findRecipe(R context) {
        return MiscUtils.iterativeSearch(this.getAllRecipes(), (recipe) -> this.matchFct.test(recipe, context));
    }

    @Nonnull
    public List<T> findMatchingRecipes(R context) {
        return this.getAllRecipes().stream()
                .filter((recipe) -> this.matchFct.test(recipe, context))
                .collect(Collectors.toList());
    }
}
