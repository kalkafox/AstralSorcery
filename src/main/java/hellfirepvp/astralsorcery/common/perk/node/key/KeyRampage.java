/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyRampage
 * Created by HellFirePvP
 * Date: 31.08.2019 / 22:07
 */
public class KeyRampage extends KeyPerk {

    private static final float defaultRampageChance = 1F;
    private static final int defaultRampageDuration = 100;

    public static final Config CONFIG = new Config("key.rampage");

    public KeyRampage(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(EventPriority.LOWEST, this::onEntityDeath);
    }

    private void onEntityDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() != null && source.getEntity() instanceof Player) {
            Player player = (Player) source.getEntity();
            LogicalSide direction = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, direction);
            if (direction.isServer() && prog.getPerkData().hasPerkEffect(this)) {
                float ch = CONFIG.rampageChance.get().floatValue();
                ch = PerkAttributeHelper.getOrCreateMap(player, direction)
                        .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, ch);
                if (random.nextFloat() < ch) {

                    int dur = CONFIG.rampageDuration.get();
                    dur = Math.round(PerkAttributeHelper.getOrCreateMap(player, direction)
                            .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_RAMPAGE_DURATION, dur));
                    dur = Math.round(PerkAttributeHelper.getOrCreateMap(player, direction)
                            .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, dur));
                    if (dur > 0) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, dur, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, dur, 1, false, false, true));
                    }
                }
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue rampageChance;
        private ModConfigSpec.IntValue rampageDuration;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.rampageChance = cfgBuilder
                    .comment("Defines the chance to gain rampage buffs when killing a mob")
                    .translation(translationKey("rampageChance"))
                    .defineInRange("rampageChance", defaultRampageChance, 0.05F, 1F);

            this.rampageDuration = cfgBuilder
                    .comment("Defines the duration of the rampage in ticks")
                    .translation(translationKey("rampageDuration"))
                    .defineInRange("rampageDuration", defaultRampageDuration, 10, 100_000);
        }
    }
}
