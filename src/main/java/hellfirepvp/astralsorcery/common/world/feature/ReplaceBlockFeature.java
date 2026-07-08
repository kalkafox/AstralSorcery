/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.feature;

import hellfirepvp.astralsorcery.common.world.feature.config.ReplaceBlockConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import hellfirepvp.astralsorcery.common.util.Constants;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ReplaceBlockFeature
 * Created by HellFirePvP
 * Date: 20.11.2020 / 16:56
 */
public class ReplaceBlockFeature extends Feature<ReplaceBlockConfig> {

    public ReplaceBlockFeature() {
        super(ReplaceBlockConfig.CODEC);
    }

    @Override
    public boolean generate(WorldGenLevel reader, ChunkGenerator generator, Random rand, BlockPos pos, ReplaceBlockConfig config) {
        if (config.target.test(reader.getBlockState(pos), rand)) {
            return setBlockState(reader, pos, config.state);
        }
        return true;
    }

    protected boolean setBlockState(ServerLevelAccessor world, BlockPos pos, BlockState state) {
        return world.setBlockState(pos, state, Constants.BlockFlags.BLOCK_UPDATE);
    }
}
