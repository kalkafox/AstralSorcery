/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.camera;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.player.Player;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CameraTransformerSettingsCache
 * Created by HellFirePvP
 * Date: 02.12.2019 / 20:08
 */
public abstract class CameraTransformerSettingsCache implements ICameraTransformer {

    private boolean active = false;

    private boolean bobView = false, hideGui = false, isFlying = false;
    private CameraType thirdPersonView;

    private Vector3 startPosition;
    private float startYaw, startPitch;

    @Override
    public void onStartTransforming(float pTicks) {
        Minecraft mc = Minecraft.getInstance();

        this.bobView = mc.options.bobView().get();
        this.hideGui = mc.options.hideGui;
        this.thirdPersonView = mc.options.getCameraType();
        Player player = mc.player;
        this.isFlying = player.getAbilities().flying;
        this.startPosition = new Vector3(player.getX(), player.getY(), player.getZ());
        this.startYaw = player.getYRot();
        this.startPitch = player.getXRot();
        player.lerpMotion(0, 0, 0);
        this.active = true;
    }

    @Override
    public void onStopTransforming(float pTicks) {
        if (active) {
            Options settings = Minecraft.getInstance().options;
            settings.bobView().set(bobView);
            settings.hideGui = hideGui;
            settings.setCameraType(thirdPersonView);
            Player player = Minecraft.getInstance().player;
            player.getAbilities().flying = isFlying;
            player.moveTo(startPosition.getX(), startPosition.getY(), startPosition.getZ(), startYaw, startPitch);
            player.lerpMotion(0, 0, 0);
            this.active = false;
        }
    }

    @Override
    public void transformRenderView(float pTicks) {
        if (!active) {
            return;
        }

        Options settings = Minecraft.getInstance().options;
        settings.hideGui = true;
        settings.bobView().set(false);
        settings.setCameraType(CameraType.THIRD_PERSON_BACK);
        Minecraft.getInstance().player.getAbilities().flying = true;
        Minecraft.getInstance().player.lerpMotion(0, 0, 0);
    }

}
