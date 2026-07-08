/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeChargeMaximum
 * Created by HellFirePvP
 * Date: 08.03.2020 / 16:46
 */
public class AttributeTypeChargeMaximum extends PerkAttributeType {

    public AttributeTypeChargeMaximum() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_ALIGNMENT_CHARGE_MAXIMUM);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(this::onAttributePostProcess);
    }

    @Override
    public void onApply(Player player, LogicalSide side, ModifierSource source) {
        super.onApply(player, side, source);
        AlignmentChargeHandler.INSTANCE.updateMaximum(player, side);
    }

    @Override
    public void onRemove(Player player, LogicalSide side, boolean removedCompletely, ModifierSource source) {
        super.onRemove(player, side, removedCompletely, source);
        AlignmentChargeHandler.INSTANCE.updateMaximum(player, side);
    }

    private void onAttributePostProcess(AttributeEvent.PostProcessModded processEvent) {
        if (processEvent.getType() instanceof AttributeTypeChargeMaximum && processEvent.getValue() < 0) {
            processEvent.setValue(0);
        }
    }
}
