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
import net.minecraft.world.damagesource.DamageType;

/**
 * Keys into the data-driven damage type registry; the entries themselves live in
 * data/astralsorcery/damage_type/*.json.
 */
public class DamageTypesAS {

    private DamageTypesAS() {}

    public static final ResourceKey<DamageType> BLEED = key("bleed");
    public static final ResourceKey<DamageType> STELLAR = key("stellar");
    // Own type with vanilla's "thorns" message id: tagging minecraft:thorns itself
    // would change vanilla thorns behavior globally.
    public static final ResourceKey<DamageType> REFLECT = key("reflect");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, AstralSorcery.key(name));
    }
}
