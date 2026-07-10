/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.sky.astral.AstralSkyRenderer;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.ISkyRenderHandler;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ChainingSkyRenderer
 * Created by HellFirePvP
 * Date: 13.01.2020 / 19:48
 */
public class ChainingSkyRenderer implements ISkyRenderHandler {

    private final ISkyRenderHandler existingSkyRenderer;

    public ChainingSkyRenderer(ISkyRenderHandler existingSkyRenderer) {
        this.existingSkyRenderer = existingSkyRenderer;
    }

    @Override
    public void render(int ticks, float a, PoseStack renderStack, ClientLevel level, Minecraft mc) {
        EventFlags.SKY_RENDERING.executeWithFlag(() -> {
            ResourceKey<Level> dim = level.dimension();
            if (level.effects().skyType() == DimensionSpecialEffects.FogType.NORMAL) {
                if (RenderingConfig.CONFIG.dimensionsWithOnlyConstellationRendering.get().contains(dim.getLocation())) {
                    if (existingSkyRenderer != null) {
                        existingSkyRenderer.render(ticks, a, renderStack, level, mc);
                    } else {
                        ISkyRenderHandler existing = level.effects().getSkyRenderHandler();
                        level.effects().setSkyRenderHandler(null);
                        Minecraft.getInstance().worldRenderer.renderSky(renderStack, a);
                        level.effects().setSkyRenderHandler(existing);
                    }

                    this.renderConstellations(level, renderStack, a);
                } else {
                    AstralSkyRenderer.INSTANCE.render(ticks, a, renderStack, level, mc);
                }
            } else {
                ISkyRenderHandler existing = level.effects().getSkyRenderHandler();
                level.effects().setSkyRenderHandler(null);
                //Actually ends up calling renderEndSky
                Minecraft.getInstance().worldRenderer.renderSky(renderStack, a);
                level.effects().setSkyRenderHandler(existing);
            }
        });
    }

    private void renderConstellations(ClientLevel level, PoseStack renderStack, float pTicks) {
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        Blending.ADDITIVE_ALPHA.apply();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(false);
        float alphaSubRain = 1.0F - level.getRainStrength(pTicks);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alphaSubRain);

        renderStack.pushPose();
        renderStack.mulPose(Axis.XP.rotationDegrees(180));
        AstralSkyRenderer.renderConstellationsSky(level, renderStack, pTicks);
        renderStack.popPose();

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        RenderSystem.depthMask(true);
        RenderSystem.disableTexture();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
    }
}
