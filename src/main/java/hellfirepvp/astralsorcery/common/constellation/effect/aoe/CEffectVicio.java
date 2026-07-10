/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectStatus;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperTemporaryFlight;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectVicio
 * Created by HellFirePvP
 * Date: 11.06.2019 / 20:46
 */
public class CEffectVicio extends ConstellationEffect implements ConstellationEffectStatus {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("vicio");
    public static VicioConfig CONFIG = new VicioConfig();

    public CEffectVicio(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.vicio);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        if (random.nextInt(3) == 0) {
            Vector3 r = new Vector3(
                    pos.getX() + random.nextFloat() * 4 * (random.nextBoolean() ? 1 : -1) + 0.5,
                    pos.getY() + random.nextFloat() * 2 + 0.5,
                    pos.getZ() + random.nextFloat() * 4 * (random.nextBoolean() ? 1 : -1) + 0.5);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(r)
                    .setDeltaMovement(Vector3.random().setY(0).mul(0.03F))
                    .setScaleMultiplier(0.45F)
                    .color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_VICIO))
                    .setGravityStrength(-0.002F)
                    .setMaxAge(40);
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(r)
                    .setDeltaMovement(new Vector3(0, random.nextFloat() * 0.03F, 0))
                    .setScaleMultiplier(0.45F)
                    .color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_VICIO))
                    .setGravityStrength(-0.002F)
                    .setMaxAge(40);
        }
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        return false;
    }

    @Override
    public boolean runStatusEffect(Level level, BlockPos pos, int mirrorAmount, ConstellationEffectProperties isDirty, @Nullable IMinorConstellation possibleTraitEffect) {
        boolean foundPlayer = false;
        double range = isDirty.getSize();
        if (isDirty.isCorrupted()) {
            List<LivingEntity> entities = level.getEntitiesWithinAABB(LivingEntity.class, BOX.offset(pos).grow(range));
            for (LivingEntity entity : entities) {
                if (entity instanceof ServerPlayer) {
                    ServerPlayer pl = (ServerPlayer) entity;
                    if (pl.gameMode.getGameType().isSurvival()) {
                        boolean prev = pl.getAbilities().allowFlying;
                        pl.getAbilities().allowFlying = false;
                        pl.getAbilities().flying = false;
                        if (prev) {
                            pl.onUpdateAbilities();
                        }
                    }
                    markPlayerAffected(pl);
                }
                foundPlayer = true;
                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 9));
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 9));
            }
        } else {
            List<ServerPlayer> entities = level.getEntitiesWithinAABB(ServerPlayer.class, BOX.offset(pos).grow(range));
            for (ServerPlayer pl : entities) {
                if (EventHelperTemporaryFlight.allowFlight(pl)) {
                    boolean prev = pl.getAbilities().allowFlying;
                    pl.getAbilities().allowFlying = true;
                    foundPlayer = true;
                    if (!prev) {
                        pl.onUpdateAbilities();
                    }
                }
                markPlayerAffected(pl);
            }
        }
        return foundPlayer;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class VicioConfig extends Config {

        private VicioConfig() {
            super("vicio", 24D, 16D);
        }
    }
}
