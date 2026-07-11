/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FormattedText;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenConstellationPaper
 * Created by HellFirePvP
 * Date: 02.08.2019 / 20:32
 */
public class ScreenConstellationPaper extends WidthHeightScreen {

    private final IConstellation constellation;
    private List<MoonPhase> phases = null;

    public ScreenConstellationPaper(IConstellation cst) {
        super(cst.getConstellationName(), 344, 275);
        this.constellation = cst;
        resolvePhases();
    }

    private void resolvePhases() {
        WorldContext ctx = SkyHandler.getContext(Minecraft.getInstance().level, LogicalSide.CLIENT);
        if (ctx != null) {
            phases = new ArrayList<>();
            for (MoonPhase currentPhase : MoonPhase.values()) {
                if (ctx.getConstellationHandler().isActiveInPhase(this.constellation, currentPhase)) {
                    phases.add(currentPhase);
                }
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_CLOSE, 1F, 1F);
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double xpos, double ypos) {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        RenderSystem.enableDepthTest();
        drawWHRect(renderStack, TexturesAS.TEX_GUI_CONSTELLATION_PAPER);
        drawHeader(renderStack);
        drawConstellation(renderStack);
        drawPhaseInformation(renderStack);
    }

    private void drawHeader(PoseStack renderStack) {
        MutableComponent name = this.constellation.getConstellationName();
        float length = font.width(name) * 1.8F;
        double offsetLeft = (width >> 1) - (length / 2);
        int offsetTop = topPos + 45;

        renderStack.pushPose();
        renderStack.translate(offsetLeft + 2, offsetTop, this.getGuiZLevel());
        renderStack.scale(1.8F, 1.8F, 1F);
        RenderingDrawUtils.renderStringAt(name, renderStack, font, 0xAA4D4D4D, false);
        renderStack.popPose();
    }

    private void drawConstellation(PoseStack renderStack) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        RenderingConstellationUtils.renderConstellationIntoGUI(ColorsAS.CONSTELLATION_TYPE_BLANK,
                constellation, renderStack,
                width / 2F - 145 / 2F, topPos + 84,
                this.getGuiZLevel(),
                145, 145, 2F, () -> 0.5F,
                true, false);

        RenderSystem.disableBlend();
    }

    private void drawPhaseInformation(PoseStack renderStack) {
        if (this.phases == null) {
            this.resolvePhases();
        }

        List<MoonPhase> phases = this.phases == null ? Collections.emptyList() : this.phases;
        if (phases.isEmpty()) {
            FormattedText text = Component.translatable("astralsorcery.journal.constellation.unknown");
            RenderingDrawUtils.renderStringCentered(Minecraft.getInstance().font, renderStack,
                    text, leftPos + guiWidth / 2 + 25, topPos + 239,
                    1.8F, 0xAA4D4D4D);
        } else {
            int size = 16;
            int offsetX = (width / 2) - (phases.size() * (size + 2)) / 2;
            int offsetY = topPos + 237;
            for (int i = 0; i < phases.size(); i++) {
                phases.get(i).getTexture().bindTexture();
                RenderSystem.enableBlend();
                Blending.DEFAULT.apply();
                RenderingGuiUtils.drawRect(renderStack, offsetX + (i * (size + 2)), offsetY, this.getGuiZLevel(), size, size);
                RenderSystem.disableBlend();
            }
        }
    }
}
