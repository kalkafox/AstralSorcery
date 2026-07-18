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
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.base.patreon.FlareColor;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
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

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeNebulaCloud
 * Created by HellFirePvP
 * Date: 12.12.2020 / 16:27
 */
public class TypeNebulaCloud extends PatreonEffect implements ITickHandler {

    private final UUID playerUUID;

    public TypeNebulaCloud(UUID effectUUID, @Nullable FlareColor flareColor, UUID playerUUID) {
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
            spawnCloudParticles(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnCloudParticles(Player player) {
        Vector3 playerPos = Vector3.atEntityCorner(player).addY(0.1F);

        for (int i = 0; i < 3; i++) {
            float oX = (random.nextFloat() - random.nextFloat()) * 2F;
            float oZ = (random.nextFloat() - random.nextFloat()) * 2F;
            Vector3 offset = new Vector3(oX, random.nextFloat() * 0.1F, oZ);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(playerPos.clone().add(offset))
                    .setAlphaMultiplier(0.8F)
                    .alpha1arg(((VFXAlphaFunction<EntityVisualFX>) (fx, alphaIn, pTicks) -> {
                        if (shouldDoEffect(player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                            if (player.getXRot() > 40) {
                                return Mth.clamp(1F - (player.getXRot() - 40F) / 20F, 0, 1F) * alphaIn;
                            }
                        }
                        return alphaIn;
                    }).andThen(VFXAlphaFunction.FADE_OUT))
                    .color(VFXColorFunction.WHITE)
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.3F)
                    .setMaxAge(40 + random.nextInt(20));
        }

        if (random.nextInt(16) == 0) {
            Vector3 from = Vector3.random().setY(0).normalize().mul(random.nextFloat() * 2F).addY(random.nextFloat() * 0.1F);
            Vector3 to   = Vector3.random().setY(0).normalize().mul(random.nextFloat() * 2F).addY(random.nextFloat() * 0.1F);

            EffectHelper.of(EffectTemplatesAS.LIGHTNING)
                    .spawn(playerPos.clone().add(from))
                    .makeDefault(playerPos.clone().add(to))
                    .color(VFXColorFunction.WHITE)
                    .alpha1arg((fx, alphaIn, pTicks) -> {
                        if (shouldDoEffect(player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                            if (player.getXRot() > 40) {
                                return Mth.clamp(1F - (Math.abs(player.getXRot()) - 40F) / 20F, 0, 1F) * alphaIn;
                            }
                        }
                        return alphaIn;
                    });
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
        return "PatreonEffect - Nebula Cloud " + this.playerUUID.toString();
    }
}
