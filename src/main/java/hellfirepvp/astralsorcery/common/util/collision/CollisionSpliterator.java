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

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CollisionSpliterator
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * Stand-in for vanilla's old {@code net.minecraft.util.math.shapes.VoxelShapeSpliterator} (1.16.5),
 * which no longer exists in 1.21.1 - the vanilla entity-collision code no longer iterates collision
 * shapes through a dedicated Spliterator class, so there is nothing left to Mixin into
 * (see {@code hellfirepvp.astralsorcery.mixin.MixinVoxelShapeSpliterator}, which is currently inert/
 * broken for the same reason and is out of scope for this pass). This holder only exists so
 * {@link CollisionHelper} and {@link CollisionManager} keep compiling against the same shape of data
 * (an entity + its query AABB) that the old mixin used to hand them.
 */
public class CollisionSpliterator {

    public final Entity entity;
    public final AABB aabb;

    public CollisionSpliterator(Entity entity, AABB aabb) {
        this.entity = entity;
        this.aabb = aabb;
    }
}
