/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.TileEntityScreen;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravedStarMap;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktEngraveGlass;
import hellfirepvp.astralsorcery.common.tile.TileRefractionTable;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenRefractionTable
 * Created by HellFirePvP
 * Date: 27.04.2020 / 21:07
 */
public class ScreenRefractionTable extends TileEntityScreen<TileRefractionTable> {

    private static final Rectangle PLACEMENT_GRID = new Rectangle(
            68 + DrawnConstellation.CONSTELLATION_DRAW_SIZE, 45 + DrawnConstellation.CONSTELLATION_DRAW_SIZE,
            120 - (DrawnConstellation.CONSTELLATION_DRAW_SIZE * 2), 120 - (DrawnConstellation.CONSTELLATION_DRAW_SIZE * 2));

    private final Map<Rectangle, IConstellation> mapRenderedConstellations = new HashMap<>();

    private final List<DrawnConstellation> currentlyDrawnConstellations = new ArrayList<>();
    private IConstellation dragging = null;

    public ScreenRefractionTable(TileRefractionTable tile) {
        super(tile, 188, 256);
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        RenderSystem.enableDepthTest();
        super.render(renderStack, xpos, ypos, pTicks);
        this.mapRenderedConstellations.clear();

        if (this.getTile().hasParchment()) {
            this.drawWHRect(renderStack, TexturesAS.TEX_GUI_REFRACTION_TABLE_PARCHMENT);
        } else {
            this.drawWHRect(renderStack, TexturesAS.TEX_GUI_REFRACTION_TABLE_EMPTY);
        }

        if (DayTimeHelper.getCurrentDaytimeDistribution(this.getTile().getLevel()) <= 0.05 || !this.getTile().hasParchment()) {
            this.currentlyDrawnConstellations.clear();
            this.dragging = null;
        }

        List<FormattedText> tooltip = new ArrayList<>();
        Font tooltipRenderer = Minecraft.getInstance().font;

        tooltipRenderer = this.renderTileItems(renderStack, xpos, ypos, tooltip, tooltipRenderer);
        this.renderConstellationOptions(renderStack, xpos, ypos, tooltip);
        this.renderRunningHalo(renderStack);
        this.renderInputItem(renderStack);
        this.renderDrawnConstellations(renderStack, xpos, ypos, tooltip);
        this.renderDraggedConstellations(renderStack);
        this.renderDragging(renderStack, xpos, ypos);

        if (!tooltip.isEmpty()) {
            this.setBlitOffset(510);
            RenderingDrawUtils.renderBlueTooltipComponents(renderStack, xpos, ypos, this.getGuiZLevel(), tooltip, tooltipRenderer, true);
            this.setBlitOffset(0);
        }
    }

    private void renderDragging(PoseStack renderStack, int xpos, int ypos) {
        if (this.dragging == null) {
            return;
        }

        int whDrawn = DrawnConstellation.CONSTELLATION_DRAW_SIZE;
        Point offset = new Point(xpos, ypos);
        offset.translate(-whDrawn, -whDrawn);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderingConstellationUtils.renderConstellationIntoGUI(dragging, renderStack, offset.x, offset.y, this.getGuiZLevel(),
                whDrawn * 2, whDrawn * 2, 1.4F,
                () -> DayTimeHelper.getCurrentDaytimeDistribution(this.getTile().getLevel()), true, false);
        RenderSystem.disableBlend();

        this.renderFilledBox(renderStack, offset.x, offset.y, whDrawn * 2, whDrawn * 2, dragging.getTierRenderColor());
        Rectangle r = new Rectangle(PLACEMENT_GRID);
        r.grow(DrawnConstellation.CONSTELLATION_DRAW_SIZE, DrawnConstellation.CONSTELLATION_DRAW_SIZE);
        r.translate(leftPos, topPos);
        this.renderFilledBox(renderStack, r.x, r.y, r.width, r.height, dragging.getTierRenderColor());
    }

    private void renderDraggedConstellations(PoseStack renderStack) {
        int whDrawn = DrawnConstellation.CONSTELLATION_DRAW_SIZE;
        for (DrawnConstellation dragged : this.currentlyDrawnConstellations) {
            Point offset = new Point(dragged.getPoint());
            offset.translate(leftPos, topPos);
            offset.translate(PLACEMENT_GRID.x, PLACEMENT_GRID.y);
            offset.translate(-whDrawn, -whDrawn);

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderingConstellationUtils.renderConstellationIntoGUI(dragged.getConstellation(), renderStack,
                    offset.x, offset.y, this.getGuiZLevel(),
                    whDrawn * 2, whDrawn * 2, 1.4F,
                    () -> DayTimeHelper.getCurrentDaytimeDistribution(this.getTile().getLevel()), true, false);
            RenderSystem.disableBlend();
        }
    }

    private void renderInputItem(PoseStack renderStack) {
        if (this.getTile().getInputStack().isEmpty() || this.getTile().hasParchment()) {
            return;
        }

        this.setBlitOffset(100);
        ItemStack from = this.getTile().getInputStack();
        RenderSystem.disableDepthTest();

        renderStack.pushPose();
        renderStack.translate(leftPos + 63 + 16.25, topPos + 42 + 16.25, getGuiZLevel());
        renderStack.scale(6F, 6F, 1F);

        RenderingUtils.renderItemStackGUI(renderStack, from, null);

        renderStack.popPose();

        RenderSystem.enableDepthTest();
        this.setBlitOffset(0);
    }

    private void renderRunningHalo(PoseStack renderStack) {
        if (!(this.getTile().getRunProgress() > 0)) {
            return;
        }

        SpritesAS.SPR_HALO_INFUSION.bindTexture();
        Tuple<Float, Float> uvFrame = SpritesAS.SPR_HALO_INFUSION.getUVOffset(ClientScheduler.getClientTick());

        float scale = 160F;

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.disableAlphaTest();

        renderStack.pushPose();
        renderStack.translate(guiWidth / 2F, guiHeight / 2F, 0);
        renderStack.scale(-scale / 2, -scale / 2, 1);

        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, this)
                    .dim(scale, scale)
                    .color(1F, 1F, 1F, this.getTile().getRunProgress())
                    .tex(uvFrame.getA(), uvFrame.getB(), SpritesAS.SPR_HALO_INFUSION.getUWidth(), SpritesAS.SPR_HALO_INFUSION.getVWidth())
                    .draw();
        });

        renderStack.popPose();

        RenderSystem.enableAlphaTest();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    private void renderDrawnConstellations(PoseStack renderStack, int xpos, int ypos, List<FormattedText> tooltip) {
        ItemStack glass = this.getTile().getGlassStack();
        if (glass.isEmpty()) {
            return;
        }
        Level level = this.getTile().getLevel();
        float nightPerc = DayTimeHelper.getCurrentDaytimeDistribution(level);
        WorldContext ctx = SkyHandler.getContext(level, LogicalSide.CLIENT);
        if (ctx == null || !this.getTile().doesSeeSky() || nightPerc <= 0.05F) {
            return;
        }
        EngravedStarMap map = ItemInfusedGlass.getEngraving(glass);
        if (map == null) {
            return;
        }
        for (DrawnConstellation cst : map.getDrawnConstellations()) {
            int whDrawn = DrawnConstellation.CONSTELLATION_DRAW_SIZE;
            Point offset = new Point(cst.getPoint());
            offset.translate(leftPos, topPos);
            offset.translate(PLACEMENT_GRID.x, PLACEMENT_GRID.y);
            offset.translate(-whDrawn, -whDrawn);

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderingConstellationUtils.renderConstellationIntoGUI(cst.getConstellation(), renderStack,
                    offset.x, offset.y, this.getGuiZLevel(),
                    whDrawn * 2, whDrawn * 2, 1.6F,
                    () -> DayTimeHelper.getCurrentDaytimeDistribution(level) * 0.8F, true, false);
            RenderSystem.disableBlend();
        }
    }

    private void renderConstellationOptions(PoseStack renderStack, int xpos, int ypos, List<FormattedText> tooltip) {
        ItemStack glass = this.getTile().getGlassStack();
        if (glass.isEmpty()) {
            return;
        }
        Level level = this.getTile().getLevel();
        float nightPerc = DayTimeHelper.getCurrentDaytimeDistribution(level);
        WorldContext ctx = SkyHandler.getContext(level, LogicalSide.CLIENT);
        if (ctx == null || !this.getTile().doesSeeSky() || nightPerc <= 0.05F) {
            return;
        }
        List<IConstellation> cstList = ctx.getActiveCelestialsHandler().getActiveConstellations()
                .stream()
                .filter(c -> ResearchHelper.getClientProgress().hasConstellationDiscovered(c))
                .collect(Collectors.toList());

        Random random = new Random(WorldSeedCache.getSeedIfPresent(level.dimension()).orElse(0x515F1EB654AB915EL));
        for (int i = 0; i < ctx.getConstellationHandler().getLastTrackedDay(); i++) {
            random.nextLong();
        }
        Collections.shuffle(cstList, random);

        for (int i = 0; i < Math.min(cstList.size(), 12); i++) {
            IConstellation cst = cstList.get(i);
            int offsetX = leftPos + (i % 2 == 0 ? 8 : 232);
            int offsetY = topPos + (40 + (i / 2) * 23);

            Rectangle rct = new Rectangle(offsetX, offsetY, 16, 16);
            this.mapRenderedConstellations.put(rct, cst);

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderingConstellationUtils.renderConstellationIntoGUI(Color.WHITE, cst, renderStack,
                    offsetX, offsetY, this.getGuiZLevel(),
                    16, 16, 0.5,
                    () -> DayTimeHelper.getCurrentDaytimeDistribution(level), true, false);
            RenderSystem.disableBlend();

            if (rct.contains(xpos, ypos)) {
                tooltip.add(cst.getConstellationName());
            }
        }
    }

    private Font renderTileItems(PoseStack renderStack, int xpos, int ypos, List<FormattedText> tooltip, Font tooltipRenderer) {
        this.setBlitOffset(100);

        ItemStack from = this.getTile().getInputStack();
        if (!from.isEmpty()) {
            Rectangle itemRct = new Rectangle(leftPos + 111, topPos + 8, 16, 16);
            renderStack.pushPose();
            renderStack.translate(itemRct.x, itemRct.y, getGuiZLevel());
            RenderingUtils.renderItemStackGUI(renderStack, from, null);
            renderStack.popPose();

            if (itemRct.contains(xpos, ypos)) {
                Font custom = from.getItem().getFont(from);
                if (custom != null) {
                    tooltipRenderer = custom;
                }
                tooltip.addAll(from.getTooltipLines(getMinecraft().player, Minecraft.getInstance().options.advancedItemTooltips ?
                        TooltipFlag.TooltipFlags.ADVANCED : TooltipFlag.TooltipFlags.NORMAL));
            }
        }
        ItemStack glass = this.getTile().getGlassStack();
        if (!glass.isEmpty()) {
            Rectangle itemRct = new Rectangle(leftPos + 129, topPos + 8, 16, 16);
            renderStack.pushPose();
            renderStack.translate(itemRct.x, itemRct.y, getGuiZLevel());
            RenderingUtils.renderItemStackGUI(renderStack, glass, null);
            renderStack.popPose();

            if (itemRct.contains(xpos, ypos)) {
                Font custom = glass.getItem().getFont(glass);
                if (custom != null) {
                    tooltipRenderer = custom;
                }
                tooltip.addAll(glass.getTooltipLines(getMinecraft().player, Minecraft.getInstance().options.advancedItemTooltips ?
                        TooltipFlag.TooltipFlags.ADVANCED : TooltipFlag.TooltipFlags.NORMAL));
            }
        }

        this.setBlitOffset(0);
        return tooltipRenderer;
    }
    
    private void renderFilledBox(PoseStack renderStack, float offsetX, float offsetY, float width, float height, Color c) {
        Random random = new Random(0x12);
        float r = c.getRed() / 255F;
        float g = c.getGreen() / 255F;
        float b = c.getBlue() / 255F;
        Supplier<Float> alpha = () -> 0.1F + 0.4F * ((Mth.sin(random.nextInt(200) + ClientScheduler.getClientTick() / 20F) + 1F) / 2F);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.disableAlphaTest();
        RenderSystem.lineWidth(2F);
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();

        RenderingUtils.draw(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.vertex(offset, offsetX, offsetY, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX +width, offsetY, 0).color(r, g, b, alpha.get()).endVertex();

            buf.vertex(offset, offsetX + width, offsetY, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX + width, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();

            buf.vertex(offset, offsetX + width, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();

            buf.vertex(offset, offsetX, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX, offsetY, 0).color(r, g, b, alpha.get()).endVertex();
        });

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.currentlyDrawnConstellations.size() >= 3) {
            List<DrawnConstellation> copyList = new ArrayList<>(this.currentlyDrawnConstellations);

            PktEngraveGlass engraveGlass = new PktEngraveGlass(
                    this.getTile().getLevel().dimension(),
                    this.getTile().getBlockPos(), copyList);
            PacketChannel.CHANNEL.sendToServer(engraveGlass);
            this.currentlyDrawnConstellations.clear();
        }
    }

    @Override
    public boolean mouseClicked(double xpos, double ypos, int buttonId) {
        if (super.mouseClicked(xpos, ypos, buttonId)) {
            return true;
        }

        if (buttonId == 0 &&
                dragging == null &&
                this.getTile().hasParchment() &&
                this.getTile().hasUnengravedGlass() &&
                this.currentlyDrawnConstellations.size() < 3) {
            tryPick(xpos, ypos);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double xpos, double ypos, int click) {
        if (super.mouseReleased(xpos, ypos, click)) {
            return true;
        }

        if (click == 0 &&
                dragging != null &&
                this.getTile().hasParchment() &&
                this.getTile().hasUnengravedGlass() &&
                this.currentlyDrawnConstellations.size() < 3) {
            tryDrop(xpos, ypos);
        }
        return false;
    }

    private void tryDrop(double xpos, double ypos) {
        if (this.dragging != null) {
            if (PLACEMENT_GRID.contains(xpos - leftPos, ypos - topPos)) {
                Point gridPoint = new Point((int) Math.round(xpos), (int) Math.round(ypos));
                gridPoint.translate(-this.leftPos, -this.topPos);
                gridPoint.translate(-PLACEMENT_GRID.x, -PLACEMENT_GRID.y);

                this.currentlyDrawnConstellations.add(new DrawnConstellation(gridPoint, dragging));
            }
            this.dragging = null;
        }
    }

    private void tryPick(double xpos, double ypos) {
        for (Rectangle r : mapRenderedConstellations.keySet()) {
            if (r.contains(xpos, ypos)) {
                dragging = mapRenderedConstellations.get(r);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
