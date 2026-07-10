/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CollisionHelper
 * Created by HellFirePvP
 * Date: 19.12.2020 / 10:00
 */
public class CollisionHelper {

    public static boolean onCollision(CollisionSpliterator iterator, Consumer<? super VoxelShape> action) {
        if (!CollisionManager.needsCustomCollision(iterator.entity)) {
            return false;
        }
        AABB box = CollisionManager.getIteratorBoundingBoxes(iterator, iterator.entity);
        if (box == null) {
            return false;
        }

        VoxelShape floor = Shapes.create(box);
        if (Shapes.compare(floor, Shapes.create(iterator.aabb.grow(1.0E-7D)), BooleanOp.AND)) {
            action.accept(floor);
            return true;
        }
        return false;
    }

    @Nullable
    public static Vec3 onEntityCollision(Vec3 allowedMovement, Entity entity) {
        if (!CollisionManager.needsCustomCollision(entity)) {
            return null;
        }
        List<AABB> additionalBoxes = CollisionManager.getAdditionalBoundingBoxes(entity);
        AABB entityBox = entity.getBoundingBox().grow(1.0E-7D);
        for (AABB box : additionalBoxes) {
            double newYMovement = Shapes.create(box).getAllowedOffset(Direction.Axis.Y, entityBox, allowedMovement.y);
            allowedMovement = new Vec3(allowedMovement.x, newYMovement, allowedMovement.z);
        }

        return allowedMovement;
    }
}
