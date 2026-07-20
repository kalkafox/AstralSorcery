/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryWell
 * Created by HellFirePvP
 * Date: 05.09.2020 / 12:37
 */
public class CategoryWell extends JEICategory<WellLiquefaction> {

    private final IDrawable background, icon;

    public CategoryWell(IGuiHelper guiHelper) {
        super(IntegrationJEI.TYPE_WELL);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/lightwell.png"), 0, 0, 116, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.WELL));
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
    public void draw(WellLiquefaction recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.icon.draw(guiGraphics, 46, 20);
    }

    @Override
    public List<WellLiquefaction> getRecipes() {
        return RecipeTypesAS.TYPE_WELL.getAllRecipes();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WellLiquefaction recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 19)
                .addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .setFluidRenderer(FluidType.BUCKET_VOLUME, false, 16, 16)
                .addFluidStack(recipe.getFluidOutput(), FluidType.BUCKET_VOLUME);
    }
}
