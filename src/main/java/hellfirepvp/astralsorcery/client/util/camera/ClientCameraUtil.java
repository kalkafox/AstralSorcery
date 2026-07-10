/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientCameraUtil
 * Created by HellFirePvP
 * Date: 02.12.2019 / 20:24
 */
public class ClientCameraUtil {

    public static void positionCamera(Player renderView, float pTicks, double x, double y, double z, double prevX, double prevY, double prevZ, double yRot, double yawPrev, double pitch, double pitchPrev) {
        double dYaw = Mth.positiveModulo(yRot - yawPrev, 360d);
        // Use the smaller arc
        if (dYaw > 180) {
            dYaw -= 360;
        }
        yawPrev = yRot - dYaw;
        float iYaw = Mth.lerp(pTicks, (float) yawPrev, (float) yRot);
        float iPitch = Mth.lerp(pTicks, (float) pitchPrev, (float) pitch);

        Minecraft mc = Minecraft.getInstance();
        Entity rv = mc.getRenderViewEntity();
        if (rv == null || !rv.equals(renderView)) {
            mc.setRenderViewEntity(renderView);
            rv = renderView;
        }
        Player render = (Player) rv;

        render.setPosRaw(x, y, z);
        render.xo = prevX;
        render.yo = prevY;
        render.zo = prevZ;
        render.xOld = prevX;
        render.yOld = prevY;
        render.zOld = prevZ;

        render.setYRot(iYaw);
        render.yRotO =     iYaw;
        render.yHeadRot =     iYaw;
        render.yHeadRotO = iYaw;
        render.cameraYaw =           iYaw;
        render.oBob =       iYaw;
        render.yBodyRot =     iYaw;
        render.yBodyRotO = iYaw;
        render.setXRot(iPitch);
        render.xRotO =   iPitch;

        render = Minecraft.getInstance().player;

        render.setPosRaw(x, y, z);
        render.xo = prevX;
        render.yo = prevY;
        render.zo = prevZ;
        render.xOld = prevX;
        render.yOld = prevY;
        render.zOld = prevZ;

        render.setYRot(iYaw);
        render.yRotO =     iYaw;
        render.yHeadRot =     iYaw;
        render.yHeadRotO = iYaw;
        render.cameraYaw =           iYaw;
        render.oBob =       iYaw;
        render.yBodyRot =     iYaw;
        render.yBodyRotO = iYaw;
        render.setXRot(iPitch);
        render.xRotO =   iPitch;
    }

    public static void resetCamera() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Player player = mc.player;
            mc.setRenderViewEntity(player);
            //double x = player.getPosX();
            //double y = player.getPosY();
            //double z = player.getPosZ();
            //EntityRendererManager rm = mc.getRenderManager();
            //rm.setRenderPosition(x, y, z);

            if (mc.screen != null) {
                mc.displayGuiScreen(null);
            }
        }
    }
}
