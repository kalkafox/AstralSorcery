/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.TextureAtlas;

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
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
    }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        return new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);
    }
}
