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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeCritMultiplier
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:23
 */
public class AttributeTypeCritMultiplier extends PerkAttributeType {

    public AttributeTypeCritMultiplier() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_INC_CRIT_MULTIPLIER, true);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(EventPriority.LOWEST, this::onArrowCrit);
        eventBus.addListener(EventPriority.LOWEST, this::onHitCrit);
    }

    private void onArrowCrit(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if (!arrow.isCritArrow()) {
                return;
            }

            Entity shooter = arrow.getOwner();
            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                LogicalSide direction = this.getSide(player);
                if (!hasTypeApplied(player, direction)) {
                    return;
                }
                float dmgMod = PerkAttributeHelper.getOrCreateMap(player, direction)
                        .modifyValue(player, ResearchHelper.getProgress(player, direction), this, 1F);
                dmgMod = AttributeEvent.postProcessModded(player, this, dmgMod);
                arrow.setBaseDamage(arrow.getBaseDamage() * dmgMod);
            }
        }
    }

    private void onHitCrit(CriticalHitEvent event) {
        if (!event.isCriticalHit()) {
            return; //No crit
        }

        Player player = event.getEntity();
        LogicalSide direction = this.getSide(player);
        if (!hasTypeApplied(player, direction)) {
            return;
        }

        float dmgMod = PerkAttributeHelper.getOrCreateMap(player, direction)
                .modifyValue(player, ResearchHelper.getProgress(player, direction), this, 1F);
        dmgMod = AttributeEvent.postProcessModded(player, this, dmgMod);
        event.setDamageMultiplier(event.getDamageMultiplier() * dmgMod);
    }
}
