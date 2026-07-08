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
    public void playClientEffect(Level world, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        if (rand.nextInt(3) == 0) {
            Vector3 r = new Vector3(
                    pos.getX() + rand.nextFloat() * 4 * (rand.nextBoolean() ? 1 : -1) + 0.5,
                    pos.getY() + rand.nextFloat() * 2 + 0.5,
                    pos.getZ() + rand.nextFloat() * 4 * (rand.nextBoolean() ? 1 : -1) + 0.5);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(r)
                    .setMotion(Vector3.random().setY(0).multiply(0.03F))
                    .setScaleMultiplier(0.45F)
                    .color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_VICIO))
                    .setGravityStrength(-0.002F)
                    .setMaxAge(40);
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(r)
                    .setMotion(new Vector3(0, rand.nextFloat() * 0.03F, 0))
                    .setScaleMultiplier(0.45F)
                    .color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_VICIO))
                    .setGravityStrength(-0.002F)
                    .setMaxAge(40);
        }
    }

    @Override
    public boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        return false;
    }

    @Override
    public boolean runStatusEffect(Level world, BlockPos pos, int mirrorAmount, ConstellationEffectProperties modified, @Nullable IMinorConstellation possibleTraitEffect) {
        boolean foundPlayer = false;
        double range = modified.getSize();
        if (modified.isCorrupted()) {
            List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, BOX.offset(pos).grow(range));
            for (LivingEntity entity : entities) {
                if (entity instanceof ServerPlayer) {
                    ServerPlayer pl = (ServerPlayer) entity;
                    if (pl.interactionManager.getGameType().isSurvivalOrAdventure()) {
                        boolean prev = pl.abilities.allowFlying;
                        pl.abilities.allowFlying = false;
                        pl.abilities.isFlying = false;
                        if (prev) {
                            pl.sendPlayerAbilities();
                        }
                    }
                    markPlayerAffected(pl);
                }
                foundPlayer = true;
                entity.addPotionEffect(new MobEffectInstance(Effects.SLOWNESS, 200, 9));
                entity.addPotionEffect(new MobEffectInstance(Effects.MINING_FATIGUE, 200, 9));
            }
        } else {
            List<ServerPlayer> entities = world.getEntitiesWithinAABB(ServerPlayerEntity.class, BOX.offset(pos).grow(range));
            for (ServerPlayer pl : entities) {
                if (EventHelperTemporaryFlight.allowFlight(pl)) {
                    boolean prev = pl.abilities.allowFlying;
                    pl.abilities.allowFlying = true;
                    foundPlayer = true;
                    if (!prev) {
                        pl.sendPlayerAbilities();
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
