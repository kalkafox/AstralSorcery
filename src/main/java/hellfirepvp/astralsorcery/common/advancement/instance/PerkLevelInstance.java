/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.advancement.PerkLevelTrigger;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkLevelInstance
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:23
 */
public class PerkLevelInstance extends AbstractCriterionTriggerInstance {

    private int levelNeeded = 0;

    private PerkLevelInstance(ResourceLocation criterionIn) {
        super(criterionIn, EntityPredicate.AndPredicate.ANY);
    }

    public static PerkLevelInstance reachLevel(int level) {
        PerkLevelInstance instance = new PerkLevelInstance(PerkLevelTrigger.ID);
        instance.levelNeeded = level;
        return instance;
    }

    @Override
    public JsonObject serialize(SerializationContext conditions) {
        JsonObject out = super.serialize(conditions);
        out.addProperty("levelNeeded", this.levelNeeded);
        return out;
    }

    public static PerkLevelInstance deserialize(ResourceLocation id, JsonObject json) {
        PerkLevelInstance instance = new PerkLevelInstance(id);
        instance.levelNeeded = GsonHelper.getAsInt(json, "levelNeeded");
        return instance;
    }

    public boolean test(ServerPlayer player) {
        return ResearchHelper.getProgress(player, LogicalSide.SERVER).getPerkData().getPerkLevel(player, LogicalSide.SERVER) >= this.levelNeeded;
    }
}
