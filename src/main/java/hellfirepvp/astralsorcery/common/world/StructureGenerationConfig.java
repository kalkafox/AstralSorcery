/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world;

import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureGenerationConfig
 * Created by HellFirePvP
 * Date: 19.11.2020 / 22:05
 */
public class StructureGenerationConfig extends FeatureGenerationConfig {

    // 1.21 port: structure spacing/separation are baked into the structure-set
    // JSON at datagen time and are no longer runtime-configurable TOML values.
    private final int spacing, separation;

    public StructureGenerationConfig(ResourceLocation featureName, int spacing, int separation) {
        this(featureName.getPath(), spacing, separation);
    }

    public StructureGenerationConfig(String featureName, int spacing, int separation) {
        super(featureName);
        this.spacing = spacing;
        this.separation = separation;
    }

    public int getSpacing() {
        return this.spacing;
    }

    public int getSeparation() {
        return this.separation;
    }

    public int getSalt() {
        return Math.abs(this.getPathFromLocation().hashCode());
    }
}
