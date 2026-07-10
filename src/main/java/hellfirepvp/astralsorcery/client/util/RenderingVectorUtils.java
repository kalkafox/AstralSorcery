/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.util.draw.RenderInfo;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderingVectorUtils
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:27
 */
public class RenderingVectorUtils {

    public static Vector3 getStandardTranslationRemovalVector(float a) {
        Vec3 viewDistance = RenderInfo.getInstance().getARI().getPosition();
        return new Vector3(viewDistance);
    }

    public static Vector3 interpolatePosition(Entity e, float a) {
        return new Vector3(
                interpolate(e.xo, e.getX(), a),
                interpolate(e.yo, e.getY(), a),
                interpolate(e.zo, e.getZ(), a)
        );
    }

    public static Vector3 interpolate(Vector3 oldV, Vector3 newV, float a) {
        return new Vector3(
                interpolate(oldV.getX(), newV.getX(), a),
                interpolate(oldV.getY(), newV.getY(), a),
                interpolate(oldV.getZ(), newV.getZ(), a)
        );
    }

    public static double interpolate(double oldP, double newP, float a) {
        if (oldP == newP) return oldP;
        return oldP + ((newP - oldP) * a);
    }

    public static float interpolate(float oldP, float newP, float a) {
        if (oldP == newP) return oldP;
        return oldP + ((newP - oldP) * a);
    }

    public static float interpolateRotation(float prevRotation, float nextRotation, float partialTick) {
        float rot = nextRotation - prevRotation;
        while (rot >= 180.0F) {
            rot -= 360.0F;
        }
        while (rot >= 180.0F) {
            rot -= 360.0F;
        }
        return prevRotation + partialTick * rot;
    }

}
