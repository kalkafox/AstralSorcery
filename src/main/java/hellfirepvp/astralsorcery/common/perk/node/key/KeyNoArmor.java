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
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyNoArmor
 * Created by HellFirePvP
 * Date: 31.08.2019 / 21:42
 */
public class KeyNoArmor extends KeyPerk {

    private static final float defaultDamageTakenMultiplier = 0.7F;

    public static final Config CONFIG = new Config("key.no_armor");

    public KeyNoArmor(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(this::onLivingHurt);
    }

    private void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        LogicalSide direction = this.getSide(player);
        PlayerProgress prog = ResearchHelper.getProgress(player, direction);
        if (prog.getPerkData().hasPerkEffect(this)) {
            int eq = 0;
            for (ItemStack stack : player.getArmorSlots()) {
                if (!stack.isEmpty()) {
                    eq++;
                }
            }
            if (eq < 2) {
                float multiplier = CONFIG.damageTakenMultiplier.get().floatValue();
                float effMulti = PerkAttributeHelper.getOrCreateMap(player, direction).getAttributeInstance(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT);
                event.setAmount(event.getAmount() * (multiplier * (1F / effMulti)));
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue damageTakenMultiplier;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.damageTakenMultiplier = cfgBuilder
                    .comment("The multiplier that is applied to damage the player receives. The lower the more damage is negated.")
                    .translation(translationKey("damageTakenMultiplier"))
                    .defineInRange("damageTakenMultiplier", defaultDamageTakenMultiplier, 0.1F, 1F);
        }
    }
}
