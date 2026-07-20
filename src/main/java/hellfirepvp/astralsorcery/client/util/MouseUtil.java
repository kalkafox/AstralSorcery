/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MouseUtil
 * Created by HellFirePvP
 * Date: 14.02.2020 / 21:52
 */
public class MouseUtil {

    public static void ungrab() {
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    /**
     * Grab the mouse while keeping the current screen open.
     *
     * Vanilla's {@link MouseHandler#grabMouse()} always calls {@code setScreen(null)}. On 1.16 that
     * screen close could be intercepted by cancelling {@code GuiOpenEvent}; on NeoForge 1.21
     * {@code ScreenEvent.Opening} no longer fires for {@code setScreen(null)} and
     * {@code ScreenEvent.Closing} is not cancellable, so the grab is replicated here without the
     * screen change (fields opened via accesstransformer.cfg).
     */
    public static void grab() {
        Minecraft mc = Minecraft.getInstance();
        MouseHandler mouse = mc.mouseHandler;
        if (!mc.isWindowActive() || mouse.isMouseGrabbed()) {
            return;
        }
        if (!Minecraft.ON_OSX) {
            KeyMapping.setAll();
        }
        mouse.mouseGrabbed = true;
        mouse.xpos = mc.getWindow().getScreenWidth() / 2.0D;
        mouse.ypos = mc.getWindow().getScreenHeight() / 2.0D;
        InputConstants.grabOrReleaseMouse(mc.getWindow().getWindow(), GLFW.GLFW_CURSOR_DISABLED, mouse.xpos, mouse.ypos);
        mouse.ignoreFirstMove = true;
    }

}
