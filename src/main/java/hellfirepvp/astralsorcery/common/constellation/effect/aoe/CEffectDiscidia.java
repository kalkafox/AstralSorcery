/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ConstellationEffectEntityCollect;
import hellfirepvp.astralsorcery.common.data.config.registry.TechnicalEntityRegistry;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.DamageSourceUtil;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
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
 * Class: CEffectDiscidia
 * Created by HellFirePvP
 * Date: 23.11.2019 / 21:35
 */
public class CEffectDiscidia extends ConstellationEffectEntityCollect<LivingEntity> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("discidia");
    public static DiscidiaConfig CONFIG = new DiscidiaConfig();

    public CEffectDiscidia(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.discidia, LivingEntity.class,
                entity -> entity.isAlive() && TechnicalEntityRegistry.INSTANCE.canAffect(entity));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        Vector3 playAt = new Vector3(pos).add(0.5, 0.5, 0.5);
        if (pos.equals(pedestal.getBlockPos())) {
            playAt.add(
                    random.nextFloat() * 0.1 * (random.nextBoolean() ? 1 : -1),
                    random.nextFloat() * 5,
                    random.nextFloat() * 0.1 * (random.nextBoolean() ? 1 : -1));
        }
        Vector3 motion = Vector3.random().setY(0).mul(0.05);

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(playAt)
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .motion(VFXMotionController.decelerate(() -> motion))
                .color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_DISCIDIA))
                .setScaleMultiplier(0.4F)
                .setMaxAge(35);
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        boolean didEffect = false;

        float damage = CONFIG.damage.get().floatValue(); //Randomize?..
        Player owner = this.getOwningPlayerInWorld(level, pos);
        DamageSource src = owner == null ? CommonProxy.DAMAGE_SOURCE_STELLAR :
                DamageSourceUtil.withEntityDirect(CommonProxy.DAMAGE_SOURCE_STELLAR, owner);
        List<LivingEntity> entities = this.collectEntities(level, pos, properties);
        for (LivingEntity entity : entities) {
            if (random.nextInt(6) != 0) {
                continue;
            }
            if (properties.isCorrupted() && entity instanceof Mob && entity.getType().getCategory() == MobCategory.MONSTER) {
                entity.heal(damage);
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 1));
            } else {
                if (entity instanceof Player) {
                    continue;
                }
                if (entity.equals(owner)) {
                    continue;
                }
                DamageUtil.shotgunAttack(entity, e -> DamageUtil.hurt(entity, src, damage));
            }
            if (entity instanceof Player) {
                markPlayerAffected((Player) entity);
            }

            didEffect = true;
        }

        return didEffect;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class DiscidiaConfig extends Config {

        private final double defaultDamage = 3D;

        public ModConfigSpec.DoubleValue damage;

        public DiscidiaConfig() {
            super("discidia", 10D, 2D);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.damage = cfgBuilder
                    .comment("Defines the max. possible damage dealt per damage tick.")
                    .translation(translationKey("damage"))
                    .defineInRange("damage", this.defaultDamage, 0.1, 128.0D);
        }
    }
}
