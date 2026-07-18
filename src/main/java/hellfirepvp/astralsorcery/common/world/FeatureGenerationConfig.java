/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FeatureGenerationConfig
 * Created by HellFirePvP
 * Date: 19.11.2020 / 21:37
 */
public class FeatureGenerationConfig extends ConfigEntry {

    // 1.21 port: worldgen placement is datapack-driven now, so the runtime
    // world/enabled gates are looked up by feature name from the mod's own
    // placement filter (WorldFilteredPlacement). Biome selection moved into
    // biome tags / biome modifier JSONs and is no longer TOML-configurable.
    private static final Map<String, FeatureGenerationConfig> CONFIGS_BY_NAME = new HashMap<>();

    private List<ResourceKey<Level>> levels = new ArrayList<>();
    private boolean defaultEveryWorld = false;

    private ModConfigSpec.BooleanValue enabled;
    private ModConfigSpec.BooleanValue everyWorld;
    private ModConfigSpec.ConfigValue<List<String>> worldNames;

    public FeatureGenerationConfig(ResourceLocation featureName) {
        this(featureName.getPath());
    }

    public FeatureGenerationConfig(String featureName) {
        super(featureName);
        CONFIGS_BY_NAME.put(featureName, this);
    }

    @Nullable
    public static FeatureGenerationConfig byName(String featureName) {
        return CONFIGS_BY_NAME.get(featureName);
    }

    public <T extends FeatureGenerationConfig> T generatesInWorlds(List<ResourceKey<Level>> levels) {
        this.levels = levels;
        return (T) this;
    }

    public <T extends FeatureGenerationConfig> T setGenerateEveryWorld() {
        this.defaultEveryWorld = true;
        return (T) this;
    }

    @Override
    public void createEntries(ModConfigSpec.Builder cfgBuilder) {
        this.enabled = cfgBuilder
                .comment("Set this to false to disable this worldgen feature.")
                .translation(translationKey("enabled"))
                .define("enabled", true);
        this.everyWorld = cfgBuilder
                .comment("Set this to true to let this feature generate in any world. (Does NOT work for structures!)")
                .translation(translationKey("everyWorld"))
                .define("everyWorld", this.defaultEveryWorld);

        List<String> defaultWorlds = levels.stream()
                .map(ResourceKey::location)
                .map(ResourceLocation::getPath)
                .collect(Collectors.toList());
        this.worldNames = cfgBuilder
                .comment("Sets the worlds to generate this feature in. (Does NOT work for structures!)")
                .translation(translationKey("worldNames"))
                .define("worldNames", defaultWorlds);
    }

    public boolean isEnabled() {
        return this.enabled == null || this.enabled.get();
    }

    public boolean generatesIn(ResourceKey<Level> dimension) {
        if (this.everyWorld != null && this.everyWorld.get()) {
            return true;
        }
        if (this.worldNames == null) {
            return true;
        }
        return this.worldNames.get().stream()
                .map(ResourceLocation::parse)
                .map(key -> ResourceKey.create(Registries.DIMENSION, key))
                .anyMatch(dimension::equals);
    }
}
