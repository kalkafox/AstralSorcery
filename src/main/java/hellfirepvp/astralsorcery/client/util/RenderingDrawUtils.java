/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.render.IDrawRenderTypeBuffer;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.util.draw.RenderInfo;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderingDrawUtils
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:27
 */
public class RenderingDrawUtils {

    private static final Random random = new Random();
    private static final PoseStack EMPTY = new PoseStack();

    public static void renderStringCentered(@Nullable Font fr, PoseStack renderStack, FormattedText text, int x, int y, float scale, int color) {
        if (fr == null) {
            fr = Minecraft.getInstance().font;
        }

        float strLength = fr.width(text) * scale;
        float offsetLeft = x - strLength;

        renderStack.pushPose();
        renderStack.translate(offsetLeft, y, 0);
        renderStack.scale(scale, scale, scale);
        renderStringAt(fr, renderStack, text, color);
        renderStack.popPose();
    }

    public static float drawInternal(FormattedText text) {
        return renderStringAt(text, EMPTY, Minecraft.getInstance().font, Color.WHITE.getRGB(), false);
    }

    public static float drawInternal(FormattedCharSequence text) {
        return renderStringAt(text, EMPTY, Minecraft.getInstance().font, Color.WHITE.getRGB(), false);
    }

    public static float drawInternal(FormattedText text, int color) {
        return renderStringAt(text, EMPTY, Minecraft.getInstance().font, color, false);
    }

    public static float drawInternal(FormattedCharSequence text, int color) {
        return renderStringAt(text, EMPTY, Minecraft.getInstance().font, color, false);
    }

    public static float drawInternal(@Nullable Font fr, FormattedText text, int color) {
        return renderStringAt(text, EMPTY, fr, color, false);
    }

    public static float drawInternal(@Nullable Font fr, FormattedCharSequence text, int color) {
        return renderStringAt(text, EMPTY, fr, color, false);
    }

    public static float renderStringAt(@Nullable Font fr, PoseStack renderStack, FormattedText text, int color) {
        return renderStringAt(text, renderStack, fr, color, true);
    }

    public static float renderStringAt(@Nullable Font fr, PoseStack renderStack, FormattedCharSequence text, int color) {
        return renderStringAt(text, renderStack, fr, color, true);
    }

    public static float renderStringAt(FormattedText text, PoseStack renderStack, @Nullable Font fr, int color, boolean dropShadow) {
        return renderStringAt(Language.getInstance().getVisualOrder(text), renderStack, fr, color, dropShadow);
    }

    public static float renderStringAt(FormattedCharSequence text, PoseStack renderStack, @Nullable Font fr, int color, boolean dropShadow) {
        if (fr == null) {
            fr = Minecraft.getInstance().font;
        }
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(new ByteBufferBuilder(256));
        int length = fr.drawInBatch(text, 0, 0, color, dropShadow, renderStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightmapUtil.getPackedFullbrightCoords());
        buffer.endBatch();
        return length;
    }

    public static Rectangle drawInfoStar(PoseStack renderStack, IDrawRenderTypeBuffer buffer, float widthHeightBase, float pTicks) {
        VertexConsumer vb = buffer.getBuffer(RenderTypesAS.GUI_MISC_INFO_STAR);

        float tick = ClientScheduler.getClientTick() + pTicks;
        float deg = (tick * 2) % 360F;
        float wh = widthHeightBase - (widthHeightBase / 6F) * (Mth.sin((float) Math.toRadians(((tick) * 4) % 360F)) + 1F);
        drawInfoStarSingle(renderStack, vb, wh, Math.toRadians(deg));

        deg = ((tick + 22.5F) * 2) % 360F;
        wh = widthHeightBase - (widthHeightBase / 6F) * (Mth.sin((float) Math.toRadians(((tick + 45F) * 4) % 360F)) + 1F);
        drawInfoStarSingle(renderStack, vb, wh, Math.toRadians(deg));

        buffer.draw(RenderTypesAS.GUI_MISC_INFO_STAR);
        return new Rectangle(Mth.floor(-widthHeightBase / 2F), Mth.floor(-widthHeightBase / 2F),
                Mth.floor(widthHeightBase), Mth.floor(widthHeightBase));
    }

    private static void drawInfoStarSingle(PoseStack renderStack, VertexConsumer vb, float widthHeight, double deg) {
        Vector3 offset = new Vector3(-widthHeight / 2D, -widthHeight / 2D, 0).mirror(deg, Vector3.RotAxis.Z_AXIS);
        Vector3 uv01   = new Vector3(-widthHeight / 2D,  widthHeight / 2D, 0).mirror(deg, Vector3.RotAxis.Z_AXIS);
        Vector3 uv11   = new Vector3( widthHeight / 2D,  widthHeight / 2D, 0).mirror(deg, Vector3.RotAxis.Z_AXIS);
        Vector3 uv10   = new Vector3( widthHeight / 2D, -widthHeight / 2D, 0).mirror(deg, Vector3.RotAxis.Z_AXIS);

        Matrix4f matr = renderStack.last().pose();
        vb.addVertex(matr, (float) uv01.getX(),   (float) uv01.getY(),   0).setUv(0, 1);
        vb.addVertex(matr, (float) uv11.getX(),   (float) uv11.getY(),   0).setUv(1, 1);
        vb.addVertex(matr, (float) uv10.getX(),   (float) uv10.getY(),   0).setUv(1, 0);
        vb.addVertex(matr, (float) offset.getX(), (float) offset.getY(), 0).setUv(0, 0);
    }

    public static void renderBlueTooltipComponents(PoseStack renderStack, float x, float y, float blitOffset,
                                                   List<FormattedText> tooltipData, Font font, boolean isFirstLineHeadline) {
        List<Tuple<ItemStack, FormattedText>> stackTooltip = MapStream.ofValues(tooltipData, t -> ItemStack.EMPTY).toTupleList();
        renderBlueTooltip(renderStack, x, y, blitOffset, stackTooltip, font, isFirstLineHeadline);
    }

    public static void renderBlueTooltip(PoseStack renderStack, float x, float y, float blitOffset,
                                         List<Tuple<ItemStack, FormattedText>> tooltipData, Font font, boolean isFirstLineHeadline) {
        renderTooltip(renderStack, x, y, blitOffset, tooltipData, font, isFirstLineHeadline, 0xFF000027, 0xFF000044, Color.WHITE);
    }

    public static void renderTooltip(PoseStack renderStack, float x, float y, float blitOffset,
                                     List<Tuple<ItemStack, FormattedText>> tooltipData, Font font, boolean isFirstLineHeadline,
                                     int color, int colorFade, Color strColor) {
        int stackBoxSize = 18;

        if (!tooltipData.isEmpty()) {
            boolean anyItemFound = false;

            int maxWidth = 0;
            for (Tuple<ItemStack, FormattedText> toolTip : tooltipData) {
                Font customFR = toolTip.getA().getItem().getFont(toolTip.getA());
                if (customFR == null) {
                    customFR = font;
                }
                int width = customFR.width(toolTip.getB());
                if (!toolTip.getA().isEmpty()) {
                    anyItemFound = true;
                }
                if (anyItemFound) {
                    width += stackBoxSize;
                }
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
            if (x + 15 + maxWidth > Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                x -= maxWidth + 24;
            }

            int formatWidth = anyItemFound ? maxWidth - stackBoxSize : maxWidth;
            List<Tuple<ItemStack, List<FormattedCharSequence>>> lengthLimitedToolTip = new LinkedList<>();
            for (Tuple<ItemStack, FormattedText> toolTip : tooltipData) {
                Font customFR = toolTip.getA().getItem().getFont(toolTip.getA());
                if (customFR == null) {
                    customFR = font;
                }

                List<FormattedCharSequence> textLines = customFR.split(toolTip.getB(), formatWidth);
                if (textLines.isEmpty()) {
                    textLines = Collections.singletonList(FormattedCharSequence.EMPTY);
                }
                lengthLimitedToolTip.add(new Tuple<>(toolTip.getA(), textLines));
            }

            float pX = x + 12;
            float pY = y - 12;
            int sumLineHeight = 0;
            if (!lengthLimitedToolTip.isEmpty()) {
                if (lengthLimitedToolTip.size() > 1 && isFirstLineHeadline) {
                    sumLineHeight += 2;
                }
                Iterator<Tuple<ItemStack, List<FormattedCharSequence>>> iterator = lengthLimitedToolTip.iterator();
                while (iterator.hasNext()) {
                    Tuple<ItemStack, List<FormattedCharSequence>> toolTip = iterator.next();
                    int segmentHeight = 0;
                    if (!toolTip.getA().isEmpty()) {
                        segmentHeight += 2;
                        segmentHeight += stackBoxSize;
                        segmentHeight += (Math.max(toolTip.getB().size() - 1, 0)) * 10;
                    } else {
                        segmentHeight += toolTip.getB().size() * 10;
                    }
                    if (!iterator.hasNext()) {
                        segmentHeight -= 2;
                    }
                    sumLineHeight += segmentHeight;
                }
            }

            drawGradientRect(renderStack, blitOffset, pX - 3,           pY - 4,                 pX + maxWidth + 3, pY - 3,                 color, colorFade);
            drawGradientRect(renderStack, blitOffset, pX - 3,           pY + sumLineHeight + 3, pX + maxWidth + 3, pY + sumLineHeight + 4, color, colorFade);
            drawGradientRect(renderStack, blitOffset, pX - 3,           pY - 3,                 pX + maxWidth + 3, pY + sumLineHeight + 3, color, colorFade);
            drawGradientRect(renderStack, blitOffset, pX - 4,           pY - 3,                 pX - 3,           pY + sumLineHeight + 3, color, colorFade);
            drawGradientRect(renderStack, blitOffset, pX + maxWidth + 3,pY - 3,                 pX + maxWidth + 4, pY + sumLineHeight + 3, color, colorFade);

            int col = (color & 0x00FFFFFF) | color & 0xFF000000;
            drawGradientRect(renderStack, blitOffset, pX - 3,           pY - 3 + 1,             pX - 3 + 1,       pY + sumLineHeight + 3 - 1, color, col);
            drawGradientRect(renderStack, blitOffset, pX + maxWidth + 2,pY - 3 + 1,             pX + maxWidth + 3, pY + sumLineHeight + 3 - 1, color, col);
            drawGradientRect(renderStack, blitOffset, pX - 3,           pY - 3,                 pX + maxWidth + 3, pY - 3 + 1,                 col,   col);
            drawGradientRect(renderStack, blitOffset, pX - 3,           pY + sumLineHeight + 2, pX + maxWidth + 3, pY + sumLineHeight + 3,     color, color);

            int offset = anyItemFound ? stackBoxSize : 0;

            renderStack.pushPose();
            renderStack.translate(pX, pY, 0);
            boolean first = true;
            for (Tuple<ItemStack, List<FormattedCharSequence>> toolTip : lengthLimitedToolTip) {
                int minYShift = 10;
                if (!toolTip.getA().isEmpty()) {
                    renderStack.pushPose();
                    renderStack.translate(0, 0, blitOffset);
                    RenderingUtils.renderItemStackGUI(renderStack, toolTip.getA(), null);
                    renderStack.popPose();

                    minYShift = stackBoxSize;
                    renderStack.translate(0, 2, 0);
                }
                for (FormattedCharSequence text : toolTip.getB()) {
                    Font customFR = toolTip.getA().getItem().getFont(toolTip.getA());
                    if (customFR == null) {
                        customFR = font;
                    }
                    renderStack.pushPose();
                    renderStack.translate(offset, 0, blitOffset);
                    renderStringAt(text, renderStack, customFR, strColor.getRGB(), false);
                    renderStack.popPose();

                    renderStack.translate(0, 10, 0);
                    minYShift -= 10;
                }
                if (minYShift > 0) {
                    renderStack.translate(0, minYShift, 0);
                }
                if (isFirstLineHeadline && first) {
                    renderStack.translate(0, 2, 0);
                }
                first = false;
            }
            renderStack.popPose();
        }
    }

    public static void renderBlueTooltipBox(PoseStack renderStack, int x, int y, int width, int height) {
        renderTooltipBox(renderStack, x, y, width, height, 0x000027, 0x000044);
    }

    public static void renderTooltipBox(PoseStack renderStack, int x, int y, int width, int height, int color, int colorFade) {
        int pX = x + 12;
        int pY = y - 12;

        drawGradientRect(renderStack, 0, pX - 3,           pY - 4,          pX + width + 3, pY - 3,         color, colorFade);
        drawGradientRect(renderStack, 0, pX - 3,           pY + height + 3, pX + width + 3, pY + height + 4, color, colorFade);
        drawGradientRect(renderStack, 0, pX - 3,           pY - 3,          pX + width + 3, pY + height + 3, color, colorFade);
        drawGradientRect(renderStack, 0, pX - 4,           pY - 3,          pX - 3,         pY + height + 3, color, colorFade);
        drawGradientRect(renderStack, 0, pX + width + 3,   pY - 3,          pX + width + 4, pY + height + 3, color, colorFade);

        int col = (color & 0x00FFFFFF) | color & 0xFF000000;
        drawGradientRect(renderStack, 0, pX - 3,           pY - 3 + 1,      pX - 3 + 1,     pY + height + 3 - 1, color, col);
        drawGradientRect(renderStack, 0, pX + width + 2,   pY - 3 + 1,      pX + width + 3, pY + height + 3 - 1, color, col);
        drawGradientRect(renderStack, 0, pX - 3,           pY - 3,          pX + width + 3, pY - 3 + 1,          col,   col);
        drawGradientRect(renderStack, 0, pX - 3,           pY + height + 2, pX + width + 3, pY + height + 3,     color, color);
    }

    public static void drawGradientRect(PoseStack renderStack, float blitOffset, float left, float top, float right, float bottom, int startColor, int endColor) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed   = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >>  8 & 255) / 255.0F;
        float startBlue  = (float) (startColor       & 255) / 255.0F;
        float endAlpha   = (float) (endColor   >> 24 & 255) / 255.0F;
        float endRed     = (float) (endColor   >> 16 & 255) / 255.0F;
        float endGreen   = (float) (endColor   >>  8 & 255) / 255.0F;
        float endBlue    = (float) (endColor         & 255) / 255.0F;

        Blending.DEFAULT.apply();

        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.addVertex(offset, right,    top, blitOffset).setColor(startRed, startGreen, startBlue, startAlpha);
            buf.addVertex(offset,  left,    top, blitOffset).setColor(startRed, startGreen, startBlue, startAlpha);
            buf.addVertex(offset,  left, bottom, blitOffset).setColor(  endRed,   endGreen,   endBlue,   endAlpha);
            buf.addVertex(offset, right, bottom, blitOffset).setColor(  endRed,   endGreen,   endBlue,   endAlpha);
        });

    }

    public static void renderLightRayFan(PoseStack renderStack, MultiBufferSource buffer, Color color, long seed, int minScale, float scale, int count) {
        random.initNoise(seed);

        float f1 = ClientScheduler.getClientTick() / 400.0F;
        float f2 = 0.0F;
        int alpha = (int) (255.0F * (1.0F - f2));

        VertexConsumer vb = buffer.getBuffer(RenderTypesAS.EFFECT_LIGHTRAY_FAN);

        renderStack.pushPose();
        for (int i = 0; i < count; i++) {
            renderStack.pushPose();
            renderStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            renderStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            renderStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            renderStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            renderStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            renderStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + f1 * 360.0F));
            Matrix4f matr = renderStack.last().pose();

            float fa = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
            fa /= 30.0F / (Math.min(minScale, 10 * scale) / 10.0F);
            f4 /= 30.0F / (Math.min(minScale, 10 * scale) / 10.0F);

            vb.addVertex(matr, 0F,      0F, 0F)        .setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            vb.addVertex(matr, 0F,      0F, 0F)        .setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            vb.addVertex(matr, -0.7F * f4, fa, -0.5F * f4).setColor(color.getRed(), color.getGreen(), color.getBlue(), 0);
            vb.addVertex(matr,  0.7F * f4, fa, -0.5F * f4).setColor(color.getRed(), color.getGreen(), color.getBlue(), 0);
            vb.addVertex(matr, 0F,     0F, 0F)        .setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            vb.addVertex(matr, 0F,     0F, 0F)        .setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            vb.addVertex(matr, 0.7F * f4, fa, -0.5F * f4).setColor(color.getRed(), color.getGreen(), color.getBlue(), 0);
            vb.addVertex(matr, 0F,        fa,    1F * f4).setColor(color.getRed(), color.getGreen(), color.getBlue(), 0);
            vb.addVertex(matr, 0F,      0F, 0F)        .setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            vb.addVertex(matr, 0F,      0F, 0F)        .setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            vb.addVertex(matr, 0F,         fa,    1F * f4).setColor(color.getRed(), color.getGreen(), color.getBlue(), 0);
            vb.addVertex(matr, -0.7F * f4, fa, -0.5F * f4).setColor(color.getRed(), color.getGreen(), color.getBlue(), 0);

            renderStack.popPose();
        }
        renderStack.popPose();

        RenderingUtils.refreshDrawing(vb, RenderTypesAS.EFFECT_LIGHTRAY_FAN);
    }

    public static void renderFacingFullQuadVB(VertexConsumer vb, PoseStack renderStack, double px, double py, double pz, float scale, float angle, int r, int g, int b, int alpha) {
        renderFacingQuadVB(vb, renderStack, px, py, pz, scale, angle, 0F, 0F, 1F, 1F, r, g, b, alpha);
    }

    public static void renderFacingSpriteVB(VertexConsumer vb, PoseStack renderStack, double px, double py, double pz, float scale, float angle, SpriteSheetResource sprite, long spriteTick, int r, int g, int b, int alpha) {
        Tuple<Float, Float> uv = sprite.getUVOffset(spriteTick);
        renderFacingQuadVB(vb, renderStack, px, py, pz, scale, angle, uv.getA(), uv.getB(), sprite.getULength(), sprite.getVLength(), r, g, b, alpha);
    }

    public static void renderFacingQuadVB(VertexConsumer vb, PoseStack renderStack, double px, double py, double pz, float scale, float angle, float u, float v, float uLength, float vLength, int r, int g, int b, int alpha) {
        Vector3 pos = new Vector3(px, py, pz);

        RenderInfo ri = RenderInfo.getInstance();
        Camera ari = ri.getARI();

        float arX =  ri.getRotationX();
        float arZ =  ri.getRotationZ();
        float arYZ = ri.getRotationYZ();
        float arXY = ri.getRotationXY();
        float arXZ = ri.getRotationXZ();

        Vec3 viewDistance = ari.getPosition();
        Vector3f forwards = ari.getLookVector();

        Vector3 iPos = new Vector3(viewDistance);
        Vector3 v1 = new Vector3(-arX * scale - arYZ * scale, -arXZ * scale, -arZ * scale - arXY * scale);
        Vector3 v2 = new Vector3(-arX * scale + arYZ * scale,  arXZ * scale, -arZ * scale + arXY * scale);
        Vector3 v3 = new Vector3( arX * scale + arYZ * scale,  arXZ * scale,  arZ * scale + arXY * scale);
        Vector3 v4 = new Vector3( arX * scale - arYZ * scale, -arXZ * scale,  arZ * scale - arXY * scale);
        if (angle != 0.0F) {
            float cAngle = Mth.cos(angle * 0.5F);
            float cAngleSq = cAngle * cAngle;

            Vector3 vAngle = new Vector3(
                    Mth.sin(angle * 0.5F) * forwards.getX(),
                    Mth.sin(angle * 0.5F) * forwards.getY(),
                    Mth.sin(angle * 0.5F) * forwards.getZ());

            v1 = vAngle.clone()
                    .mul(2 * v1.dot(vAngle))
                    .add(v1.clone().mul(cAngleSq - vAngle.dot(vAngle)))
                    .add(vAngle.clone().cross(v1.clone().mul(2 * cAngle)));
            v2 = vAngle.clone()
                    .mul(2 * v2.dot(vAngle))
                    .add(v2.clone().mul(cAngleSq - vAngle.dot(vAngle)))
                    .add(vAngle.clone().cross(v2.clone().mul(2 * cAngle)));
            v3 = vAngle.clone()
                    .mul(2 * v3.dot(vAngle))
                    .add(v3.clone().mul(cAngleSq - vAngle.dot(vAngle)))
                    .add(vAngle.clone().cross(v3.clone().mul(2 * cAngle)));
            v4 = vAngle.clone()
                    .mul(2 * v4.dot(vAngle))
                    .add(v4.clone().mul(cAngleSq - vAngle.dot(vAngle)))
                    .add(vAngle.clone().cross(v4.clone().mul(2 * cAngle)));
        }

        Matrix4f matr = renderStack.last().pose();
        pos.clone().add(v1).subtract(iPos).drawPos(matr, vb).setColor(r, g, b, alpha).setUv(u + uLength, v + vLength);
        pos.clone().add(v2).subtract(iPos).drawPos(matr, vb).setColor(r, g, b, alpha).setUv(u + uLength, v);
        pos.clone().add(v3).subtract(iPos).drawPos(matr, vb).setColor(r, g, b, alpha).setUv(u, v );
        pos.clone().add(v4).subtract(iPos).drawPos(matr, vb).setColor(r, g, b, alpha).setUv(u, v + vLength);
    }

    public static void renderTexturedCubeCentralColorLighted(VertexConsumer buf, PoseStack renderStack,
                                                             float u, float v, float uLength, float vLength,
                                                             int r, int g, int b, int a,
                                                             int combinedLight) {

        Matrix4f matr = renderStack.last().pose();

        buf.addVertex(matr, -0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v).setLight(combinedLight);
        buf.addVertex(matr,  0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setLight(combinedLight);
        buf.addVertex(matr,  0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setLight(combinedLight);
        buf.addVertex(matr, -0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setLight(combinedLight);

        buf.addVertex(matr, -0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v).setLight(combinedLight);
        buf.addVertex(matr,  0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setLight(combinedLight);
        buf.addVertex(matr,  0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setLight(combinedLight);
        buf.addVertex(matr, -0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setLight(combinedLight);

        buf.addVertex(matr, -0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setLight(combinedLight);
        buf.addVertex(matr, -0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setLight(combinedLight);
        buf.addVertex(matr, -0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setLight(combinedLight);
        buf.addVertex(matr, -0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v).setLight(combinedLight);

        buf.addVertex(matr,  0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setLight(combinedLight);
        buf.addVertex(matr,  0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setLight(combinedLight);
        buf.addVertex(matr,  0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setLight(combinedLight);
        buf.addVertex(matr,  0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v).setLight(combinedLight);

        buf.addVertex(matr,  0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v).setLight(combinedLight);
        buf.addVertex(matr, -0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setLight(combinedLight);
        buf.addVertex(matr, -0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setLight(combinedLight);
        buf.addVertex(matr,  0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setLight(combinedLight);

        buf.addVertex(matr, -0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v).setLight(combinedLight);
        buf.addVertex(matr,  0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setLight(combinedLight);
        buf.addVertex(matr,  0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setLight(combinedLight);
        buf.addVertex(matr, -0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setLight(combinedLight);
    }

    public static void renderTexturedCubeCentralColorNormal(PoseStack renderStack, VertexConsumer vb,
                                                            float u, float v, float uLength, float vLength,
                                                            int r, int g, int b, int a,
                                                            Matrix3f normalMatr) {

        Matrix4f offset = renderStack.last().pose();
        vb.addVertex(offset, -0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setNormal(normalMatr, 0, 0, 0);

        vb.addVertex(offset, -0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setNormal(normalMatr, 0, 0, 0);

        vb.addVertex(offset, -0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v).setNormal(normalMatr, 0, 0, 0);

        vb.addVertex(offset,  0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v).setNormal(normalMatr, 0, 0, 0);

        vb.addVertex(offset,  0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F, -0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F,  0.5F, -0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setNormal(normalMatr, 0, 0, 0);

        vb.addVertex(offset, -0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F, -0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset,  0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u + uLength, v + vLength).setNormal(normalMatr, 0, 0, 0);
        vb.addVertex(offset, -0.5F,  0.5F,  0.5F).setColor(r, g, b, a).setUv(u, v + vLength).setNormal(normalMatr, 0, 0, 0);
    }

    public static void renderAngleRotatedTexturedRectVB(VertexConsumer vb, PoseStack renderStack, Vector3 renderOffset, Vector3 axis, float angleRad, float scale, float u, float v, float uLength, float vLength, int r, int g, int b, int a) {
        Vector3 renderStart = axis.clone().perpendicular().mirror(angleRad, axis).normalize();
        Matrix4f matr = renderStack.last().pose();

        Vector3 vec = renderStart.clone().mirror(Math.toRadians(90), axis).normalize().mul(scale).add(renderOffset);
        vec.drawPos(matr, vb).setColor(r, g, b, a).setUv(u, v + vLength);

        vec = renderStart.clone().mul(-1).normalize().mul(scale).add(renderOffset);
        vec.drawPos(matr, vb).setColor(r, g, b, a).setUv(u + uLength, v + vLength);

        vec = renderStart.clone().mirror(Math.toRadians(270), axis).normalize().mul(scale).add(renderOffset);
        vec.drawPos(matr, vb).setColor(r, g, b, a).setUv(u + uLength, v);

        vec = renderStart.clone().normalize().mul(scale).add(renderOffset);
        vec.drawPos(matr, vb).setColor(r, g, b, a).setUv(u, v);
    }

}
