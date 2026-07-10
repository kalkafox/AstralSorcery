/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.progression;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.screen.helper.ScalingPoint;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPages;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.network.chat.FormattedText;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalClusterRenderer
 * Created by HellFirePvP
 * Date: 03.08.2019 / 18:06
 */
public class ScreenJournalClusterRenderer {

    private ProgressionSizeHandler progressionSizeHandler;
    private ResearchProgression progression;
    private ScalingPoint mousePointScaled;
    private ScalingPoint previousMousePointScaled;

    private int renderOffsetX, renderOffsetY;
    private int renderGuiHeight, renderGuiWidth;
    private boolean hasPrevOffset = false;

    private float alpha = 1F;

    private Map<Rectangle, ResearchNode> clickableNodes = new HashMap<>();

    public ScreenJournalClusterRenderer(ResearchProgression progression, int guiHeight, int guiWidth, int leftPos, int topPos) {
        this.progression = progression;
        this.progressionSizeHandler = new ProgressionSizeHandler(progression);
        this.progressionSizeHandler.setMaxScale(1.2F);
        this.progressionSizeHandler.setMinScale(0.1F);
        this.progressionSizeHandler.setScaleSpeed(0.9F / 20F);
        this.progressionSizeHandler.updateSize();
        this.progressionSizeHandler.forceScaleTo(0.1F);

        this.mousePointScaled = ScalingPoint.createPoint(0, 0, this.progressionSizeHandler.getScalingFactor(), false);
        this.centerMouse();
        this.applyMovedMouseOffset();

        this.renderOffsetX = leftPos;
        this.renderOffsetY = topPos;
        this.renderGuiHeight = guiHeight;
        this.renderGuiWidth = guiWidth;
    }

    public boolean propagateClick(ScreenJournalProgression parent, double xpos, double ypos) {
        Rectangle frame = new Rectangle(renderOffsetX, renderOffsetY, renderGuiWidth, renderGuiHeight);
        if (frame.contains(xpos, ypos)) {
            for (Rectangle r : clickableNodes.keySet()) {
                if (r.contains(xpos, ypos)) {
                    ResearchNode clicked = clickableNodes.get(r);
                    Minecraft.getInstance().setScreen(new ScreenJournalPages(parent, clicked));
                    return true;
                }
            }
        }
        return false;
    }

    public void drawMouseHighlight(PoseStack renderStack, float blitOffset, int xpos, int ypos) {
        Rectangle frame = new Rectangle(renderOffsetX, renderOffsetY, renderGuiWidth, renderGuiHeight);
        if (frame.contains(xpos, ypos)) {
            for (Rectangle r : clickableNodes.keySet()) {
                if (r.contains(xpos, ypos)) {
                    FormattedText name = clickableNodes.get(r).getName();

                    renderStack.pushPose();
                    renderStack.translate(r.getX(), r.getY(), blitOffset + 200);
                    renderStack.scale(progressionSizeHandler.getScalingFactor(), progressionSizeHandler.getScalingFactor(), 1F);
                    RenderingDrawUtils.renderBlueTooltipComponents(renderStack, 0, 0, 0, Lists.newArrayList(name), Minecraft.getInstance().font, false);
                    renderStack.popPose();
                }
            }
        }
    }

    public void centerMouse() {
        Point.Float center = this.progressionSizeHandler.getRelativeCenter();
        this.moveMouse(center.x, center.y);
    }

    public void moveMouse(float changedX, float changedY) {
        if (hasPrevOffset) {
            mousePointScaled.updateScaledPos(
                    progressionSizeHandler.clampX(previousMousePointScaled.getScaledPosX() + changedX),
                    progressionSizeHandler.clampY(previousMousePointScaled.getScaledPosY() + changedY),
                    progressionSizeHandler.getScalingFactor());
        } else {
            mousePointScaled.updateScaledPos(
                    progressionSizeHandler.clampX(changedX),
                    progressionSizeHandler.clampY(changedY),
                    progressionSizeHandler.getScalingFactor());
        }
    }

    public void applyMovedMouseOffset() {
        this.previousMousePointScaled = ScalingPoint.createPoint(
                mousePointScaled.getScaledPosX(),
                mousePointScaled.getScaledPosY(),
                progressionSizeHandler.getScalingFactor(),
                true);
        this.hasPrevOffset = true;
    }

    public void handleZoomOut() {
        this.progressionSizeHandler.handleZoomOut();
        rescale(progressionSizeHandler.getScalingFactor());
    }

    public void handleZoomIn() {
        this.progressionSizeHandler.handleZoomIn();
        rescale(progressionSizeHandler.getScalingFactor());
    }

    public float xpos() {
        return mousePointScaled.getX();
    }

    public float ypos() {
        return mousePointScaled.getY();
    }

    private void rescale(float newScale) {
        this.mousePointScaled.rescale(newScale);
        if (this.previousMousePointScaled != null) {
            this.previousMousePointScaled.rescale(newScale);
        }
        moveMouse(0, 0);
    }

    public void drawClusterScreen(PoseStack renderStack, WidthHeightScreen parentGui, float blitOffset) {
        clickableNodes.clear();

        drawNodesAndConnections(renderStack, parentGui, blitOffset);
    }

    private void drawNodesAndConnections(PoseStack renderStack, WidthHeightScreen parentGui, float blitOffset) {
        alpha = progressionSizeHandler.getScalingFactor(); //between 0.25F and ~1F
        alpha -= 0.25F;
        alpha /= 0.75F;
        alpha = Mth.clamp(alpha, 0F, 1F);

        Map<ResearchNode, Point.Float> displayPositions = new HashMap<>();
        for (ResearchNode node : progression.getResearchNodes()) {
            if (!node.canSee(ResearchHelper.getClientProgress())) {
                continue;
            }
            Point.Float from = this.progressionSizeHandler.scalePointToGui(parentGui, this.mousePointScaled, new Point.Float(node.renderPosX, node.renderPosZ));
            for (ResearchNode target : node.getConnectionsTo()) {
                Point.Float to = this.progressionSizeHandler.scalePointToGui(parentGui, this.mousePointScaled, new Point.Float(target.renderPosX, target.renderPosZ));
                drawConnection(renderStack, from.x, from.y, to.x, to.y, blitOffset);
            }

            displayPositions.put(node, from);
        }
        displayPositions.forEach((node, pos) -> renderNodeToGUI(renderStack, node, pos, blitOffset));
    }

    private void renderNodeToGUI(PoseStack renderStack, ResearchNode node, Point.Float offset, float blitOffset) {
        float zoomedWH = progressionSizeHandler.getZoomedWHNode();
        float offsetX = offset.x - zoomedWH / 2F;
        float offsetY = offset.y - zoomedWH / 2F;

        node.getBackgroundTexture().resolve().bindTexture();
        if (progressionSizeHandler.getScalingFactor() >= 0.7) {
            clickableNodes.put(new Rectangle(Mth.floor(offsetX), Mth.floor(offsetY), Mth.floor(zoomedWH), Mth.floor(zoomedWH)), node);
        }
        drawResearchItemBackground(zoomedWH, offsetX, offsetY, blitOffset);

        float pxWH = progressionSizeHandler.getZoomedWHNode() / 16F;

        switch (node.getNodeRenderType()) {
            case ITEM_STACK:
                renderStack.pushPose();
                renderStack.translate(offsetX, offsetY, 0);
                renderStack.scale(progressionSizeHandler.getScalingFactor(), progressionSizeHandler.getScalingFactor(), 1);
                renderStack.translate(3, 3, 100);
                renderStack.scale(0.75F, 0.75F, 1);

                Lighting.turnBackOn();
                RenderingUtils.renderTranslucentItemStackModelGUI(node.getRenderItemStack(ClientScheduler.getClientTick()),
                        renderStack, Color.WHITE, Blending.DEFAULT, Mth.clamp((int) (alpha * 255F), 0, 255));
                Lighting.turnOff();

                renderStack.popPose();
                break;
            case TEXTURE_SPRITE:
                Color col = node.getTextureColorHint();

                float r = (col.getRed() / 255F)   * alpha;
                float g = (col.getGreen() / 255F) * alpha;
                float b = (col.getBlue() / 255F)  * alpha;
                float a = (col.getAlpha() / 255F) * alpha;

                SpriteSheetResource res = node.getSpriteTexture().resolveSprite();
                res.getResource().bindTexture();
                Tuple<Float, Float> uvTexture = res.getUVOffset(ClientScheduler.getClientTick());

                renderStack.pushPose();
                renderStack.translate(offsetX, offsetY, 0);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                    Matrix4f matr = renderStack.last().pose();
                    buf.vertex(matr, pxWH, zoomedWH - pxWH, blitOffset)
                            .color(r, g, b, a)
                            .tex(uvTexture.getA(), uvTexture.getB() + res.getVLength())
                            .endVertex();
                    buf.vertex(matr, zoomedWH - pxWH, zoomedWH - pxWH, blitOffset)
                            .color(r, g, b, a)
                            .tex(uvTexture.getA() + res.getULength(), uvTexture.getB() + res.getVLength())
                            .endVertex();
                    buf.vertex(matr, zoomedWH - pxWH, pxWH, blitOffset)
                            .color(r, g, b, a)
                            .tex(uvTexture.getA() + res.getULength(), uvTexture.getB())
                            .endVertex();
                    buf.vertex(matr, pxWH, pxWH, blitOffset)
                            .color(r, g, b, a)
                            .tex(uvTexture.getA(), uvTexture.getB())
                            .endVertex();
                });

                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();

                renderStack.popPose();
                break;
            default:
                break;
        }
    }

    private void drawConnection(PoseStack renderStack, float originX, float originY, float targetX, float targetY, float blitOffset) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        RenderSystem.lineWidth(4F);

        long clientTicks = ClientScheduler.getClientTick();
        Vector3 origin = new Vector3(originX, originY, 0);
        Vector3 lineState = origin.vectorFromHereTo(targetX, targetY, 0);
        int bodyCubes = (int) Math.ceil(lineState.length() / 1); //1 = max line segment length
        int activeSegment = (int) (clientTicks % bodyCubes);
        Vector3 segmentIter = lineState.divide(bodyCubes);
        RenderingUtils.draw(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (int i = bodyCubes; i >= 0; i--) {
                double lx = origin.getX();
                double ly = origin.getY();
                origin.add(segmentIter);

                float brightness = 0.6F;
                brightness += (0.4F * evaluateBrightness(i, activeSegment));

                drawLinePart(buf, renderStack, lx, ly, origin.getX(), origin.getY(), blitOffset, brightness);
            }
        });

        RenderSystem.lineWidth(2.0F);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private void drawLinePart(VertexConsumer buf, PoseStack renderStack, double lx, double ly, double hx, double hy, float blitOffset, float brightness) {
        Matrix4f offset = renderStack.last().pose();
        buf.vertex(offset, (float) lx, (float) ly, blitOffset)
                .color(brightness * alpha, brightness * alpha, brightness * alpha, 0.4F * alpha)
                .endVertex();
        buf.vertex(offset, (float) hx, (float) hy, blitOffset)
                .color(brightness * alpha, brightness * alpha, brightness * alpha, 0.4F * alpha)
                .endVertex();
    }

    private float evaluateBrightness(int segment, int activeSegment) {
        if (segment == activeSegment) return 1.0F;
        float res = ((float) (10 - Math.abs(activeSegment - segment))) / 10F;
        return Math.max(0, res);
    }

    private void drawResearchItemBackground(double zoomedWH, double xAdd, double yAdd, float blitOffset) {
        RenderSystem.enableBlend();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            buf.vertex(xAdd,            yAdd + zoomedWH, blitOffset).color(alpha, alpha, alpha, alpha).tex(0, 1).endVertex();
            buf.vertex(xAdd + zoomedWH, yAdd + zoomedWH, blitOffset).color(alpha, alpha, alpha, alpha).tex(1, 1).endVertex();
            buf.vertex(xAdd + zoomedWH, yAdd,            blitOffset).color(alpha, alpha, alpha, alpha).tex(1, 0).endVertex();
            buf.vertex(xAdd,            yAdd,            blitOffset).color(alpha, alpha, alpha, alpha).tex(0, 0).endVertex();
        });
        RenderSystem.disableBlend();
    }
}
