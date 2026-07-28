/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.util.collision.CollisionHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinEntity
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 */
@Mixin(Entity.class)
public class MixinEntity {

    // 1.21 port: 1.16 hooked VoxelShapeSpliterator, which no longer exists. Entity#collectColliders is
    // the single place where the world border, entity collisions and block collisions get merged into
    // the shape list that both Entity#collide and Entity#collideBoundingBox run the movement math on,
    // so appending here keeps vanilla collision (walls, step-up) fully intact.
    @Inject(method = "collectColliders", at = @At("RETURN"), cancellable = true)
    private static void addCustomCollision(Entity entity, Level level, List<VoxelShape> collisions, AABB boundingBox,
                                           CallbackInfoReturnable<List<VoxelShape>> cir) {
        if (entity == null) {
            return;
        }
        List<VoxelShape> additional = CollisionHelper.getCustomCollisionShapes(entity, boundingBox);
        if (additional.isEmpty()) {
            return;
        }
        List<VoxelShape> merged = new java.util.ArrayList<>(cir.getReturnValue());
        merged.addAll(additional);
        cir.setReturnValue(merged);
    }
}
