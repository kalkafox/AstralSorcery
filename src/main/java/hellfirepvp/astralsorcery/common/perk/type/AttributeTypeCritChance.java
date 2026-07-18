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
import hellfirepvp.astralsorcery.common.perk.modifier.AttributeModifierCritChance;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeCritChance
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:18
 */
public class AttributeTypeCritChance extends PerkAttributeType {

    public AttributeTypeCritChance() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_INC_CRIT_CHANCE);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(EventPriority.HIGH, this::onArrowCrit);
        eventBus.addListener(EventPriority.LOW, this::onHitCrit);
    }

    @Nonnull
    @Override
    public PerkAttributeModifier createModifier(float modifier, ModifierType mode) {
        return new AttributeModifierCritChance(this, mode, modifier);
    }

    private void onArrowCrit(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            Entity shooter = arrow.getOwner();
            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                LogicalSide direction = this.getSide(player);
                if (!hasTypeApplied(player, direction)) {
                    return;
                }
                float critChance = PerkAttributeHelper.getOrCreateMap(player, direction)
                        .modifyValue(player, ResearchHelper.getProgress(player, direction), this, 0F);
                critChance = AttributeEvent.postProcessModded(player, this, critChance);
                critChance /= 100.0F;
                if (critChance >= random.nextFloat()) {
                    arrow.setCritArrow(true);
                }
            }
        }
    }

    private void onHitCrit(CriticalHitEvent event) {
        if (event.isVanillaCritical() || event.isCriticalHit()) {
            return;
        }
        Player player = event.getEntity();
        LogicalSide direction = this.getSide(player);
        if (!hasTypeApplied(player, direction)) {
            return;
        }

        float critChance = PerkAttributeHelper.getOrCreateMap(player, direction)
                .modifyValue(player, ResearchHelper.getProgress(player, direction), this, 0F);
        critChance = AttributeEvent.postProcessModded(player, this, critChance);
        critChance /= 100.0F;
        if (critChance >= random.nextFloat()) {
            event.setCriticalHit(true);
        }
    }
}
