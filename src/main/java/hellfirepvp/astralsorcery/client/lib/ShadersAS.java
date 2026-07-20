/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.lib;

import net.minecraft.client.renderer.ShaderInstance;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ShadersAS
 * Created by HellFirePvP
 * Date: 18.07.2026 / 21:40
 */
public class ShadersAS {

    // position_tex_color without the vanilla alpha-0.1 discard; keeps the smooth falloff
    // on soft particle/flare textures that 1.16 rendered with alpha test disabled.
    public static ShaderInstance EFFECT_TEX_COLOR;

    private ShadersAS() {}
}
