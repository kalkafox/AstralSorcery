/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.common.block.tile.BlockChalice;
import hellfirepvp.astralsorcery.common.block.tile.BlockFountain;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ChaliceHelper
 * Created by HellFirePvP
 * Date: 09.11.2019 / 15:01
 */
public class ChaliceHelper {

    @Nonnull
    public static List<BlockPos> findNearbyChalices(Level level, BlockPos origin, int distance) {
        Vector3 thisVector = new Vector3(origin).add(0.5, 1.5, 0.5);

        List<BlockPos> foundChalices = BlockDiscoverer.searchForBlocksAround(level, origin, Mth.clamp(distance, 0, 16),
                (w, pos, state) -> !pos.equals(origin) &&
                        state.getBlock() instanceof BlockChalice &&
                        !w.hasNeighborSignal(pos) &&
                        !(w.getBlockState(pos.below()).getBlock() instanceof BlockFountain));

        foundChalices.removeIf(pos -> {
            Vector3 chaliceVector = new Vector3(pos).add(0.5, 1.5, 0.5);
            RaytraceAssist assist = new RaytraceAssist(thisVector, chaliceVector);
            return !assist.isClear(level);
        });

        return foundChalices;
    }

    //Distance must be positive and less or equal to 16
    @Nonnull
    public static List<TileChalice> findNearbyChalicesContaining(Level level, BlockPos origin, FluidStack expected, int distance) {
        List<TileChalice> out = new LinkedList<>();
        for (BlockPos chalicePos : findNearbyChalices(level, origin, distance)) {
            TileChalice chalice = MiscUtils.getTileAt(level, chalicePos, TileChalice.class, true);
            if (chalice != null && chalice.getTank().drain(expected, IFluidHandler.FluidAction.SIMULATE).getAmount() >= expected.getAmount()) {
                out.add(chalice);
            }
        }

        return out;
    }

    //Distance must be positive and less or equal to 16
    //Only returns a value if the chalices combined fulfilled the amount requirement.
    @Nonnull
    public static Optional<List<TileChalice>> findNearbyChalicesCombined(Level level, BlockPos origin, FluidStack expected, int distance) {
        FluidStack required = expected.copy();

        List<TileChalice> out = new LinkedList<>();
        for (BlockPos chalicePos : findNearbyChalices(level, origin, distance)) {
            TileChalice chalice = MiscUtils.getTileAt(level, chalicePos, TileChalice.class, true);
            if (chalice != null) {
                FluidStack drained = chalice.getTank().drain(expected, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    required.shrink(drained.getAmount());
                    out.add(chalice);
                }
            }
        }

        if (required.isEmpty()) {
            return Optional.of(out);
        }
        return Optional.empty();
    }

    public static boolean doChalicesContainCombined(Level level, Collection<BlockPos> chalicePositions, FluidStack expected) {
        FluidStack required = expected.copy();

        for (BlockPos pos : chalicePositions) {
            TileChalice chalice = MiscUtils.getTileAt(level, pos, TileChalice.class, true);
            if (chalice != null) {
                FluidStack drained = chalice.getTank().drain(expected, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    required.shrink(drained.getAmount());
                }
            }
        }

        return required.isEmpty();
    }
}
