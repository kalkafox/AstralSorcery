/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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

    protected InputScreen(Component name) {
        super(name);
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
