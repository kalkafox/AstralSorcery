/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncStepAssist;
import hellfirepvp.astralsorcery.common.perk.CooldownPerk;
import hellfirepvp.astralsorcery.common.perk.PerkCooldownHelper;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import hellfirepvp.astralsorcery.common.perk.tick.PlayerTickPerk;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyStepAssist
 * Created by HellFirePvP
 * Date: 31.08.2019 / 22:53
 */
public class KeyStepAssist extends KeyPerk implements PlayerTickPerk, CooldownPerk {

    public KeyStepAssist(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);
        bus.addListener(EventPriority.LOWEST, this::onTeleport);
    }

    @Override
    public void onPlayerTick(Player player, LogicalSide direction) {
        if (direction.isServer()) {
            AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
            if (stepHeight == null) {
                return;
            }
            double storageY = stepHeight.getBaseValue();
            if (!PerkCooldownHelper.isCooldownActiveForPlayer(player, this)) {
                stepHeight.setBaseValue(storageY + 0.5D);
            } else {
                if (stepHeight.getBaseValue() < 1.1D) {
                    stepHeight.setBaseValue(1.1D);
                }
            }
            PerkCooldownHelper.forceSetCooldownForPlayer(player, this, 20);
            if (storageY != stepHeight.getBaseValue() && player instanceof ServerPlayer) {
                if (MiscUtils.isConnectionEstablished((ServerPlayer) player)) {
                    PktSyncStepAssist sync = new PktSyncStepAssist((float) stepHeight.getBaseValue());
                    PacketChannel.CHANNEL.sendToPlayer(player, sync);
                }
            }
        }
    }

    @Override
    public void onCooldownTimeout(Player player) {
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight == null) {
            return;
        }
        stepHeight.setBaseValue(stepHeight.getBaseValue() - 0.5D);
        if (stepHeight.getBaseValue() < 0.6D) {
            stepHeight.setBaseValue(0.6D);
        }

        if (player instanceof ServerPlayer && MiscUtils.isConnectionEstablished((ServerPlayer) player)) {
            PktSyncStepAssist sync = new PktSyncStepAssist((float) stepHeight.getBaseValue());
            PacketChannel.CHANNEL.sendToPlayer(player, sync);
        }
    }

    private void onTeleport(EntityTravelToDimensionEvent event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide() && event.getEntity() instanceof Player) {
            PerkCooldownHelper.removeAllCooldowns((Player) event.getEntity(), LogicalSide.SERVER);
        }
    }
}
