/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.sky.ChainingSkyRenderer;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.neoforged.neoforge.client.ISkyRenderHandler;
import net.neoforged.neoforge.client.event.EntityViewRenderEvent;
import net.neoforged.neoforge.client.event.RenderWorldLastEvent;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SkyRenderEventHandler
 * Created by HellFirePvP
 * Date: 12.01.2020 / 22:16
 */
public class SkyRenderEventHandler {

    public static void onRender(RenderWorldLastEvent event) {
        ClientLevel world = Minecraft.getInstance().world;
        if (world != null && world.func_239132_a_().func_241683_c_() == DimensionRenderInfo.FogType.NORMAL) {
            ISkyRenderHandler render = world.func_239132_a_().getSkyRenderHandler();
            if (!(render instanceof ChainingSkyRenderer)) {
                String strDimKey = world.getDimensionKey().getLocation().toString();
                if (RenderingConfig.CONFIG.dimensionsWithSkyRendering.get().contains(strDimKey)) {
                    world.func_239132_a_().setSkyRenderHandler(new ChainingSkyRenderer(world.func_239132_a_().getSkyRenderHandler()));
                }
            }
        }
    }

    public static void onFog(EntityViewRenderEvent.FogColors event) {
        ClientLevel world = Minecraft.getInstance().world;
        if (world != null) {
            String strDimKey = world.getDimensionKey().getLocation().toString();
            if (world.func_239132_a_().func_241683_c_() == DimensionRenderInfo.FogType.NORMAL &&
                    RenderingConfig.CONFIG.dimensionsWithSkyRendering.get().contains(strDimKey) &&
                    !RenderingConfig.CONFIG.dimensionsWithOnlyConstellationRendering.get().contains(strDimKey) &&
                    world.func_239132_a_().getSkyRenderHandler() instanceof ChainingSkyRenderer) {

                WorldContext ctx = SkyHandler.getContext(world, LogicalSide.CLIENT);

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
