/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.screen.base.SkyScreen;
import hellfirepvp.astralsorcery.client.screen.base.TileConstellationDiscoveryScreen;
import hellfirepvp.astralsorcery.client.screen.telescope.TelescopeRotationDrawArea;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktRotateTelescope;
import hellfirepvp.astralsorcery.common.tile.TileTelescope;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenTelescope
 * Created by HellFirePvP
 * Date: 15.01.2020 / 17:16
 */
public class ScreenTelescope extends TileConstellationDiscoveryScreen<TileTelescope, TelescopeRotationDrawArea> implements SkyScreen, NavigationArrowScreen {

    private TileTelescope.TelescopeRotation rotation;

    private Rectangle rectArrowCW = null, rectArrowCCW = null;

    public ScreenTelescope(TileTelescope telescope) {
        super(telescope, 280, 280);
        this.rotation = telescope.getRotation();
    }

    @Nonnull
    @Override
    protected List<TelescopeRotationDrawArea> createDrawAreas() {
        List<TelescopeRotationDrawArea> areas = new LinkedList<>();
        for (TileTelescope.TelescopeRotation r : TileTelescope.TelescopeRotation.values()) {
            areas.add(new TelescopeRotationDrawArea(this, r, this.getGuiBox()));
        }
        return areas;
    }

    @Override
    protected void fillConstellations(WorldContext ctx, List<TelescopeRotationDrawArea> drawAreas) {
        Random gen = ctx.getDayRandom();
        PlayerProgress prog = ResearchHelper.getClientProgress();

        List<IWeakConstellation> cst = new ArrayList<>();
        for (IConstellation active : ctx.getActiveCelestialsHandler().getCurrentRenderPositions().keySet()) {
            if (active instanceof IWeakConstellation && active.canDiscover(Minecraft.getInstance().player, prog)) {
                cst.add((IWeakConstellation) active);
            }
        }
        Collections.shuffle(cst, gen);
        cst = cst.subList(0, Math.min(drawAreas.size(), cst.size()));
        for (IWeakConstellation constellation : cst) {
            Point foundPoint;
            TelescopeRotationDrawArea associatedArea;
            do {
                associatedArea = MiscUtils.getRandomEntry(drawAreas, gen);
                foundPoint = findEmptySpace(gen, associatedArea);
            } while (foundPoint == null);
            associatedArea.addConstellationToArea(constellation, foundPoint, DEFAULT_CONSTELLATION_SIZE);
        }
    }

    private Point findEmptySpace(Random random, TelescopeRotationDrawArea area) {
        int size = DEFAULT_CONSTELLATION_SIZE;
        int wdh = guiWidth  - 6 - size;
        int hgt = guiHeight - 6 - size;
        int rX = 6 + random.nextInt(wdh);
        int rY = 6 + random.nextInt(hgt);
        Rectangle constellationRect = new Rectangle(rX, rY, size, size);
        for (ConstellationDisplayInformation info : area.getDisplayMap().values()) {
            Point offset = info.getCameraPosition();
            Rectangle otherRect = new Rectangle(offset.x, offset.y, size, size);
            if (otherRect.intersects(constellationRect)) {
                return null;
            }
        }
        return new Point(rX, rY);
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        RenderSystem.enableDepthTest();
        super.render(renderStack, xpos, ypos, pTicks);

        this.drawWHRect(renderStack, TexturesAS.TEX_GUI_TELESCOPE);

        this.drawConstellationCell(renderStack, pTicks);

        this.drawNavArrows(renderStack, xpos, ypos, pTicks);
    }

    private void drawNavArrows(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        this.rectArrowCCW = this.drawArrow(renderStack, leftPos - 40, topPos + (guiHeight / 2), this.getGuiZLevel(), Type.LEFT, xpos, ypos, pTicks);
        this.rectArrowCW = this.drawArrow(renderStack, leftPos + guiWidth + 10, topPos + (guiHeight / 2), this.getGuiZLevel(), Type.RIGHT, xpos, ypos, pTicks);

        RenderSystem.disableBlend();
    }

    private void drawConstellationCell(PoseStack renderStack, float pTicks) {
        boolean canSeeSky = this.canObserverSeeSky(this.getTile().getBlockPos(), 1);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        this.setBlitOffset(-10);
        this.drawSkyBackground(renderStack, pTicks, canSeeSky);

        if (!this.isInitialized()) {
            this.setBlitOffset(0);

            Blending.DEFAULT.apply();
            RenderSystem.disableBlend();
            return;
        }

        WorldContext ctx = this.getContext();
        if (ctx != null && canSeeSky) {
            Random gen = ctx.getDayRandom();
            PlayerProgress prog = ResearchHelper.getClientProgress();

            for (int i = 0; i < this.rotation.ordinal(); i++) {
                gen.nextFloat(); //Flush
            }

            this.setBlitOffset(-9);
            float starSize = 5F;
            TexturesAS.TEX_STAR_1.bindTexture();
            RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
                for (int i = 0; i < 72 + gen.nextInt(108); i++) {
                    float innerOffsetX = starSize + gen.nextFloat() * (guiWidth  - starSize * 2) + this.getGuiLeft();
                    float innerOffsetY = starSize + gen.nextFloat() * (guiHeight - starSize * 2) + this.getGuiTop();
                    float brightness = 0.4F + (RenderingConstellationUtils.stdFlicker(ClientScheduler.getClientTick(), pTicks, 10 + gen.nextInt(20))) * 0.5F;
                    brightness = this.multiplyStarBrightness(pTicks, brightness);

                    RenderingGuiUtils.rect(buf, renderStack, this)
                            .at(innerOffsetX, innerOffsetY)
                            .dim(starSize, starSize)
                            .color(brightness, brightness, brightness, brightness)
                            .draw();
                }
            });

            this.setBlitOffset(-7);
            for (TelescopeRotationDrawArea area : this.getVisibleDrawAreas()) {
                for (IConstellation cst : area.getDisplayMap().keySet()) {
                    ConstellationDisplayInformation info = area.getDisplayMap().get(cst);
                    info.getFrameDrawInformation().clear();

                    Point pos = info.getCameraPosition();
                    int size = (int) info.getRenderSize();

                    float rainBr = 1F - Minecraft.getInstance().level.getRainStrength(pTicks);
                    Map<StarLocation, Rectangle.Float> cstRenderInfo = RenderingConstellationUtils.renderConstellationIntoGUI(
                            cst, renderStack,
                            pos.x + leftPos,
                            pos.y + topPos,
                            this.getGuiZLevel(),
                            size, size,
                            2.5F,
                            () -> RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), pTicks, 5 + gen.nextInt(15)) * rainBr,
                            prog.hasConstellationDiscovered(cst),
                            true);

                    info.getFrameDrawInformation().putAll(cstRenderInfo);
                }
            }

            this.setBlitOffset(-5);
            this.renderDrawnLines(renderStack, gen, pTicks);
        }

        this.setBlitOffset(0);

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    private void drawSkyBackground(PoseStack renderStack, float pTicks, boolean canSeeSky) {
        Tuple<Color, Color> rgbFromTo = SkyScreen.getSkyGradient(canSeeSky, 1F, pTicks);
        RenderingDrawUtils.drawGradientRect(renderStack, this.getGuiZLevel(),
                this.leftPos + 5, this.topPos + 5,
                this.leftPos + this.guiWidth - 5, this.topPos + this.guiHeight - 5,
                rgbFromTo.getA().getRGB(), rgbFromTo.getB().getRGB());
    }
    @Override
    public boolean mouseClicked(double xpos, double ypos, int buttonId) {
        if (super.mouseClicked(xpos, ypos, buttonId)) {
            return true;
        }

        Point p = new Point((int) xpos, (int) ypos);
        if (rectArrowCW != null && rectArrowCW.contains(p)) {
            PktRotateTelescope pkt = new PktRotateTelescope(true, this.getTile().getLevel().dimension(), this.getTile().getBlockPos());
            PacketChannel.CHANNEL.sendToServer(pkt);
            return true;
        }
        if (rectArrowCCW != null && rectArrowCCW.contains(p)) {
            PktRotateTelescope pkt = new PktRotateTelescope(false, this.getTile().getLevel().dimension(), this.getTile().getBlockPos());
            PacketChannel.CHANNEL.sendToServer(pkt);
            return true;
        }
        return false;
    }

    public void handleRotationChange(boolean isClockwise) {
        this.rotation = isClockwise ? rotation.nextClockWise() : rotation.nextCounterClockWise();
        this.clearDrawing();
    }

    public TileTelescope.TelescopeRotation getRotation() {
        return rotation;
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double xpos, double ypos) {
        return true;
    }

    @Override
    protected boolean isMouseRotatingGui() {
        return false;
    }
}
