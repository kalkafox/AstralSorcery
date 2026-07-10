/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.page;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.render.IDrawRenderTypeBuffer;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.client.StructureRenderer;
import hellfirepvp.observerlib.api.structure.Structure;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.network.chat.FormattedText;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPageStructure
 * Created by HellFirePvP
 * Date: 22.08.2019 / 21:18
 */
public class RenderPageStructure extends RenderablePage {

    private final StructureRenderer structureRenderer;
    private final Structure structure;
    private final Vector3 shift;
    private final List<Tuple<ItemStack, FormattedText>> contentStacks;
    private final FormattedText name;

    private Optional<Integer> drawSlice = Optional.empty();
    private Rectangle.Float switchView = null, sliceUp = null, sliceDown = null, switchRequiredAir = null;
    private long totalRenderFrame = 0;
    private boolean showAirBlocks = false;

    public RenderPageStructure(@Nullable ResearchNode node, int nodePage, Structure structure, @Nullable FormattedText name, @Nonnull Vector3 shift) {
        super(node, nodePage);
        this.structure = structure;
        this.structureRenderer = new StructureRenderer(this.structure).setIsolateIndividualBlock(true);
        this.name = name;
        this.shift = shift;
        this.contentStacks = new ArrayList<>();
        structure.getAsStacks(this.structureRenderer.getRenderWorld(), Minecraft.getInstance().player).forEach(stack -> {
            ItemStack display = ItemUtils.copyStackWithSize(stack, 1);
            FormattedText description = Component.literal(stack.getCount() + "x ").append(stack.getDisplayName());
            this.contentStacks.add(new Tuple<>(display, description));
        });
    }

    @Override
    public void render(PoseStack renderStack, float x, float y, float z, float pTicks, float xpos, float ypos) {
        this.totalRenderFrame++;

        this.renderStructure(renderStack, x, y, pTicks);
        float shift = this.renderSizeDescription(renderStack, x, y + 5, z);

        if (this.name != null) {
            renderHeadline(renderStack, x + shift, y + 5, z, this.name);
        }

        this.renderSliceButtons(renderStack, x, y + 10, z, xpos, ypos);
    }

    private void renderSliceButtons(PoseStack renderStack, float offsetX, float offsetY, float blitOffset, float xpos, float ypos) {
        TexturesAS.TEX_GUI_BOOK_STRUCTURE_ICONS.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        this.switchView = null;
        this.sliceDown = null;
        this.sliceUp = null;
        this.switchRequiredAir = null;

        this.switchView = new Rectangle.Float(offsetX + 152, offsetY + 10, 16, 16);
        float u = this.drawSlice.isPresent() ? 0.5F : 0;

        RenderingGuiUtils.drawTexturedRect(renderStack, switchView.x, switchView.y, blitOffset,
                switchView.width, switchView.height,
                u, 0, 0.5F, 0.25F);

        if (this.drawSlice.isPresent()) {
            int yLevel = this.drawSlice.get();

            int minSlice = this.getCurrentMinSlice();
            int maxSlice = this.getCurrentMaxSlice();

            if (yLevel < minSlice) {
                yLevel = maxSlice;
            }
            if (yLevel > maxSlice) {
                yLevel = maxSlice;
            }

            if (minSlice <= yLevel - 1) {
                sliceDown = new Rectangle.Float(offsetX + 160, offsetY + 28, 11, 16);
                renderStack.pushPose();
                renderStack.translate(sliceDown.x + (sliceDown.width / 2), sliceDown.y + (sliceDown.height / 2), blitOffset);
                float v = 2F / 4F;
                if (sliceDown.contains(xpos, ypos)) {
                    v = 1F / 4F;
                    renderStack.scale(1.1F, 1.1F, 1F);
                }
                renderStack.translate(-sliceDown.width / 2, -sliceDown.height / 2, 0);
                RenderingGuiUtils.drawTexturedRect(renderStack, sliceDown.width, sliceDown.height,
                        12F / 32F, v, 11F / 32F, 1F / 4F);
                renderStack.popPose();
            }

            if (maxSlice >= yLevel + 1) {
                sliceUp = new Rectangle.Float(offsetX + 148, offsetY + 28, 11, 16);
                renderStack.pushPose();
                renderStack.translate(sliceUp.x + (sliceUp.width / 2), sliceUp.y + (sliceUp.height / 2), blitOffset);
                float v = 2F / 4F;
                if (sliceUp.contains(xpos, ypos)) {
                    v = 1F / 4F;
                    renderStack.scale(1.1F, 1.1F, 1F);
                }
                renderStack.translate(-sliceUp.width / 2, -sliceUp.height / 2, 0);
                RenderingGuiUtils.drawTexturedRect(renderStack, sliceUp.width, sliceUp.height,
                        0F / 32F, v, 11F / 32F, 1F / 4F);
                renderStack.popPose();
            }
        }

        this.switchRequiredAir = new Rectangle.Float(offsetX + 134, offsetY + 10, 16, 16);
        RenderingGuiUtils.drawTexturedRect(renderStack, switchRequiredAir.x, switchRequiredAir.y, blitOffset, switchRequiredAir.width, switchRequiredAir.height,
                0, 0.75F, 0.5F, 0.25F);
        if (this.showAirBlocks) {
            BlockAtlasTexture.getInstance().bindTexture();
            RenderSystem.depthMask(false);

            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.BLOCK, buf -> {
                renderStack.pushPose();
                renderStack.translate(switchRequiredAir.x + 13, switchRequiredAir.y + 11, blitOffset + 60);
                renderStack.scale(7, -7, 7);
                renderStack.mirror(Axis.XP.rotationDegrees(30));
                renderStack.mirror(Axis.YP.rotationDegrees(225));

                RenderingUtils.renderSimpleBlockModel(Blocks.BLACK_STAINED_GLASS.defaultBlockState(), renderStack, buf);
                renderStack.popPose();
            });

            RenderSystem.depthMask(true);
        }

        RenderSystem.disableBlend();
    }

    private int getCurrentMinSlice() {
        int minSlice = this.structure.getMinimumOffset().getY();
        if (!this.showAirBlocks) {
            for (int yy = minSlice; yy <= this.structure.getMaximumOffset().getY(); yy++) {
                boolean onlyAir = this.structure.getStructureSlice(yy).stream()
                        .allMatch(tpl -> tpl.getB().equals(MatchableState.REQUIRES_AIR));
                if (!onlyAir) {
                    return yy;
                }
            }
        }
        return minSlice;
    }

    private int getCurrentMaxSlice() {
        int maxSlice = this.structure.getMaximumOffset().getY();
        if (!this.showAirBlocks) {
            for (int yy = maxSlice; yy >= this.structure.getMinimumOffset().getY(); yy--) {
                boolean onlyAir = this.structure.getStructureSlice(yy).stream()
                        .allMatch(tpl -> tpl.getB().equals(MatchableState.REQUIRES_AIR));
                if (!onlyAir) {
                    return yy;
                }
            }
        }
        return maxSlice;
    }

    private void renderHeadline(PoseStack renderStack, float offsetX, float offsetY, float blitOffset, FormattedText title) {
        float scale = 1.3F;
        RenderSystem.disableDepthTest();

        renderStack.pushPose();
        renderStack.translate(offsetX, offsetY, blitOffset);
        renderStack.scale(scale, scale, scale);
        RenderingDrawUtils.renderStringAt(title, renderStack, null, 0x00DDDDDD, true);
        renderStack.popPose();

        RenderSystem.enableDepthTest();
    }

    private float renderSizeDescription(PoseStack renderStack, float offsetX, float offsetY, float blitOffset) {
        Vector3 size = new Vector3(this.structure.getMaximumOffset()).subtract(this.structure.getMinimumOffset()).add(1, 1, 1);
        Font fr = RenderablePage.getFont();
        float scale = 1.3F;
        FormattedText description = Component.literal(String.format("%s - %s - %s", size.getBlockX(), size.getBlockY(), size.getBlockZ()));
        float length = fr.getStringPropertyWidth(description) * scale;

        RenderSystem.disableDepthTest();

        renderStack.pushPose();
        renderStack.translate(offsetX, offsetY, blitOffset);
        renderStack.scale(scale, scale, scale);
        RenderingDrawUtils.renderStringAt(description, renderStack, fr, 0x00DDDDDD, true);
        renderStack.popPose();

        this.drawSlice.ifPresent(yLevel -> {
            int min = this.getCurrentMinSlice();
            int max = this.getCurrentMaxSlice();
            int height = max - min;
            int level = yLevel - min;
            FormattedText slice = Component.literal(String.format("%s / %s", level + 1, height + 1));

            renderStack.pushPose();
            renderStack.translate(offsetX, offsetY + 14, blitOffset);
            renderStack.scale(scale, scale, scale);
            RenderingDrawUtils.renderStringAt(slice, renderStack, fr, 0x00DDDDDD, true);
            renderStack.popPose();
        });

        RenderSystem.enableDepthTest();
        return length + 8F;
    }

    private void renderStructure(PoseStack renderStack, float offsetX, float offsetY, float pTicks) {
        Point.Double renderOffset = renderOffset(offsetX + 8, offsetY);
        this.structureRenderer.setRenderWithRequiredAir(this.showAirBlocks);
        this.structureRenderer.render3DSliceGUI(renderStack, renderOffset.x + shift.getX(), renderOffset.y + shift.getY(), pTicks, drawSlice);
        this.structureRenderer.setRenderWithRequiredAir(false);
    }

    private Point.Double renderOffset(float stdPageOffsetX, float stdPageOffsetY) {
        return new Point.Double(stdPageOffsetX + JournalPage.DEFAULT_WIDTH * 0.45, stdPageOffsetY + JournalPage.DEFAULT_HEIGHT * 0.6);
    }

    @Override
    public void postRender(PoseStack renderStack, float x, float y, float z, float pTicks, float xpos, float ypos) {
        renderStack.pushPose();
        renderStack.translate(x + 160, y + 10, z);
        Rectangle rect = RenderingDrawUtils.drawInfoStar(renderStack, IDrawRenderTypeBuffer.defaultBuffer(), 15, pTicks);
        rect.translate((int) (x + 160), (int) (y + 10));
        renderStack.popPose();

        if (rect.contains(xpos, ypos)) {
            RenderingDrawUtils.renderBlueTooltip(renderStack, x + 160, y + 10, z + 650, this.contentStacks, RenderablePage.getFont(), false);
        }

        if (this.switchView != null && this.switchView.contains(xpos, ypos)) {
            FormattedText switchInfo = Component.translatable("astralsorcery.journal.structure.switch_view");
            RenderingDrawUtils.renderBlueTooltipComponents(renderStack, this.switchView.x + this.switchView.width / 2, this.switchView.y + this.switchView.height / 2, z + 500,
                    Lists.newArrayList(switchInfo), RenderablePage.getFont(), false);
        }
        if (this.switchRequiredAir != null && this.switchRequiredAir.contains(xpos, ypos)) {
            FormattedText switchInfo = Component.translatable("astralsorcery.journal.structure.required_air");
            RenderingDrawUtils.renderBlueTooltipComponents(renderStack, this.switchRequiredAir.x + this.switchRequiredAir.width / 2, this.switchRequiredAir.y + this.switchRequiredAir.height / 2, z + 500,
                    Lists.newArrayList(switchInfo), RenderablePage.getFont(), false);
        }
    }

    @Override
    public boolean propagateMouseDrag(double mouseDX, double mouseDZ) {
        this.structureRenderer.rotateFromMouseDrag((float) mouseDX, (float) mouseDZ);
        return true;
    }

    @Override
    public boolean propagateMouseClick(double xpos, double mouseZ) {
        if (switchView != null && switchView.contains(xpos, mouseZ)) {
            if (drawSlice.isPresent()) {
                drawSlice = Optional.empty();
            } else {
                drawSlice = Optional.of(this.getCurrentMinSlice());
            }
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (sliceUp != null && drawSlice.isPresent() && sliceUp.contains(xpos, mouseZ)) {
            drawSlice = Optional.of(drawSlice.get() + 1);
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (sliceDown != null && drawSlice.isPresent() && sliceDown.contains(xpos, mouseZ)) {
            drawSlice = Optional.of(drawSlice.get() - 1);
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (switchRequiredAir != null && switchRequiredAir.contains(xpos, mouseZ)) {
            showAirBlocks = !showAirBlocks;
            if (drawSlice.isPresent()) {
                int yLevel = this.drawSlice.get();
                int minSlice = this.getCurrentMinSlice();
                int maxSlice = this.getCurrentMaxSlice();
                if (yLevel < minSlice) {
                    yLevel = maxSlice;
                }
                if (yLevel > maxSlice) {
                    yLevel = maxSlice;
                }
                this.drawSlice = Optional.of(yLevel);
            }
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        return false;
    }
}
