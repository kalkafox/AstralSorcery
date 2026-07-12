/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.PerkLevelInstance;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkLevelTrigger
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:30
 */
public class PerkLevelTrigger extends SimpleCriterionTrigger<PerkLevelInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("perk_level");

    @Override
    public Codec<PerkLevelInstance> codec() {
        return PerkLevelInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (i) -> i.test(player));
    }
}
