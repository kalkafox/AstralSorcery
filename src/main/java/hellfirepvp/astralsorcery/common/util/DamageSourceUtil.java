/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DamageSourceUtil
 * Created by HellFirePvP
 * Date: 17.11.2018 / 08:29
 */
// 1.21 port: DamageSource is an immutable holder around a data-driven DamageType (Holder<DamageType>)
// instead of a class hierarchy with per-instance mutable flags (bypassArmor/setIsFire/etc. are gone -
// that behavior is now expressed by tagging the DamageType itself, e.g. DamageTypeTags.BYPASSES_ARMOR,
// via datapack JSON). newType() below builds an unregistered (direct) Holder<DamageType>, which keeps
// this compiling and preserves msgId/death-message behavior, but the mod's own damage sources will NOT
// be seen as matching those vanilla tags by generic combat code (e.g. armor damage reduction) until
// they're registered for real via data/astralsorcery/damage_type/*.json + tag JSON additions - left as
// a follow-up, same as other data-driven-registry caveats in this port.
public class DamageSourceUtil {

    public static DamageSource newType(@Nonnull String damageType) {
        return new DamageSource(Holder.direct(new DamageType(damageType, 0.1F)));
    }

    public static DamageSource withEntityDirect(@Nonnull String damageType, @Nullable Entity source) {
        return new DamageSource(Holder.direct(new DamageType(damageType, 0.1F)), source);
    }

    public static DamageSource withEntityIndirect(@Nonnull String damageType, @Nullable Entity actualSource, @Nullable Entity indirectSource) {
        return new DamageSource(Holder.direct(new DamageType(damageType, 0.1F)), indirectSource, actualSource);
    }

    @Nonnull
    public static DamageSource withEntityDirect(@Nonnull DamageSource damageType, @Nullable Entity source) {
        return source != null ? new DamageSource(damageType.typeHolder(), source) : damageType;
    }

    @Nonnull
    public static DamageSource withEntityIndirect(@Nonnull DamageSource damageType, @Nullable Entity actualSource, @Nullable Entity indirectSource) {
        return new DamageSource(damageType.typeHolder(), indirectSource, actualSource);
    }

}
