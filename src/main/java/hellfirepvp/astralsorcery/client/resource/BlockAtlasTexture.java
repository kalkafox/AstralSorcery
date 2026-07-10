/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAtlasTexture
 * Created by HellFirePvP
 * Date: 05.06.2020 / 21:54
 */
public class BlockAtlasTexture extends AbstractRenderableTexture.Full {

    private static final BlockAtlasTexture INSTANCE = new BlockAtlasTexture();

    private BlockAtlasTexture() {
        super(AstralSorcery.key("block_atlas_reference"));
    }

    public static BlockAtlasTexture getInstance() {
        return INSTANCE;
    }

    @Override
    public void bindTexture() {
        TextureManager mgr = Minecraft.getInstance().getTextureManager();
        mgr.bindTexture(TextureAtlas.LOCATION_BLOCKS_TEXTURE);
    }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        return new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS_TEXTURE, false, false);
    }
}
