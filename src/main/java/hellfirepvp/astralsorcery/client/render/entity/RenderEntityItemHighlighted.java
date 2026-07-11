/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.entity.item.EntityItemHighlighted;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderEntityItemHighlighted
 * Created by HellFirePvP
 * Date: 18.08.2019 / 10:37
 */
public class RenderEntityItemHighlighted extends ItemEntityRenderer {

    public RenderEntityItemHighlighted(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ItemEntity entity, float entityYaw, float a, PoseStack renderStack, MultiBufferSource buffer, int packedLight) {
        if (entity instanceof EntityItemHighlighted && ((EntityItemHighlighted) entity).hasCustomColor()) {
            renderStack.pushPose();
            renderStack.translate(0, 0.35F, 0);
            RenderingDrawUtils.renderLightRayFan(renderStack, buffer,
                    ((EntityItemHighlighted) entity).getHighlightColor(), 160420L + entity.getId(),
                    16, 12, 15);
            renderStack.popPose();
        }

        super.render(entity, entityYaw, a, renderStack, buffer, packedLight);
    }
}
