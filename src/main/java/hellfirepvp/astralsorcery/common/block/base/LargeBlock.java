/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LargeBlock
 * Created by HellFirePvP
 * Date: 16.02.2020 / 08:30
 */
public interface LargeBlock {

    public AABB getBlockSpace();

    default public boolean canPlaceAt(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        AABB box = this.getBlockSpace();

        BlockPos.Mutable mPos = new BlockPos.Mutable();
        for (int xx = (int) box.minX; xx <= box.maxX; xx++) {
            for (int yy = (int) box.minY; yy <= box.maxY; yy++) {
                for (int zz = (int) box.minZ; zz <= box.maxZ; zz++) {
                    mPos.setPos(pos.getX() + xx, pos.getY() + yy, pos.getZ() + zz);
                    if (!level.isEmptyBlock(mPos) && !level.getBlockState(mPos).isReplaceable(BlockPlaceContext.at(ctx, mPos, Direction.DOWN))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
