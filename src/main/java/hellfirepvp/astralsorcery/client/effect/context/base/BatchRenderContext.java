/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.context.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.effect.EntityDynamicFX;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHandler;
import hellfirepvp.astralsorcery.client.render.IDrawRenderTypeBuffer;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.order.OrderSortable;
import hellfirepvp.observerlib.client.util.RenderTypeDecorator;
import net.minecraft.client.renderer.RenderType;

import java.util.List;
import java.util.function.BiFunction;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BatchRenderContext
 * Created by HellFirePvP
 * Date: 07.07.2019 / 10:58
 */
public class BatchRenderContext<T extends EntityVisualFX> extends OrderSortable {

    private static int counter = 0;

    private final int id;
    private final SpriteSheetResource sprite;
    private boolean drawWithTexture = true;
    protected RenderType renderType;
    protected BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator;

    public BatchRenderContext(RenderType renderType,
                              BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator) {
        this(new SpriteSheetResource(BlockAtlasTexture.getInstance()), renderType, particleCreator);
    }

    public BatchRenderContext(AbstractRenderableTexture texture,
                              RenderType renderType,
                              BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator) {
        this(new SpriteSheetResource(texture), renderType, particleCreator);
    }

    public BatchRenderContext(SpriteSheetResource sprite,
                              RenderType renderType,
                              BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator) {
        this.id = counter++;
        this.sprite = sprite;
        this.renderType = renderType;
        this.particleCreator = particleCreator.andThen(fx -> {
            int frames = this.sprite.getFrameCount();
            if (frames > 1) {
                fx.setMaxAge(frames);
            }
            return fx;
        });
    }

    public T createParticle(Vector3 pos) {
        return this.particleCreator.apply(this, pos);
    }

    public BatchRenderContext<T> setDrawWithTexture(boolean drawWithTexture) {
        this.drawWithTexture = drawWithTexture;
        return this;
    }

    public SpriteSheetResource getSprite() {
        return sprite;
    }

    public void render(List<EffectHandler.PendingEffect> effects, PoseStack renderStack, IDrawRenderTypeBuffer drawBuffer, float pTicks) {
        //Erase type due to impossible typing
        BatchRenderContext blankCtx = this;
        effects.stream()
                .filter(effect -> effect.getEffect() instanceof EntityDynamicFX)
                .forEach(effect -> ((EntityDynamicFX) effect.getEffect()).renderNow(blankCtx, renderStack, drawBuffer, pTicks));

        RenderType drawType = this.getRenderType();
        if (this.drawWithTexture) {
            drawType = RenderTypeDecorator.wrapSetup(this.getRenderType(),
                    () -> this.getSprite().bindTexture(),
                    () -> BlockAtlasTexture.getInstance().bindTexture());
        }
        VertexConsumer buf = drawBuffer.getBuffer(drawType);
        effects.forEach(effect -> effect.getEffect().render(this, renderStack, buf, pTicks));
        this.drawBatched(buf, drawBuffer);
    }

    private void drawBatched(VertexConsumer buf, IDrawRenderTypeBuffer renderTypeBuffer) {
        // 1.21 port: manual sortVertexData is gone; translucency sorting happens via
        // MeshData.sortQuads/sortOnUpload inside the buffer source when the RenderType requests it.
        renderTypeBuffer.draw();
    }

    public RenderType getRenderType() {
        return renderType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchRenderContext that = (BatchRenderContext) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
