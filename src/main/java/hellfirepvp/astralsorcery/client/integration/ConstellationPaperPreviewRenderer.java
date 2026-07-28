/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.integration;

import hellfirepvp.astralsorcery.client.screen.ScreenConstellationPaper;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class ConstellationPaperPreviewRenderer {

    private static final float SCALE = 0.4F;
    private static final int PREVIEW_WIDTH = (int) (275 * SCALE);
    private static final int PREVIEW_HEIGHT = (int) (344 * SCALE);
    private static final int CURSOR_OFFSET = 12;

    private ConstellationPaperPreviewRenderer() {}

    public static void render(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
        if (!(stack.getItem() instanceof ItemConstellationPaper paper)) {
            return;
        }

        IConstellation constellation = paper.getConstellation(stack);
        if (constellation == null || !ResearchHelper.getClientProgress().hasConstellationDiscovered(constellation)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int x = mouseX + CURSOR_OFFSET;
        int y = mouseY + CURSOR_OFFSET;
        if (x + PREVIEW_WIDTH > minecraft.getWindow().getGuiScaledWidth()) {
            x = mouseX - CURSOR_OFFSET - PREVIEW_WIDTH;
        }
        if (y + PREVIEW_HEIGHT > minecraft.getWindow().getGuiScaledHeight()) {
            y = mouseY - CURSOR_OFFSET - PREVIEW_HEIGHT;
        }

        ScreenConstellationPaper.renderPreview(graphics.pose(), constellation,
                Math.max(0, x), Math.max(0, y), 500F, SCALE);
    }
}
