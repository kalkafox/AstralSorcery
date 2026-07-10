/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectArmara
 * Created by HellFirePvP
 * Date: 29.03.2020 / 13:53
 */
public class MantleEffectArmara extends MantleEffect {

    public static ArmaraConfig CONFIG = new ArmaraConfig();

    public MantleEffectArmara() {
        super(ConstellationsAS.armara);
    }

    @Override
    protected void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);

        bus.addListener(EventPriority.HIGH, this::onHurt);
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    protected void tickServer(Player player) {
        super.tickServer(player);

        if (getCurrentImmunityStacks(player) >= CONFIG.immunityStacks.get()) {
            setCurrentImmunityRechargeTick(player, CONFIG.immunityRechargeTicks.get());
            return;
        }
        int tick = getCurrentImmunityRechargeTick(player);
        tick--;
        if (tick <= 0) {
            if (AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerStack.get())) {
                setCurrentImmunityRechargeTick(player, CONFIG.immunityRechargeTicks.get());
                setCurrentImmunityStacks(player, getCurrentImmunityStacks(player) + 1);
                AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerStack.get(), false);
            }
        } else {
            setCurrentImmunityRechargeTick(player, tick);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(Player player) {
        super.tickClient(player);

        this.playCapeSparkles(player, 0.15F);

        Vector3 at = Vector3.atEntityCorner(player);
        at.addY(player.getBbHeight() / 3F * 2F);

        Vector3 lookVec = new Vector3(player.getLookAngle()).normalize();

        int stacks = getCurrentImmunityStacks(player);
        if (stacks > 0) {
            Random sRand = new Random(player.getUUID().hashCode());
            for (int i = 0; i < stacks; i++) {
                Vector3 axis = Vector3.random(sRand);
                axis.setX(axis.getX() * 0.35F);
                axis.setZ(axis.getZ() * 0.35F);
                Vector3 perpEffect = axis.clone().perpendicular();

                float scale = random.nextFloat() * 0.2F + 0.2F;
                int ticksPerCircle = 80 + sRand.nextInt(50);
                int tick = (player.tickCount) % ticksPerCircle;

                Vector3 anglePlayer = perpEffect.normalize()
                        .mirror(Math.toRadians(360 * ((float) (tick) / (float) (ticksPerCircle))), axis)
                        .normalize();
                Vector3 pos = anglePlayer.clone().mul(sRand.nextFloat() * 0.4F + 0.9F).add(at);

                float alpha = 0.8F;
                if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                    float deg = (float) Math.toDegrees(lookVec.angle(anglePlayer));
                    if (deg < 70F) {
                        float tansparentDegree = 40F;
                        alpha *= Mth.clamp((deg - tansparentDegree) / (80F - tansparentDegree), 0F, 1F);
                    }
                }

                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setAlphaMultiplier(alpha)
                        .color(VFXColorFunction.constant(ColorsAS.MANTLE_ARMARA_STACKS))
                        .setScaleMultiplier(scale)
                        .setMaxAge(20 + random.nextInt(20));

                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setAlphaMultiplier(alpha)
                        .color(VFXColorFunction.WHITE)
                        .setScaleMultiplier(scale * 0.4F)
                        .setMaxAge(10 + random.nextInt(10));
            }
        }
    }

    private void onHurt(LivingIncomingDamageEvent event) {
        Level level = event.getEntity().getCommandSenderWorld();
        LivingEntity hurt = event.getEntity();

        if (level.isClientSide()) {
            return;
        }
        MantleEffectArmara armara = ItemMantle.getEffect(hurt, ConstellationsAS.armara);
        if (armara != null && this.shouldPreventDamage(hurt, event.getSource(), false)) {
            event.setCanceled(true);
        }
    }

    private boolean shouldPreventDamage(LivingEntity hurt, DamageSource source, boolean simulate) {
        if (source.isBypassInvul()) {
            return false;
        }
        int stacks = getCurrentImmunityStacks(hurt);
        if (stacks <= 0) {
            return false;
        }
        if (!simulate) {
            stacks--;
            setCurrentImmunityStacks(hurt, stacks);
        }
        return true;
    }

    private int getCurrentImmunityRechargeTick(LivingEntity entity) {
        return this.getData(entity).getInt("ITick");
    }

    private void setCurrentImmunityRechargeTick(LivingEntity entity, int tick) {
        this.getData(entity).putInt("ITick", tick);
    }

    private int getCurrentImmunityStacks(LivingEntity entity) {
        return this.getData(entity).getInt("IStacks");
    }

    private void setCurrentImmunityStacks(LivingEntity entity, int stacks) {
        this.getData(entity).putInt("IStacks", stacks);
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class ArmaraConfig extends Config {

        private final int defaultImmunityStacks = 3;
        private final int defaultImmunityRechargeTicks = 300;

        private final int defaultChargeCostPerStack = 750;

        public ModConfigSpec.IntValue immunityStacks;
        public ModConfigSpec.IntValue immunityRechargeTicks;

        public ModConfigSpec.IntValue chargeCostPerStack;

        public ArmaraConfig() {
            super("armara");
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.immunityStacks = cfgBuilder
                    .comment("Set the max amount of immunity stacks.")
                    .translation(translationKey("immunityStacks"))
                    .defineInRange("immunityStacks", this.defaultImmunityStacks, 0, 10);
            this.immunityRechargeTicks = cfgBuilder
                    .comment("Sets the amount of ticks between immunity stack recharges.")
                    .translation(translationKey("immunityRechargeTicks"))
                    .defineInRange("immunityRechargeTicks", this.defaultImmunityRechargeTicks, 20, 1_000_000);

            this.chargeCostPerStack = cfgBuilder
                    .comment("Set the amount alignment charge consumed per created immunity stack")
                    .translation(translationKey("chargeCostPerStack"))
                    .defineInRange("chargeCostPerStack", this.defaultChargeCostPerStack, 0, 1000);
        }
    }
}
