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
import net.minecraft.util.ReuseableStream;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinEntity
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 */
@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "collideBoundingBoxHeuristically", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private static void addCustomCollision(Entity entity, Vec3 vec, AABB collisionBox, Level world, CollisionContext context, ReuseableStream<VoxelShape> potentialHits, CallbackInfoReturnable<Vec3> cir) {
        if (entity == null) {
            return;
        }
        Vec3 allowedMovement = CollisionHelper.onEntityCollision(vec, entity);
        if (allowedMovement != null) {
            cir.setReturnValue(allowedMovement);
        }
    }
}
