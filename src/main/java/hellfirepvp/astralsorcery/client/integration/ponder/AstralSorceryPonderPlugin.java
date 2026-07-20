/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.integration.ponder;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.integration.ponder.scene.AltarScenes;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AstralSorceryPonderPlugin
 * Created by HellFirePvP
 * Date: 20.07.2026
 *
 * Only loaded/registered when Ponder is present (see
 * {@link hellfirepvp.astralsorcery.client.ClientProxy#onClientSetup}, gated behind
 * {@code Mods.PONDER.executeIfPresent}), so referencing Ponder's API here is safe.
 */
public class AstralSorceryPonderPlugin implements PonderPlugin {

    public static void register() {
        PonderIndex.addPlugin(new AstralSorceryPonderPlugin());
    }

    @Override
    public String getModId() {
        return AstralSorcery.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ResourceLocation attunementAltar = BuiltInRegistries.BLOCK.getKey(BlocksAS.ATTUNEMENT_ALTAR);
        helper.addStoryBoard(attunementAltar, "altar/attunement_altar", AltarScenes::attunementAltar);
        helper.addStoryBoard(attunementAltar, "altar/attuning", AltarScenes::attuning);
    }
}
