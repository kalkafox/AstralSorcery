/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.enchantment.EnchantmentHelperAS;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyAreaOfEffect
 * Created by HellFirePvP
 * Date: 25.08.2019 / 19:10
 */
public class KeyAreaOfEffect extends KeyAddEnchantment {

    public KeyAreaOfEffect(ResourceLocation name, float x, float y) {
        super(name, x, y);
        this.fillItemCategory(Enchantments.SWEEPING_EDGE, 2);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);
        bus.addListener(EventPriority.HIGH, this::onDamage);
    }

    private void onDamage(LivingIncomingDamageEvent event) {
        if (EventFlags.SWEEP_ATTACK.isSet()) {
            return;
        }

        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player && source.getDirectEntity() != player) {
            LogicalSide direction = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, direction);
            if (prog.getPerkData().hasPerkEffect(this)) {
                LivingEntity attacked = event.getEntity();

                float sweepingPercentage;
                Entity indirectSource = source.getDirectEntity();
                if (indirectSource instanceof ThrownTrident) {
                    ItemStack tridentStack = ((ThrownTrident) indirectSource).getWeaponItem();
                    int sweepLevel = getSweepingLevel(player, tridentStack);
                    sweepingPercentage = getSweepingDamageRatio(sweepLevel);
                } else {
                    sweepingPercentage = getSweepingLevel(player);
                }
                if (sweepingPercentage > 0) {
                    sweepingPercentage = PerkAttributeHelper.getOrCreateMap(player, direction)
                            .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, sweepingPercentage);
                    float toApply = event.getAmount() * sweepingPercentage;

                    float range = 2.5F * PerkAttributeHelper.getOrCreateMap(player, direction).getAttributeInstance(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT);
                    EventFlags.SWEEP_ATTACK.executeWithFlag(() -> {
                        for (LivingEntity target : attacked.level().getEntitiesOfClass(LivingEntity.class,
                                attacked.getBoundingBox().inflate(range, range / 2F, range))) {
                            if (MiscUtils.canPlayerAttackServer(player, target) && !player.equals(target)) {
                                DamageUtil.hurt(target, source, toApply);
                            }
                        }
                    });
                }
            }
        }
    }

    private static float getSweepingLevel(Player player) {
        Optional<Holder.Reference<Enchantment>> sweeping = EnchantmentHelperAS.getHolder(player.registryAccess(), Enchantments.SWEEPING_EDGE);
        int level = sweeping.map(enchantment -> EnchantmentHelper.getEnchantmentLevel(enchantment, player)).orElse(0);
        return getSweepingDamageRatio(level);
    }

    private static int getSweepingLevel(Player player, ItemStack stack) {
        return EnchantmentHelperAS.getHolder(player.registryAccess(), Enchantments.SWEEPING_EDGE)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    private static float getSweepingDamageRatio(int level) {
        return level > 0 ? 1.0F - 1.0F / (level + 1) : 0;
    }
}
