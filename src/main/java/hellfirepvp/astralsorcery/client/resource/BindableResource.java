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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BindableResource
 * Created by HellFirePvP
 * Date: 07.05.2016 / 00:50
 */
@OnlyIn(Dist.CLIENT)
public class BindableResource extends AbstractRenderableTexture.Full implements ReloadableResource {

    private AbstractTexture resource = null;
    private String path = null;

    protected BindableResource(ResourceLocation key) {
        super(key);
    }

    BindableResource(String path) {
        this(AstralSorcery.key(path.replaceAll("[^a-zA-Z0-9\\.\\-]", "_")));
        this.path = path;
        allocateGlId();
    }

    public String getPath() {
        return path;
    }

    public SpriteSheetResource asSpriteSheet(int height, int width) {
        return new SpriteSheetResource(this, height, width);
    }

    public void invalidateAndReload() {
        Minecraft.getInstance().getTextureManager().release(this.getKey());
        this.resource = null;
    }

    protected AbstractTexture allocateGlId() {
        if (AssetLibrary.isReloading()) {
            return null;
        }
        TextureManager mgr = Minecraft.getInstance().getTextureManager();
        AbstractTexture resource = mgr.getTexture(this.getKey());
        if (resource != null) {
            return resource;
        }
        mgr.register(this.getKey(), new SimpleTexture(ResourceLocation.parse(this.getPath())));
        return mgr.getTexture(this.getKey());
    }

    @Override
    public void bindTexture() {
        if (AssetLibrary.isReloading()) {
            return; //we do nothing but wait.
        }
        if (this.resource == null) {
            this.resource = allocateGlId();
        }
        if (this.resource == null) {
            return;
        }
        RenderSystem.setShaderTexture(0, this.resource.getId());
    }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        return new RenderStateShard.TextureStateShard(this.getKey(), false, false) {
            @Override
            public void setupRenderState() {
                BindableResource.this.bindTexture();
                if (BindableResource.this.resource != null) {
                    BindableResource.this.resource.setFilter(false, false);
                }
            }
        };
    }
}
