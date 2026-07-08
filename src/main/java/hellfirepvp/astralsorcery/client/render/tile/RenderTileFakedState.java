/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.base.TileFakedState;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import hellfirepvp.observerlib.client.util.RenderTypeDecorator;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderTileFakedState
 * Created by HellFirePvP
 * Date: 28.11.2019 / 19:52
 */
public class RenderTileFakedState extends CustomTileEntityRenderer<TileFakedState> {

    public RenderTileFakedState(BlockEntityRenderDispatcher tileRenderer) {
        super(tileRenderer);
    }

    @Override
    public void render(TileFakedState tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        BlockState fakedState = tile.getFakedState();
        if (fakedState.getBlock() instanceof AirBlock) {
            return;
        }
        Color blendColor = tile.getOverlayColor();
        int[] color = new int[] { blendColor.getRed(), blendColor.getGreen(), blendColor.getBlue(), 128 };

        RenderType type = RenderTypeLookup.func_239221_b_(fakedState);
        RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(type, () -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
        }, () -> {
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        });
        BufferDecoratorBuilder decorator = BufferDecoratorBuilder.withColor(((r, g, b, a) -> color));
        VertexConsumer buf = renderTypeBuffer.getBuffer(decorated);
        RenderingUtils.renderSimpleBlockModel(fakedState, renderStack, decorator.decorate(buf), tile.getPos(), tile, true);
    }
}
