/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockFakedState;
import hellfirepvp.astralsorcery.common.tile.TileTreeBeaconComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTreeBeaconComponent
 * Created by HellFirePvP
 * Date: 04.09.2020 / 19:18
 */
public class BlockTreeBeaconComponent extends BlockFakedState {

    public BlockTreeBeaconComponent() {
        super(Properties.of().mapColor(MapColor.NONE)
                .strength(-1F, 3600000.0F)
                .lightLevel(state -> 12));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        this.showBreakingParticles(level, pos, random);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTreeBeaconComponent(pos, state);
    }
}
