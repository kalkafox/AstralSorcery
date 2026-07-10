/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.telescope;

import hellfirepvp.astralsorcery.client.screen.base.ConstellationDiscoveryScreen;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PlayerAngledConstellationInformation
 * Created by HellFirePvP
 * Date: 15.02.2020 / 16:02
 */
public class PlayerAngledConstellationInformation extends ConstellationDiscoveryScreen.ConstellationDisplayInformation {

    private final float yRot;
    private final float pitch;

    public PlayerAngledConstellationInformation(float size, float yRot, float pitch) {
        super(new Point(), size);
        this.yRot = yRot;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yRot;
    }

    public float getPitch() {
        return pitch;
    }
}
