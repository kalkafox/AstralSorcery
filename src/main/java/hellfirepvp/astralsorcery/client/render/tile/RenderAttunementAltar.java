/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.model.builtin.ModelAttunementAltar;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderAttunementAltar
 * Created by HellFirePvP
 * Date: 17.11.2019 / 07:52
 */
public class RenderAttunementAltar extends CustomTileEntityRenderer<TileAttunementAltar> {

    private static final ModelAttunementAltar MODEL_ATTUNEMENT_ALTAR = new ModelAttunementAltar();

    public RenderAttunementAltar(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(TileAttunementAltar tile) {
        return super.getRenderBoundingBox(tile).inflate(3.5, 2, 3.5);
    }

    @Override
    public void render(TileAttunementAltar tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        renderStack.pushPose();
        renderStack.translate(0.5, 0.5, 0.5);
        renderStack.mulPose(Axis.XP.rotationDegrees(180));
        MODEL_ATTUNEMENT_ALTAR.render(renderStack, renderTypeBuffer, combinedLight, combinedOverlay);
        renderStack.popPose();

        float spinDur = TileAttunementAltar.MAX_START_ANIMATION_SPIN;
        float spinStart = TileAttunementAltar.MAX_START_ANIMATION_TICK;

        float startY = -1.2F;
        float endY   = -0.5F;
        float activationProgress = RenderingVectorUtils.interpolate(
                tile.prevActivationTick / spinStart,
                tile.activationTick / spinStart,
                pTicks);
        float framePosY = endY + (endY - startY) * easeOutQuart(activationProgress);

        double generalAnimationTick = (ClientScheduler.getClientTick() + pTicks) / 4D;
        if (tile.animate) {
            if (tile.tesrLocked) {
                tile.tesrLocked = false;
            }
        } else {
            if (tile.tesrLocked) {
                generalAnimationTick = 7.25D;
            } else {
                if (Math.abs((generalAnimationTick % spinDur) - 7.25D) <= 0.3125) {
                    generalAnimationTick = 7.25D;
                    tile.tesrLocked = true;
                }
            }
        }

        for (int i = 1; i < 9; i++) {
            float incrementer = (spinDur / 8F) * i;

            double aFrame =     generalAnimationTick + incrementer;
            double prevAFrame = generalAnimationTick + incrementer - 1;
            double renderFrame = RenderingVectorUtils.interpolate(prevAFrame, aFrame, 0);

            double partRenderFrame = (renderFrame % spinDur) / spinDur;
            float normalized = (float) (partRenderFrame * 2F * Math.PI);

            float xOffset = Mth.cos(normalized);
            float zDist = Mth.sin(normalized);
            float rotation = RenderingVectorUtils.interpolate(tile.prevActivationTick / spinStart, tile.activationTick / spinStart, pTicks);

            renderStack.pushPose();
            renderStack.translate(0.5, framePosY, 0.5);
            renderStack.mulPose(Axis.XP.rotationDegrees(180));
            MODEL_ATTUNEMENT_ALTAR.renderHovering(renderStack, renderTypeBuffer.getBuffer(MODEL_ATTUNEMENT_ALTAR.getGeneralType()), combinedLight, combinedOverlay, 1F, 1F, 1F, 1F, xOffset, zDist, rotation);
            renderStack.popPose();
        }
    }

    private static float easeOutQuart(float x) {
        return 1F - (float) Math.pow(1F - x, 4);
    }
}
