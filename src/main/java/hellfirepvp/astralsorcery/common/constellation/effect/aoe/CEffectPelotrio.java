/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalPelotrio;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ListEntries;
import hellfirepvp.astralsorcery.common.data.config.registry.EntityTransmutationRegistry;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectPelotrio
 * Created by HellFirePvP
 * Date: 01.02.2020 / 14:44
 */
public class CEffectPelotrio extends CEffectAbstractList<ListEntries.EntitySpawnEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("pelotrio");
    private static final AABB PROXIMITY_BOX = new AABB(0, 0, 0, 0, 0, 0);

    public static PelotrioConfig CONFIG = new PelotrioConfig();

    public CEffectPelotrio(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.pelotrio, CONFIG.maxAmount.get(), (world, pos, state) -> {
            if (!(world instanceof ServerLevel)) {
                return false;
            }
            pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos).up();
            return ListEntries.EntitySpawnEntry.createEntry((ServerLevel) world, pos, SpawnReason.SPAWNER) != null;
        });
    }

    @Nullable
    @Override
    public ListEntries.EntitySpawnEntry recreateElement(CompoundTag tag, BlockPos pos) {
        return null;
    }

    @Nullable
    @Override
    public ListEntries.EntitySpawnEntry createElement(Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel)) {
            return null;
        }
        pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos).up();
        return ListEntries.EntitySpawnEntry.createEntry((ServerLevel) world, pos, SpawnReason.SPAWNER);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level world, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

        if (rand.nextFloat() < 0.2F) {
            Vector3 at = Vector3.random().normalize().multiply(rand.nextFloat() * prop.getSize()).add(pos).add(0.5, 0.5, 0.5);

            EffectHelper.spawnSource(new FXOrbitalPelotrio(at)
                    .setOrbitAxis(Vector3.random())
                    .setOrbitRadius(0.8 + rand.nextFloat() * 0.7)
                    .setTicksPerRotation(20 + rand.nextInt(20)));
        }
    }

    @Override
    public boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        if (!(world instanceof ServerLevel)) {
            return false;
        }

        boolean update = false;

        List<LivingEntity> nearbyEntities = world.getEntitiesWithinAABB(LivingEntity.class, PROXIMITY_BOX.offset(pos).grow(properties.getSize()));

        if (properties.isCorrupted()) {
            for (LivingEntity entity : nearbyEntities) {
                if (entity != null && entity.isAlive() && rand.nextInt(300) == 0) {
                    LivingEntity transmuted = EntityTransmutationRegistry.INSTANCE.transmuteEntity((ServerLevel) world, entity);
                    if (transmuted != null) {
                        transmuted.addPotionEffect(new MobEffectInstance(EffectsAS.EFFECT_DROP_MODIFIER, Integer.MAX_VALUE, 1));
                        AstralSorcery.getProxy().scheduleDelayed(() -> world.addEntity(transmuted));
                        update = true;
                    }
                }
            }
            return update;
        }

        ListEntries.EntitySpawnEntry entry = this.getRandomElementChanced();
        if (entry != null) {
            int count = entry.getCounter();
            count++;
            entry.setCounter(count);
            sendConstellationPing(world, new Vector3(entry.getPos()).add(0.5, 0.5, 0.5));
            if (count >= 10) {
                entry.spawn((ServerLevel) world, SpawnReason.SPAWNER);
                removeElement(entry);
            }

            update = true;
        }

        if (nearbyEntities.size() > CONFIG.proximityAmount.get()) {
            return update; //Flood prevention
        }

        if (rand.nextFloat() < CONFIG.spawnChance.get()) {
            if (findNewPosition(world, pos, properties).left().isPresent()) {
                update = true;
            }
        }
        return update;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class PelotrioConfig extends CountConfig {

        private final double defaultSpawnChance = 0.05D;
        private final int defaultProximityAmount = 24;

        public ModConfigSpec.DoubleValue spawnChance;
        public ModConfigSpec.IntValue proximityAmount;

        public PelotrioConfig() {
            super("pelotrio", 12D, 0D, 5);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.spawnChance = cfgBuilder
                    .comment("Defines the per-tick chance that a new position for a entity-spawn will be searched for.")
                    .translation(translationKey("spawnChance"))
                    .defineInRange("spawnChance", this.defaultSpawnChance, 0, 1);

            this.proximityAmount = cfgBuilder
                    .comment("Defines the threshold at which the ritual will stop spawning mobs. If there are more or equal amount of mobs near this ritual, the ritual will not spawn more mobs. Mainly to reduce potential server lag.")
                    .translation(translationKey("proximityAmount"))
                    .defineInRange("proximityAmount", this.defaultProximityAmount, 0, 256);
        }
    }
}
