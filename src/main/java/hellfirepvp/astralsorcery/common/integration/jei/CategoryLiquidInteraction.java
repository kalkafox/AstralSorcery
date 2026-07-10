/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import net.minecraft.network.chat.Component;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteractionContext;
import hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei.JEIInteractionResultRegistry;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidStack;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.neoforged.neoforge.fluids.FluidType;

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
        super(IntegrationJEI.CATEGORY_LIQUID_INTERACTION);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/interaction.png"), 0, 0, 112, 54);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(BlocksAS.CHALICE));
    }

    @Override
    public Class<? extends LiquidInteraction> getRecipeClass() {
        return LiquidInteraction.class;
    }

    @Override
    public IDrawable getNoItemIcon() {
        return this.background;
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
    public void draw(LiquidInteraction recipe, PoseStack renderStack, double xpos, double ypos) {
        this.icon.draw(renderStack, 3, 36);
        this.icon.draw(renderStack, 93, 36);

        JEIInteractionResultRegistry.get(recipe.getObject().getId())
                .ifPresent(handler -> handler.drawRecipe(recipe, renderStack, xpos, ypos));

        FluidStack testMatch1 = new FluidStack(recipe.getReactant1(), FluidType.BUCKET_VOLUME);
        FluidStack testMatch2 = new FluidStack(recipe.getReactant2(), FluidType.BUCKET_VOLUME);
        LiquidInteractionContext ctx = new LiquidInteractionContext(testMatch1, testMatch2);
        Collection<LiquidInteraction> sameInteractions = RecipeTypesAS.TYPE_LIQUID_INTERACTION.findMatchingRecipes(ctx);
        if (!sameInteractions.isEmpty()) {
            int totalWeight = sameInteractions.stream().mapToInt(LiquidInteraction::getWeight).sum();
            float perc = ((float) recipe.getWeight() / totalWeight) * 100;

            Font fr = Minecraft.getInstance().font;
            MutableComponent txt = Component.translatable("jei.astralsorcery.tip.chance", FORMAT_CHANCE.format(perc));
            int width = fr.getStringPropertyWidth(txt);
            fr.draw(renderStack, txt, 74 - width, 44, 0x333333);
        }
    }

    @Override
    public void setIngredients(LiquidInteraction recipe, IIngredients ingredients) {
        ImmutableList.Builder<List<FluidStack>> fluidInputs = ImmutableList.builder();

        fluidInputs.add(Collections.singletonList(recipe.getReactant1()));
        fluidInputs.add(Collections.singletonList(recipe.getReactant2()));

        ingredients.setInputLists(VanillaTypes.FLUID, fluidInputs.build());

        JEIInteractionResultRegistry.get(recipe.getObject().getId())
                .ifPresent(handler -> handler.addToRecipeIngredients(recipe, ingredients));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, LiquidInteraction recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();

        fluidStacks.init(0, true, 2 + 1, 18 + 1, 16, 16, recipe.getReactant1().getAmount(), false, null);
        fluidStacks.init(1, true, 92 + 1, 18 + 1, 16, 16,  recipe.getReactant2().getAmount(), false, null);

        fluidStacks.set(ingredients);

        JEIInteractionResultRegistry.get(recipe.getObject().getId())
                .ifPresent(handler -> handler.addToRecipeLayout(recipeLayout, recipe, ingredients));
    }
}
