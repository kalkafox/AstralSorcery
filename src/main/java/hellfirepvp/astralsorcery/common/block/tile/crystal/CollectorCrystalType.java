/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile.crystal;

import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.world.level.material.MapColor;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CollectorCrystalType
 * Created by HellFirePvP
 * Date: 10.08.2019 / 20:34
 */
public enum CollectorCrystalType {

    ROCK_CRYSTAL(ColorsAS.ROCK_CRYSTAL, MapColor.TERRACOTTA_WHITE),
    CELESTIAL_CRYSTAL(ColorsAS.CELESTIAL_CRYSTAL, MapColor.COLOR_CYAN);

    private final Color displayColor;
    private final MapColor matColor;

    CollectorCrystalType(Color displayColor, MapColor matColor) {
        this.displayColor = displayColor;
        this.matColor = matColor;
    }

    public Color getDisplayColor() {
        return displayColor;
    }

    public MapColor getMaterialColor() {
        return matColor;
    }
}
