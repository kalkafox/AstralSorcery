/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base.patreon.types;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXPositionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.base.patreon.FlareColor;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeStarHalo
 * Created by HellFirePvP
 * Date: 04.12.2020 / 17:59
 */
public class TypeStarHalo extends PatreonEffect implements ITickHandler {

    private final UUID playerUUID;

    public TypeStarHalo(UUID effectUUID, @Nullable FlareColor flareColor, UUID playerUUID) {
        super(effectUUID, flareColor);
        this.playerUUID = playerUUID;
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

        if (direction.isClient() && shouldDoEffect(player)) {
            spawnHaloParticles(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnHaloParticles(Player player) {
        Vector3 headPos = Vector3.atEntityCorner(player).addY(player.getEyeHeight(player.getPose()));

        for (int i = 0; i < 3; i++) {
            Vector3 offset = MiscUtils.getRandomCirclePosition(new Vector3(), Vector3.RotAxis.Y_AXIS, 0.3F);
            float scale = 0.16F + random.nextFloat() * 0.12F;
            int age = 20 + random.nextInt(10);
            MiscUtils.applyRandomOffset(offset, random, 0.02F);

            FXFacingParticle particle = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(headPos.clone().addY(0.4F).add(offset))
                    .setAlphaMultiplier(0.8F)
                    .alpha1arg(((VFXAlphaFunction<EntityVisualFX>) (fx, alphaIn, pTicks) -> {
                        if (shouldDoEffect(player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                            if (player.getXRot() < -30) {
                                return Mth.clamp(1F - (Math.abs(player.getXRot()) - 30F) / 15F, 0, 1F) * alphaIn;
                            }
                        }
                        return alphaIn;
                    }).andThen(VFXAlphaFunction.PYRAMID))
                    .color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_TYPE_WEAK))
                    .setScaleMultiplier(scale)
                    .setMaxAge(age);
            if (random.nextInt(3) == 0) {
                particle.color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_VICIO));
            }

            FXFacingParticle starParticle = null;
            if (random.nextInt(5) == 0) {
                starParticle = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(headPos.clone().addY(0.4F).add(offset))
                        .setAlphaMultiplier(0.8F)
                        .color(VFXColorFunction.WHITE)
                        .alpha1arg(((VFXAlphaFunction<EntityVisualFX>) (fx, alphaIn, pTicks) -> {
                            if (shouldDoEffect(player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                                if (player.getXRot() < -30) {
                                    return Mth.clamp(1F - (Math.abs(player.getXRot()) - 30F) / 15F, 0, 1F) * alphaIn;
                                }
                            }
                            return alphaIn;
                        }).andThen(VFXAlphaFunction.PYRAMID))
                        .setScaleMultiplier(scale * 0.6F)
                        .setMaxAge(age);
            }

            if (random.nextInt(4) != 0) {
                particle.position(new VFXPositionController<EntityVisualFX>() {
                    @Nonnull
                    @Override
                    public Vector3 finalizePosition(@Nonnull EntityVisualFX fx, @Nonnull Vector3 position, @Nonnull Vector3 motionToBeMoved) {
                        if (shouldDoEffect(player)) {
                            Vector3 diff = new Vector3(player.xo - player.getX(), player.yo - player.getY(), player.zo - player.getZ());
                            diff.divide(4);
                            return Vector3.atEntityCorner(player).add(diff).addY(player.getEyeHeight(player.getPose()))
                                    .addY(0.4F)
                                    .add(offset);
                        }
                        return new Vector3();
                    }
                });
                if (starParticle != null) {
                    starParticle.position(new VFXPositionController<EntityVisualFX>() {
                        @Nonnull
                        @Override
                        public Vector3 finalizePosition(@Nonnull EntityVisualFX fx, @Nonnull Vector3 position, @Nonnull Vector3 motionToBeMoved) {
                            if (shouldDoEffect(player)) {
                                Vector3 diff = new Vector3(player.xo - player.getX(), player.yo - player.getY(), player.zo - player.getZ());
                                diff.divide(4);
                                return Vector3.atEntityCorner(player).add(diff).addY(player.getEyeHeight(player.getPose()))
                                        .addY(0.4F)
                                        .add(offset);
                            }
                            return new Vector3();
                        }
                    });
                }
            }
        }
    }

    private boolean shouldDoEffect(Player player) {
        return player.getUUID().equals(playerUUID) &&
                (player.getPose() == Pose.STANDING || player.getPose() == Pose.CROUCHING) &&
                !player.hasEffect(MobEffects.INVISIBILITY);
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
        return "PatreonEffect - Star halo " + this.playerUUID.toString();
    }
}
