/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.auxiliary.gateway.CelestialGatewayHandler;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktUpdateGateways
 * Created by HellFirePvP
 * Date: 02.06.2019 / 00:44
 */
public class PktUpdateGateways extends ASPacket<PktUpdateGateways> {

    private Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> positions = new HashMap<>();

    public PktUpdateGateways() {}

    public PktUpdateGateways(Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> positions) {
        this.positions = positions;
    }

    @Nonnull
    @Override
    public Encoder<PktUpdateGateways> encoder() {
        return (packet, buffer) -> {
            buffer.writeInt(packet.positions.size());
            for (ResourceKey<Level> dim : packet.positions.keySet()) {
                ByteBufUtils.writeVanillaRegistryEntry(buffer, dim);
                ByteBufUtils.writeCollection(buffer, packet.positions.get(dim), (buf, node) -> node.write(buf));
            }
        };
    }

    @Nonnull
    @Override
    public Decoder<PktUpdateGateways> decoder() {
        return buffer -> {
            PktUpdateGateways pkt = new PktUpdateGateways();
            int dimSize = buffer.readInt();
            for (int i = 0; i < dimSize; i++) {
                ResourceKey<Level> dim = ByteBufUtils.readVanillaRegistryEntry(buffer);
                pkt.positions.put(dim, ByteBufUtils.readList(buffer, GatewayCache.GatewayNode::read));
            }
            return pkt;
        };
    }

    @Nonnull
    @Override
    public Handler<PktUpdateGateways> handler() {
        return new Handler<PktUpdateGateways>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktUpdateGateways packet, NetworkEvent.Context context) {
                context.enqueueWork(() -> CelestialGatewayHandler.INSTANCE.updateClientCache(packet.positions));
            }

            @Override
            public void handle(PktUpdateGateways packet, NetworkEvent.Context context, LogicalSide direction) {}
        };
    }
}
