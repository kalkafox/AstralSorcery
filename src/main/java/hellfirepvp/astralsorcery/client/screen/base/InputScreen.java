/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: InputScreen
 * Created by HellFirePvP
 * Date: 10.08.2019 / 17:18
 */
public class InputScreen extends Screen {

    private final Set<Integer> heldKeys = new HashSet<>();

    private double oMouseX, oMouseY;
    private boolean dragging = false;

    // 1.21 port: Screen lost get/setBlitOffset; the mod's own gui draw helpers still layer by z,
    // so the offset is kept here as a plain field.
    private int blitOffset = 0;

    @Nullable
    private GuiGraphics currentGraphics = null;

    protected InputScreen(Component name) {
        super(name);
    }

    public int getBlitOffset() {
        return this.blitOffset;
    }

    public void setBlitOffset(int blitOffset) {
        this.blitOffset = blitOffset;
    }

    // 1.21 port: vanilla render() takes GuiGraphics; the mod's screens draw through their own
    // PoseStack-based helpers, so bridge here and let subclasses keep overriding the PoseStack variant.
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        this.currentGraphics = graphics;
        this.render(graphics.pose(), mouseX, mouseY, pTicks);
        this.currentGraphics = null;
    }

    public void render(PoseStack renderStack, int mouseX, int mouseY, float pTicks) {
        if (this.currentGraphics != null) {
            super.render(this.currentGraphics, mouseX, mouseY, pTicks);
        }
    }

    @Nullable
    public GuiGraphics getCurrentGraphics() {
        return this.currentGraphics;
    }

    @Override
    public void tick() {
        heldKeys.forEach(this::keyPressedTick);

        super.tick();
    }

    protected void keyPressedTick(int key) {}

    protected void mouseDragStart(double xpos, double ypos) {}

    protected void mouseDragStop(double xpos, double ypos, double mouseDiffX, double mouseDiffY) {}

    protected void mouseDragTick(double xpos, double ypos, double mouseDiffX, double mouseDiffY, double mouseOffsetX, double mouseOffsetY) {}

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        heldKeys.add(key);
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        heldKeys.remove(key);
        return super.keyReleased(key, scanCode, modifiers);
    }

    public boolean isCurrentlyDragging() {
        return this.dragging;
    }

    protected void stopDragging(double xpos, double ypos) {
        if (this.dragging) {
            this.dragging = false;
            this.mouseDragStop(xpos, ypos, oMouseX, oMouseY);
        }
    }

    @Override
    public boolean mouseClicked(double xpos, double ypos, int click) {
        if (click == 0) {
            this.dragging = true;
            this.oMouseX = xpos;
            this.oMouseY = ypos;
            this.mouseDragStart(xpos, ypos);
        }
        return super.mouseClicked(xpos, ypos, click);
    }

    @Override
    public boolean mouseReleased(double xpos, double ypos, int click) {
        if (click == 0) {
            this.stopDragging(xpos, ypos);
        }
        return super.mouseReleased(xpos, ypos, click);
    }

    @Override
    public boolean mouseDragged(double xpos, double ypos, int clickType, double offsetX, double offsetY) {
        if (clickType == 0 && this.dragging) {
            double diffX = this.oMouseX - xpos;
            double diffY = this.oMouseY - ypos;
            this.mouseDragTick(xpos, ypos, diffX, diffY, offsetX, offsetY);
        }
        return super.mouseDragged(xpos, ypos, clickType, offsetX, offsetY);
    }
}
