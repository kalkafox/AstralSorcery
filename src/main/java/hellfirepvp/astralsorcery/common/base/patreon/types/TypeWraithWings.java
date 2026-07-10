/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base.patreon.types;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.render.ObjModelRender;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.base.patreon.FlareColor;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeWraithWings
 * Created by HellFirePvP
 * Date: 04.03.2020 / 23:06
 */
public class TypeWraithWings extends PatreonEffect {

    private final UUID playerUUID;

    public TypeWraithWings(UUID effectUUID, @Nullable FlareColor flareColor, UUID playerUUID) {
        super(effectUUID, flareColor);
        this.playerUUID = playerUUID;
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);

        bus.register(this);
    }

    private boolean shouldDoEffect(Player player) {
        return player.getUUID().equals(playerUUID) &&
                !player.isPassenger() &&
                !player.isFallFlying() &&
                !player.isPotionActive(MobEffects.INVISIBILITY);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    void onRender(RenderPlayerEvent.Post event) {
        Player player = event.getPlayer();
        if (!shouldDoEffect(player)) {
            return;
        }
        PoseStack renderStack = event.getPoseStack();

        float rot = RenderingVectorUtils.interpolateRotation(player.yBodyRotO, player.yBodyRot, event.getPartialRenderTick());

        float yOffset = 1.2F;
        if (player.isShiftKeyDown() && !player.getAbilities().flying) {
            yOffset = 1F;
        }

        renderStack.pushPose();

        float swimAngle = player.getSwimAnimation(event.getPartialRenderTick());
        if (swimAngle > 0) {
            float waterPitch = player.isInWater() ? -90.0F - player.getXRot() : -90.0F;
            float bodySwimAngle = Mth.lerp(swimAngle, 0.0F, waterPitch);
            renderStack.mulPose(Axis.YP.rotationDegrees(180 - rot));
            renderStack.mulPose(Axis.XP.rotationDegrees(bodySwimAngle));
            if (player.isVisuallySwimming()) {
                renderStack.translate(0, -1, 0.3);
            }
        } else {
            renderStack.mulPose(Axis.YP.rotationDegrees(180 - rot));
        }

        renderStack.translate(0, yOffset, 0);
        renderStack.scale(0.32F, 0.32F, 0.32F);

        RenderTypesAS.MODEL_WRAITH_WINGS.setupRenderState();

        renderStack.pushPose();
        renderStack.translate(-2.3, 0, 0.8);
        renderStack.mulPose(Axis.YP.rotationDegrees(10));
        ObjModelRender.renderWraithWings(renderStack);
        renderStack.popPose();

        renderStack.pushPose();
        renderStack.mulPose(Axis.YP.rotationDegrees(180));
        renderStack.translate(-2.3, 0, -0.8);
        renderStack.mulPose(Axis.YN.rotationDegrees(10));
        ObjModelRender.renderWraithWings(renderStack);
        renderStack.popPose();

        RenderTypesAS.MODEL_WRAITH_WINGS.clearRenderState();

        renderStack.popPose();
    }
}
