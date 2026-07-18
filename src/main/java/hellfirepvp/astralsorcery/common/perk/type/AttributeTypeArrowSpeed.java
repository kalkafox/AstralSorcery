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
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeArrowSpeed
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:08
 */
public class AttributeTypeArrowSpeed extends PerkAttributeType {

    public AttributeTypeArrowSpeed() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_PROJ_SPEED, true);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(this::onArrowFire);
    }

    private void onArrowFire(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            Entity shooter = arrow.getOwner();
            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                LogicalSide direction = this.getSide(player);
                if (!hasTypeApplied(player, direction)) {
                    return;
                }

                Vector3 motion = new Vector3(arrow.getDeltaMovement());
                float mul = PerkAttributeHelper.getOrCreateMap(player, direction)
                        .modifyValue(player, ResearchHelper.getProgress(player, direction), this, 1F);
                mul = AttributeEvent.postProcessModded(player, this, mul);
                motion = MiscUtils.limitVelocityToMinecraftLimit(motion.mul(mul));
                arrow.setDeltaMovement(motion.toVector3d());
            }
        }
    }
}
