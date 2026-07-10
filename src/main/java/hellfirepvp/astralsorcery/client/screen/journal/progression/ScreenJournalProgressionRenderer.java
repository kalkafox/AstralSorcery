/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.progression;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.helper.ScalingPoint;
import hellfirepvp.astralsorcery.client.screen.helper.ScreenRenderBoundingBox;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.network.chat.FormattedText;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalProgressionRenderer
 * Created by HellFirePvP
 * Date: 03.08.2019 / 17:02
 */
public class ScreenJournalProgressionRenderer {

    private final GalaxySizeHandler sizeHandler;
    private final ScreenJournalProgression parentGui;

    public ScreenRenderBoundingBox realRenderBox;
    private int realCoordLowerX, realCoordLowerY;
    private int realRenderWidth, realRenderHeight;

    private final ScalingPoint mousePointScaled;
    private ScalingPoint previousMousePointScaled;

    private ResearchProgression focusedClusterZoom = null, focusedClusterMouse = null;
    private ScreenJournalClusterRenderer clusterRenderer = null;

    private long doubleClickLast = 0L;

    private boolean hasPrevOffset = false;
    private final Map<Rectangle, ResearchProgression> clusterRectMap = new HashMap<>();

    public ScreenJournalProgressionRenderer(ScreenJournalProgression gui) {
        this.parentGui = gui;
        this.sizeHandler = new GalaxySizeHandler();
        refreshSize();
        this.mousePointScaled = ScalingPoint.createPoint(
                this.sizeHandler.clampX(this.sizeHandler.getTotalWidth() / 2F),
                this.sizeHandler.clampY(this.sizeHandler.getTotalHeight() / 2F),
                this.sizeHandler.getScalingFactor(),
                false);
        this.moveMouse(this.sizeHandler.getTotalWidth() / 2, this.sizeHandler.getTotalHeight() / 2);
        applyMovedMouseOffset();
    }

    public void refreshSize() {
        this.sizeHandler.updateSize();
    }

    public void setBox(int left, int top, int right, int bottom) {
        this.realRenderBox = new ScreenRenderBoundingBox(left, top, right, bottom);
        this.realRenderWidth = (int) this.realRenderBox.getWidth();
        this.realRenderHeight = (int) this.realRenderBox.getHeight();
    }

    public void moveMouse(float changedX, float changedY) {
        if (sizeHandler.getScalingFactor() >= 6.1D && clusterRenderer != null) {
            clusterRenderer.moveMouse(changedX, changedY);
        } else {
            if (hasPrevOffset) {
                mousePointScaled.updateScaledPos(
                        sizeHandler.clampX(previousMousePointScaled.getScaledPosX() + changedX),
                        sizeHandler.clampY(previousMousePointScaled.getScaledPosY() + changedY),
                        sizeHandler.getScalingFactor());
            } else {
                mousePointScaled.updateScaledPos(
                        sizeHandler.clampX(mousePointScaled.getScaledPosX()),
                        sizeHandler.clampY(mousePointScaled.getScaledPosY()),
                        sizeHandler.getScalingFactor());
            }
        }
    }

    public void applyMovedMouseOffset() {
        if (sizeHandler.getScalingFactor() >= 6.1D && clusterRenderer != null) {
            clusterRenderer.applyMovedMouseOffset();
        } else {
            this.previousMousePointScaled = ScalingPoint.createPoint(
                    mousePointScaled.getScaledPosX(),
                    mousePointScaled.getScaledPosY(),
                    sizeHandler.getScalingFactor(),
                    true);
            this.hasPrevOffset = true;
        }
    }

    public void updateOffset(int leftPos, int topPos) {
        this.realCoordLowerX = leftPos;
        this.realCoordLowerY = topPos;
    }

    public void centerMouse() {
        this.moveMouse(parentGui.getGuiLeft() + this.sizeHandler.getTotalWidth() / 2F, parentGui.getGuiTop() + this.sizeHandler.getTotalHeight() / 2F);
    }

    public void updateMouseState() {
        moveMouse(0, 0);
    }

    public void unfocus() {
        focusedClusterZoom = null;
    }

    public void focus(@Nonnull ResearchProgression researchCluster) {
        this.focusedClusterZoom = researchCluster;
        this.clusterRenderer = new ScreenJournalClusterRenderer(researchCluster, realRenderHeight, realRenderWidth, realCoordLowerX, realCoordLowerY);
    }

    //Nothing to actually click here, we redirect if we can.
    public boolean propagateClick(float xpos, float ypos) {
        if (clusterRenderer != null) {
            if (sizeHandler.getScalingFactor() > 6) {
                if (clusterRenderer.propagateClick(parentGui, xpos, ypos)) {
                    return true;
                }
            }
        }
        if (focusedClusterMouse != null) {
            if (sizeHandler.getScalingFactor() <= 6) {
                long current = System.currentTimeMillis();
                if (current - this.doubleClickLast < 400L) {
                    int timeout = 500; //Handles irregular clicks on the GUI so it doesn't loop trying to find a focus cluster
                    while (focusedClusterMouse != null && sizeHandler.getScalingFactor() < 9.9 && timeout > 0) {
                        handleZoomIn(xpos, ypos);
                        timeout--;
                    }
                    this.doubleClickLast = 0L;
                    return true;
                }
                this.doubleClickLast = current;
            }
        }
        return false;
    }

    public void drawMouseHighlight(PoseStack renderStack, float blitOffset, int xpos, int ypos) {
        if (clusterRenderer != null && sizeHandler.getScalingFactor() > 6) {
            clusterRenderer.drawMouseHighlight(renderStack, blitOffset, xpos, ypos);
        }
    }

    public void resetZoom() {
        sizeHandler.resetZoom();
        rescale(sizeHandler.getScalingFactor());
    }

    public void handleZoomOut() {
        this.sizeHandler.handleZoomOut();
        rescale(sizeHandler.getScalingFactor());

        if (this.sizeHandler.getScalingFactor() <= 4.0) {
            unfocus();
        } else if (this.sizeHandler.getScalingFactor() >= 6.0 && this.clusterRenderer != null) {
            clusterRenderer.handleZoomOut();
        }
    }

    /**
     * Thresholds for zooming in:
     * 1.0 - 4.0 don't care.
     * 4.0 - 6.0 has to have focus + centering to center of cluster
     * 6.0 - 10.0 transition (6.0 - 8.0) + cluster rendering + handling (cursor movement)
     */
    public void handleZoomIn(float xpos, float ypos) {
        float scale = sizeHandler.getScalingFactor();
        //double nextScale = Math.min(10.0D, scale + 0.2D);
        if (scale >= 4.0F) {
            if (focusedClusterZoom == null) {
                ResearchProgression prog = tryFocusCluster(xpos, ypos);
                if (prog != null) {
                    focus(prog);
                }
            }
            if (focusedClusterZoom == null) {
                return;
            }
            if (scale < 6.1F) { //Floating point shenanigans
                float vDiv = (2F - (scale - 4F)) * 10F;
                JournalCluster cluster = JournalProgressionClusterMapping.getClusterMapping(focusedClusterZoom);
                float x = this.sizeHandler.evRelativePosX(cluster.x);
                float y = this.sizeHandler.evRelativePosY(cluster.y);
                float width  = this.sizeHandler.scaledDistanceX(cluster.x, cluster.maxX);
                float height = this.sizeHandler.scaledDistanceY(cluster.y, cluster.maxY);
                Vector3 center = new Vector3(x + width / 2, y + height / 2, 0);
                Vector3 mousePos = new Vector3(mousePointScaled.getScaledPosX(), mousePointScaled.getScaledPosY(), 0);
                Vector3 dir = center.subtract(mousePos);
                if (vDiv > 0.05) {
                    dir.divide(vDiv);
                }
                if (!hasPrevOffset) {
                    mousePointScaled.updateScaledPos(
                            sizeHandler.clampX((float) (mousePos.getX() + dir.getX())),
                            sizeHandler.clampY((float) (mousePos.getY() + dir.getY())),
                            sizeHandler.getScalingFactor());
                } else {
                    previousMousePointScaled.updateScaledPos(
                            sizeHandler.clampX((float) (mousePos.getX() + dir.getX())),
                            sizeHandler.clampY((float) (mousePos.getY() + dir.getY())),
                            sizeHandler.getScalingFactor());
                }

                updateMouseState();
            } else if (clusterRenderer != null) {
                clusterRenderer.handleZoomIn();
            }
        }
        this.sizeHandler.handleZoomIn();
        this.mousePointScaled.rescale(sizeHandler.getScalingFactor());
        if (this.previousMousePointScaled != null) {
            this.previousMousePointScaled.rescale(sizeHandler.getScalingFactor());
        }
    }

    private void rescale(float newScale) {
        this.mousePointScaled.rescale(newScale);
        if (this.previousMousePointScaled != null) {
            this.previousMousePointScaled.rescale(newScale);
        }
        updateMouseState();
    }

    public void drawProgressionPart(PoseStack renderStack, float blitOffset, int xpos, int ypos) {
        drawBackground(renderStack, blitOffset);

        drawClusters(renderStack, blitOffset);

        focusedClusterMouse = tryFocusCluster(xpos, ypos);

        float scaleX = this.mousePointScaled.getX();
        float scaleY = this.mousePointScaled.getY();

        if (sizeHandler.getScalingFactor() >= 6.1D && focusedClusterZoom != null && clusterRenderer != null) {
            JournalCluster cluster = JournalProgressionClusterMapping.getClusterMapping(focusedClusterZoom);
            drawClusterBackground(renderStack, cluster.clusterBackgroundTexture, blitOffset);

            clusterRenderer.drawClusterScreen(renderStack, this.parentGui, blitOffset);
            scaleX = clusterRenderer.xpos();
            scaleY = clusterRenderer.ypos();
        }

        if (focusedClusterMouse != null) {
            JournalCluster cluster = JournalProgressionClusterMapping.getClusterMapping(focusedClusterMouse);
            float width  = this.sizeHandler.scaledDistanceX(cluster.x, cluster.maxX);
            float height = this.sizeHandler.scaledDistanceY(cluster.y, cluster.maxY);
            Point.Float offset = this.sizeHandler.scalePointToGui(this.parentGui, this.mousePointScaled, new Point.Float(cluster.x, cluster.y));

            float scale = sizeHandler.getScalingFactor();
            float br = 1F;
            if (scale > 8.01F) {
                br = 0F;
            } else if (scale >= 6F) {
                br = 1F - ((scale - 6F) / 2F);
            }

            FormattedText name = focusedClusterMouse.getName();
            float length = Minecraft.getInstance().font.getStringPropertyWidth(name) * 1.4F;
            int alpha = 0xCC;
            alpha *= br;
            alpha = Math.max(alpha, 5);
            int color = 0x5A28FF | (alpha << 24);

            renderStack.pushPose();
            renderStack.translate(offset.x + (width / 2F) - length / 2D, offset.y + (height / 3F), 0);
            renderStack.scale(1.4F, 1.4F, 1F);
            RenderingDrawUtils.renderStringAt(name, renderStack, null, color, true);
            renderStack.popPose();
        }

        drawStarParallaxLayers(renderStack, scaleX, scaleY, blitOffset);
    }

    @Nullable
    private ResearchProgression tryFocusCluster(double xpos, double ypos) {
        for (Rectangle r : this.clusterRectMap.keySet()) {
            if (r.contains(xpos, ypos)) {
                return this.clusterRectMap.get(r);
            }
        }
        return null;
    }

    private void drawClusters(PoseStack renderStack, float blitOffset) {
        clusterRectMap.clear();
        if (sizeHandler.getScalingFactor() >= 8.01) return;

        PlayerProgress thisProgress = ResearchHelper.getClientProgress();
        for (ResearchProgression progress : thisProgress.getResearchProgression()) {
            renderCluster(renderStack, progress, JournalProgressionClusterMapping.getClusterMapping(progress), blitOffset);
        }
    }

    private void renderCluster(PoseStack renderStack, ResearchProgression p, JournalCluster cluster, float blitOffset) {
        Point.Float pCluster = this.sizeHandler.scalePointToGui(this.parentGui, this.mousePointScaled, new Point.Float(cluster.x, cluster.y));
        float width  = this.sizeHandler.scaledDistanceX(cluster.x, cluster.maxX);
        float height = this.sizeHandler.scaledDistanceY(cluster.y, cluster.maxY);

        Rectangle r = new Rectangle(Mth.floor(pCluster.x), Mth.floor(pCluster.y), Mth.floor(width), Mth.floor(height));
        clusterRectMap.put(r, p);

        cluster.cloudTexture.bindTexture();

        float scale = sizeHandler.getScalingFactor();
        float br;
        if (scale > 8.01F) {
            br = 0F;
        } else if (scale >= 6F) {
            br = 1F - ((scale - 6F) / 2F);
        } else {
            br = 1F;
        }

        RenderSystem.enableBlend();
        Blending.ADDITIVEDARK.apply();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.vertex(offset, pCluster.x + 0,     pCluster.y + height, blitOffset).color(br, br, br, br).tex(0, 1).endVertex();
            buf.vertex(offset, pCluster.x + width, pCluster.y + height, blitOffset).color(br, br, br, br).tex(1, 1).endVertex();
            buf.vertex(offset, pCluster.x + width, pCluster.y + 0,      blitOffset).color(br, br, br, br).tex(1, 0).endVertex();
            buf.vertex(offset, pCluster.x + 0,     pCluster.y + 0,      blitOffset).color(br, br, br, br).tex(0, 0).endVertex();
        });

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    private void drawClusterBackground(PoseStack renderStack, AbstractRenderableTexture tex, float blitOffset) {
        float scale = sizeHandler.getScalingFactor();
        float br;
        if (scale > 8.01F) {
            br = 0.75F;
        } else if (scale >= 6F) {
            br = ((scale - 6F) / 2F) * 0.75F;
        } else {
            br = 0F;
        }

        tex.bindTexture();
        RenderSystem.enableBlend();
        Blending.ADDITIVEDARK.apply();

        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.vertex(offset, realCoordLowerX,                   realCoordLowerY + realRenderHeight, blitOffset).color(br, br, br, br).tex(0, 1).endVertex();
            buf.vertex(offset, realCoordLowerX + realRenderWidth, realCoordLowerY + realRenderHeight, blitOffset).color(br, br, br, br).tex(1, 1).endVertex();
            buf.vertex(offset, realCoordLowerX + realRenderWidth, realCoordLowerY,                    blitOffset).color(br, br, br, br).tex(1, 0).endVertex();
            buf.vertex(offset, realCoordLowerX,                   realCoordLowerY,                    blitOffset).color(br, br, br, br).tex(0, 0).endVertex();
        });

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void drawBackground(PoseStack renderStack, float blitOffset) {
        float br = 0.35F;
        TexturesAS.TEX_GUI_BACKGROUND_DEFAULT.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.vertex(offset, realCoordLowerX,                   realCoordLowerY + realRenderHeight, blitOffset).color(br, br, br, 1.0F).tex(0, 1).endVertex();
            buf.vertex(offset, realCoordLowerX + realRenderWidth, realCoordLowerY + realRenderHeight, blitOffset).color(br, br, br, 1.0F).tex(1, 1).endVertex();
            buf.vertex(offset, realCoordLowerX + realRenderWidth, realCoordLowerY,                    blitOffset).color(br, br, br, 1.0F).tex(1, 0).endVertex();
            buf.vertex(offset, realCoordLowerX,                   realCoordLowerY,                    blitOffset).color(br, br, br, 1.0F).tex(0, 0).endVertex();
        });
    }

    private void drawStarParallaxLayers(PoseStack renderStack, float scalePosX, float scalePosY, float blitOffset) {
        TexturesAS.TEX_GUI_STARFIELD_OVERLAY.bindTexture();
        RenderSystem.enableBlend();
        Blending.OVERLAYDARK.apply();

        float offsetX = scalePosX / 2000F;
        float offsetY = scalePosY / 1000F;

        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            drawStarOverlay(buf, renderStack, blitOffset, offsetX, offsetY, 2F);
            drawStarOverlay(buf, renderStack, blitOffset, offsetX, offsetY, 1.5F);
            drawStarOverlay(buf, renderStack, blitOffset, offsetX, offsetY, 1F);
            drawStarOverlay(buf, renderStack, blitOffset, offsetX, offsetY, 0.75F);
            drawStarOverlay(buf, renderStack, blitOffset, offsetX, offsetY, 0.5F);
            drawStarOverlay(buf, renderStack, blitOffset, offsetX, offsetY, 0.3F);
        });

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void drawStarOverlay(VertexConsumer buf, PoseStack renderStack, float blitOffset, float scalePosX, float scalePosY, float scaleFactor) {
        float scale = this.sizeHandler.getScalingFactor() / 40F;

        float x      = this.parentGui.getGuiLeft();
        float y      = this.parentGui.getGuiTop();
        float width  = this.parentGui.getGuiWidth();
        float height = this.parentGui.getGuiHeight();

        float u  = 0.2F + scalePosX + scaleFactor + scale;
        float v  = 0.2F + scalePosY + scaleFactor + scale;
        float uL = 0.6F * scaleFactor - (scale * 2);
        float vL = 0.6F * scaleFactor - (scale * 2);

        if (vL <= 0 || uL <= 0) {
            return;
        }

        Matrix4f offset = renderStack.last().pose();
        buf.vertex(offset, x, y + height, blitOffset)
                .color(0.75F, 0.75F, 0.75F, 0.7F).tex(u,  v + vL).endVertex();
        buf.vertex(offset, x + width, y + height, blitOffset)
                .color(0.75F, 0.75F, 0.75F, 0.7F).tex(u + uL, v + vL).endVertex();
        buf.vertex(offset, x + width, y, blitOffset)
                .color(0.75F, 0.75F, 0.75F, 0.7F).tex(u + uL, v).endVertex();
        buf.vertex(offset, x, y, blitOffset)
                .color(0.75F, 0.75F, 0.75F, 0.7F).tex(u, v).endVertex();
    }
}
