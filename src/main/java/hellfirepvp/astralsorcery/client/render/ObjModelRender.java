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
import hellfirepvp.astralsorcery.client.util.obj.WavefrontObject;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ObjModelRender
 * Created by HellFirePvP
 * Date: 05.04.2020 / 10:59
 */
public class ObjModelRender {

    private static WavefrontObject crystalModel;
    //private static VertexBuffer vboCrystal;

    private static WavefrontObject celestialWingsModel;
    private static VertexBuffer vboCelestialWings;

    private static WavefrontObject wraithWingsModel;
    private static VertexBuffer wraithWingsBones, wraithWingsWing;

    public static void renderCrystal(PoseStack renderStack, VertexConsumer buf, Runnable drawFn) {
        if (crystalModel == null) {
            crystalModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "crystal");
        }
        //if (vboCrystal == null) {
        //    int[] transparent = new int[] { 255, 255, 255, 65 };
        //    BufferDecoratorBuilder.withColor((r, g, b, a) -> transparent)
        //            .decorate(Tessellator.getInstance().getBuffer(),
        //                    (BufferBuilder decorated) -> vboCrystal = crystalModel.batch(decorated));
        //}

        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(renderStack.last().pose());
        crystalModel.render(buf);
        drawFn.run();
        RenderSystem.popMatrix();

        //vboCrystal.bindBuffer();
        //DefaultVertexFormats.POSITION_COLOR_TEX.setupBufferState(0L);
        //vboCrystal.draw(renderStack.getLast().getMatrix(), crystalModel.getGLDrawingMode());
        //DefaultVertexFormats.POSITION_COLOR_TEX.clearBufferState();
        //VertexBuffer.unbindBuffer();
    }

    public static void renderCelestialWings(PoseStack renderStack) {
        if (celestialWingsModel == null) {
            celestialWingsModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "celestial_wings");
        }
        if (vboCelestialWings == null) {
            int[] lightGray = new int[] { 178, 178, 178, 255 };
            BufferDecoratorBuilder.withColor((r, g, b, a) -> lightGray)
                    .decorate(Tesselator.getInstance().getBuffer(),
                            (BufferBuilder decorated) -> vboCelestialWings = celestialWingsModel.batch(decorated));
        }
        vboCelestialWings.bindBuffer();
        RenderTypesAS.POSITION_COLOR_TEX_NORMAL.setupBufferState(0L);
        vboCelestialWings.draw(renderStack.last().pose(), celestialWingsModel.getGLDrawingMode());
        RenderTypesAS.POSITION_COLOR_TEX_NORMAL.clearBufferState();
        VertexBuffer.unbind();
    }

    public static void renderWraithWings(PoseStack renderStack) {
        if (wraithWingsModel == null) {
            wraithWingsModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "wraith_wings");
        }

        if (wraithWingsBones == null) {
            int[] gray = new int[] { 77, 77, 77, 255 };
            BufferDecoratorBuilder.withColor((r, g, b, a) -> gray)
                    .decorate(Tesselator.getInstance().getBuffer(),
                            (BufferBuilder decorated) -> wraithWingsBones = wraithWingsModel.batchOnly(decorated, "Bones"));
        }
        if (wraithWingsWing == null) {
            int[] black = new int[] { 0, 0, 0, 255 };
            BufferDecoratorBuilder.withColor((r, g, b, a) -> black)
                    .decorate(Tesselator.getInstance().getBuffer(),
                            (BufferBuilder decorated) -> wraithWingsWing = wraithWingsModel.batchOnly(decorated, "Wing"));
        }

        wraithWingsBones.bindBuffer();
        RenderTypesAS.POSITION_COLOR_TEX_NORMAL.setupBufferState(0L);
        wraithWingsBones.draw(renderStack.last().pose(), wraithWingsModel.getGLDrawingMode());
        RenderTypesAS.POSITION_COLOR_TEX_NORMAL.clearBufferState();
        VertexBuffer.unbind();

        wraithWingsWing.bindBuffer();
        RenderTypesAS.POSITION_COLOR_TEX_NORMAL.setupBufferState(0L);
        wraithWingsWing.draw(renderStack.last().pose(), wraithWingsModel.getGLDrawingMode());
        RenderTypesAS.POSITION_COLOR_TEX_NORMAL.clearBufferState();
        VertexBuffer.unbind();
    }
}
