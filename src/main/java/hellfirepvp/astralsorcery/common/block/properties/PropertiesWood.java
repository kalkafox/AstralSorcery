/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.properties;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PropertiesWood
 * Created by HellFirePvP
 * Date: 20.07.2019 / 20:00
 */
public class PropertiesWood {

    public static Block.Properties defaultInfusedWood() {
        return Block.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(2.5F, 7F)
                .sound(SoundType.WOOD);
    }

}
