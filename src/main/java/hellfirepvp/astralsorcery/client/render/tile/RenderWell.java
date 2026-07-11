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
import hellfirepvp.astralsorcery.common.tile.TileWell;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderWell
 * Created by HellFirePvP
 * Date: 22.09.2019 / 15:54
 */
public class RenderWell extends CustomTileEntityRenderer<TileWell> {

    public RenderWell(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TileWell tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        PrecisionSingleFluidTank tank = tile.getTank();
        if (!tank.getType().isEmpty() && tank.getFluidAmount() > 0) {
            FluidStack contained = tank.getType();
            TextureAtlasSprite tas = RenderingUtils.getParticleIcon(contained);
            Color fluidColor = new Color(IClientFluidTypeExtensions.of(contained.getFluid()).getTintColor(contained), true);
            VertexConsumer buf = renderTypeBuffer.getBuffer(RenderTypesAS.TER_WELL_LIQUID);

            Vector3 offset = new Vector3(0.5D, 0.32D, 0.5D).addY(tank.getPercentageFilled() * 0.6);

            RenderingDrawUtils.renderAngleRotatedTexturedRectVB(buf, renderStack, offset, Vector3.RotAxis.Y_AXIS, (float) Math.toRadians(45F), 0.54F,
                    tas.getU0(), tas.getV0(), tas.getU1() - tas.getU0(), tas.getV1() - tas.getV0(),
                    fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255);
        }

        ItemStack catalyst = tile.getItems().getStackInSlot(0);
        if (!catalyst.isEmpty()) {
            RenderingUtils.renderItemAsEntity(catalyst, renderStack, renderTypeBuffer, 0.5F, 0.75F, 0.5F, combinedLight, pTicks, tile.getTicksExisted());
        }
    }
}
