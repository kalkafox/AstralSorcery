/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.FeatureGenerationConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Runtime gate for datapack-driven placed features: looks up the mod's TOML
 * worldgen config by feature name and filters on the enabled flag and the
 * configured dimension whitelist.
 *
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldFilteredPlacement
 * Created by HellFirePvP
 * Date: 20.11.2020 / 15:52
 */
public class WorldFilteredPlacement extends PlacementFilter {

    public static final MapCodec<WorldFilteredPlacement> CODEC = RecordCodecBuilder.mapCodec(codecInstance -> codecInstance
            .group(Codec.STRING.fieldOf("config").forGetter(placement -> placement.configName))
            .apply(codecInstance, WorldFilteredPlacement::new));

    private final String configName;

    public WorldFilteredPlacement(String configName) {
        this.configName = configName;
    }

    public static WorldFilteredPlacement forConfig(FeatureGenerationConfig config) {
        return new WorldFilteredPlacement(config.getPath());
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        FeatureGenerationConfig config = FeatureGenerationConfig.byName(this.configName);
        if (config == null) {
            return true;
        }
        return config.isEnabled() && config.generatesIn(context.getLevel().getLevel().dimension());
    }

    @Override
    public PlacementModifierType<?> type() {
        return WorldGenerationAS.Placements.WORLD_FILTER;
    }
}
