/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteractionContext;
import hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei.JEIInteractionResultRegistry;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryLiquidInteraction
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:31
 */
public class CategoryLiquidInteraction extends JEICategory<LiquidInteraction> {

    private static final DecimalFormat FORMAT_CHANCE = new DecimalFormat("0.00");

    private final IDrawable background, icon;

    public CategoryLiquidInteraction(IGuiHelper guiHelper) {
        super(IntegrationJEI.TYPE_LIQUID_INTERACTION);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/interaction.png"), 0, 0, 112, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.CHALICE));
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
    public List<LiquidInteraction> getRecipes() {
        return RecipeTypesAS.TYPE_LIQUID_INTERACTION.getAllRecipes();
    }

    @Override
    public void draw(LiquidInteraction recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.icon.draw(guiGraphics, 3, 36);
        this.icon.draw(guiGraphics, 93, 36);

        JEIInteractionResultRegistry.get(recipe.getObject().getId())
                .ifPresent(handler -> handler.drawRecipe(recipe, guiGraphics, mouseX, mouseY));

        FluidStack testMatch1 = new FluidStack(recipe.getReactant1().getFluid(), FluidType.BUCKET_VOLUME);
        FluidStack testMatch2 = new FluidStack(recipe.getReactant2().getFluid(), FluidType.BUCKET_VOLUME);
        LiquidInteractionContext ctx = new LiquidInteractionContext(testMatch1, testMatch2);
        Collection<LiquidInteraction> sameInteractions = RecipeTypesAS.TYPE_LIQUID_INTERACTION.findMatchingRecipes(ctx);
        if (!sameInteractions.isEmpty()) {
            int totalWeight = sameInteractions.stream().mapToInt(LiquidInteraction::getWeight).sum();
            float perc = ((float) recipe.getWeight() / totalWeight) * 100;

            Font fr = Minecraft.getInstance().font;
            MutableComponent txt = Component.translatable("jei.astralsorcery.tip.chance", FORMAT_CHANCE.format(perc));
            int width = fr.width(txt);
            guiGraphics.drawString(fr, txt, 74 - width, 44, 0x333333, false);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiquidInteraction recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 19)
                .setFluidRenderer(recipe.getReactant1().getAmount(), false, 16, 16)
                .addFluidStack(recipe.getReactant1().getFluid(), recipe.getReactant1().getAmount());
        builder.addSlot(RecipeIngredientRole.INPUT, 93, 19)
                .setFluidRenderer(recipe.getReactant2().getAmount(), false, 16, 16)
                .addFluidStack(recipe.getReactant2().getFluid(), recipe.getReactant2().getAmount());

        JEIInteractionResultRegistry.get(recipe.getObject().getId())
                .ifPresent(handler -> handler.addToRecipeLayout(builder, recipe));
    }
}
