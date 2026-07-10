/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base.patreon.types;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.render.ObjModelRender;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.base.patreon.FlareColor;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeCelestialWings
 * Created by HellFirePvP
 * Date: 04.04.2020 / 20:56
 */
public class TypeCelestialWings extends PatreonEffect implements ITickHandler {

    private final UUID playerUUID;

    public TypeCelestialWings(UUID effectUUID, @Nullable FlareColor flareColor, UUID playerUUID) {
        super(effectUUID, flareColor);
        this.playerUUID = playerUUID;
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);

        bus.register(this);
    }

    @Override
    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        super.attachTickListeners(registrar);

        registrar.accept(this);
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player player = (Player) context[0];
        LogicalSide direction = (LogicalSide) context[1];

        if (direction.isClient() &&
                shouldDoEffect(player) &&
                (Minecraft.getInstance().player != null &&
                        Minecraft.getInstance().player.getUUID().equals(playerUUID) &&
                        !Minecraft.getInstance().options.getCameraType().isFirstPerson())) { //Not-in-first-person
            playEffects(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEffects(Player player) {
        float rot = RenderingVectorUtils.interpolateRotation(player.yBodyRotO, player.yBodyRot, 0);
        float yOffset = 1.3F;
        if (player.isShiftKeyDown()) {
            yOffset = 1F;
        }
        float f = Math.abs((ClientScheduler.getSystemClientTick() % 240) - 120F) / 120F;
        double offset = Math.cos(f * 2 * Math.PI) * 0.03;

        Vector3 forwards = new Vector3(1, 0, 0).mirror(Math.toRadians(360F - rot), Vector3.RotAxis.Y_AXIS).normalize();
        Vector3 pos = Vector3.atEntityCorner(player);
        pos.setY(player.getY() + yOffset + offset);

        for (int i = 0; i < 4; i++) {
            double height = -0.1 + Math.min(random.nextFloat() * 1.3, random.nextFloat() * 1.3);
            double distance = 1.2F - (random.nextFloat() * 0.6) * (1 - Math.max(0, height));

            Vector3 dir = forwards.clone().mirror(Math.toRadians(180 * (random.nextBoolean() ? 1 : 0)), Vector3.RotAxis.Y_AXIS)
                    .normalize()
                    .mul(distance);

            Vector3 at = pos.clone().addY(height).add(dir);

            Color col = Color.getHSBColor(0.68F, 1, 0.6F - random.nextFloat() * 0.5F);

            FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .color(VFXColorFunction.constant(col))
                    .setScaleMultiplier(0.27F + random.nextFloat() * 0.1F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setMaxAge(25 + random.nextInt(20));

            if (random.nextInt(4) == 0) {
                p.setScaleMultiplier(0.09F + random.nextFloat() * 0.02F)
                        .color(VFXColorFunction.WHITE)
                        .setMaxAge(10 + random.nextInt(8));
            } else {
                p.setGravityStrength(0.0003F);
            }
        }
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
        this.renderWings(player, event.getPoseStack(), event.getPartialRenderTick());
    }

    @OnlyIn(Dist.CLIENT)
    private void renderWings(Player player, PoseStack renderStack, float pTicks) {
        float rot = RenderingVectorUtils.interpolateRotation(player.yBodyRotO, player.yBodyRot, pTicks);
        float yOffset = 1.3F;
        if (player.isShiftKeyDown() && !player.getAbilities().flying) {
            yOffset = 1F;
        }
        float f = Math.abs((ClientScheduler.getSystemClientTick() % 240) - 120F) / 120F;
        double offset = Math.cos(f * 2 * Math.PI) * 0.03;

        renderStack.pushPose();
        renderStack.translate(0, yOffset + offset, 0);
        renderStack.mulPose(Axis.YP.rotationDegrees(180F - rot));
        renderStack.scale(0.02F, 0.02F, 0.02F);

        RenderTypesAS.MODEL_CELESTIAL_WINGS.setupRenderState();

        renderStack.translate(-25, 0, 0);
        ObjModelRender.renderCelestialWings(renderStack);
        renderStack.mulPose(Axis.YP.rotationDegrees(180F));
        renderStack.translate(-50, 0, 0);
        ObjModelRender.renderCelestialWings(renderStack);
        renderStack.popPose();

        RenderTypesAS.MODEL_CELESTIAL_WINGS.clearRenderState();
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "PatreonEffect - Celestial Wings " + this.playerUUID.toString();
    }
}
