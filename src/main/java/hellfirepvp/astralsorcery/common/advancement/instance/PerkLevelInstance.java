/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement.instance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;

import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkLevelInstance
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:23
 */
public record PerkLevelInstance(Optional<ContextAwarePredicate> player,
                                int levelNeeded) implements SimpleCriterionTrigger.SimpleInstance {

    public static final Codec<PerkLevelInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PerkLevelInstance::player),
            Codec.INT.fieldOf("levelNeeded").forGetter(PerkLevelInstance::levelNeeded)
    ).apply(inst, PerkLevelInstance::new));

    public static Criterion<PerkLevelInstance> reachLevel(int level) {
        return AdvancementsAS.PERK_LEVEL.createCriterion(new PerkLevelInstance(Optional.empty(), level));
    }

    public boolean test(ServerPlayer player) {
        return ResearchHelper.getProgress(player, LogicalSide.SERVER).getPerkData().getPerkLevel(player, LogicalSide.SERVER) >= this.levelNeeded;
    }
}
