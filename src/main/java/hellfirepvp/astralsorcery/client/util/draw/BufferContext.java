/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.draw;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BufferContext
 * Created by HellFirePvP
 * Date: 08.07.2019 / 20:39
 */
// 1.21 port: BufferBuilder is no longer reusable across begin/draw cycles; this wraps a persistent
// ByteBufferBuilder and creates a fresh BufferBuilder per begin(), delegating VertexConsumer calls.
public class BufferContext implements VertexConsumer {

    private final ByteBufferBuilder backingBuffer;
    private BufferBuilder buffer = null;
    private VertexFormat format = null;

    BufferContext(int size) {
        this.backingBuffer = new ByteBufferBuilder(size);
    }

    public void begin(VertexFormat.Mode mode, VertexFormat format) {
        if (this.buffer == null) {
            this.buffer = new BufferBuilder(this.backingBuffer, mode, format);
            this.format = format;
        }
    }

    public void draw() {
        if (this.buffer != null) {
            MeshData data = this.buffer.build();
            if (data != null) {
                RenderingUtils.draw(this.format, data);
            }
            this.buffer = null;
            this.format = null;
        }
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return this.buffer.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return this.buffer.setColor(r, g, b, a);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this.buffer.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this.buffer.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this.buffer.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this.buffer.setNormal(x, y, z);
    }

}
