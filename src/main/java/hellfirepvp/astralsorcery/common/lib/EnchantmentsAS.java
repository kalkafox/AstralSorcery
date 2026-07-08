/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EnchantmentsAS
 * Created by HellFirePvP
 * Date: 02.05.2020 / 12:42
 */
public class EnchantmentsAS {

    private EnchantmentsAS() {}

    public static final ResourceKey<Enchantment> NIGHT_VISION = key("night_vision");
    public static final ResourceKey<Enchantment> SCORCHING_HEAT = key("scorching_heat");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, AstralSorcery.key(name));
    }

}
