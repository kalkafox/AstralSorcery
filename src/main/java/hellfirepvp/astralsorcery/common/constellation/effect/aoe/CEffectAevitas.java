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
import hellfirepvp.astralsorcery.common.auxiliary.CropHelper;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectAevitas
 * Created by HellFirePvP
 * Date: 27.07.2019 / 21:54
 */
public class CEffectAevitas extends CEffectAbstractList<CropHelper.GrowablePlant> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("aevitas");
    public static AevitasConfig CONFIG = new AevitasConfig();

    public CEffectAevitas(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.aevitas, CONFIG.maxAmount.get(), (level, pos, state) -> CropHelper.wrapPlant(level, pos) != null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        if (random.nextBoolean()) {
            ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(
                            pos.getX() + random.nextFloat() * (prop.getSize() / 2F) * (random.nextBoolean() ? 1 : -1) + 0.5,
                            pos.getY() + random.nextFloat() * (prop.getSize() / 4F) + 0.5,
                            pos.getZ() + random.nextFloat() * (prop.getSize() / 2F) * (random.nextBoolean() ? 1 : -1) + 0.5))
                    .setGravityStrength(-0.005F)
                    .setScaleMultiplier(0.45F)
                    .color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_AEVITAS))
                    .setMaxAge(35);
        }
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        boolean changed = false;
        CropHelper.GrowablePlant plant = getRandomElementChanced();
        if (plant != null) {
            changed = MiscUtils.executeWithChunk(level, plant.getBlockPos(), changed, (changedFlag) -> {
                if (properties.isCorrupted()) {
                    if (level instanceof ServerLevel) {
                        CropHelper.HarvestablePlant harvestablePlant = CropHelper.wrapHarvestablePlant(level, plant.getBlockPos());
                        if (harvestablePlant != null) {
                            NonNullList<ItemStack> drops = harvestablePlant.harvestDropsAndReplant((ServerLevel) level, random, 1);
                            drops.forEach(drop -> ItemUtils.dropItem(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
                            changedFlag = !drops.isEmpty();
                        } else if (BlockUtils.breakBlockWithoutPlayer(((ServerLevel) level), plant.getBlockPos())) {
                            changedFlag = true;
                        }
                    } else {
                        if (level.removeBlock(plant.getBlockPos(), false)) {
                            changedFlag = true;
                        }
                    }
                } else {
                    if (!plant.isValid(level)) {
                        removeElement(plant.getBlockPos());
                        changedFlag = true;
                    } else {
                        if (plant.tryGrow(level, random)) {
                            PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.CROP_GROWTH)
                                    .addData(buf -> ByteBufUtils.writeVector(buf, new Vector3(plant.getBlockPos())));
                            PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(level, plant.getBlockPos(), 16));
                            changedFlag = true;
                        }
                    }
                }
                return changedFlag;
            }, false);
        }

        if (this.findNewPosition(level, pos, properties)
                .ifRight(attemptedPos -> sendConstellationPing(level, new Vector3(attemptedPos).add(0.5, 0.5, 0.5)))
                .left().isPresent()) changed = true;
        if (this.findNewPosition(level, pos, properties)
                .ifRight(attemptedPos -> sendConstellationPing(level, new Vector3(attemptedPos).add(0.5, 0.5, 0.5)))
                .left().isPresent()) changed = true;

        int amplifier = CONFIG.potionAmplifier.get();
        List<LivingEntity> entities = level.getEntitiesWithinAABB(LivingEntity.class, BOX.offset(pos).grow(properties.getSize()));
        for (LivingEntity entity : entities) {
            if (entity.isAlive()) {
                if (properties.isCorrupted()) {
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(EffectsAS.EFFECT_BLEED, 120, amplifier * 2));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.WEAKNESS, 120, amplifier * 3));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.HUNGER, 120, amplifier * 4));
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120, amplifier * 2));
                } else {
                    EntityUtils.applyPotionEffectAtHalf(entity, new MobEffectInstance(MobEffects.REGENERATION, 120, amplifier));
                }
                if (entity instanceof Player) {
                    markPlayerAffected((Player) entity);
                }
            }
        }

        return changed;
    }

    @Nullable
    @Override
    public CropHelper.GrowablePlant recreateElement(CompoundTag tag, BlockPos pos) {
        return CropHelper.fromNBT(tag, pos);
    }

    @Nullable
    @Override
    public CropHelper.GrowablePlant createElement(Level level, BlockPos pos) {
        return CropHelper.wrapPlant(level, pos);
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    @OnlyIn(Dist.CLIENT)
    public static void showBreakingParticles(PktPlayEffect event) {
        Vector3 at = ByteBufUtils.readVector(event.getExtraData());
        for (int i = 0; i < 8; i++) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at.clone().add(random.nextFloat(), 0.2, random.nextFloat()))
                    .setDeltaMovement(new Vector3(0, 0.005 + random.nextFloat() * 0.01, 0))
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.constant(Color.GREEN));
        }
    }

    private static class AevitasConfig extends CountConfig {

        private final int defaultPotionAmplifier = 1;

        public ModConfigSpec.IntValue potionAmplifier;

        public AevitasConfig() {
            super("aevitas", 10D, 4D, 200);
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
