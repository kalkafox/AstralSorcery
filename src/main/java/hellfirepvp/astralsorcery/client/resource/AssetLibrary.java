/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.sky.astral.AstralSkyRenderer;
import hellfirepvp.astralsorcery.common.util.object.CacheReference;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AssetLibrary
 * Created by HellFirePvP
 * Date: 09.08.2016 / 11:00
 */
public class AssetLibrary implements ResourceManagerReloadListener {

    public static AssetLibrary INSTANCE = new AssetLibrary();
    private static boolean fadeIn = false;

    private static final Map<AssetLoader.SubLocation, Map<String, AbstractRenderableTexture>> loadedTextures = new HashMap<>();
    private static final Map<ResourceLocation, GeneratedResource> dynamicTextures = new HashMap<>();
    private static final List<ReloadableResource> reloadableResources = new ArrayList<>();

    private AssetLibrary() {}

    public static Supplier<AbstractRenderableTexture> loadReference(AssetLoader.TextureLocation location, String... path) {
        return new CacheReference<>(() -> loadTexture(location, path));
    }

    public static AbstractRenderableTexture loadTexture(AssetLoader.TextureLocation location, String... path) {
        String name = String.join("/", path);
        if (name.endsWith(".png")) {
            throw new IllegalArgumentException("Tried to loadTexture with appended .png from the AssetLibrary!");
        }
        AbstractRenderableTexture resource = loadedTextures.computeIfAbsent(location, l -> new HashMap<>())
                .computeIfAbsent(name, str -> AssetLoader.loadTexture(location, str));
        reloadableResources.add((ReloadableResource) resource);
        return resource;
    }

    public static GeneratedResource loadGeneratedResource(ResourceLocation key, Supplier<BufferedImage> imageGenerator, boolean blur) {
        return loadGeneratedResource(key, imageGenerator, blur, false);
    }

    public static GeneratedResource loadGeneratedResource(ResourceLocation key, Supplier<BufferedImage> imageGenerator, boolean blur, boolean clamp) {
        if (dynamicTextures.containsKey(key)) {
            return dynamicTextures.get(key);
        }
        GeneratedResource resource = new GeneratedResource(key, imageGenerator, blur, clamp);
        reloadableResources.add(resource);
        dynamicTextures.put(key, resource);
        return resource;
    }

    public static boolean isReloading() {
        return fadeIn;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (fadeIn) {
            return;
        }
        fadeIn = true;
        AstralSorcery.log.info("[AssetLibrary] Refreshing and Invalidating Resources");
        reloadableResources.forEach(ReloadableResource::invalidateAndReload);
        fadeIn = false;

        //Reload buffer during next render
        AstralSkyRenderer.INSTANCE.reset();

        AstralSorcery.log.info("[AssetLibrary] Successfully reloaded library.");
    }

}
