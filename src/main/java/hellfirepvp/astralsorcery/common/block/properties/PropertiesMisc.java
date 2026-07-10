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
import net.minecraft.client.resources.model.Material;
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
        return Block.Properties.create(Material.AIR, MapColor.NONE)
                .doesNotBlockMovement();
    }

    public static Block.Properties defaultSand() {
        return Block.Properties.create(Material.SAND, MapColor.SAND)
                .hardnessAndResistance(0.5F)

                .sound(SoundType.SAND);
    }

    public static Block.Properties defaultRock() {
        return Block.Properties.create(Material.STONE, MapColor.STONE)
                .hardnessAndResistance(1.5F, 6.0F)

                .sound(SoundType.STONE);
    }

    public static Block.Properties defaultMetal(MaterialColor color) {
        return Block.Properties.create(Material.IRON, color)
                .hardnessAndResistance(1.5F, 6.0F)


                .sound(SoundType.METAL);
    }

    public static Block.Properties defaultPlant() {
        return Block.Properties.create(Material.PLANT)
                .doesNotBlockMovement()
                .hardnessAndResistance(0)
                .sound(SoundType.PLANT);
    }

    public static Block.Properties defaultTickingPlant() {
        return Block.Properties.create(Material.PLANT)
                .doesNotBlockMovement()
                .randomTicks()
                .hardnessAndResistance(0)
                .sound(SoundType.PLANT);
    }

    public static Block.Properties defaultGoldMachinery() {
        return Block.Properties.create(Material.IRON, MapColor.GOLD)
                .hardnessAndResistance(1.0F, 4.0F)
                .sound(SoundType.STONE);
    }

}
