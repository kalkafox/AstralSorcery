/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.advancement.*;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;

import static hellfirepvp.astralsorcery.common.lib.AdvancementsAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryAdvancements
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:34
 */
public class RegistryAdvancements {

    public static void init() {
        DISCOVER_CONSTELLATION = AstralRegistries.register(AstralRegistries.CRITERION_TRIGGERS, DiscoverConstellationTrigger.ID, new DiscoverConstellationTrigger());
        ATTUNE_SELF = AstralRegistries.register(AstralRegistries.CRITERION_TRIGGERS, AttuneSelfTrigger.ID, new AttuneSelfTrigger());
        ATTUNE_CRYSTAL = AstralRegistries.register(AstralRegistries.CRITERION_TRIGGERS, AttuneCrystalTrigger.ID, new AttuneCrystalTrigger());
        ALTAR_CRAFT = AstralRegistries.register(AstralRegistries.CRITERION_TRIGGERS, AltarCraftTrigger.ID, new AltarCraftTrigger());
        PERK_LEVEL = AstralRegistries.register(AstralRegistries.CRITERION_TRIGGERS, PerkLevelTrigger.ID, new PerkLevelTrigger());
    }

}
