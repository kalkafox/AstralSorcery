/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.sky.astral.AstralSkyRenderer;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.fml.LogicalSide;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SkyRenderEventHandler
 * Created by HellFirePvP
 * Date: 12.01.2020 / 22:16
 */
// 1.21 port: ISkyRenderHandler/setSkyRenderHandler are gone; the astral sky is drawn at
// RenderLevelStageEvent.Stage.AFTER_SKY on top of the vanilla sky instead of replacing it
// (full replacement would need a custom DimensionSpecialEffects registration per dimension).
public class SkyRenderEventHandler {

    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) {
            return;
        }
        String strDimKey = level.dimension().location().toString();
        if (!RenderingConfig.CONFIG.dimensionsWithSkyRendering.get().contains(strDimKey)) {
            return;
        }

        PoseStack renderStack = event.getPoseStack();
        float pTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        EventFlags.SKY_RENDERING.executeWithFlag(() -> {
            if (RenderingConfig.CONFIG.dimensionsWithOnlyConstellationRendering.get().contains(strDimKey)) {
                renderConstellations(level, renderStack, pTicks);
            } else {
                AstralSkyRenderer.INSTANCE.render(event.getRenderTick(), pTicks, renderStack, level, Minecraft.getInstance());
            }
        });
    }

    private static void renderConstellations(ClientLevel level, PoseStack renderStack, float pTicks) {
        RenderSystem.enableBlend();
        Blending.ADDITIVE_ALPHA.apply();
        RenderSystem.depthMask(false);
        float alphaSubRain = 1.0F - level.getRainLevel(pTicks);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alphaSubRain);

        renderStack.pushPose();
        renderStack.mulPose(Axis.XP.rotationDegrees(180));
        AstralSkyRenderer.renderConstellationsSky(level, renderStack, pTicks);
        renderStack.popPose();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.depthMask(true);
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    public static void onFog(ViewportEvent.ComputeFogColor event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            String strDimKey = level.dimension().location().toString();
            if (level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL &&
                    RenderingConfig.CONFIG.dimensionsWithSkyRendering.get().contains(strDimKey) &&
                    !RenderingConfig.CONFIG.dimensionsWithOnlyConstellationRendering.get().contains(strDimKey)) {

                WorldContext ctx = SkyHandler.getContext(level, LogicalSide.CLIENT);

                if (ctx != null && ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
                    float perc = ctx.getCelestialEventHandler().getSolarEclipsePercent();
                    perc = 0.05F + (perc * 0.95F);

                    event.setRed(event.getRed() * perc);
                    event.setGreen(event.getGreen() * perc);
                    event.setBlue(event.getBlue() * perc);
                }
            }
        }
    }

}
