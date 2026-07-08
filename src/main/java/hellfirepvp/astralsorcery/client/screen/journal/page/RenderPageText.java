/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.page;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.text.*;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPageText
 * Created by HellFirePvP
 * Date: 10.10.2019 / 17:31
 */
public class RenderPageText extends RenderablePage {

    private final Font fontRenderer;
    private final List<FormattedCharSequence> localizedText;

    public RenderPageText(String unlocalized) {
        this(RenderablePage.getFontRenderer(), unlocalized);
    }

    public RenderPageText(Font fontRenderer, String unlocalized) {
        super(null, -1);
        this.fontRenderer = fontRenderer;
        this.localizedText = buildLines(unlocalized);
    }

    private List<FormattedCharSequence> buildLines(String unlocText) {
        String text = LanguageMap.getInstance().func_230503_a_(unlocText);
        List<FormattedCharSequence> lines = new LinkedList<>();
        for (String segment : text.split("<NL>")) {
            lines.addAll(fontRenderer.trimStringToWidth(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
            lines.add(IReorderingProcessor.field_242232_a);
        }
        return lines;
    }

    @Override
    public void render(PoseStack renderStack, float x, float y, float z, float pTicks, float mouseX, float mouseY) {
        renderStack.push();
        renderStack.translate(x, y, z);
        for (FormattedCharSequence text : this.localizedText) {
            RenderingDrawUtils.renderStringAt(text, renderStack, this.fontRenderer, 0x00CCCCCC, false);
            renderStack.translate(0, 10, 0);
        }
        renderStack.pop();
    }
}
