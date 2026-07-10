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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyDamageEffects
 * Created by HellFirePvP
 * Date: 31.08.2019 / 17:26
 */
public class KeyDamageEffects extends KeyPerk {

    private static final float defaultApplicationChance = 0.08F;

    public static final Config CONFIG = new Config("key.damage_effects");

    public KeyDamageEffects(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(EventPriority.LOWEST, this::onDamageResult);
    }

    private void onDamageResult(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() != null && source.getEntity() instanceof Player) {
            Player player = (Player) source.getEntity();
            LogicalSide direction = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, direction);
            if (prog.getPerkData().hasPerkEffect(this)) {
                LivingEntity attacked = event.getEntity();
                float chance = PerkAttributeHelper.getOrCreateMap(player, direction)
                        .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, CONFIG.applicationChance.get().floatValue());
                if (random.nextFloat() < chance) {
                    switch (random.nextInt(3)) {
                        case 0:
                            attacked.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1, false, false, true));
                            break;
                        case 1:
                            attacked.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1, false, false, true));
                            break;
                        case 2:
                            attacked.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1, false, false, true));
                            attacked.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, false, false, true));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue applicationChance;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.applicationChance = cfgBuilder
                    .comment("Defines the chance per hit to apply additional effects.")
                    .translation(translationKey("applicationChance"))
                    .defineInRange("applicationChance", defaultApplicationChance, 0.01F, 0.2F);
        }
    }
}
