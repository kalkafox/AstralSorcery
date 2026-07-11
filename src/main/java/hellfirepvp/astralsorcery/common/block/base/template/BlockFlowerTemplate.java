/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base.template;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.effect.MobEffect;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFlowerTemplate
 * Created by HellFirePvP
 * Date: 23.04.2020 / 18:16
 *
 * NOTE: 1.21 FlowerBlock no longer exposes overridable getStewEffect()/getEffectDuration()
 * hooks - the suspicious stew effect is now baked into an immutable SuspiciousStewEffects field
 * set from the constructor (see FlowerBlock#getSuspiciousEffects / SuspiciousEffectHolder).
 * Subclasses now provide the effect Holder + duration (in seconds) directly to this
 * constructor instead of overriding getters.
 */
public abstract class BlockFlowerTemplate extends FlowerBlock implements CustomItemBlock {

    public BlockFlowerTemplate(Holder<MobEffect> stewEffect, float stewEffectSeconds, Properties properties) {
        super(stewEffect, stewEffectSeconds, properties);
    }
}
