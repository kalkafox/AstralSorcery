/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
 * Class: KeyCullingAttack
 * Created by HellFirePvP
 * Date: 31.08.2019 / 16:51
 */
public class KeyCullingAttack extends KeyPerk {

    private static final float defaultCullHealth = 0.15F;
    private static final int defaultChargeCost = 250;

    public static final Config CONFIG = new Config("key.culling");

    public KeyCullingAttack(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide side, IEventBus bus) {
        super.attachListeners(side, bus);

        bus.addListener(EventPriority.LOW, this::onDamage);
    }

    private void onDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (source.getTrueSource() != null && source.getTrueSource() instanceof Player) {
            Player player = (Player) source.getTrueSource();
            LogicalSide side = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, side);
            if (side.isServer() && prog.getPerkData().hasPerkEffect(this)) {
                LivingEntity attacked = event.getEntityLiving();
                float actCull = PerkAttributeHelper.getOrCreateMap(player, side)
                        .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, CONFIG.cullHealth.get().floatValue());
                float lifePerc = attacked.getHealth() / attacked.getMaxHealth();
                if (lifePerc < actCull && AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCost.get(), false)) {
                    attacked.setHealth(0); // Try faithfully...
                    attacked.getDataManager().set(LivingEntity.HEALTH, 0F); // ... then set just it forcefully.
                }
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue cullHealth;
        private ModConfigSpec.IntValue chargeCost;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.cullHealth = cfgBuilder
                    .comment("Defines the percentage at how low the entities' health as to be to then cull the entity.")
                    .translation(translationKey("cullHealth"))
                    .defineInRange("cullHealth", defaultCullHealth, 0.05F, 0.5F);
            this.chargeCost = cfgBuilder
                    .comment("Defines the amount of starlight charge consumed per culling attempt.")
                    .translation(translationKey("chargeCost"))
                    .defineInRange("chargeCost", defaultChargeCost, 1, 500);
        }
    }
}
