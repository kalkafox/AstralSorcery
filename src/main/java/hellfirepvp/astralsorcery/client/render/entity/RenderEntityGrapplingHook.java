/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.entity.technical.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderEntityGrapplingHook
 * Created by HellFirePvP
 * Date: 29.02.2020 / 20:04
 */
public class RenderEntityGrapplingHook extends EntityRenderer<EntityGrapplingHook> {

    public RenderEntityGrapplingHook(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityGrapplingHook entity, float entityYaw, float a, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        int alphaMultiplier;
        if (entity.isDespawning()) {
            alphaMultiplier = Mth.clamp(127 - ((int) (entity.despawnPercentage(a) * 255F)), 0, 255);
        } else {
            alphaMultiplier = 255;
        }
        if (alphaMultiplier <= 1E-4) {
            return;
        }

        Vector3 entityPos = RenderingVectorUtils.interpolatePosition(entity, a);
        List<Vector3> lineState = entity.buildLine(a);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.disableCull();

        //Main grappling hook sprite
        SpritesAS.SPR_GRAPPLING_HOOK.bindTexture();

        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
            RenderingDrawUtils.renderFacingSpriteVB(buf, matrixStack,
                    entityPos.getX(), entityPos.getY(), entityPos.getZ(),
                    1.3F, 0F,
                    SpritesAS.SPR_GRAPPLING_HOOK, ClientScheduler.getClientTick() + entity.tickCount,
                    255, 255, 255, alphaMultiplier);
        });

        //Small line of particles
        TexturesAS.TEX_PARTICLE_LARGE.bindTexture();
        Blending.ADDITIVE_ALPHA.apply();

        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
            for (Vector3 pos : lineState) {
                Vector3 at = pos.mul(2).add(entityPos);
                RenderingDrawUtils.renderFacingFullQuadVB(buf, matrixStack,
                        at.getX(), at.getY(), at.getZ(),
                        0.3F, 0F,
                        50, 40, 180, (int) (alphaMultiplier * 0.8F));
            }
        });

        RenderSystem.enableCull();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGrapplingHook entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
