/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryInfuser
 * Created by HellFirePvP
 * Date: 05.09.2020 / 12:37
 */
public class CategoryInfuser extends JEICategory<LiquidInfusion> {

    private static final int[][] FLUID_POSITIONS = new int[][] {
            {30, 57}, {49, 57}, {68, 57},
            {11, 76}, {87, 76},
            {11, 95}, {87, 95},
            {11, 114}, {87, 114},
            {30, 133}, {49, 133}, {68, 133}
    };

    private final IDrawable background, icon;

    public CategoryInfuser(IGuiHelper guiHelper) {
        super(IntegrationJEI.TYPE_INFUSER);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/infuser.png"), 0, 0, 116, 162);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.INFUSER));
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
    public List<LiquidInfusion> getRecipes() {
        return RecipeTypesAS.TYPE_INFUSION.getAllRecipes();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiquidInfusion recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 50, 96)
                .addIngredients(recipe.getItemInput());

        for (int[] position : FLUID_POSITIONS) {
            builder.addSlot(RecipeIngredientRole.INPUT, position[0] + 1, position[1] + 1)
                    .setFluidRenderer(FluidType.BUCKET_VOLUME, false, 16, 16)
                    .addFluidStack(recipe.getLiquidInput(), FluidType.BUCKET_VOLUME);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 19)
                .addItemStack(recipe.getOutputForRender(Collections.emptyList()));
    }
}
