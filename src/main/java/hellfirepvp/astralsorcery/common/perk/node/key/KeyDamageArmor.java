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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyDamageArmor
 * Created by HellFirePvP
 * Date: 31.08.2019 / 17:15
 */
public class KeyDamageArmor extends KeyPerk {

    private static final float defaultDamagePerArmor = 0.05F;

    public static final Config CONFIG = new Config("key.damage_armor");

    public KeyDamageArmor(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(EventPriority.LOW, this::onDamage);
    }

    private void onDamage(LivingIncomingDamageEvent event) {
        LivingEntity attacked = event.getEntityLiving();
        if (attacked instanceof Player) {
            Player player = (Player) attacked;
            LogicalSide direction = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, direction);
            if (prog.getPerkData().hasPerkEffect(this)) {
                int armorPieces = 0;
                for (ItemStack itemStack : player.getArmorSlots()) {
                    if (!itemStack.isEmpty()) {
                        armorPieces++;
                    }
                }
                if (armorPieces == 0) {
                    return;
                }

                double dmgArmor = CONFIG.damagePerArmor.get();
                float dmg = event.getAmount();
                dmg *= ((dmgArmor * armorPieces) * PerkAttributeHelper.getOrCreateMap(player, direction)
                        .getAttributeInstance(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT));
                event.setAmount(Math.max(event.getAmount() - dmg, 0));

                int armorDmg = Mth.ceil(dmg * 1.3F);
                for (ItemStack stack : player.getArmorSlots()) {
                    stack.damageItem(armorDmg, player, (pl) -> pl.sendBreakAnimation(EquipmentSlot.MAINHAND));
                }
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue damagePerArmor;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.damagePerArmor = cfgBuilder
                    .comment("Defines how much damage is dealt additionally to armor. This value gets multiplied by the amount of armorpieces the entity you're attacking wears.")
                    .translation(translationKey("damagePerArmor"))
                    .defineInRange("damagePerArmor", defaultDamagePerArmor, 0.01F, 0.2F);
        }
    }
}
