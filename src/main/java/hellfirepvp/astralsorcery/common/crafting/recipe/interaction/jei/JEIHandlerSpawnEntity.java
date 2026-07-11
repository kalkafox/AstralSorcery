/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.LightmapUtil;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.interaction.InteractionResult;
import hellfirepvp.astralsorcery.common.crafting.recipe.interaction.ResultSpawnEntity;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIHandlerSpawnEntity
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:51
 */
public class JEIHandlerSpawnEntity extends JEIInteractionResultHandler {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addToRecipeLayout(IRecipeLayout recipeLayout, LiquidInteraction recipe, IIngredients ingredients) {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addToRecipeIngredients(LiquidInteraction recipe, IIngredients ingredients) {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawRecipe(LiquidInteraction recipe, PoseStack renderStack, double xpos, double ypos) {
        InteractionResult result = recipe.getObject();
        if (!(result instanceof ResultSpawnEntity)) {
            return;
        }
        Entity le = ((ResultSpawnEntity) result).getType().create(Minecraft.getInstance().level);
        if (!(le instanceof LivingEntity)) {
            return;
        }

        renderStack.pushPose();
        renderStack.translate(55, 35, 500);
        renderStack.scale(15, 15, 15);
        renderStack.mulPose(Axis.XP.rotationDegrees(180));
        renderStack.mulPose(Axis.YP.rotationDegrees(145));
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(new ByteBufferBuilder(256));
        Minecraft.getInstance().getRenderManager()
                .renderEntityStatic(le, 0, 0, 0, 0, 0, renderStack, buffer, LightmapUtil.getPackedFullbrightCoords());
        buffer.endBatch();
        renderStack.popPose();
    }
}
