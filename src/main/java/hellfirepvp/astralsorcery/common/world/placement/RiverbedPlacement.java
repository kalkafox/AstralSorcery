/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.placement;

import com.mojang.serialization.MapCodec;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RiverbedPlacement
 * Created by HellFirePvP
 * Date: 20.11.2020 / 17:10
 */
public class RiverbedPlacement extends PlacementModifier {

    public static final MapCodec<RiverbedPlacement> CODEC = MapCodec.unit(RiverbedPlacement::new);

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        WorldGenLevel level = context.getLevel();
        int x = random.nextInt(16) + pos.getX();
        int z = random.nextInt(16) + pos.getZ();
        int y = context.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        if (y <= level.getMinBuildHeight()) {
            return Stream.of();
        }

        BlockPos floor = new BlockPos(x, y - 4, z);

        boolean foundWater = false;
        for (int yy = 0; yy < 5; yy++) {
            BlockPos check = floor.relative(Direction.UP, yy);
            BlockState state = level.getBlockState(check);
            if (state.getFluidState().is(FluidTags.WATER) || state.is(BlockTags.ICE)) {
                foundWater = true;
                floor = check.below();
                break;
            }
        }
        if (foundWater && level.getBlockState(floor).is(BlockTags.SAND)) {
            return Stream.of(floor);
        }
        return Stream.of();
    }

    @Override
    public PlacementModifierType<?> type() {
        return WorldGenerationAS.Placements.RIVERBED;
    }
}
