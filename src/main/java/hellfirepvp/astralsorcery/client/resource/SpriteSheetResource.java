/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SpriteSheetResource
 * Created by HellFirePvP
 * Date: 14.09.2016 / 09:15
 */
public class SpriteSheetResource extends AbstractRenderableTexture {

    protected float uPart, vPart;
    protected int frameCount;
    protected int height, width;

    private final AbstractRenderableTexture resource;

    public SpriteSheetResource(AbstractRenderableTexture resource) {
        this(resource, 1, 1);
    }

    public SpriteSheetResource(AbstractRenderableTexture resource, int height, int width) {
        super(NameUtil.suffixPath(resource.getKey(), "_sprite"));
        if (height <= 0 || width <= 0)
            throw new IllegalArgumentException("Can't instantiate a sprite sheet without any rows or columns!");

        frameCount = height * width;
        this.height = height;
        this.width = width;
        this.resource = resource;

        this.uPart = 1F / ((float) width);
        this.vPart = 1F / ((float) height);
    }

    @Override
    public void bindTexture() {
        this.resource.bindTexture();
    }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        return this.resource.asState();
    }

    @Override
    public Tuple<Float, Float> getUVOffset() {
        long timer = ClientScheduler.getClientTick();
        return getUVOffset(timer);
    }

    @Override
    public float getUWidth() {
        return getULength();
    }

    @Override
    public float getVWidth() {
        return getVLength();
    }

    public AbstractRenderableTexture getResource() {
        return resource;
    }

    public float getULength() {
        return uPart;
    }

    public float getVLength() {
        return vPart;
    }

    public Tuple<Float, Float> getUVOffset(long frameTimer) {
        int frame = (int) (frameTimer % frameCount);
        return new Tuple<>((frame % width) * uPart, (frame / width) * vPart);
    }

    public Tuple<Float, Float> getUVOffset(EntityComplexFX fx, float pTicks, float spriteDisplayFactor) {
        float agePart = fx.getAge() * spriteDisplayFactor + pTicks;
        float perc = agePart / fx.getMaxAge();
        long timer = Mth.floor(this.getFrameCount() * perc);
        return getUVOffset(timer);
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getRows() {
        return height;
    }

    public int getColumns() {
        return width;
    }
}
