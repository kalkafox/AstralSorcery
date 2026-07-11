/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalPages
 * Created by HellFirePvP
 * Date: 03.08.2019 / 16:54
 */
public class ScreenJournalPages extends ScreenJournal implements NavigationArrowScreen {

    private static ScreenJournalPages openGuiInstance;
    private static boolean saveSite = true;

    @Nullable
    private final ScreenJournalProgression origin;
    @Nullable
    private final Screen cameFrom;
    private final ResearchNode researchNode;
    private final List<RenderablePage> pages;

    private boolean informPreviousClose = true;
    private int currentPageOffset = 0; //* 2 = left page.
    private Rectangle rectBack, rectNext, rectPrev;

    public ScreenJournalPages(@Nullable ScreenJournalProgression origin, ResearchNode node) {
        super(node.getName(), NO_BOOKMARK);
        this.researchNode = node;
        this.origin = origin;
        this.cameFrom = null;
        List<JournalPage> pageList = node.getPages();
        this.pages = new ArrayList<>(pageList.size());
        for (int i = 0; i < pageList.size(); i++) {
            this.pages.add(pageList.get(i).buildRenderPage(node, i));
        }
    }

    //Use this to use this screen independently of the actual journal.
    public ScreenJournalPages(@Nullable Screen cameFrom, ResearchNode detailedInformation, int exactPage) {
        super(detailedInformation.getName(), NO_BOOKMARK);
        this.researchNode = detailedInformation;
        this.origin = null;
        this.cameFrom = cameFrom;
        this.currentPageOffset = exactPage / 2;
        List<JournalPage> pageList = detailedInformation.getPages();
        this.pages = new ArrayList<>(pageList.size());
        for (int i = 0; i < pageList.size(); i++) {
            this.pages.add(pageList.get(i).buildRenderPage(detailedInformation, i));
        }
    }

    public static ScreenJournalPages getClearOpenGuiInstance() {
        ScreenJournalPages gui = openGuiInstance;
        openGuiInstance = null;
        return gui;
    }

    public int getCurrentPageOffset() {
        return currentPageOffset;
    }

    public ResearchNode getResearchNode() {
        return researchNode;
    }

    @Override
    public void init() {
        super.init();

        if (origin != null) {
            origin.preventRefresh();
            origin.width = width;
            origin.height = height;
            origin.init();
        }
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        super.render(renderStack, xpos, ypos, pTicks);

        if (origin != null) {
            drawDefault(renderStack, TexturesAS.TEX_GUI_BOOK_BLANK, xpos, ypos);
        } else {
            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            drawWHRect(renderStack, TexturesAS.TEX_GUI_BOOK_BLANK);
            RenderSystem.disableBlend();
        }

        this.setBlitOffset(100);
        int pageYOffset = 20;

        //Headline
        if (this.currentPageOffset == 0) {
            int width = font.width(this.getTitle());

            renderStack.pushPose();
            renderStack.translate(leftPos + 117, topPos + 22, this.getGuiZLevel());
            renderStack.scale(1.3F, 1.3F, 1F);
            renderStack.translate(-width / 2F, 0, 0);
            RenderingDrawUtils.renderStringAt(font, renderStack, this.getTitle(), 0x00DDDDDD);
            renderStack.popPose();

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            TexturesAS.TEX_GUI_BOOK_UNDERLINE.bindTexture();
            RenderingGuiUtils.drawRect(renderStack, leftPos + 30, topPos + 35, this.getGuiZLevel(), 175, 6);
            RenderSystem.disableBlend();

            pageYOffset += 30;
        }

        int index = currentPageOffset * 2;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            page.render(renderStack, leftPos + 30, topPos + pageYOffset, this.getGuiZLevel(), pTicks, xpos, ypos);
        }
        index = index + 1;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            page.render(renderStack, leftPos + 220, topPos + 20, this.getGuiZLevel(), pTicks, xpos, ypos);
        }

        this.setBlitOffset(120);
        drawNavArrows(renderStack, pTicks, xpos, ypos);
        this.setBlitOffset(100);

        index = currentPageOffset * 2;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            page.postRender(renderStack, leftPos + 30, topPos + pageYOffset, this.getGuiZLevel(), pTicks, xpos, ypos);
        }
        index = index + 1;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            page.postRender(renderStack, leftPos + 220, topPos + 20, this.getGuiZLevel(), pTicks, xpos, ypos);
        }

        this.setBlitOffset(0);
    }

    private void drawNavArrows(PoseStack renderStack, float a, int xpos, int ypos) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        this.rectNext = null;
        this.rectPrev = null;
        this.rectBack = this.drawArrow(renderStack, leftPos + 197, topPos + 230, this.getGuiZLevel(), Type.LEFT, xpos, ypos, a);

        int cIndex = currentPageOffset * 2;
        if (cIndex > 0) {
            this.rectPrev = this.drawArrow(renderStack, leftPos + 25, topPos + 220, this.getGuiZLevel(), Type.LEFT, xpos, ypos, a);
        }
        int nextIndex = cIndex + 2;
        if (pages.size() >= (nextIndex + 1)) {
            this.rectNext = this.drawArrow(renderStack, leftPos + 367, topPos + 220, this.getGuiZLevel(), Type.RIGHT, xpos, ypos, a);
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double xpos, double ypos) {
        if (origin != null) {
            origin.expectReInit();
            saveSite = false;
        } else {
            informPreviousClose = false;
        }
        return true;
    }

    @Override
    public void onClose() {
        if (origin != null) {
            if (saveSite) {
                openGuiInstance = this;
                ScreenJournalProgression.getJournalInstance().preventRefresh();
                Minecraft.getInstance().setScreen(null);
            } else {
                saveSite = true;
                openGuiInstance = null;
                Minecraft.getInstance().setScreen(origin);
            }
        } else {
            if (cameFrom != null && informPreviousClose) {
                cameFrom.onClose();
            }
            Minecraft.getInstance().setScreen(cameFrom);
        }
    }

    @Override
    protected void mouseDragTick(double xpos, double ypos, double mouseDiffX, double mouseDiffY, double mouseOffsetX, double mouseOffsetY) {
        int index = currentPageOffset * 2;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            if (page != null) {
                if (page.propagateMouseDrag(mouseOffsetX, mouseOffsetY)) {
                    return;
                }
            }
        }
        index += 1;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            if (page != null) {
                page.propagateMouseDrag(mouseOffsetX, mouseOffsetY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double xpos, double ypos, int mouseButton) {
        if (super.mouseClicked(xpos, ypos, mouseButton)) {
            return true;
        }

        if (mouseButton == 1) {
            return true;
        }

        if (mouseButton != 0) {
            return false;
        }

        if (origin != null) {
            if (handleBookmarkClick(xpos, ypos)) {
                saveSite = false;
                return true;
            }
        }
        if (rectBack != null && rectBack.contains(xpos, ypos)) {
            if (origin != null) {
                origin.expectReInit();
                saveSite = false;
                this.onClose();
                return true;
            } else {
                informPreviousClose = false;
                this.onClose();
                return true;
            }
        }
        if (rectPrev != null && rectPrev.contains(xpos, ypos)) {
            this.currentPageOffset -= 1;
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (rectNext != null && rectNext.contains(xpos, ypos)) {
            this.currentPageOffset += 1;
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }

        int index = currentPageOffset * 2;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            if (page != null) {
                if (page.propagateMouseClick(xpos, ypos)) {
                    return true;
                }
            }
        }
        index += 1;
        if (pages.size() > index) {
            RenderablePage page = pages.get(index);
            if (page != null) {
                if (page.propagateMouseClick(xpos, ypos)) {
                    return true;
                }
            }
        }
        return false;
    }
}
