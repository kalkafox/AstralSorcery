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
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
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

    private final Supplier<RecipeHolder<?>> recipeProvider;

    private JournalPageRecipe(Supplier<RecipeHolder<?>> recipeProvider) {
        this.recipeProvider = recipeProvider;
    }

    public static JournalPageRecipe fromName(ResourceLocation recipeId) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            return mgr.byKey(recipeId).orElse(null);
        });
    }

    public static JournalPageRecipe fromOutputPreferAltarRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            RecipeHolder<?> recipe = mgr.getAllRecipesFor(RecipeTypesAS.TYPE_ALTAR.getType())
                    .stream()
                    .filter(h -> outputTest.test(((SimpleAltarRecipe) h.value()).getOutputForRender(Collections.emptyList())))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }

            return mgr.getAllRecipesFor(RecipeType.CRAFTING)
                    .stream()
                    .filter(h -> outputTest.test(h.value().getResultItem(Minecraft.getInstance().level.registryAccess())))
                    .findFirst()
                    .orElse(null);
        });
    }

    public static JournalPageRecipe fromOutputPreferVanillaRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            RecipeHolder<?> recipe = mgr.getAllRecipesFor(RecipeType.CRAFTING)
                    .stream()
                    .filter(h -> outputTest.test(h.value().getResultItem(Minecraft.getInstance().level.registryAccess())))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }

            return mgr.getAllRecipesFor(RecipeTypesAS.TYPE_ALTAR.getType())
                    .stream()
                    .filter(h -> outputTest.test(((SimpleAltarRecipe) h.value()).getOutputForRender(Collections.emptyList())))
                    .findFirst()
                    .orElse(null);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        RecipeHolder<?> recipe = this.recipeProvider.get();
        if (recipe != null && recipe.value() instanceof SimpleAltarRecipe altarRecipe) {
            return new RenderPageAltarRecipe(node, nodePage, altarRecipe);
        } else if (recipe != null) {
            return RenderPageRecipe.fromRecipe(node, nodePage, recipe);
        } else {
            return new RenderPageText("astralsorcery.journal.recipe.removalinfo");
        }
    }
}
