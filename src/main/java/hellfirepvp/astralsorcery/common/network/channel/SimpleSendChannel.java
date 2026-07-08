/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.channel;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.network.base.PacketContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Sending facade backed by NeoForge custom payloads.
 */
public class SimpleSendChannel {

    public <P extends ASPacket<P>> void sendToPlayer(Player player, P packet) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, PacketChannel.envelope(packet));
        }
    }

    public <P extends ASPacket<P>> void sendToAll(P packet) {
        PacketDistributor.sendToAllPlayers(PacketChannel.envelope(packet));
    }

    public <P extends ASPacket<P>> void sendToAllObservingChunk(P packet, LevelChunk chunk) {
        if (chunk.getLevel() instanceof ServerLevel level) {
            PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), PacketChannel.envelope(packet));
        }
    }

    public <P extends ASPacket<P>> void sendToAllAround(P packet, PacketChannel.TargetPoint point) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ServerLevel level = server.getLevel(point.dimension());
        if (level != null) {
            PacketDistributor.sendToPlayersNear(level, null, point.x(), point.y(), point.z(), point.range(),
                    PacketChannel.envelope(packet));
        }
    }

    public <P extends ASPacket<P>> void sendToServer(P packet) {
        PacketDistributor.sendToServer(PacketChannel.envelope(packet));
    }

    public void reply(ASPacket<?> packet, PacketContext context) {
        context.reply(packet);
    }
}
