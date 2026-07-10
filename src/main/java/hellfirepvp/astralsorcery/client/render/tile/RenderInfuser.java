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
import hellfirepvp.astralsorcery.common.tile.TileInfuser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderInfuser
 * Created by HellFirePvP
 * Date: 09.11.2019 / 21:51
 */
public class RenderInfuser extends CustomTileEntityRenderer<TileInfuser> {

    public RenderInfuser(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TileInfuser tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        ItemStack stack = tile.getItemInput();
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(0.5F, 0.75F, 0.5F);
            RenderingUtils.renderItemAsEntity(stack, renderStack, renderTypeBuffer, 0, 0, 0, combinedLight, pTicks, tile.getTicksExisted());
            renderStack.popPose();
        }
    }
}
