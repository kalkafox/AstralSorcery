/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CollisionHelper
 * Created by HellFirePvP
 * Date: 19.12.2020 / 10:00
 */
public class CollisionHelper {

    /**
     * Collects the mod-added collision shapes intersecting the given query box, ready to be merged into
     * the shape list vanilla's movement math runs on.
     */
    public static List<VoxelShape> getCustomCollisionShapes(@Nullable Entity entity, AABB queryBox) {
        if (!CollisionManager.needsCustomCollision(entity)) {
            return Collections.emptyList();
        }
        VoxelShape query = Shapes.create(queryBox.inflate(1.0E-7D));
        List<VoxelShape> shapes = new ArrayList<>();
        for (AABB box : CollisionManager.getAdditionalBoundingBoxes(entity)) {
            VoxelShape shape = Shapes.create(box);
            if (Shapes.joinIsNotEmpty(shape, query, BooleanOp.AND)) {
                shapes.add(shape);
            }
        }
        return shapes;
    }
}
