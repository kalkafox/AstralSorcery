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
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.AnimalHelper;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ConstellationEffectEntityCollect;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectBootes
 * Created by HellFirePvP
 * Date: 23.11.2019 / 21:06
 */
public class CEffectBootes extends ConstellationEffectEntityCollect<LivingEntity> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("bootes");
    public static BootesConfig CONFIG = new BootesConfig();

    public CEffectBootes(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.bootes, LivingEntity.class, entity -> AnimalHelper.getHandler(entity) != null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        if (random.nextInt(3) == 0) {
            ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

            Vector3 playAt = new Vector3(pos).add(0.5, 0.5, 0.5).add(
                    random.nextFloat() * (prop.getSize() / 2F) * (random.nextBoolean() ? 1 : -1),
                    random.nextFloat() * (prop.getSize() / 4F),
                    random.nextFloat() * (prop.getSize() / 2F) * (random.nextBoolean() ? 1 : -1));
            Vector3 motion = Vector3.random().mul(0.015);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(playAt)
                    .setDeltaMovement(motion)
                    .color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_BOOTES))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.5F)
                    .setMaxAge(30 + random.nextInt(20));
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(playAt)
                    .setDeltaMovement(motion.clone().negate())
                    .color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_BOOTES))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.5F)
                    .setMaxAge(30 + random.nextInt(20));
        }
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        boolean didEffect = false;

        List<LivingEntity> entities = this.collectEntities(level, pos, properties);
        Collections.shuffle(entities);
        entities.subList(0, Math.min(25, entities.size()));
        for (LivingEntity entity : entities) {
            AnimalHelper.HerdableAnimal animal = AnimalHelper.getHandler(entity);
            if (animal == null) {
                continue;
            }

            if (properties.isCorrupted()) {
                entity.invulnerableTime = 0;
                entity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(EffectsAS.EFFECT_DROP_MODIFIER), 1000, 5));
                if (DamageUtil.hurt(entity, CommonProxy.DAMAGE_SOURCE_STELLAR, 5_000)) {
                    didEffect = true;
                }
                continue;
            }

            if (random.nextFloat() < CONFIG.herdingChance.get()) {
                didEffect = MiscUtils.executeWithChunk(level, entity.blockPosition(), didEffect, (didEffectFlag) -> {

                    List<ItemStack> rawDrops = EntityUtils.generateLoot(entity, random, CommonProxy.DAMAGE_SOURCE_STELLAR, null);
                    List<ItemStack> drops = new ArrayList<>();
                    rawDrops.forEach(drop -> {
                        for (int i = 0; i < drop.getCount(); i++) {
                            drops.add(ItemUtils.copyStackWithSize(drop, 1));
                        }
                    });
                    for (ItemStack drop : drops) {
                        if (random.nextFloat() < CONFIG.herdingLootChance.get() &&
                                ItemUtils.dropItemNaturally(level, entity.getX(), entity.getY(), entity.getZ(), drop) != null) {

                            didEffectFlag = true;
                        }
                    }
                    return didEffectFlag;
                }, false);
                sendConstellationPing(level, Vector3.atEntityCorner(entity));
            }
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

    private static class BootesConfig extends Config {

        private final double defaultHerdingChance = 0.05;
        private final double defaultHerdingLootChance = 0.01;

        public ModConfigSpec.DoubleValue herdingChance;
        public ModConfigSpec.DoubleValue herdingLootChance;

        public BootesConfig() {
            super("bootes", 12D, 4D);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.herdingChance = cfgBuilder
                    .comment("Set the chance that an registered animal will be considered for generating loot if it is close to the ritual.")
                    .translation(translationKey("herdingChance"))
                    .defineInRange("herdingChance", this.defaultHerdingChance, 0, 1.0);
            this.herdingLootChance = cfgBuilder
                    .comment("Set the chance that a drop that has been found on the entity's loot table is actually dropped.")
                    .translation(translationKey("herdingLootChance"))
                    .defineInRange("herdingLootChance", this.defaultHerdingLootChance, 0, 1.0);
        }
    }
}
