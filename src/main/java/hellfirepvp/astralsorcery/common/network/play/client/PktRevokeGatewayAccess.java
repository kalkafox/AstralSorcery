/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.PlayerReference;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktRevokeGatewayAccess
 * Created by HellFirePvP
 * Date: 21.10.2020 / 22:20
 */
public class PktRevokeGatewayAccess extends ASPacket<PktRevokeGatewayAccess> {

    private ResourceKey<Level> dim = null;
    private BlockPos pos = BlockPos.ZERO;
    private UUID revokeUUID = null;

    public PktRevokeGatewayAccess() {}

    public PktRevokeGatewayAccess(ResourceKey<Level> dim, BlockPos pos, UUID revokeUUID) {
        this.dim = dim;
        this.pos = pos;
        this.revokeUUID = revokeUUID;
    }

    @Nonnull
    @Override
    public Encoder<PktRevokeGatewayAccess> encoder() {
        return (packet, buffer) -> {
            ByteBufUtils.writeVanillaRegistryEntry(buffer, packet.dim);
            ByteBufUtils.writePos(buffer, packet.pos);
            ByteBufUtils.writeUUID(buffer, packet.revokeUUID);
        };
    }

    @Nonnull
    @Override
    public Decoder<PktRevokeGatewayAccess> decoder() {
        return buffer -> new PktRevokeGatewayAccess(
                ByteBufUtils.readVanillaRegistryEntry(buffer),
                ByteBufUtils.readPos(buffer),
                ByteBufUtils.readUUID(buffer));
    }

    @Nonnull
    @Override
    public Handler<PktRevokeGatewayAccess> handler() {
        return (packet, context, direction) -> {
            if (direction.isServer()) {
                Player sender = context.getSender();
                if (sender == null) {
                    return;
                }

                MinecraftServer srv = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
                Level level = srv.getLevel(packet.dim);

                TileCelestialGateway gateway = MiscUtils.getTileAt(level, packet.pos, TileCelestialGateway.class, false);
                if (gateway != null && gateway.isLocked() && gateway.getOwner() != null && gateway.getOwner().isAlwaysExperienceDropper(sender)) {
                    BlockPos testPos = Vector3.atEntityCorner(sender).toBlockPos();
                    TileCelestialGateway playerGateway = MiscUtils.getTileAt(level, testPos, TileCelestialGateway.class, false);
                    if (gateway.equals(playerGateway)) {
                        PlayerReference removedPlayer = gateway.removeAllowedUser(packet.revokeUUID);
                        if (removedPlayer != null) {
                            PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.GATEWAY_REVOKE_EFFECT)
                                    .addData(buffer -> ByteBufUtils.writePos(buffer, gateway.getBlockPos()));
                            PacketChannel.CHANNEL.sendToPlayer(sender, pkt);

                            Component accessGrantedMessage = Component.translatable(
                                    "astralsorcery.misc.link.gateway.unlink",
                                    removedPlayer.getOwner())
                                    .withStyle(ChatFormatting.GREEN);
                            sender.sendSystemMessage(accessGrantedMessage);
                        }
                    }
                }
            }
        };
    }
}
