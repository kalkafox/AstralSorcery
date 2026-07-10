/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournal;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalOverlay
 * Created by HellFirePvP
 * Date: 04.08.2019 / 09:11
 */
public abstract class ScreenJournalOverlay extends ScreenJournal {

    private final ScreenJournal origin;

    protected ScreenJournalOverlay(Component titleIn, ScreenJournal origin) {
        super(titleIn, origin.getGuiHeight(), origin.getGuiWidth(), NO_BOOKMARK);
        this.origin = origin;
    }

    @Override
    public boolean isPauseScreen() {
        return origin.isPauseScreen();
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        super.init(mc, width, height);

        origin.init(mc, width, height);
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        super.render(renderStack, xpos, ypos, pTicks);

        origin.render(renderStack, 0, 0, pTicks);
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double xpos, double ypos) {
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.origin);
    }

    @Override
    public void removed() {
        super.removed();

        if (origin instanceof ScreenJournalProgression) {
            ((ScreenJournalProgression) origin).expectReInit();
        }

        if (origin instanceof ScreenJournalPerkTree) {
            ((ScreenJournalPerkTree) origin).expectReinit = true;
        }
    }

    @Override
    public boolean charTyped(char charCode, int keyModifiers) {
        if (super.charTyped(charCode, keyModifiers)) {
            return true;
        }

        if (Minecraft.getInstance().screen != this && Minecraft.getInstance().screen != origin) {
            Minecraft.getInstance().setScreen(origin);
            return true;
        }
        return false;
    }
}
