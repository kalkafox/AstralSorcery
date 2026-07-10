/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.model.builtin.ModelObservatory;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.entity.technical.EntityObservatoryHelper;
import hellfirepvp.astralsorcery.common.tile.TileObservatory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderObservatory
 * Created by HellFirePvP
 * Date: 16.02.2020 / 10:34
 */
public class RenderObservatory extends CustomTileEntityRenderer<TileObservatory> {

    private static final ModelObservatory MODEL_OBSERVATORY = new ModelObservatory();

    public RenderObservatory(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TileObservatory tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        Entity ridden;
        Player player = Minecraft.getInstance().player;
        if (player != null &&
                (ridden = Minecraft.getInstance().player.getVehicle()) != null &&
                ridden instanceof EntityObservatoryHelper &&
                ((EntityObservatoryHelper) ridden).getAssociatedObservatory() != null) {
            ((EntityObservatoryHelper) ridden).applyObservatoryRotationsFrom(tile, player, false);
        }

        float prevYaw = tile.prevObservatoryYaw;
        float yRot = tile.observatoryYaw;
        float prevPitch = tile.prevObservatoryPitch;
        float pitch = tile.observatoryPitch;

        float iYawDegree = RenderingVectorUtils.interpolateRotation(prevYaw + 180, yRot + 180, pTicks);
        float iPitchDegree = RenderingVectorUtils.interpolateRotation(prevPitch, pitch, pTicks);


        renderStack.pushPose();
        renderStack.translate(0.5F, 1.5F, 0.5F);
        renderStack.mulPose(Axis.XP.rotationDegrees(180F));
        renderStack.mulPose(Axis.YP.rotationDegrees(180F));
        //renderStack.scale(0.0625F, 0.0625F, 0.0625F);

        MODEL_OBSERVATORY.setupRotations(iYawDegree, iPitchDegree);
        MODEL_OBSERVATORY.render(renderStack, renderTypeBuffer, combinedLight, combinedOverlay);

        renderStack.popPose();
    }
}
