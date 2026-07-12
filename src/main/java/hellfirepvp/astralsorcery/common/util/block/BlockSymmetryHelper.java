/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.data.BiDiPair;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockSymmetryHelper
 * Created by HellFirePvP
 * Date: 21.12.2020 / 17:59
 */
public class BlockSymmetryHelper {

    public SymmetryResult getDotSymmetry(BlockGetter level, BlockPos center, int radiusLayer, boolean allowMirrorSymmetry, Predicate<BlockState> applicableStateFilter) {
        List<BlockPos> layerPositions = BlockGeometry.getHollowSphere(radiusLayer + 1, radiusLayer);
        SymmetryResult result = new SymmetryResult(layerPositions.size());
        Set<BlockPos> visitedBlocks = new HashSet<>();

        for (BlockPos offset : layerPositions) {
            BlockPos at = center.offset(offset);
            if (visitedBlocks.contains(at)) {
                continue;
            }
            visitedBlocks.add(at);

            BlockState state = level.getBlockState(at);
            if (offset.getX() == 0 || offset.getY() == 0 || offset.getZ() == 0) {
                if (!state.isAir()) {
                    result.fillerBlocks.add(at);
                }
                continue;
            }

            if (applicableStateFilter.test(state)) {
                BlockPos dotSym = center.subtract(offset);
                BlockState dotState = level.getBlockState(dotSym);
                if (applicableStateFilter.test(dotState)) {
                    result.symmetryPairs.add(new BiDiPair<>(at, dotSym));

                    if (!allowMirrorSymmetry) {
                        checkMirrorSymmetry(level, new Vec3i(-offset.getX(),  offset.getY(),  offset.getZ()), center, result, visitedBlocks);
                        checkMirrorSymmetry(level, new Vec3i( offset.getX(), -offset.getY(),  offset.getZ()), center, result, visitedBlocks);
                        checkMirrorSymmetry(level, new Vec3i( offset.getX(),  offset.getY(), -offset.getZ()), center, result, visitedBlocks);
                    }
                } else if (!dotState.isAir()) {
                    result.fillerBlocks.add(at);
                    result.fillerBlocks.add(dotSym);
                }

                visitedBlocks.add(dotSym);
            } else if (!state.isAir()) {
                result.fillerBlocks.add(at);
            }
        }

        result.density = (result.fillerBlocks.size() + result.symmetryPairs.size() * 2F) / ((float) result.totalCount);
        return result;
    }

    private static void checkMirrorSymmetry(BlockGetter level, Vec3i offset, BlockPos center, SymmetryResult result, Set<BlockPos> visitedBlocks) {
        BlockPos at = center.offset(offset);
        BlockState state = level.getBlockState(at);
        visitedBlocks.add(at);

        if (!state.isAir()) {
            result.fillerBlocks.add(at);
        }

        BlockPos dotSym = center.subtract(offset);
        BlockState dotState = level.getBlockState(dotSym);
        visitedBlocks.add(dotSym);

        if (!dotState.isAir()) {
            result.fillerBlocks.add(at);
        }
    }

    public static class SymmetryResult {

        private final int totalCount;
        private float density = 0F;
        private final Set<BiDiPair<BlockPos, BlockPos>> symmetryPairs = new HashSet<>();
        private final Set<BlockPos> fillerBlocks = new HashSet<>();

        private SymmetryResult(int totalCount) {
            this.totalCount = totalCount;
        }

        public float getDensity() {
            return density;
        }

        public Set<BiDiPair<BlockPos, BlockPos>> getSymmetryPairs() {
            return symmetryPairs;
        }

        public Set<BlockPos> getFillerBlocks() {
            return fillerBlocks;
        }
    }
}
