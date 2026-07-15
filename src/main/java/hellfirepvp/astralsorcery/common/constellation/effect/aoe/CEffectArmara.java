/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalArmara;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ConstellationEffectEntityCollect;
import hellfirepvp.astralsorcery.common.data.config.registry.TechnicalEntityRegistry;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperSpawnDeny;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedCrystalBase;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.WorldBlockPos;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import hellfirepvp.astralsorcery.common.util.tick.TickTokenMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectArmara
 * Created by HellFirePvP
 * Date: 28.07.2019 / 09:07
 */
public class CEffectArmara extends ConstellationEffectEntityCollect<LivingEntity> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("armara");
    public static ArmaraConfig CONFIG = new ArmaraConfig();

    private int rememberedTimeout = 0;

    public CEffectArmara(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.armara, LivingEntity.class, (e) -> e.isAlive() && TechnicalEntityRegistry.INSTANCE.canAffect(e));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        if (pedestal.getTicksExisted() % 20 == 0) {
            EffectHelper.spawnSource(new FXOrbitalArmara(new Vector3(pos).add(0.5, 0.5, 0.5))
                    .setOrbitRadius(0.8 + random.nextFloat() * 0.7)
                    .setOrbitAxis(Vector3.RotAxis.Y_AXIS)
                    .setTicksPerRotation(20 + random.nextInt(20)));
        }

        ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

        ItemStack socket = pedestal.getCurrentCrystal();
        if (!socket.isEmpty() && socket.getItem() instanceof ItemAttunedCrystalBase) {
            IMinorConstellation trait = ((ItemAttunedCrystalBase) socket.getItem()).getTraitConstellation(socket);
            if (trait != null) {
                trait.affectConstellationEffect(prop);
                if (prop.isCorrupted()) {
                    return;
                }
            }
        }

        List<Entity> projectiles = level.getEntitiesOfClass(Entity.class, BOX.move(pos).inflate(prop.getSize()));
        if (!projectiles.isEmpty()) {
            for (Entity e : projectiles) {
                if (e.isAlive() && TechnicalEntityRegistry.INSTANCE.canAffect(e)) {
                    if (e instanceof Projectile) {
                        double xRatio = (pos.getX() + 0.5) - e.getX();
                        double zRatio = (pos.getZ() + 0.5) - e.getZ();
                        float f = (float) Math.sqrt(xRatio * xRatio + zRatio * zRatio);
                        Vector3 motion = new Vector3(e.getDeltaMovement());
                        motion.mul(new Vector3(0.5, 1, 0.5));
                        motion.subtract(xRatio / f * 0.4, 0, zRatio / f * 0.4);
                        ((Projectile) e).shoot(motion.getX(), motion.getY(), motion.getZ(), 1.5F, 0F);
                    } else if (e instanceof Mob) {
                        ((LivingEntity) e).knockback(0.4F, (pos.getX() + 0.5) - e.getX(), (pos.getZ() + 0.5) - e.getZ());
                    }
                }
            }
        }
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        int toAdd = 2 + random.nextInt(5);
        WorldBlockPos at = WorldBlockPos.wrapServer(level, pos);
        TickTokenMap.SimpleTickToken<Double> accessToken = EventHelperSpawnDeny.spawnDenyRegions.get(at);
        if (accessToken != null) {
            int next = accessToken.getRemainingTimeout() + toAdd;
            if (next > 400) next = 400;
            accessToken.setIdleTimeout(next);
            rememberedTimeout = next;
        } else {
            rememberedTimeout = Math.min(400, rememberedTimeout + toAdd);
            EventHelperSpawnDeny.spawnDenyRegions.put(at, new TickTokenMap.SimpleTickToken<>(properties.getSize(), rememberedTimeout));
        }

        if (!properties.isCorrupted()) {
            List<Entity> projectiles = level.getEntitiesOfClass(Entity.class, BOX.move(pos).inflate(properties.getSize()));
            if (!projectiles.isEmpty()) {
                for (Entity e : projectiles) {
                    if (e.isAlive() && TechnicalEntityRegistry.INSTANCE.canAffect(e)) {
                        if (e instanceof Projectile) {
                            double xRatio = (pos.getX() + 0.5) - e.getX();
                            double zRatio = (pos.getZ() + 0.5) - e.getZ();
                            float f = (float) Math.sqrt(xRatio * xRatio + zRatio * zRatio);
                            Vector3 motion = new Vector3(e.getDeltaMovement());
                            motion.mul(new Vector3(0.5, 1, 0.5));
                            motion.subtract(xRatio / f * 0.4, 0, zRatio / f * 0.4);
                            ((Projectile) e).shoot(motion.getX(), motion.getY(), motion.getZ(), 1.5F, 0F);
                        } else if (e instanceof Mob) {
                            ((LivingEntity) e).knockback(0.4F, (pos.getX() + 0.5) - e.getX(), (pos.getZ() + 0.5) - e.getZ());
                        }
                    }
                }
            }
        }

        int potionAmplifier = CONFIG.potionAmplifier.get();
        List<LivingEntity> entities = this.collectEntities(level, pos, properties);
        for (LivingEntity entity : entities) {
            if (entity.isAlive() && (entity instanceof Mob || entity instanceof Player)) {
                if (properties.isCorrupted()) {
                    if (entity instanceof Player) {
                        continue;
                    }

                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, potionAmplifier + 4));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.REGENERATION, 100, potionAmplifier + 4));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, potionAmplifier + 2));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, potionAmplifier + 4));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.WATER_BREATHING, 100, potionAmplifier + 4));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.DIG_SPEED, 100, potionAmplifier + 4));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(EffectsAS.EFFECT_DROP_MODIFIER), 100, 5));
                } else {
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, Math.min(potionAmplifier, 3), true, true));
                    if (entity instanceof Player) {
                        EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.ABSORPTION, 30, potionAmplifier, true, false));
                    }
                }
                if (entity instanceof Player) {
                    markPlayerAffected((Player) entity);
                }
            }
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
    public void readFromNBT(CompoundTag cmp) {
        super.readFromNBT(cmp);

        this.rememberedTimeout = cmp.getInt("rememberedTimeout");
    }

    @Override
    public void save(CompoundTag cmp) {
        super.save(cmp);

        cmp.putInt("rememberedTimeout", this.rememberedTimeout);
    }

    private static class ArmaraConfig extends Config {

        private final int defaultPotionAmplifier = 1;

        public ModConfigSpec.IntValue potionAmplifier;

        public ArmaraConfig() {
            super("armara", 16, 2);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.potionAmplifier = cfgBuilder
                    .comment("Set the amplifier for the potion effects this ritual provides.")
                    .translation(translationKey("potionAmplifier"))
                    .defineInRange("potionAmplifier", this.defaultPotionAmplifier, 0, 10);
        }
    }
}
