/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.obj;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * HellFirePvP@Admin
 * Date: 15.06.2015 / 00:07
 * on WingsExMod
 * Face
 */
public class Face {

    Vertex[] vertices;
    Vertex[] vertexNormals;
    Vertex faceNormal;
    TextureCoordinate[] textureCoordinates;

    @OnlyIn(Dist.CLIENT)
    void addFaceForRender(VertexConsumer vb) {
        addFaceForRender(vb, 0.0004F);
    }

    @OnlyIn(Dist.CLIENT)
    void addFaceForRender(VertexConsumer vb, float textureOffset) {
        float averageU = 0F;
        float averageV = 0F;

        for (TextureCoordinate textureCoordinate : textureCoordinates) {
            averageU += textureCoordinate.u;
            averageV += textureCoordinate.v;
        }

        averageU = averageU / textureCoordinates.length;
        averageV = averageV / textureCoordinates.length;

        for (int i = 0; i < vertices.length; ++i) {
            float uOffset = textureOffset;
            float vOffset = textureOffset;

            if (textureCoordinates[i].u > averageU) {
                uOffset = -uOffset;
            }
            if (textureCoordinates[i].v > averageV) {
                vOffset = -vOffset;
            }

            vb.addVertex(vertices[i].x, vertices[i].y, vertices[i].z)
                    .setColor(255, 255, 255, 255)
                    .setUv(textureCoordinates[i].u + uOffset, textureCoordinates[i].v + vOffset)
                    .setNormal(faceNormal.x, faceNormal.y, faceNormal.z)
                    ;
        }
    }

    Vertex calculateFaceNormal() {
        Vector3 v1 = new Vector3(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
        Vector3 v2 = new Vector3(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
        Vector3 normalVector = v1.cross(v2).normalize();

        return new Vertex((float) normalVector.getX(), (float) normalVector.getY(), (float) normalVector.getZ());
    }
}