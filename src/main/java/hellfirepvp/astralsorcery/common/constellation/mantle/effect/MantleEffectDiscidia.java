/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectDiscidia
 * Created by HellFirePvP
 * Date: 29.03.2020 / 15:16
 */
public class MantleEffectDiscidia extends MantleEffect {

    public static DiscidiaConfig CONFIG = new DiscidiaConfig();

    public MantleEffectDiscidia() {
        super(ConstellationsAS.discidia);
    }

    @Override
    protected void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);

        bus.addListener(this::onAttack);
        bus.addListener(EventPriority.HIGHEST, this::onHurt);
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(Player player) {
        super.tickClient(player);

        float effChance = 0.1F;
        if (this.getLastAttackDamage(player) > 0) {
            effChance = 0.2F;
        }
        this.playCapeSparkles(player, effChance);
    }

    private void onAttack(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntityLiving();
        Level level = attacked.getCommandSenderWorld();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        if (level.isClientSide()) {
            return;
        }
        if (attacker instanceof Player) {
            if (attacked instanceof ServerPlayer && MiscUtils.isPlayerFakeMP((ServerPlayer) attacked)) {
                return;
            }
            Player player = (Player) attacker;

            MantleEffectDiscidia eff = ItemMantle.getEffect(player, ConstellationsAS.discidia);
            if (eff != null) {
                EventFlags.MANTLE_DISCIDIA_ADDED.executeWithFlag(() -> {
                    float added = this.getLastAttackDamage(player);

                    if (added > 0.1F && AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerAttack.get())) {
                        DamageUtil.shotgunAttack(attacked, entity -> DamageUtil.hurt(entity, CommonProxy.DAMAGE_SOURCE_STELLAR, added / 2F));
                        DamageUtil.shotgunAttack(attacked, entity -> DamageUtil.hurt(entity, DamageSource.causePlayerDamage(player), added / 2F, player));

                        AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerAttack.get(), false);
                    }
                });
            }
        }
    }

    private void onHurt(LivingIncomingDamageEvent event) {
        Level level = event.getEntity().getCommandSenderWorld();
        LivingEntity hurt = event.getEntityLiving();

        if (level.isClientSide()) {
            return;
        }
        MantleEffectDiscidia armara = ItemMantle.getEffect(hurt, ConstellationsAS.discidia);
        if (armara != null) {
            this.writeLastAttackDamage(hurt, event.getAmount());
        }
    }

    public void writeLastAttackDamage(LivingEntity entity, float dmgIn) {
        this.getData(entity).putFloat("lastAttack", dmgIn);
    }

    public float getLastAttackDamage(LivingEntity entity) {
        return this.getData(entity).getFloat("lastAttack") * CONFIG.damageMultiplier.get().floatValue();
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class DiscidiaConfig extends Config {

        private final double defaultDamageMultiplier = 1.5F;

        private final int defaultChargeCostPerAttack = 100;

        public ModConfigSpec.DoubleValue damageMultiplier;

        public ModConfigSpec.IntValue chargeCostPerAttack;

        public DiscidiaConfig() {
            super("discidia");
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.damageMultiplier = cfgBuilder
                    .comment("Sets the multiplier for how much of the received damage is converted into additional damage.")
                    .translation(translationKey("damageMultiplier"))
                    .defineInRange("damageMultiplier", this.defaultDamageMultiplier, 0.0, 100.0);

            this.chargeCostPerAttack = cfgBuilder
                    .comment("Set the amount alignment charge consumed per attack enhanced by the mantle")
                    .translation(translationKey("chargeCostPerAttack"))
                    .defineInRange("chargeCostPerAttack", this.defaultChargeCostPerAttack, 0, 1000);
        }
    }
}
