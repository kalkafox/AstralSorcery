/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderStateUtil
 * Created by HellFirePvP
 * Date: 29.08.2020 / 14:32
 */
public class RenderStateUtil {

    public static class CullState extends RenderStateShard.CullStateShard {

        private final boolean enabled;

        public CullState(boolean enabled) {
            super(enabled);
            this.enabled = enabled;
        }

        @Override
        public void setupRenderState() {
            super.setupRenderState();
            if (!enabled) {
                RenderSystem.disableCull();
            }
        }

        @Override
        public void clearRenderState() {
            super.clearRenderState();
        }
    }

    public static class WriteMaskState extends RenderStateShard.WriteMaskStateShard {

        private final boolean writeColor;
        private final boolean writeDepth;

        public WriteMaskState(boolean writeColor, boolean writeDepth) {
            super(writeColor, writeDepth);
            this.writeColor = writeColor;
            this.writeDepth = writeDepth;
        }

        @Override
        public void setupRenderState() {
            super.setupRenderState();
            if (writeDepth) {
                RenderSystem.depthMask(true);
            }
            if (writeColor) {
                RenderSystem.colorMask(true, true, true, true);
            }
        }

        @Override
        public void clearRenderState() {
            super.clearRenderState();
            if (writeDepth) {
                RenderSystem.depthMask(true);
            }
            if (writeColor) {
                RenderSystem.colorMask(true, true, true, true);
            }
        }
    }
}
