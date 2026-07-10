/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectFornax
 * Created by HellFirePvP
 * Date: 23.02.2020 / 08:15
 */
public class MantleEffectFornax extends MantleEffect {

    public static FornaxConfig CONFIG = new FornaxConfig();

    public MantleEffectFornax() {
        super(ConstellationsAS.fornax);
    }

    @Override
    protected void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);
        bus.addListener(this::onHurt);
    }

    private void onHurt(LivingIncomingDamageEvent event) {
        Level level = event.getEntity().getCommandSenderWorld();
        if (level.isClientSide()) {
            return;
        }

        LivingEntity attacked = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity) {
            if (attacked instanceof ServerPlayer && MiscUtils.isPlayerFakeMP((ServerPlayer) attacked)) {
                return;
            }

            if (attacker.isOnFire() && ItemMantle.getEffect((LivingEntity) attacker, ConstellationsAS.fornax) != null) {
                event.setAmount((float) (event.getAmount() * CONFIG.damageIncreaseInFire.get()));
            }
        }

        if (event.getSource().isFire() && ItemMantle.getEffect(attacked, ConstellationsAS.fornax) != null) {
            if (CONFIG.healPercentFromFireDamage.get() > 0) {
                attacked.heal((float) (event.getAmount() * CONFIG.healPercentFromFireDamage.get()));
            }

            event.setAmount((float) (event.getAmount() * CONFIG.damageReductionInFire.get()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(Player player) {
        super.tickClient(player);

        if (player.isOnFire()) {
            this.playCapeSparkles(player, 0.75F);
        } else {
            this.playCapeSparkles(player, 0.25F);
        }
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class FornaxConfig extends Config {

        private final double defaultDamageReductionInFire = 0.4F;
        private final double defaultDamageIncreaseInFire = 1.6F;
        private final double defaultHealPercentFromFireDamage = 0.6F;

        public ModConfigSpec.DoubleValue damageReductionInFire;
        public ModConfigSpec.DoubleValue damageIncreaseInFire;
        public ModConfigSpec.DoubleValue healPercentFromFireDamage;

        public FornaxConfig() {
            super("fornax");
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.damageReductionInFire = cfgBuilder
                    .comment("Sets the multiplier for how much damage you take from fire damage while wearing a fornax mantle.")
                    .translation(translationKey("damageReductionInFire"))
                    .defineInRange("damageReductionInFire", this.defaultDamageReductionInFire, 0.0, 1.0);
            this.damageIncreaseInFire = cfgBuilder
                    .comment("Sets the multiplier for how much more damage the player deals when ignited while wearing a fornax mantle.")
                    .translation(translationKey("damageIncreaseInFire"))
                    .defineInRange("damageIncreaseInFire", this.defaultDamageIncreaseInFire, 1.0, 3.0);
            this.healPercentFromFireDamage = cfgBuilder
                    .comment("Sets the multiplier for how much healing the player receives from the original damage when being hit by fire damage.")
                    .translation(translationKey("healPercentFromFireDamage"))
                    .defineInRange("healPercentFromFireDamage", this.defaultHealPercentFromFireDamage, 0.0, 3.0);
        }
    }
}
