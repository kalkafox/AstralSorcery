/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.block.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryAltarDiscovery
 * Created by HellFirePvP
 * Date: 05.09.2020 / 14:16
 */
public class CategoryAltar extends JEICategory<SimpleAltarRecipe> {

    private final IDrawable background, icon;
    private final AltarType altarType;

    public CategoryAltar(RecipeType<SimpleAltarRecipe> recipeType, String textureRef, BlockAltar altarRef, IGuiHelper guiHelper) {
        super(recipeType);
        this.background = guiHelper.createDrawable(AstralSorcery.key(String.format("textures/gui/jei/%s.png", textureRef)), 0, 0, 116, 162);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(altarRef));
        this.altarType = altarRef.getAltarType();
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

    public AltarType getAltarType() {
        return altarType;
    }

    @Override
    public ResourceLocation getRegistryName(SimpleAltarRecipe recipe) {
        return recipe.getId();
    }

    @Override
    public List<SimpleAltarRecipe> getRecipes() {
        return RecipeTypesAS.TYPE_ALTAR.getRecipes(recipe -> recipe.getAltarType().equals(this.getAltarType()));
    }

    @Override
    public void draw(SimpleAltarRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.drawRelayInputs(recipe, guiGraphics);

        if (recipe.getFocusConstellation() != null) {
            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            IConstellation cst = recipe.getFocusConstellation();
            RenderingConstellationUtils.renderConstellationIntoGUI(Color.BLACK, cst, guiGraphics.pose(),
                    0, 0, 0,
                    50, 50, 1.2F,
                    () -> 0.9F, true, false);
            RenderSystem.disableBlend();
        }
    }

    private void drawRelayInputs(SimpleAltarRecipe recipe, GuiGraphics guiGraphics) {
        this.forEachRelayInput(recipe, (stack, x, y) -> {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(x, y, 0);
            guiGraphics.renderItem(stack, 0, 0);
            pose.popPose();
        });
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, SimpleAltarRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        this.forEachRelayInput(recipe, (stack, x, y) -> {
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                Minecraft mc = Minecraft.getInstance();
                tooltip.addAll(stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player,
                        mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL));
            }
        });
    }

    /**
     * Computes the currently displayed stack and top-left position of each orbiting relay input,
     * so drawing and tooltip hit-testing share the same (time-dependent) placement.
     */
    private void forEachRelayInput(SimpleAltarRecipe recipe, RelayInputVisitor visitor) {
        List<WrappedIngredient> relayInputs = recipe.getRelayInputs();
        int additional = relayInputs.size();
        if (additional <= 0) {
            return;
        }

        // Center of the middle grid slot; slots start at (12, 58) with a 19px step and 16px items.
        double centerX = 12 + 19 * 2 + 8;
        double centerY = 58 + 19 * 2 + 8;
        long tick = ClientScheduler.getClientTick();
        // Wall-clock based rotation (40s cycle, same speed as the previous 800 ticks)
        // so the orbit advances every frame instead of stepping once per client tick.
        double rotation = (System.currentTimeMillis() % 40000L) / 40000.0 * 2.0 * Math.PI;
        for (int i = 0; i < additional; i++) {
            double part = ((double) i) / ((double) additional) * 2.0 * Math.PI;
            part += Math.PI + rotation;
            double xAdd = Math.sin(part) * 60.0;
            double yAdd = Math.cos(part) * 60.0;

            ItemStack[] displayed = relayInputs.get(i).getIngredient().getItems();
            if (displayed.length == 0) {
                continue;
            }
            ItemStack stack = displayed[(int) ((tick / 20L) % displayed.length)];
            visitor.visit(stack, centerX + xAdd - 8, centerY + yAdd - 8);
        }
    }

    @FunctionalInterface
    private interface RelayInputVisitor {

        void visit(ItemStack stack, double x, double y);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SimpleAltarRecipe altarRecipe, IFocusGroup focuses) {
        int step = 19;
        int xOffset = 11 + 1;
        int yOffset = 57 + 1;

        AltarRecipeGrid grid = altarRecipe.getInputs();
        for (int yy = 0; yy < AltarRecipeGrid.GRID_SIZE; yy++) {
            for (int xx = 0; xx < AltarRecipeGrid.GRID_SIZE; xx++) {
                int slot = xx + yy * AltarRecipeGrid.GRID_SIZE;
                builder.addSlot(RecipeIngredientRole.INPUT, xOffset + step * xx, yOffset + step * yy)
                        .addIngredients(grid.getIngredient(slot));
            }
        }

        // Relay inputs sit on spectral relays around the altar, not in the container.
        // They render manually in draw() so they can orbit the grid; the invisible
        // ingredients keep them part of recipe lookups without a fixed slot.
        for (WrappedIngredient relayInput : altarRecipe.getRelayInputs()) {
            builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                    .addIngredients(relayInput.getIngredient());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 19)
                .addItemStack(altarRecipe.getOutputForRender(Collections.emptyList()));
    }
}
