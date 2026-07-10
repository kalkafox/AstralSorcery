/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.screen.journal.bookmark.BookmarkProvider;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournal
 * Created by HellFirePvP
 * Date: 03.08.2019 / 16:27
 */
public class ScreenJournal extends WidthHeightScreen {

    public static final int NO_BOOKMARK = -1;
    protected static List<BookmarkProvider> bookmarks = Lists.newArrayList();

    protected final int bookmarkIndex;

    protected Map<Rectangle, BookmarkProvider> drawnBookmarks = Maps.newHashMap();

    protected ScreenJournal(Component titleIn, int bookmarkIndex) {
        this(titleIn, 270, 420, bookmarkIndex);
    }

    public ScreenJournal(Component titleIn, int guiHeight, int guiWidth, int bookmarkIndex) {
        super(titleIn, guiHeight, guiWidth);
        this.bookmarkIndex = bookmarkIndex;
    }

    public static boolean addBookmark(BookmarkProvider bookmarkProvider) {
        int index = bookmarkProvider.getIndex();
        if (MiscUtils.contains(bookmarks, bm -> bm.getIndex() == index)) {
            return false;
        }
        bookmarks.add(bookmarkProvider);
        return true;
    }

    protected FormattedCharSequence localize(FormattedText txt) {
        return Language.getInstance().getVisualOrder(txt);
    }

    protected void drawDefault(PoseStack renderStack, AbstractRenderableTexture texture, int xpos, int ypos) {
        this.setBlitOffset(100);
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        drawWHRect(renderStack, texture);
        RenderSystem.disableBlend();

        drawBookmarks(renderStack, xpos, ypos);
        this.setBlitOffset(0);
    }

    private void drawBookmarks(PoseStack renderStack, int xpos, int ypos) {
        drawnBookmarks.clear();

        int bookmarkWidth  = 67;
        int bookmarkHeight = 15;
        float bookmarkGap    = 18;

        float offsetX = leftPos + guiWidth - 17.25F;
        float offsetY = topPos  + 20;

        bookmarks.sort(Comparator.comparing(BookmarkProvider::getIndex));

        for (BookmarkProvider bookmarkProvider : bookmarks) {
            if (bookmarkProvider.canSee()) {
                Rectangle r = drawBookmark(
                        renderStack, offsetX, offsetY,
                        bookmarkWidth, bookmarkHeight,
                        bookmarkWidth + (bookmarkIndex == bookmarkProvider.getIndex() ? 0 : 5),
                        this.getGuiZLevel(),
                        bookmarkProvider.getUnlocalizedName(), 0xDDDDDDDD, xpos, ypos,
                        bookmarkProvider.getTextureBookmark(), bookmarkProvider.getTextureBookmarkStretched());
                drawnBookmarks.put(r, bookmarkProvider);
                offsetY += bookmarkGap;
            }
        }
    }

    private Rectangle drawBookmark(PoseStack renderStack,
                                   float offsetX, float offsetY, int width, int height, int mouseOverWidth,
                                   float blitOffset, MutableComponent title, int titleRGBColor, int xpos, int ypos,
                                   AbstractRenderableTexture texture, AbstractRenderableTexture textureStretched) {
        texture.bindTexture();

        Rectangle r = new Rectangle(Mth.floor(offsetX), Mth.floor(offsetY), Mth.floor(width), Mth.floor(height));
        if (r.contains(xpos, ypos)) {
            if (mouseOverWidth > width) {
                textureStretched.bindTexture();
            }
            width = mouseOverWidth;
            r = new Rectangle(Mth.floor(offsetX), Mth.floor(offsetY), Mth.floor(width), Mth.floor(height));
        }

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        int actualWidth = width;
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, offsetX, offsetY, blitOffset, actualWidth, height).draw();
        });
        RenderSystem.disableBlend();

        renderStack.pushPose();
        renderStack.translate(offsetX + 2, offsetY + 4, blitOffset + 50);
        renderStack.scale(0.7F, 0.7F, 0.7F);
        RenderingDrawUtils.renderStringAt(null, renderStack, title, titleRGBColor);
        renderStack.popPose();
        return r;
    }

    protected boolean handleBookmarkClick(double xpos, double ypos) {
        return handleJournalNavigationBookmarkClick(xpos, ypos);
    }

    private boolean handleJournalNavigationBookmarkClick(double xpos, double ypos) {
        for (Rectangle bookmarkRectangle : drawnBookmarks.keySet()) {
            BookmarkProvider provider = drawnBookmarks.get(bookmarkRectangle);
            if (bookmarkIndex != provider.getIndex() && bookmarkRectangle.contains(xpos, ypos)) {
                ScreenJournalProgression.resetJournal();
                Minecraft.getInstance().setScreen(provider.getGuiScreen());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
