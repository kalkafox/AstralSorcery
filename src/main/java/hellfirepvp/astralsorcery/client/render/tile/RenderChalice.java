/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import java.awt.*;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderChalice
 * Created by HellFirePvP
 * Date: 11.11.2019 / 20:27
 */
public class RenderChalice extends CustomTileEntityRenderer<TileChalice> {

    public RenderChalice(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TileChalice tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        FluidStack stack = tile.getTank().getType();
        if (stack.isEmpty()) {
            return;
        }
        TextureAtlasSprite tas = RenderingUtils.getParticleIcon(stack);
        if (tas == null) {
            return;
        }

        Vector3 rotation = RenderingVectorUtils.interpolate(tile.getPrevRotation(), tile.getRotation(), pTicks);
        Color color = new Color(ColorUtils.getOverlayColor(stack));
        float percSize = 0.125F + (tile.getTank().getPercentageFilled() * 0.375F);

        float ulength = tas.getU1() - tas.getU0();
        float vlength = tas.getV1() - tas.getV0();

        float uPart = ulength * percSize;
        float vPart = vlength * percSize;
        float uOffset = tas.getU0() + ulength / 2F - uPart / 2F;
        float vOffset = tas.getV0() + vlength / 2F - vPart / 2F;

        renderStack.pushPose();
        renderStack.translate(0.5F, 1.4F, 0.5F);
        renderStack.mulPose(Axis.XP.rotationDegrees((float) rotation.getX()));
        renderStack.mulPose(Axis.YP.rotationDegrees((float) rotation.getY()));
        renderStack.mulPose(Axis.ZP.rotationDegrees((float) rotation.getZ()));
        renderStack.scale(percSize, percSize, percSize);

        VertexConsumer buf = renderTypeBuffer.getBuffer(RenderTypesAS.TER_CHALICE_LIQUID);
        RenderingDrawUtils.renderTexturedCubeCentralColorNormal(renderStack, buf,
                uOffset, vOffset, uPart, vPart,
                color.getRed(), color.getGreen(), color.getBlue(), 255,
                renderStack.last().normal());

        renderStack.popPose();
    }
}
