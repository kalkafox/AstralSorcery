/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GeneratedResource
 * Created by HellFirePvP
 * Date: 06.01.2021 / 11:00
 */
public class GeneratedResource extends BindableResource implements ReloadableResource {

    private final Supplier<BufferedImage> imageGen;
    private final boolean blur, clamp;

    GeneratedResource(ResourceLocation key, Supplier<BufferedImage> imageGenerator, boolean blur, boolean clamp) {
        super(NameUtil.prefixPath(key, "dynamic_"));
        this.imageGen = imageGenerator;
        this.blur = blur;
        this.clamp = clamp;
    }

    @Override
    protected AbstractTexture allocateGlId() {
        if (AssetLibrary.isReloading()) {
            return null;
        }
        // 1.21 port: see BindableResource.allocateGlId — getTexture(id) auto-registers, so always
        // register the in-memory texture explicitly instead of probing.
        TextureManager mgr = Minecraft.getInstance().getTextureManager();
        InMemoryTexture texture = new InMemoryTexture(this.imageGen, this.blur, this.clamp);
        mgr.register(this.getKey(), texture);
        return mgr.getTexture(this.getKey());
    }

    private static class InMemoryTexture extends AbstractTexture {

        private final Supplier<BufferedImage> imageGen;
        private final boolean blur, clamp;

        private InMemoryTexture(Supplier<BufferedImage> imageGen, boolean blur, boolean clamp) {
            this.imageGen = imageGen;
            this.blur = blur;
            this.clamp = clamp;
        }

        @Override
        public void load(ResourceManager manager) throws IOException {
            NativeImage image = NativeImage.read(NativeImage.Format.RGBA, createMemInput());
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> this.doLoad(image, this.blur, this.clamp));
            } else {
                this.doLoad(image, this.blur, this.clamp);
            }
        }

        private void doLoad(NativeImage imageIn, boolean blurIn, boolean clampIn) {
            TextureUtil.prepareImage(this.getId(), 0, imageIn.getWidth(), imageIn.getHeight());
            imageIn.upload(0, 0, 0, 0, 0, imageIn.getWidth(), imageIn.getHeight(), blurIn, clampIn, false, true);
        }

        private InputStream createMemInput() throws IOException {
            BufferedImage bufferedImage = imageGen.get();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            baos.close();
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }
}
