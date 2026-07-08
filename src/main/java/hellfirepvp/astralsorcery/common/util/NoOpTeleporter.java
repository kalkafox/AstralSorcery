/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NoOpTeleporter
 * Created by HellFirePvP
 * Date: 19.04.2017 / 14:37
 */
public class NoOpTeleporter extends PortalForcer {

    private final BlockPos targetPos;

    public NoOpTeleporter(ServerLevel worldIn, BlockPos targetPos) {
        super(worldIn);
        this.targetPos = targetPos;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        Entity created = repositionEntity.apply(false);
        created.setPositionAndUpdate(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        return created;
    }
}
