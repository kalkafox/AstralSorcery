/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DamageUtil
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:47
 */
public class DamageUtil {

    public static boolean hurt(@Nonnull Entity attacked, @Nonnull DamageSource type, float amount) {
        return attacked.hurt(type, amount);
    }

    public static boolean hurt(@Nonnull Entity attacked, @Nonnull DamageSource type, float amount, @Nullable Entity newSource) {
        DamageSource newType = DamageSourceUtil.withEntityDirect(type, newSource);
        return hurt(attacked, newType != null ? newType : type, amount);
    }

    public static <T extends LivingEntity> void shotgunAttack(T targeted, Consumer<T> fn) {
        int hurtTime = targeted.invulnerableTime;
        targeted.invulnerableTime = 0;
        try {
            fn.accept(targeted);
        } finally {
            targeted.invulnerableTime = hurtTime;
        }
    }
}
