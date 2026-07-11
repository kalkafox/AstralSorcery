/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.Tesselator;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IDrawRenderTypeBuffer
 * Created by HellFirePvP
 * Date: 06.06.2020 / 09:38
 */
public interface IDrawRenderTypeBuffer extends MultiBufferSource {

    public void draw();

    public void draw(RenderType type);

    public static IDrawRenderTypeBuffer defaultBuffer() {
        return of(MultiBufferSource.immediate(new ByteBufferBuilder(256)));
    }

    public static IDrawRenderTypeBuffer of(MultiBufferSource.BufferSource drawBuffer) {
        return new IDrawRenderTypeBuffer() {
            @Override
            public void draw() {
                drawBuffer.finish();
            }

            @Override
            public void draw(RenderType type) {
                drawBuffer.finish(type);
            }

            @Override
            public VertexConsumer getBuffer(RenderType renderType) {
                return drawBuffer.getBuffer(renderType);
            }
        };
    }

}
