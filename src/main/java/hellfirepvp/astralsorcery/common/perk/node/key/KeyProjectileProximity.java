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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyProjectileProximity
 * Created by HellFirePvP
 * Date: 31.08.2019 / 21:59
 */
public class KeyProjectileProximity extends KeyPerk {

    private static final float defaultCapDistance = 100;
    private static final float defaultMaxAdditionalMultiplier = 0.75F;

    public static final Config CONFIG = new Config("key.proj_proximity");

    public KeyProjectileProximity(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(EventPriority.HIGH, this::onProjDamage);
    }

    private void onProjDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            DamageSource source = event.getSource();
            if (source.getEntity() != null && source.getEntity() instanceof Player) {
                Player player = (Player) source.getEntity();
                LogicalSide direction = this.getSide(player);
                PlayerProgress prog = ResearchHelper.getProgress(player, direction);
                if (prog.getPerkData().hasPerkEffect(this)) {
                    float added = CONFIG.maxAdditionalMultiplier.get().floatValue();
                    added *= PerkAttributeHelper.getOrCreateMap(player, direction).getAttributeInstance(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT);

                    float capDstSq = CONFIG.capDistance.get().floatValue();
                    float dst = -(((float) (player.distanceToSqr(event.getEntity()))) - capDstSq);
                    dst /= capDstSq;
                    if (dst < 0) {
                        dst /= 10; //To make it drop a bit slower though... like. that damage reduction is... not fun :P
                    }
                    dst = Math.max(0, 1 + dst);
                    added *= dst;

                    float amt = event.getAmount();
                    amt *= Math.max(0, added); //Might become negative if too far away; prevent that :P
                    event.setAmount(amt);
                }
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue capDistance;
        private ModConfigSpec.DoubleValue maxAdditionalMultiplier;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.capDistance = cfgBuilder
                    .comment("Defines the distance at which no additional damage is awarded.")
                    .translation(translationKey("capDistance"))
                    .defineInRange("capDistance", defaultCapDistance, 4, 65_536);

            this.maxAdditionalMultiplier = cfgBuilder
                    .comment("Defines the maximum multiplier that can be reached if the distance when hitting something with projectiles is basically nothing.")
                    .translation(translationKey("maxAdditionalMultiplier"))
                    .defineInRange("maxAdditionalMultiplier", defaultMaxAdditionalMultiplier, 0.05F, 5.0F);
        }
    }
}
