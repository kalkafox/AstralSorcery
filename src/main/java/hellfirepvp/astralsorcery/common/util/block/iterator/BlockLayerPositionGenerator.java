/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.block.iterator;

import hellfirepvp.astralsorcery.common.util.block.BlockGeometry;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockLayerPositionGenerator
 * Created by HellFirePvP
 * Date: 01.02.2020 / 10:43
 */
public class BlockLayerPositionGenerator extends BlockPositionGenerator {

    private int layeringState = 0;

    private final LinkedList<BlockPos> currentPositions = new LinkedList<>();

    @Override
    protected BlockPos genNext(Vector3 offset, double radius) {
        int size = Mth.floor(radius);

        while (currentPositions.isEmpty()) {
            generatePositions(size);
        }
        return this.currentPositions.pop();
    }

    private void generatePositions(int maxLayers) {
        if (maxLayers <= 0) {
            this.currentPositions.add(BlockPos.ZERO);
            return;
        }
        this.layeringState++;
        if (this.layeringState > maxLayers) {
            this.layeringState = -maxLayers;
        }
        Collection<BlockPos> positions = BlockGeometry.getPlane(Direction.UP, maxLayers);
        positions.forEach(pos -> this.currentPositions.add(pos.offset(0, this.layeringState, 0)));
        Collections.shuffle(this.currentPositions, new Random(0xF518E23A05B27C19L));
    }

    @Override
    public void save(CompoundTag nbt) {
        nbt.putInt("layer", this.layeringState);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        this.layeringState = nbt.getInt("layer");
    }
}
