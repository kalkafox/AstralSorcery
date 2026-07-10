/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.TileSpectralRelay;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderSpectralRelay
 * Created by HellFirePvP
 * Date: 22.09.2019 / 18:33
 */
public class RenderSpectralRelay extends CustomTileEntityRenderer<TileSpectralRelay> {

    public RenderSpectralRelay(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TileSpectralRelay tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        ItemStack stack = tile.getItems().getStackInSlot(0);
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(0.5F, 0.1F, 0.5F);
            RenderingUtils.renderItemAsEntity(stack, renderStack, renderTypeBuffer, 0, 0, 0, combinedLight, pTicks, tile.getTicksExisted());
            renderStack.popPose();
        }
    }
}
