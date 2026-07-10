/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import hellfirepvp.astralsorcery.common.network.base.PacketContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktRequestSeed
 * Created by HellFirePvP
 * Date: 02.06.2019 / 14:13
 */
public class PktRequestSeed extends ASPacket<PktRequestSeed> {

    private ResourceKey<Level> dim;
    private Integer user;
    private Long seed;

    public PktRequestSeed() {}

    public PktRequestSeed(Integer user, ResourceKey<Level> dim) {
        this.dim = dim;
        this.user = user;
        this.seed = -1L;
    }

    private PktRequestSeed seed(Long seed) {
        this.seed = seed;
        return this;
    }

    @Nonnull
    @Override
    public Encoder<PktRequestSeed> encoder() {
        return (packet, buffer) -> {
            ByteBufUtils.writeOptional(buffer, packet.dim, ByteBufUtils::writeVanillaRegistryEntry);
            ByteBufUtils.writeOptional(buffer, packet.user, FriendlyByteBuf::writeInt);
            ByteBufUtils.writeOptional(buffer, packet.seed, FriendlyByteBuf::writeLong);
        };
    }

    @Nonnull
    @Override
    public Decoder<PktRequestSeed> decoder() {
        return buffer -> {
            PktRequestSeed pkt = new PktRequestSeed();

            pkt.dim = ByteBufUtils.readOptional(buffer, ByteBufUtils::readVanillaRegistryEntry);
            pkt.user = ByteBufUtils.readOptional(buffer, FriendlyByteBuf::readInt);
            pkt.seed = ByteBufUtils.readOptional(buffer, FriendlyByteBuf::readLong);

            return pkt;
        };
    }

    @Nonnull
    @Override
    public Handler<PktRequestSeed> handler() {
        return new Handler<PktRequestSeed>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktRequestSeed packet, PacketContext context) {
                context.enqueueWork(() -> WorldSeedCache.updateSeedCache(packet.dim, packet.user, packet.seed));
            }

            @Override
            public void handle(PktRequestSeed packet, PacketContext context, LogicalSide direction) {
                context.enqueueWork(() -> {
                    //TODO 1.16.2 re-check once worlds are not all constantly loaded
                    MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
                    ServerLevel w = srv.getLevel(packet.dim);
                    if (w != null) {
                        PktRequestSeed seedResponse = new PktRequestSeed(packet.user, packet.dim);
                        seedResponse.seed(MiscUtils.getRandomWorldSeed(w));
                        packet.replyWith(seedResponse, context);
                    }
                });
            }
        };
    }
}
