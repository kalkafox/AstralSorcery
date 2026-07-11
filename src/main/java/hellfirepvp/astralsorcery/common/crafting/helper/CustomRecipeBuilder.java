/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomRecipeBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 09:57
 *
 * {@code IFinishedRecipe}/{@code FinishedRecipe} (manual JSON serialization callback) is gone in
 * 1.21 - datagen now hands recipes straight to a {@link RecipeOutput}, which serializes them
 * itself via {@link net.minecraft.world.item.crafting.RecipeSerializer#codec()}, so there's no
 * more need for the {@code WrappedCustomRecipe}/{@code FinishedRecipe} shim this class used to
 * build.
 */
public abstract class CustomRecipeBuilder<R extends CustomMatcherRecipe> {

    private static final Map<RecipeType<?>, Set<ResourceLocation>> builtRecipes = new HashMap<>();

    public void build(RecipeOutput output) {
        this.build(output, null);
    }

    public void build(RecipeOutput output, @Nullable String directory) {
        R recipe = this.validateAndGet();

        String saveId = recipe.getId().getPath();
        if (directory != null) {
            saveId = directory + "/" + saveId;
        }
        saveId = this.getSerializer().getRegistryName().getPath() + "/" + saveId;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(recipe.getId().getNamespace(), saveId);

        if (!builtRecipes.computeIfAbsent(recipe.getType(), type -> new HashSet<>()).add(id)) {
            throw new IllegalArgumentException("Tried to register recipe with id " + id + " twice for type " + BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
        }
        output.accept(id, recipe, null);
    }

    @Nonnull
    protected abstract R validateAndGet();

    protected abstract CustomRecipeSerializer<R> getSerializer();
}
