/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.obj;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;

/**
 * HellFirePvP@Admin
 * Date: 15.06.2015 / 00:10
 * on WingsExMod
 * GroupObject
 */
public class GroupObject {

    public String name;
    public ArrayList<Face> faces = new ArrayList<>();

    public GroupObject() {
        this("");
    }

    public GroupObject(String name) {
        this.name = name;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(VertexConsumer vb) {
        if (faces.size() > 0) {
            for (Face face : faces) {
                face.addFaceForRender(vb);
            }
        }
    }
}