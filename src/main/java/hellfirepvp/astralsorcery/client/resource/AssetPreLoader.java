/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.client.registry.*;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AssetPreLoader
 * Created by HellFirePvP
 * Date: 11.07.2019 / 20:29
 */
public class AssetPreLoader implements ResourceManagerReloadListener {

    public static final AssetPreLoader INSTANCE = new AssetPreLoader();

    private boolean initialized = false;

    private AssetPreLoader() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (initialized) {
            return;
        }
        RegistryTextures.loadTextures();
        RegistrySprites.loadSprites();
        RegistryRenderTypes.init();
        RegistryEffectTemplates.init();
        RegistryEffectTypes.init();
        ScreenJournalPerkTree.refreshDrawBuffer();

        initialized = true;
    }

}
