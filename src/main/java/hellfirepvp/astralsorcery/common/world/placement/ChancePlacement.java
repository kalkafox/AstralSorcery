/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ChancePlacement
 * Created by HellFirePvP
 * Date: 19.11.2020 / 22:45
 */
public class ChancePlacement extends PlacementFilter {

    public static final MapCodec<ChancePlacement> CODEC = RecordCodecBuilder.mapCodec(codecInstance -> codecInstance
            .group(com.mojang.serialization.Codec.FLOAT.fieldOf("chance").forGetter(placement -> placement.chance))
            .apply(codecInstance, ChancePlacement::new));

    private final float chance;

    public ChancePlacement(float chance) {
        this.chance = Mth.clamp(chance, 0F, 1F);
    }

    public static ChancePlacement withChance(float chance) {
        return new ChancePlacement(chance);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return random.nextFloat() < this.chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return WorldGenerationAS.Placements.CHANCE;
    }
}
