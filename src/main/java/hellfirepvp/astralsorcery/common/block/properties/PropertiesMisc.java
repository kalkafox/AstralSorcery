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
 * Class: PropertiesMisc
 * Created by HellFirePvP
 * Date: 21.07.2019 / 08:25
 */
public class PropertiesMisc {

    public static Block.Properties defaultAir() {
        return Block.Properties.of()
                .mapColor(MapColor.NONE)
                .noCollission();
    }

    public static Block.Properties defaultSand() {
        return Block.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F)
                .sound(SoundType.SAND);
    }

    public static Block.Properties defaultRock() {
        return Block.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE);
    }

    public static Block.Properties defaultMetal(MapColor color) {
        return Block.Properties.of()
                .mapColor(color)
                .strength(1.5F, 6.0F)
                .sound(SoundType.METAL);
    }

    public static Block.Properties defaultPlant() {
        return Block.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .strength(0)
                .sound(SoundType.PLANT);
    }

    public static Block.Properties defaultTickingPlant() {
        return Block.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .strength(0)
                .sound(SoundType.PLANT);
    }

    public static Block.Properties defaultGoldMachinery() {
        return Block.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(1.0F, 4.0F)
                .sound(SoundType.STONE);
    }

}
