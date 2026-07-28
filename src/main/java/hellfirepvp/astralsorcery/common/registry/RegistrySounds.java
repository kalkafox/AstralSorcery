/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

import static hellfirepvp.astralsorcery.common.lib.SoundsAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistrySounds
 * Created by HellFirePvP
 * Date: 02.08.2019 / 21:20
 */
public class RegistrySounds {

    private RegistrySounds() {}

    public static void init() {
        BLOCK_COLOREDLENS_ATTACH = registerSound("block_coloredlens_attach", SoundSource.BLOCKS);

        CRAFT_ATTUNEMENT = registerSound("craft_attunement", SoundSource.MASTER);
        CRAFT_FINISH = registerSound("craft_finish", SoundSource.BLOCKS);

        ALTAR_CRAFT_START = registerSound("altar_craft_start", SoundSource.BLOCKS);
        ALTAR_CRAFT_FINISH = registerSound("altar_craft_finish", SoundSource.BLOCKS);
        ALTAR_CRAFT_LOOP_T1 = registerSound("altar_craft_loop_t1", SoundSource.BLOCKS);
        ALTAR_CRAFT_LOOP_T2 = registerSound("altar_craft_loop_t2", SoundSource.BLOCKS);
        ALTAR_CRAFT_LOOP_T3 = registerSound("altar_craft_loop_t3", SoundSource.BLOCKS);
        ALTAR_CRAFT_LOOP_T4 = registerSound("altar_craft_loop_t4", SoundSource.BLOCKS);
        ALTAR_CRAFT_LOOP_T4_WAITING = registerSound("altar_craft_loop_t4_waiting", SoundSource.BLOCKS);

        ATTUNEMENT_ATLAR_IDLE = registerSound("attunement_altar_idle_loop", SoundSource.BLOCKS);
        ATTUNEMENT_ATLAR_PLAYER_ATTUNE = registerSound("attunement_altar_player_attune", SoundSource.BLOCKS);
        ATTUNEMENT_ATLAR_ITEM_START = registerSound("attunement_altar_item_start", SoundSource.BLOCKS);
        ATTUNEMENT_ATLAR_ITEM_FINISH = registerSound("attunement_altar_item_finish", SoundSource.BLOCKS);
        ATTUNEMENT_ATLAR_ITEM_LOOP = registerSound("attunement_altar_item_loop", SoundSource.BLOCKS);

        INFUSER_CRAFT_START = registerSound("infuser_craft_start", SoundSource.BLOCKS);
        INFUSER_CRAFT_LOOP = registerSound("infuser_craft_loop", SoundSource.BLOCKS);
        INFUSER_CRAFT_FINISH = registerSound("infuser_craft_finish", SoundSource.BLOCKS);

        PERK_SEAL = registerSound("perk_seal", SoundSource.PLAYERS);
        PERK_UNSEAL = registerSound("perk_unseal", SoundSource.PLAYERS);
        PERK_UNLOCK = registerSound("perk_unlock", SoundSource.PLAYERS);
        ILLUMINATION_WAND_HIGHLIGHT = registerSound("illumination_wand_highlight", SoundSource.BLOCKS);
        ILLUMINATION_WAND_UNHIGHLIGHT = registerSound("illumination_wand_unhighlight", SoundSource.BLOCKS);
        ILLUMINATION_WAND_LIGHT = registerSound("illumination_wand_light", SoundSource.BLOCKS);

        GUI_JOURNAL_CLOSE = registerSound("gui_journal_close", SoundSource.MASTER);
        // Keep the mod sound registered for save/network compatibility, but use vanilla's
        // known-good UI page sound until the legacy OGG event works reliably on 1.21's engine.
        registerSound("gui_journal_page");
        GUI_JOURNAL_PAGE = new CategorizedSoundEvent(SoundEvents.BOOK_PAGE_TURN, SoundSource.MASTER);
    }

    private static CategorizedSoundEvent registerSound(String jsonName, SoundSource predefinedCategory) {
        return new CategorizedSoundEvent(registerSound(jsonName), predefinedCategory);
    }

    private static SoundEvent registerSound(String jsonName) {
        ResourceLocation res = AstralSorcery.key(jsonName);
        return AstralRegistries.register(AstralRegistries.SOUND_EVENTS, res, SoundEvent.createVariableRangeEvent(res));
    }

}
