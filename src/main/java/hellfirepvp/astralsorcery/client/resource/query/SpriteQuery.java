/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource.query;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SpriteQuery
 * Created by HellFirePvP
 * Date: 31.03.2017 / 14:29
 */
public class SpriteQuery extends TextureQuery {

    private final int height, width;

    private Object spriteResource;

    public SpriteQuery(AssetLoader.TextureLocation location, int height, int width, String... path) {
        super(location, path);
        this.height = height;
        this.width = width;
    }

    private SpriteQuery(Object spriteResource, int height, int width) {
        super(null, "");
        this.spriteResource = spriteResource;
        this.height = height;
        this.width = width;
    }

    @OnlyIn(Dist.CLIENT)
    public static SpriteQuery of(SpriteSheetResource res) {
        return new SpriteQuery(res, res.getRows(), res.getColumns());
    }

    public int getRows() {
        return height;
    }

    public int getColumns() {
        return width;
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public SpriteSheetResource resolveSprite() {
        if (spriteResource == null) {
            AbstractRenderableTexture res = resolve();
            spriteResource = new SpriteSheetResource(res, getRows(), getColumns());
        }
        return (SpriteSheetResource) spriteResource;
    }

}
