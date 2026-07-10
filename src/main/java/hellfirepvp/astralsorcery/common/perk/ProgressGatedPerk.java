/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ProgressGatedPerk
 * Created by HellFirePvP
 * Date: 10.07.2019 / 21:38
 */
public class ProgressGatedPerk extends AbstractPerk {

    private BiPredicate<Player, PlayerProgress> unlockFunction = (player, progress) -> true;

    private final List<IConstellation> neededConstellations = new ArrayList<>();
    private final List<ResearchProgression> neededResearch = new ArrayList<>();
    private final List<ProgressionTier> neededProgression = new ArrayList<>();

    public ProgressGatedPerk(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    public void addRequireConstellation(IConstellation cst) {
        addResearchPreRequisite((player, progress) -> progress.hasConstellationDiscovered(cst));
        this.neededConstellations.add(cst);
    }

    public void addRequireProgress(ResearchProgression research) {
        addResearchPreRequisite(((player, progress) -> progress.hasResearch(research)));
        this.neededResearch.add(research);
    }

    public void addRequireTier(ProgressionTier tier) {
        addResearchPreRequisite(((player, progress) -> progress.getTierReached().isThisLaterOrEqual(tier)));
        this.neededProgression.add(tier);
    }

    public void addResearchPreRequisite(BiPredicate<Player, PlayerProgress> unlockFunction) {
        this.unlockFunction = this.unlockFunction.and(unlockFunction);
        disableTooltipCaching(); //Cannot cache as it may change.
    }

    @Override
    public boolean mayUnlockPerk(PlayerProgress progress, Player player) {
        if (!canSee(player, progress)) {
            return false;
        }
        return super.mayUnlockPerk(progress, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addLocalizedTooltip(Collection<MutableComponent> tooltip) {
        if (!canSeeClient()) {
            tooltip.add(Component.translatable("perk.info.astralsorcery.missing_progress")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        return super.addLocalizedTooltip(tooltip);
    }

    @OnlyIn(Dist.CLIENT)
    public final boolean canSeeClient() {
        return canSee(Minecraft.getInstance().player, LogicalSide.CLIENT);
    }

    public final boolean canSee(Player player, LogicalSide direction) {
        PlayerProgress prog = ResearchHelper.getProgress(player, direction);
        if (prog.isValid()) {
            return this.canSee(player, prog);
        }
        return false;
    }

    public final boolean canSee(Player player, PlayerProgress progress) {
        return this.unlockFunction.test(player, progress);
    }

    @Override
    public void deserializeData(JsonObject perkData) {
        super.deserializeData(perkData);

        this.neededConstellations.clear();
        this.neededResearch.clear();
        this.neededProgression.clear();

        if (GsonHelper.convertToInt(perkData, "neededConstellations")) {
            JsonArray array = GsonHelper.getAsJsonArray(perkData, "neededConstellations");
            for (int i = 0; i < array.size(); i++) {
                JsonElement el = array.get(i);
                String key = GsonHelper.getString(el, String.format("neededConstellations[%s]", i));
                IConstellation cst = ConstellationRegistry.getConstellation(ResourceLocation.parse(key));
                if (cst == null) {
                    throw new JsonParseException("Unknown constellation: " + key);
                }
                this.addRequireConstellation(cst);
            }
        }

        if (GsonHelper.convertToInt(perkData, "neededResearch")) {
            JsonArray array = GsonHelper.getAsJsonArray(perkData, "neededResearch");
            for (int i = 0; i < array.size(); i++) {
                JsonElement el = array.get(i);
                String key = GsonHelper.getString(el, String.format("neededResearch[%s]", i));
                try {
                    this.addRequireProgress(ResearchProgression.valueOf(key));
                } catch (Exception exc) {
                    throw new JsonParseException("Unknown research: " + key);
                }
            }
        }

        if (GsonHelper.convertToInt(perkData, "neededProgression")) {
            JsonArray array = GsonHelper.getAsJsonArray(perkData, "neededProgression");
            for (int i = 0; i < array.size(); i++) {
                JsonElement el = array.get(i);
                String key = GsonHelper.getString(el, String.format("neededProgression[%s]", i));
                try {
                    this.addRequireTier(ProgressionTier.valueOf(key));
                } catch (Exception exc) {
                    throw new JsonParseException("Unknown progress: " + key);
                }
            }
        }
    }

    @Override
    public void serializeData(JsonObject perkData) {
        super.serializeData(perkData);

        if (!this.neededConstellations.isEmpty()) {
            JsonArray array = new JsonArray();
            for (IConstellation cst : this.neededConstellations) {
                array.add(cst.getRegistryName().toString());
            }
            perkData.add("neededConstellations", array);
        }

        if (!this.neededResearch.isEmpty()) {
            JsonArray array = new JsonArray();
            for (ResearchProgression research : this.neededResearch) {
                array.add(research.name());
            }
            perkData.add("neededResearch", array);
        }

        if (!this.neededProgression.isEmpty()) {
            JsonArray array = new JsonArray();
            for (ProgressionTier progress : this.neededProgression) {
                array.add(progress.name());
            }
            perkData.add("neededProgression", array);
        }
    }
}