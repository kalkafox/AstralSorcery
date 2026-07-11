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
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
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
 * Class: KeyDisarm
 * Created by HellFirePvP
 * Date: 31.08.2019 / 17:35
 */
public class KeyDisarm extends KeyPerk {

    private static final float defaultDropChance = 0.05F;

    public static final Config CONFIG = new Config("key.disarm");

    public KeyDisarm(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);

        bus.addListener(EventPriority.HIGH, this::onAttack);
    }

    private void onAttack(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() != null && source.getEntity() instanceof Player) {
            Player player = (Player) source.getEntity();
            LogicalSide direction = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, direction);
            if (prog.getPerkData().hasPerkEffect(this)) {
                float chance = PerkAttributeHelper.getOrCreateMap(player, direction)
                        .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, CONFIG.dropChance.get().floatValue());
                float currentChance = Mth.clamp(chance, 0F, 1F);
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (random.nextFloat() >= currentChance) {
                        continue;
                    }
                    LivingEntity attacked = event.getEntity();
                    ItemStack stack = attacked.getItemBySlot(slot);
                    if (!stack.isEmpty()) {
                        attacked.thunderHit(slot, ItemStack.EMPTY);
                        ItemUtils.dropItemNaturally(attacked.level(), attacked.getX(), attacked.getY(), attacked.getZ(), stack);
                        break;
                    }
                }
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue dropChance;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.dropChance = cfgBuilder
                    .comment("Defines the chance (in percent) per hit to make the attacked entity drop its armor.")
                    .translation(translationKey("dropChance"))
                    .defineInRange("dropChance", defaultDropChance, 0F, 1F);
        }
    }
}
