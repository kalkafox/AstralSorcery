/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.feature;

import hellfirepvp.astralsorcery.common.lib.DataAS;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RockCrystalFeature
 * Created by HellFirePvP
 * Date: 20.11.2020 / 17:14
 */
public class RockCrystalFeature extends ReplaceBlockFeature {

    @Override
    protected boolean setBlockState(ServerLevelAccessor world, BlockPos pos, BlockState state) {
        DataAS.DOMAIN_AS.getData(world.getWorld(), DataAS.KEY_ROCK_CRYSTAL_BUFFER).addOre(pos);
        return super.setBlockState(world, pos, state);
    }
}
