/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.context.base.BatchRenderContext;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FXFacingAtlasSprite
 * Created by HellFirePvP
 * Date: 10.11.2019 / 15:17
 */
public class FXFacingAtlasParticle extends EntityVisualFX {

    private TextureAtlasSprite sprite;
    private float u0 = 0, v0 = 0;
    private float uLength = 1, vLength = 1;

    public FXFacingAtlasParticle(Vector3 pos) {
        super(pos);
    }

    public <T extends FXFacingAtlasParticle> T pickSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        this.u0 = this.sprite.getU0();
        this.v0 = this.sprite.getV0();
        this.uLength = this.sprite.getU1() - this.u0;
        this.vLength = this.sprite.getV1() - this.v0;
        return (T) this;
    }

    public <T extends FXFacingAtlasParticle> T selectFraction(float percentage) {
        percentage = Mth.clamp(percentage, 0F, 1F);
        this.u0 += this.uLength * (1F - percentage) * random.nextFloat();
        this.v0 += this.vLength * (1F - percentage) * random.nextFloat();
        this.uLength *= percentage;
        this.vLength *= percentage;
        return (T) this;
    }

    @Override
    public <T extends EntityVisualFX> void render(BatchRenderContext<T> ctx, PoseStack renderStack, VertexConsumer vb, float pTicks) {
        if (this.sprite == null) {
            return;
        }

        Vector3 vec = this.getCameraPosition(pTicks);
        int alpha = this.getAlpha(pTicks);
        float fScale = this.getQuadSize(pTicks);
        Color col = this.getColor(pTicks);

        RenderingDrawUtils.renderFacingQuadVB(vb, renderStack,
                vec.getX(), vec.getY(), vec.getZ(),
                fScale, 0F,
                this.u0, this.v0, this.uLength, this.vLength,
                col.getRed(), col.getGreen(), col.getBlue(), alpha);
    }
}
