/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.tile.TilePrism;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPrism
 * Created by HellFirePvP
 * Date: 21.09.2019 / 22:39
 */
public class RenderPrism extends CustomTileEntityRenderer<TilePrism> {

    public RenderPrism(BlockEntityRenderDispatcher tileRenderer) {
        super(tileRenderer);
    }

    @Override
    public void render(TilePrism tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {}

}
