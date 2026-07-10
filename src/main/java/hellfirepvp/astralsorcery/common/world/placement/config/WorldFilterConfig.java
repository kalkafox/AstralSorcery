/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.placement.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.gen.placement.IPlacementConfig;

import java.util.List;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldFilterConfig
 * Created by HellFirePvP
 * Date: 20.11.2020 / 15:52
 */
public class WorldFilterConfig implements DecoratorConfiguration {

    public static final Codec<WorldFilterConfig> CODEC = RecordCodecBuilder.create(codecInstance -> {
        return codecInstance.group(Codec.BOOL.fieldOf("ignoreFilter").forGetter(config -> {
            return config.ignoreFilter.get();
        }), Level.CODEC.listOf().fieldOf("worldFilter").forGetter(config -> {
            return config.worldFilter.get();
        })).apply(codecInstance, WorldFilterConfig::new);
    });

    private final Supplier<Boolean> ignoreFilter;
    private final Supplier<List<ResourceKey<Level>>> worldFilter;

    public WorldFilterConfig(boolean ignoreFilter, List<ResourceKey<Level>> worldFilter) {
        this(() -> ignoreFilter, () -> worldFilter);
    }

    public WorldFilterConfig(Supplier<Boolean> ignoreFilter, Supplier<List<ResourceKey<Level>>> worldFilter) {
        this.ignoreFilter = ignoreFilter;
        this.worldFilter = worldFilter;
    }

    public boolean generatesIn(ServerLevelAccessor level) {
         return this.ignoreFilter.get() || this.worldFilter.get().contains(level.getLevel().dimension());
    }
}
