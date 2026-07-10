/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktRequestTeleport
 * Created by HellFirePvP
 * Date: 02.06.2019 / 14:53
 */
public class PktRequestTeleport extends ASPacket<PktRequestTeleport> {

    private ResourceKey<Level> dim;
    private BlockPos pos;

    public PktRequestTeleport() {}

    public PktRequestTeleport(ResourceKey<Level> dim, BlockPos pos) {
        this.dim = dim;
        this.pos = pos;
    }
    @Nonnull
    @Override
    public Encoder<PktRequestTeleport> encoder() {
        return (packet, buffer) -> {
            ByteBufUtils.writeVanillaRegistryEntry(buffer, packet.dim);
            ByteBufUtils.writePos(buffer, packet.pos);
        };
    }

    @Nonnull
    @Override
    public Decoder<PktRequestTeleport> decoder() {
        return buffer -> {
            PktRequestTeleport pkt = new PktRequestTeleport();

            pkt.dim = ByteBufUtils.readVanillaRegistryEntry(buffer);
            pkt.pos = ByteBufUtils.readPos(buffer);

            return pkt;
        };
    }

    @Nonnull
    @Override
    public Handler<PktRequestTeleport> handler() {
        return (packet, context, direction) -> {
            context.enqueueWork(() -> {
                //TODO 1.16.2 re-check once worlds are not all constantly loaded
                Player player = context.getSender();
                TileCelestialGateway gate = MiscUtils.getTileAt(player.level(), Vector3.atEntityCorner(player).toBlockPos(), TileCelestialGateway.class, false);
                if (gate != null && gate.hasMultiblock() && gate.doesSeeSky()) {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        Level to = server.getLevel(packet.dim);
                        if (to != null) {
                            GatewayCache.GatewayNode node = DataAS.DOMAIN_AS.getData(to, DataAS.KEY_GATEWAY_CACHE).getGatewayNode(packet.pos);
                            if (node != null && node.hasAccess(player)) {
                                AstralSorcery.getProxy().scheduleDelayed(() -> MiscUtils.transferEntityTo(player, to.dimension(), packet.pos));
                            }
                        }
                    }
                }
            });
        };
    }
}
