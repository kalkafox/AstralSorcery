/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryTransmutation
 * Created by HellFirePvP
 * Date: 05.09.2020 / 12:37
 */
public class CategoryTransmutation extends JEICategory<BlockTransmutation> {

    private final IDrawable background, icon;

    public CategoryTransmutation(IGuiHelper guiHelper) {
        super(IntegrationJEI.TYPE_TRANSMUTATION);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/transmutation.png"), 0, 0, 116, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.LENS));
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public int getWidth() {
        return this.background.getWidth();
    }

    @Override
    public int getHeight() {
        return this.background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public List<BlockTransmutation> getRecipes() {
        return RecipeTypesAS.TYPE_BLOCK_TRANSMUTATION.getAllRecipes();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockTransmutation recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 18)
                .addItemStacks(recipe.getInputDisplay());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addItemStack(recipe.getOutputDisplay());
    }
}
