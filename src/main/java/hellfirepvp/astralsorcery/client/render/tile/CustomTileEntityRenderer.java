/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomTileEntityRenderer
 * Created by HellFirePvP
 * Date: 21.09.2019 / 15:29
 */
public abstract class CustomTileEntityRenderer<T extends BlockEntity> extends BlockEntityRenderer<T> {

    public CustomTileEntityRenderer(BlockEntityRenderDispatcher tileRenderer) {
        super(tileRenderer);
    }

    @Override
    public abstract void render(T tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay);

}
