/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageAltarRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.neoforged.fml.LogicalSide;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalConstellationDetail
 * Created by HellFirePvP
 * Date: 04.08.2019 / 10:54
 */
public class ScreenJournalConstellationDetail extends ScreenJournal implements NavigationArrowScreen {

    private final ScreenJournal origin;
    private final IConstellation constellation;
    private final boolean detailed;

    private int doublePageID = 0;
    private int doublePages = 0;
    private List<MoonPhase> activePhases = null;

    private RenderablePage lastFramePage = null;
    private Rectangle rectBack, rectNext, rectPrev;

    private final List<FormattedCharSequence> locTextMain = new ArrayList<>();
    private final List<FormattedCharSequence> locTextRitual = new ArrayList<>();
    private final List<FormattedCharSequence> locTextRefraction = new ArrayList<>();
    private final List<FormattedCharSequence> locTextMantle = new ArrayList<>();

    public ScreenJournalConstellationDetail(ScreenJournal origin, IConstellation cst) {
        super(cst.getConstellationName(), NO_BOOKMARK);
        this.origin = origin;
        this.constellation = cst;

        this.font = Minecraft.getInstance().font;

        this.detailed = ResearchHelper.getClientProgress().hasConstellationDiscovered(cst);

        PlayerProgress playerProgress = ResearchHelper.getClientProgress();
        if (this.detailed) {
            if (playerProgress.getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT)) {
                this.doublePages++;
            }
            if (playerProgress.getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                if (!(constellation instanceof IMinorConstellation)) {
                    this.doublePages++; //mantle info pages
                }
                this.doublePages++; //constellation paper page
            }
        }

        testActivePhases();
        buildMainText();
        buildEnchText();
        buildRitualText();
        buildCapeText();
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    private void buildCapeText() {
        if (this.constellation instanceof IWeakConstellation) {
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                Component txtMantle = ((IWeakConstellation) this.constellation).getInfoMantleEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.mantle");
                locTextMantle.add(localize(headTxt));
                locTextMantle.add(FormattedCharSequence.EMPTY);

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtMantle.getString().split("<NL>")) {
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextMantle.addAll(lines);
                locTextMantle.add(FormattedCharSequence.EMPTY);
            }
        }
    }

    private void buildEnchText() {
        if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.CONSTELLATION_CRAFT)) {
            Component txtEnchantments = this.constellation.getConstellationEnchantmentDescription();

            FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.enchantments");
            locTextRefraction.add(localize(headTxt));
            locTextRefraction.add(FormattedCharSequence.EMPTY);

            List<FormattedCharSequence> lines = new LinkedList<>();
            for (String segment : txtEnchantments.getString().split("<NL>")) {
                lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                lines.add(FormattedCharSequence.EMPTY);
            }
            locTextRefraction.addAll(lines);
            locTextRefraction.add(FormattedCharSequence.EMPTY);
        }
    }

    private void buildRitualText() {
        if (this.constellation instanceof IMinorConstellation) {
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                Component txtRitual = ((IMinorConstellation) this.constellation).getInfoTraitEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.ritual.trait");
                locTextRitual.add(localize(headTxt));
                locTextRitual.add(FormattedCharSequence.EMPTY);

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtRitual.getString().split("<NL>")) {
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextRitual.addAll(lines);
            }
        } else if (this.constellation instanceof IWeakConstellation) {
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT)) {
                Component txtRitual = ((IWeakConstellation) this.constellation).getInfoRitualEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.ritual");
                locTextRitual.add(localize(headTxt));
                locTextRitual.add(FormattedCharSequence.EMPTY);

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtRitual.getString().split("<NL>")) {
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextRitual.addAll(lines);
                locTextRitual.add(FormattedCharSequence.EMPTY);
            }
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                Component txtCorruptedRitual = ((IWeakConstellation) this.constellation).getInfoCorruptedRitualEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.corruption");
                locTextRitual.add(localize(headTxt));
                locTextRitual.add(FormattedCharSequence.EMPTY);

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtCorruptedRitual.getString().split("<NL>")) {
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextRitual.addAll(lines);
                locTextRitual.add(FormattedCharSequence.EMPTY);
            }
        }
    }

    private void buildMainText() {
        Component txtDescription = this.constellation.getConstellationDescription();

        List<FormattedCharSequence> lines = new LinkedList<>();
        for (String segment : txtDescription.getString().split("<NL>")) {
            lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
            lines.add(FormattedCharSequence.EMPTY);
        }
        locTextMain.addAll(lines);
    }

    private void testActivePhases() {
        WorldContext ctx = SkyHandler.getContext(Minecraft.getInstance().level, LogicalSide.CLIENT);
        if (ctx == null) {
            return;
        }
        this.activePhases = new LinkedList<>();
        for (MoonPhase currentPhase : MoonPhase.values()) {
            if (ctx.getConstellationHandler().isActiveInPhase(this.constellation, currentPhase)) {
                this.activePhases.add(currentPhase);
            }
        }
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        this.lastFramePage = null;

        if (this.doublePageID == 0) {
            drawCstBackground(renderStack);
            drawDefault(renderStack, TexturesAS.TEX_GUI_BOOK_FRAME_LEFT, xpos, ypos);
        } else {
            drawDefault(renderStack, TexturesAS.TEX_GUI_BOOK_BLANK, xpos, ypos);
        }

        drawNavArrows(renderStack, pTicks, xpos, ypos);

        this.setBlitOffset(120);
        switch (doublePageID) {
            case 0:
                drawPageConstellation(renderStack, pTicks);
                drawPagePhaseInformation(renderStack);
                drawPageExtendedInformation(renderStack);
                break;
            case 1:
                drawRefractionTableInformation(renderStack, xpos, ypos, pTicks);
                break;
            case 2:
                drawCapeInformationPages(renderStack, xpos, ypos, pTicks);
                if (this.constellation instanceof IMinorConstellation) { //Doesn't have a 3rd double page
                    drawConstellationPaperRecipePage(renderStack, xpos, ypos, pTicks);
                }
                break;
            case 3:
                drawConstellationPaperRecipePage(renderStack, xpos, ypos, pTicks);
                break;
            default:
                break;
        }
        this.setBlitOffset(0);
    }

    private void drawRefractionTableInformation(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        for (int i = 0; i < locTextRitual.size(); i++) {
            FormattedCharSequence lineState = locTextRitual.get(i);
            renderStack.pushPose();
            renderStack.translate(leftPos + 30, topPos + 30 + i * 10, this.getGuiZLevel());
            RenderingDrawUtils.renderStringAt(lineState, renderStack, font, 0xFFCCCCCC, true);
            renderStack.popPose();
        }
        for (int i = 0; i < locTextRefraction.size(); i++) {
            FormattedCharSequence lineState = locTextRefraction.get(i);
            renderStack.pushPose();
            renderStack.translate(leftPos + 220, topPos + 30 + i * 10, this.getGuiZLevel());
            RenderingDrawUtils.renderStringAt(lineState, renderStack, font, 0xFFCCCCCC, true);
            renderStack.popPose();
        }
    }

    private void drawCapeInformationPages(PoseStack renderStack, int xpos, int ypos, float a) {
        for (int i = 0; i < locTextMantle.size(); i++) {
            FormattedCharSequence lineState = locTextMantle.get(i);
            renderStack.pushPose();
            renderStack.translate(leftPos + 30, topPos + 30 + i * 10, this.getGuiZLevel());
            RenderingDrawUtils.renderStringAt(lineState, renderStack, font, 0xFFCCCCCC, true);
            renderStack.popPose();
        }

        if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
            SimpleAltarRecipe recipe = RecipeHelper.findAltarRecipeResult(stack ->
                    stack.getItem() instanceof ItemMantle &&
                            this.constellation.equals(ItemsAS.MANTLE.getConstellation(stack)));

            if (recipe != null) {
                lastFramePage = new RenderPageAltarRecipe(null, -1, recipe);
                lastFramePage.render    (renderStack, leftPos + 220, topPos + 20, this.getGuiZLevel(), a, xpos, ypos);
                lastFramePage.postRender(renderStack, leftPos + 220, topPos + 20, this.getGuiZLevel(), a, xpos, ypos);
            }
        }
    }

    private void drawConstellationPaperRecipePage(PoseStack renderStack, int xpos, int ypos, float a) {
        if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
            SimpleAltarRecipe recipe = RecipeHelper.findAltarRecipeResult(stack ->
                    stack.getItem() instanceof ItemConstellationPaper &&
                    this.constellation.equals(ItemsAS.CONSTELLATION_PAPER.getConstellation(stack)));

            if (recipe != null) {
                lastFramePage = new RenderPageAltarRecipe(null, -1, recipe);
                lastFramePage.render    (renderStack, leftPos + 30, topPos + 20, this.getGuiZLevel(), a, xpos, ypos);
                lastFramePage.postRender(renderStack, leftPos + 30, topPos + 20, this.getGuiZLevel(), a, xpos, ypos);
            }
        }
    }

    private void drawPageExtendedInformation(PoseStack renderStack) {
        FormattedText info = this.getConstellation().getConstellationTag();
        if (!detailed) {
            info = Component.translatable("astralsorcery.journal.constellation.unknown");
        }

        int width = font.width(info);
        float chX = 305 - (width / 2F);
        renderStack.pushPose();
        renderStack.translate(leftPos + chX, topPos + 44, this.getGuiZLevel());
        RenderingDrawUtils.renderStringAt(font, renderStack, info, 0xFFCCCCCC);
        renderStack.popPose();

        if (detailed && !locTextMain.isEmpty()) {
            int offsetX = 220, offsetY = 77;
            renderStack.pushPose();
            renderStack.translate(leftPos + offsetX, topPos + offsetY, this.getGuiZLevel());
            for (FormattedCharSequence lineState : locTextMain) {
                RenderingDrawUtils.renderStringAt(font, renderStack, lineState, 0xFFCCCCCC);
                renderStack.translate(0, 13, 0);
            }
            renderStack.popPose();
        }
    }

    private void drawPagePhaseInformation(PoseStack renderStack) {
        if (this.activePhases == null) {
            this.testActivePhases();
            if (this.activePhases == null) {
                return;
            }
        }

        List<MoonPhase> phases = this.activePhases;
        if (phases.isEmpty()) {

            FormattedText none = Component.translatable("astralsorcery.journal.constellation.unknown");
            float scale = 1.8F;
            float length = font.width(none) * scale;
            float offsetLeft = leftPos + 296 - length / 2;
            int offsetTop = topPos + 199;

            renderStack.pushPose();
            renderStack.translate(offsetLeft + 10, offsetTop, getGuiZLevel());
            renderStack.scale(scale, scale, scale);
            RenderingDrawUtils.renderStringAt(none, renderStack, font, 0xCCDDDDDD, true);
            renderStack.popPose();
        } else {
            boolean known = ResearchHelper.getClientProgress().hasConstellationDiscovered(this.constellation);

            int size = 19;
            int offsetX = 95 + (width / 2) - (MoonPhase.values().length * (size + 2)) / 2;
            int offsetY = 199 + topPos;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            MoonPhase[] mPhases = MoonPhase.values();
            for (int i = 0; i < mPhases.length; i++) {
                MoonPhase currentPhase = mPhases[i];
                int index = i;

                float brightness;
                currentPhase.getTexture().bindTexture();
                if (known && this.activePhases.contains(currentPhase)) {
                    Blending.PREALPHA.apply();
                    brightness = 1F;
                } else {
                    RenderSystem.defaultBlendFunc();
                    brightness = 0.7F;
                }
                RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
                    RenderingGuiUtils.rect(buf, renderStack, offsetX + (index * (size + 2)), offsetY, this.getGuiZLevel(), size, size)
                            .color(brightness, brightness, brightness, brightness)
                            .draw();
                });
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void drawPageConstellation(PoseStack renderStack, float partial) {
        FormattedText cstName = this.constellation.getConstellationName();
        int width = font.width(cstName);

        renderStack.pushPose();
        renderStack.translate(leftPos + (305 - (width * 1.8F / 2F)), topPos + 26, this.getGuiZLevel());
        renderStack.scale(1.8F, 1.8F, 1);
        RenderingDrawUtils.renderStringAt(cstName, renderStack, font, 0xFFC3C3C3, true);
        renderStack.popPose();

        FormattedText dstInfo = constellation.getConstellationTypeDescription();
        if (!detailed) {
            dstInfo = Component.translatable("astralsorcery.journal.constellation.unknown");
        }
        width = font.width(dstInfo);

        renderStack.pushPose();
        renderStack.translate(leftPos + (305 - (width / 2F)), topPos + 219, this.getGuiZLevel());
        RenderingDrawUtils.renderStringAt(dstInfo, renderStack, font, 0xFFDDDDDD, true);
        renderStack.popPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Random random = new Random(0x4196A15C91A5E199L);
        boolean known = ResearchHelper.getClientProgress().hasConstellationDiscovered(constellation);
        RenderingConstellationUtils.renderConstellationIntoGUI(
                known ? constellation.getConstellationColor() : constellation.getTierRenderColor(), constellation, renderStack,
                leftPos + 40, topPos + 60, this.getGuiZLevel(),
                150, 150, 2F,
                () -> 0.6F + 0.4F * RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), partial, 12 + random.nextInt(10)),
                true, false);
        RenderSystem.disableBlend();
    }

    private void drawNavArrows(PoseStack renderStack, float a, int xpos, int ypos) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        this.rectNext = null;
        this.rectPrev = null;
        this.rectBack = this.drawArrow(renderStack, leftPos + 197, topPos + 230, this.getGuiZLevel(), Type.LEFT, xpos, ypos, a);

        if (doublePageID - 1 >= 0) {
            this.rectPrev = this.drawArrow(renderStack, leftPos + 25, topPos + 220, this.getGuiZLevel(), Type.LEFT, xpos, ypos, a);
        }

        if (doublePageID + 1 <= doublePages) {
            this.rectNext = this.drawArrow(renderStack, leftPos + 367, topPos + 220, this.getGuiZLevel(), Type.RIGHT, xpos, ypos, a);
        }

        RenderSystem.disableBlend();
    }

    private void drawCstBackground(PoseStack renderStack) {
        TexturesAS.TEX_BLACK.bindTexture();
        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.addVertex(offset, leftPos + 15,  topPos + 240, this.getGuiZLevel()).setColor(1F, 1F, 1F, 1F).setUv(0, 1);
            buf.addVertex(offset, leftPos + 200, topPos + 240, this.getGuiZLevel()).setColor(1F, 1F, 1F, 1F).setUv(1, 1);
            buf.addVertex(offset, leftPos + 200, topPos + 10,  this.getGuiZLevel()).setColor(1F, 1F, 1F, 1F).setUv(1, 0);
            buf.addVertex(offset, leftPos + 15,  topPos + 10,  this.getGuiZLevel()).setColor(1F, 1F, 1F, 1F).setUv(0, 0);
        });

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TexturesAS.TEX_GUI_BACKGROUND_CONSTELLATIONS.bindTexture();
        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.addVertex(offset, leftPos + 15,  topPos + 240, this.getGuiZLevel()).setColor(0.8F, 0.8F, 1F, 0.5F).setUv(0.3F, 0.9F);
            buf.addVertex(offset, leftPos + 200, topPos + 240, this.getGuiZLevel()).setColor(0.8F, 0.8F, 1F, 0.5F).setUv(0.7F, 0.9F);
            buf.addVertex(offset, leftPos + 200, topPos + 10,  this.getGuiZLevel()).setColor(0.8F, 0.8F, 1F, 0.5F).setUv(0.7F, 0.1F);
            buf.addVertex(offset, leftPos + 15,  topPos + 10,  this.getGuiZLevel()).setColor(0.8F, 0.8F, 1F, 0.5F).setUv(0.3F, 0.1F);
        });
        RenderSystem.disableBlend();
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double xpos, double ypos) {
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(origin);
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

        if (rectBack != null && rectBack.contains(xpos, ypos)) {
            Minecraft.getInstance().setScreen(origin);
            return true;
        }
        if (rectPrev != null && rectPrev.contains(xpos, ypos)) {
            if (doublePageID >= 1) {
                this.doublePageID--;
            }
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (rectNext != null && rectNext.contains(xpos, ypos)) {
            if (doublePageID <= doublePages - 1) {
                this.doublePageID++;
            }
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (doublePageID != 0 && lastFramePage != null) {
            if (lastFramePage.propagateMouseClick(xpos, ypos)) {
                return true;
            }
        }
        return false;
    }

}
