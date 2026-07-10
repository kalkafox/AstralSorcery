/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalLucerna;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectStatus;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperSpawnDeny;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.WorldBlockPos;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.tick.TickTokenMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectLucerna
 * Created by HellFirePvP
 * Date: 01.02.2020 / 10:11
 */
public class CEffectLucerna extends ConstellationEffect implements ConstellationEffectStatus {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("lucerna");
    public static LucernaConfig CONFIG = new LucernaConfig();

    private int rememberedTimeout = 0;

    public CEffectLucerna(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.lucerna);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        if (ClientScheduler.getClientTick() % 20 == 0) {
            EffectHelper.spawnSource(new FXOrbitalLucerna(new Vector3(pos).add(0.5, 0.5, 0.5))
                    .setOrbitAxis(Vector3.RotAxis.Y_AXIS)
                    .setOrbitRadius(0.8 + random.nextFloat() * 0.7)
                    .setTicksPerRotation(20 + random.nextInt(20)));
        }
    }

    @Override
    public boolean runStatusEffect(Level level, BlockPos pos, int mirrorAmount, ConstellationEffectProperties isDirty, @Nullable IMinorConstellation possibleTraitEffect) {
        if (isDirty.isCorrupted()) {
            if (level instanceof ServerLevel && DayTimeHelper.isNight(level) && random.nextBoolean()) {
                SkyHandler.getInstance().revertWorldTimeTick((ServerLevel) level);
            }
            return true;
        }

        WorldBlockPos at = WorldBlockPos.wrapServer(level, pos);
        TickTokenMap.SimpleTickToken<Double> accessToken = EventHelperSpawnDeny.spawnDenyRegions.get(at);
        if(accessToken != null && Math.abs(accessToken.getValue() - isDirty.getSize()) < 1E-3) {
            int next = accessToken.getRemainingTimeout() + 80;
            if(next > 400) next = 400;
            accessToken.setIdleTimeout(next);
            rememberedTimeout = next;
        } else {
            if(accessToken != null) {
                accessToken.setIdleTimeout(0);
            }
            rememberedTimeout = Math.min(400, rememberedTimeout + 80);
            EventHelperSpawnDeny.spawnDenyRegions.put(at, new TickTokenMap.SimpleTickToken<>(isDirty.getSize(), rememberedTimeout));
        }
        return true;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        return false;
    }

    @Override
    public void readFromNBT(CompoundTag cmp) {
        super.readFromNBT(cmp);

        this.rememberedTimeout = cmp.getInt("rememberedTimeout");
    }

    @Override
    public void save(CompoundTag cmp) {
        super.save(cmp);

        cmp.putInt("rememberedTimeout", this.rememberedTimeout);
    }

    private static class LucernaConfig extends Config {

        public LucernaConfig() {
            super("lucerna", 32D, 64D);
        }
    }
}
