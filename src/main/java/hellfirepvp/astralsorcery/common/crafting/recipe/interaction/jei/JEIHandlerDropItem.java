/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei;

import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.interaction.InteractionResult;
import hellfirepvp.astralsorcery.common.crafting.recipe.interaction.ResultDropItem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIHandlerDropItem
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:50
 */
public class JEIHandlerDropItem extends JEIInteractionResultHandler {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addToRecipeLayout(IRecipeLayoutBuilder builder, LiquidInteraction recipe) {
        InteractionResult result = recipe.getObject();
        if (result instanceof ResultDropItem dropItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 19)
                    .addItemStack(dropItem.getOutput());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawRecipe(LiquidInteraction recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }
}
