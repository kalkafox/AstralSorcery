/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.obj.WavefrontObject;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ObjModelRender
 * Created by HellFirePvP
 * Date: 05.04.2020 / 10:59
 */
public class ObjModelRender {

    private static WavefrontObject crystalModel;

    private static WavefrontObject celestialWingsModel;
    private static VertexBuffer vboCelestialWings;

    private static WavefrontObject wraithWingsModel;
    private static VertexBuffer wraithWingsBones, wraithWingsWing;

    public static void renderCrystal(PoseStack renderStack, VertexConsumer buf, Runnable drawFn) {
        if (crystalModel == null) {
            crystalModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "crystal");
        }

        Matrix4f pose = new Matrix4f(renderStack.last().pose());
        BufferDecoratorBuilder.withPosition((x, y, z) -> {
            Vector4f pos = pose.transform(new Vector4f((float) x, (float) y, (float) z, 1F));
            return new double[] { pos.x(), pos.y(), pos.z() };
        }).decorate(buf, decorated -> crystalModel.render(decorated));
        drawFn.run();
    }

    public static void renderCelestialWings(PoseStack renderStack) {
        if (celestialWingsModel == null) {
            celestialWingsModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "celestial_wings");
        }
        if (vboCelestialWings == null) {
            int[] lightGray = new int[] { 178, 178, 178, 255 };
            BufferDecoratorBuilder decorator = BufferDecoratorBuilder.withColor((r, g, b, a) -> lightGray);
            vboCelestialWings = celestialWingsModel.batch(decorator::decorate);
        }
        drawVbo(vboCelestialWings, renderStack);
    }

    public static void renderWraithWings(PoseStack renderStack) {
        if (wraithWingsModel == null) {
            wraithWingsModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "wraith_wings");
        }

        if (wraithWingsBones == null) {
            int[] gray = new int[] { 77, 77, 77, 255 };
            BufferDecoratorBuilder decorator = BufferDecoratorBuilder.withColor((r, g, b, a) -> gray);
            wraithWingsBones = wraithWingsModel.batchOnly(decorator::decorate, "Bones");
        }
        if (wraithWingsWing == null) {
            int[] black = new int[] { 0, 0, 0, 255 };
            BufferDecoratorBuilder decorator = BufferDecoratorBuilder.withColor((r, g, b, a) -> black);
            wraithWingsWing = wraithWingsModel.batchOnly(decorator::decorate, "Wing");
        }

        drawVbo(wraithWingsBones, renderStack);
        drawVbo(wraithWingsWing, renderStack);
    }

    private static void drawVbo(VertexBuffer vbo, PoseStack renderStack) {
        if (vbo == null) {
            return;
        }
        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(renderStack.last().pose());
        vbo.bind();
        vbo.drawWithShader(modelView, RenderSystem.getProjectionMatrix(),
                RenderingUtils.shaderFor(RenderTypesAS.POSITION_COLOR_TEX_NORMAL).get());
        VertexBuffer.unbind();
    }
}
