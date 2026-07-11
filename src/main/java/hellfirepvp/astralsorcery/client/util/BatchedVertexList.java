/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BatchedVertexList
 * Created by HellFirePvP
 * Date: 13.01.2020 / 21:03
 */
public class BatchedVertexList {

    private final VertexFormat vFormat;
    private VertexBuffer vbo = null;
    private boolean initialized = false;

    public BatchedVertexList(VertexFormat vFormat) {
        this.vFormat = vFormat;
    }

    public void batch(Consumer<BufferBuilder> batchFn) {
        if (this.initialized) {
            return;
        }

        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, this.vFormat);
        batchFn.accept(buf);
        MeshData data = buf.build();
        if (data != null) {
            this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            this.vbo.bind();
            this.vbo.upload(data);
            VertexBuffer.unbind();
        }

        this.initialized = true;
    }

    public void render(PoseStack renderStack) {
        if (!this.initialized || this.vbo == null) {
            return;
        }

        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(renderStack.last().pose());
        this.vbo.bind();
        this.vbo.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), RenderingUtils.shaderFor(this.vFormat).get());
        VertexBuffer.unbind();
    }

    public void reset() {
        if (this.vbo != null) {
            this.vbo.close();
            this.vbo = null;
        }

        this.initialized = false;
    }
}
