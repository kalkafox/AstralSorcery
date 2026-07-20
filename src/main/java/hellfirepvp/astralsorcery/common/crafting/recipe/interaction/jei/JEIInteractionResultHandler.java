/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei;

import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIInteractionResultHandler
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:47
 */
public abstract class JEIInteractionResultHandler {

    @OnlyIn(Dist.CLIENT)
    public abstract void addToRecipeLayout(IRecipeLayoutBuilder builder, LiquidInteraction recipe);

    @OnlyIn(Dist.CLIENT)
    public abstract void drawRecipe(LiquidInteraction recipe, GuiGraphics guiGraphics, double mouseX, double mouseY);

}
