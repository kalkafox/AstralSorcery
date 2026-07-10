/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.network.chat.FormattedText;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalConstellationOverview
 * Created by HellFirePvP
 * Date: 04.08.2019 / 09:36
 */
public class ScreenJournalConstellationOverview extends ScreenJournal implements NavigationArrowScreen {

    private static final int CONSTELLATIONS_PER_PAGE = 4;

    private static final int width = 80, height = 110;
    private static final Map<Integer, Point> offsetMap = new HashMap<>();

    private final List<IConstellation> constellations;
    private final int pageId;

    private final Map<Rectangle, IConstellation> rectCRenderMap = new HashMap<>();
    private Rectangle rectPrev, rectNext;

    private ScreenJournalConstellationOverview(int pageId, List<IConstellation> constellations) {
        super(Component.translatable("screen.astralsorcery.tome.constellations"), 20);
        this.constellations = constellations;
        this.pageId = pageId;
    }

    private ScreenJournalConstellationOverview(List<IConstellation> constellations) {
        this(0, constellations);
    }

    public static ScreenJournal getConstellationScreen() {
        PlayerProgress minecraft = ResearchHelper.getClientProgress();
        return new ScreenJournalConstellationOverview(minecraft.getSeenConstellations()
                .stream()
                .map(ConstellationRegistry::getConstellation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        drawConstellationBackground(renderStack);
        drawDefault(renderStack, TexturesAS.TEX_GUI_BOOK_FRAME_FULL, xpos, ypos);

        this.setBlitOffset(250);
        drawNavArrows(renderStack, pTicks, xpos, ypos);
        drawConstellations(renderStack, pTicks, xpos, ypos);
        this.setBlitOffset(0);
    }

    private void drawConstellationBackground(PoseStack renderStack) {
        TexturesAS.TEX_BLACK.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.vertex(offset, leftPos + 15,            topPos + guiHeight - 10, this.getGuiZLevel()).color(1F, 1F, 1F, 1F).tex(0, 1).endVertex();
            buf.vertex(offset, leftPos + guiWidth - 15, topPos + guiHeight - 10, this.getGuiZLevel()).color(1F, 1F, 1F, 1F).tex(1, 1).endVertex();
            buf.vertex(offset, leftPos + guiWidth - 15, topPos + 10,             this.getGuiZLevel()).color(1F, 1F, 1F, 1F).tex(1, 0).endVertex();
            buf.vertex(offset, leftPos + 15,            topPos + 10,             this.getGuiZLevel()).color(1F, 1F, 1F, 1F).tex(0, 0).endVertex();
        });

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TexturesAS.TEX_GUI_BACKGROUND_CONSTELLATIONS.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.vertex(offset, leftPos + 15,            topPos + guiHeight - 10, this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.7F).tex(0.1F, 0.9F).endVertex();
            buf.vertex(offset, leftPos + guiWidth - 15, topPos + guiHeight - 10, this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.7F).tex(0.9F, 0.9F).endVertex();
            buf.vertex(offset, leftPos + guiWidth - 15, topPos + 10,             this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.7F).tex(0.9F, 0.1F).endVertex();
            buf.vertex(offset, leftPos + 15,            topPos + 10,             this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.7F).tex(0.1F, 0.1F).endVertex();
        });
        RenderSystem.disableBlend();
    }

    private void drawConstellations(PoseStack renderStack, float partial, int xpos, int ypos) {
        this.rectCRenderMap.clear();
        List<IConstellation> cs = constellations.subList(pageId * CONSTELLATIONS_PER_PAGE, Math.min((pageId + 1) * CONSTELLATIONS_PER_PAGE, constellations.size()));
        for (int i = 0; i < cs.size(); i++) {
            IConstellation c = cs.get(i);
            Point p = offsetMap.get(i);
            Rectangle cstRct = drawConstellation(renderStack, c, leftPos + p.x, topPos + p.y, this.getGuiZLevel(), partial, xpos, ypos);
            rectCRenderMap.put(cstRct, c);
        }
    }

    private Rectangle drawConstellation(PoseStack renderStack, IConstellation display, double offsetX, double offsetY, float blitOffset, float partial, int xpos, int ypos) {
        Rectangle rect = new Rectangle(Mth.floor(offsetX), Mth.floor(offsetY), width, height);

        renderStack.pushPose();
        renderStack.translate(offsetX + (width / 2F), offsetY + (width / 2F), blitOffset);
        if (rect.contains(xpos, ypos)) {
            renderStack.scale(1.1F, 1.1F, 1F);
        }
        renderStack.translate(-(width / 2F), -(width / 2F), blitOffset);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Random random = new Random(0x4196A15C91A5E199L);
        RenderingConstellationUtils.renderConstellationIntoGUI(
                display.getConstellationColor(), display, renderStack,
                0, 0, 0,
                95, 95, 1.6F,
                () -> 0.5F + 0.5F * RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), partial, 12 + random.nextInt(10)),
                true, false);

        RenderSystem.disableBlend();

        FormattedText cstName = display.getConstellationName();
        float fullLength = (width / 2F) - (font.getStringPropertyWidth(cstName) / 2F);

        renderStack.translate(fullLength, 90, 10);
        RenderingDrawUtils.renderStringAt(cstName, renderStack, font, 0xBBDDDDDD, false);

        renderStack.popPose();
        return rect;
    }

    private void drawNavArrows(PoseStack renderStack, float a, int xpos, int ypos) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        this.rectNext = null;
        this.rectPrev = null;

        int cIndex = pageId * CONSTELLATIONS_PER_PAGE;
        if (cIndex > 0) {
            this.rectPrev = this.drawArrow(renderStack, leftPos + 15, topPos + 127, this.getGuiZLevel(), Type.LEFT, xpos, ypos, a);
        }
        int nextIndex = cIndex + CONSTELLATIONS_PER_PAGE;
        if (constellations.size() >= (nextIndex + 1)) {
            this.rectNext = this.drawArrow(renderStack, leftPos + 367, topPos + 127, this.getGuiZLevel(), Type.RIGHT, xpos, ypos, a);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double xpos, double ypos, int mouseButton) {
        if (super.mouseClicked(xpos, ypos, mouseButton)) {
            return true;
        }

        if (mouseButton != 0) {
            return false;
        }
        if (handleBookmarkClick(xpos, ypos)) {
            return true;
        }
        for (Rectangle r : rectCRenderMap.keySet()) {
            if (r.contains(xpos, ypos)) {
                IConstellation c = rectCRenderMap.get(r);
                Minecraft.getInstance().displayGuiScreen(new ScreenJournalConstellationDetail(this, c));
            }
        }
        if (rectPrev != null && rectPrev.contains(xpos, ypos)) {
            Minecraft.getInstance().displayGuiScreen(new ScreenJournalConstellationOverview(pageId - 1, constellations));
            return true;
        }
        if (rectNext != null && rectNext.contains(xpos, ypos)) {
            Minecraft.getInstance().displayGuiScreen(new ScreenJournalConstellationOverview(pageId + 1, constellations));
            return true;
        }
        return false;
    }

    static {
        offsetMap.put(0, new Point(45, 55));
        offsetMap.put(1, new Point(125, 105));
        offsetMap.put(2, new Point(200, 45));
        offsetMap.put(3, new Point(280, 110));
    }
}
