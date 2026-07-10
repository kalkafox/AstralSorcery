/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.page;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPageLiquidInfusion
 * Created by HellFirePvP
 * Date: 29.05.2020 / 00:22
 */
public class RenderPageLiquidInfusion extends RenderPageRecipeTemplate {

    private final LiquidInfusion recipe;

    public RenderPageLiquidInfusion(@Nullable ResearchNode node, int nodePage, LiquidInfusion recipe) {
        super(node, nodePage);
        this.recipe = recipe;
    }

    @Override
    public void render(PoseStack renderStack, float x, float y, float z, float pTicks, float xpos, float ypos) {
        this.clearFrameRectangles();

        this.renderRecipeGrid(renderStack, x, y, z, TexturesAS.TEX_GUI_BOOK_GRID_INFUSION);
        this.renderExpectedItemStackOutput(renderStack, x + 78, y + 25, z, 1.4F, this.recipe.getOutput(ItemStack.EMPTY));
        this.renderInfoStar(renderStack, x, y, z, pTicks);

        float renderX = x + 80;
        float renderY = y + 128;
        this.renderItemStack(renderStack, renderX, renderY + 15, z, 1.2F, new ItemStack(BlocksAS.INFUSER));
        this.renderExpectedIngredientInput(renderStack, renderX, renderY, z, 1.2F, 0, this.recipe.getItemInput());

        BlockAtlasTexture.getInstance().bindTexture();
        TextureAtlasSprite tas = RenderingUtils.getParticleIcon(new FluidStack(this.recipe.getLiquidInput(), FluidType.BUCKET_VOLUME));
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            renderStack.pushPose();
            renderStack.translate(x, y, z);
            this.renderLiquidInput(buf, renderStack, tas, 1, 0);
            this.renderLiquidInput(buf, renderStack, tas, 2, 0);
            this.renderLiquidInput(buf, renderStack, tas, 3, 0);
            this.renderLiquidInput(buf, renderStack, tas, 1, 4);
            this.renderLiquidInput(buf, renderStack, tas, 2, 4);
            this.renderLiquidInput(buf, renderStack, tas, 3, 4);
            this.renderLiquidInput(buf, renderStack, tas, 0, 1);
            this.renderLiquidInput(buf, renderStack, tas, 0, 2);
            this.renderLiquidInput(buf, renderStack, tas, 0, 3);
            this.renderLiquidInput(buf, renderStack, tas, 4, 1);
            this.renderLiquidInput(buf, renderStack, tas, 4, 2);
            this.renderLiquidInput(buf, renderStack, tas, 4, 3);
            renderStack.popPose();
        });
    }

    private void renderLiquidInput(BufferBuilder buf, PoseStack renderStack, TextureAtlasSprite tas, int x, int y) {
        RenderingGuiUtils.rect(buf, renderStack, 28 + x * 25.15F, 76 + y * 25.15F, 0, 22.3F, 22.3F)
                .tex(tas)
                .draw();
    }

    @Override
    public boolean propagateMouseClick(double xpos, double mouseZ) {
        return this.handleBookLookupClick(xpos, mouseZ);
    }

    @Override
    public void postRender(PoseStack renderStack, float x, float y, float z, float pTicks, float xpos, float ypos) {
        this.renderHoverTooltips(renderStack, xpos, ypos, z, this.recipe.getId());
        this.renderInfoStarTooltips(renderStack, x, y, z, xpos, ypos, (toolTip) -> {
            toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.liquid",
                    this.recipe.getLiquidInput().getAttributes().getDisplayName(new FluidStack(this.recipe.getLiquidInput(), FluidType.BUCKET_VOLUME))));
            toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.chance.format",
                    this.getInfuserChanceDescription(this.recipe.getConsumptionChance())));
            if (this.recipe.doesConsumeMultipleFluids()) {
                toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.multiple"));
            }
            if (!this.recipe.acceptsChaliceInput() && ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.no_chalice"));
            }
            if (this.recipe.doesCopyNBTToOutputs()) {
                toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.copy_nbt"));
            }
        });
    }
}
