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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import java.util.Locale;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeAllElementalResist
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:02
 */
public class AttributeTypeAllElementalResist extends PerkAttributeType {

    public AttributeTypeAllElementalResist() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_INC_ALL_ELEMENTAL_RESIST, true);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(this::onDamageTaken);
    }

    private void onDamageTaken(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        LogicalSide direction = this.getSide(player);
        if (!hasTypeApplied(player, direction)) {
            return;
        }
        DamageSource ds = event.getSource();
        if (isMaybeElementalDamage(ds)) {
            float multiplier = PerkAttributeHelper.getOrCreateMap(player, direction)
                    .modifyValue(player, ResearchHelper.getProgress(player, direction), this, 1F);
            multiplier -= 1F;
            multiplier = AttributeEvent.postProcessModded(player, this, multiplier);
            multiplier = 1F - Mth.clamp(multiplier, 0F, 1F);
            event.setAmount(event.getAmount() * multiplier);
        }
    }

    private boolean isMaybeElementalDamage(DamageSource source) {
        // "Magic" is often used for any kinds of damages... poison for example
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(Tags.DamageTypes.IS_MAGIC)) {
            return true;
        }
        String key = source.getMsgId();
        if (key == null) {
            return false;
        }
        key = key.toLowerCase(Locale.ROOT);
        return key.contains("fire") || key.contains("heat") || key.contains("lightning") ||
                key.contains("cold") || key.contains("freez") || key.contains("discharg") ||
                key.contains("electr") || key.contains("froze") || key.contains("ice");
    }

}
