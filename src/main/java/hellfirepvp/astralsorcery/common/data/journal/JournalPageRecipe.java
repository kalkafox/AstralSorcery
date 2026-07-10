/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageAltarRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageText;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JournalPageRecipe
 * Created by HellFirePvP
 * Date: 11.10.2019 / 22:29
 */
public class JournalPageRecipe implements JournalPage {

    private final Supplier<Recipe<?>> recipeProvider;

    private JournalPageRecipe(Supplier<Recipe<?>> recipeProvider) {
        this.recipeProvider = recipeProvider;
    }

    public static JournalPageRecipe fromName(ResourceLocation recipeId) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            Recipe<?> recipe = mgr.getRecipes(RecipeTypesAS.TYPE_ALTAR.getType()).get(recipeId);
            if (recipe != null) {
                return recipe;
            }

            recipe = mgr.getRecipes(RecipeType.CRAFTING).get(recipeId);
            if (recipe != null) {
                return recipe;
            }
            return null;
        });
    }

    public static JournalPageRecipe fromOutputPreferAltarRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            Recipe<?> recipe = mgr.getRecipes(RecipeTypesAS.TYPE_ALTAR.getType()).values()
                    .stream()
                    .map(r -> (SimpleAltarRecipe) r)
                    .filter(r -> outputTest.test(r.getOutputForRender(Collections.emptyList())))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }

            recipe = mgr.getRecipes(RecipeType.CRAFTING).values()
                    .stream()
                    .filter(r -> outputTest.test(r.getResultItem()))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }
            return null;
        });
    }

    public static JournalPageRecipe fromOutputPreferVanillaRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            Recipe<?> recipe = mgr.getRecipes(RecipeType.CRAFTING).values()
                    .stream()
                    .filter(r -> outputTest.test(r.getResultItem()))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }

            recipe = mgr.getRecipes(RecipeTypesAS.TYPE_ALTAR.getType()).values()
                    .stream()
                    .map(r -> (SimpleAltarRecipe) r)
                    .filter(r -> outputTest.test(r.getOutputForRender(Collections.emptyList())))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }
            return null;
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        Recipe<?> recipe = this.recipeProvider.get();
        if (recipe instanceof SimpleAltarRecipe) {
            return new RenderPageAltarRecipe(node, nodePage, (SimpleAltarRecipe) recipe);
        } else if (recipe != null) {
            return RenderPageRecipe.fromRecipe(node, nodePage, recipe);
        } else {
            return new RenderPageText("astralsorcery.journal.recipe.removalinfo");
        }
    }
}
