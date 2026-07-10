/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.tick;

import hellfirepvp.astralsorcery.common.data.research.PlayerPerkData;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.world.entity.player.Player;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import java.util.EnumSet;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkTickHelper
 * Created by HellFirePvP
 * Date: 25.08.2019 / 22:04
 */
public class PerkTickHelper implements ITickHandler {

    public static final PerkTickHelper INSTANCE = new PerkTickHelper();

    private PerkTickHelper() {}

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player ticked = (Player) context[0];
        LogicalSide direction = (LogicalSide) context[1];
        PlayerProgress prog = ResearchHelper.getProgress(ticked, direction);
        if (prog.isValid()) {
            PlayerPerkData perkData = prog.getPerkData();
            for (AbstractPerk perk : perkData.getEffectGrantingPerks()) {
                if (perk instanceof PlayerTickPerk) {
                    ((PlayerTickPerk) perk).onPlayerTick(ticked, direction);
                }
            }
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "PlayerPerkHandler";
    }
}
