/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.collision;

import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectAevitas;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CollisionManager
 * Created by HellFirePvP
 * Date: 19.12.2020 / 14:20
 */
public class CollisionManager {

    private static final List<CustomCollisionHandler> customHandlers = new ArrayList<>();

    public static void init() {
        register(new MantleEffectAevitas.PlayerWalkableAir());
    }

    public static void register(CustomCollisionHandler handler) {
        customHandlers.add(handler);
    }

    public static boolean needsCustomCollision(@Nullable Entity entity) {
        for (CustomCollisionHandler handler : customHandlers) {
            if (handler.shouldAddCollisionFor(entity)) {
                return true;
            }
        }
        return false;
    }

    public static List<AABB> getAdditionalBoundingBoxes(@Nullable Entity entity) {
        List<AABB> additionalCollision = new ArrayList<>();
        AABB entityBox = entity != null ? entity.getBoundingBox() : new AABB(BlockPos.ZERO);
        customHandlers.stream()
                .filter(handler -> handler.shouldAddCollisionFor(entity))
                .forEach(handler -> handler.addCollision(entity, entityBox, additionalCollision));
        return additionalCollision;
    }
}
