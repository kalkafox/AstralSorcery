/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeProjectileAttackDamage
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:42
 */
public class AttributeTypeProjectileAttackDamage extends PerkAttributeType {

    public AttributeTypeProjectileAttackDamage() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_PROJ_DAMAGE, true);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(EventPriority.LOW, this::onProjectileDamage);
    }

    private void onProjectileDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().isProjectile()) {
            DamageSource source = event.getSource();
            if (source.getEntity() != null && source.getEntity() instanceof Player) {
                Player player = (Player) source.getEntity();
                LogicalSide direction = this.getSide(player);
                if (!hasTypeApplied(player, direction)) {
                    return;
                }

                float amt = PerkAttributeHelper.getOrCreateMap(player, direction)
                        .modifyValue(player, ResearchHelper.getProgress(player, direction), this, event.getAmount());
                amt = AttributeEvent.postProcessModded(player, this, amt);
                event.setAmount(amt);
            }
        }
    }
}
