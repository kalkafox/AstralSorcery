/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.*;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import hellfirepvp.astralsorcery.common.network.base.PacketContext;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktSyncKnowledge
 * Created by HellFirePvP
 * Date: 02.06.2019 / 08:42
 */
public class PktSyncKnowledge extends ASPacket<PktSyncKnowledge> {

    public static final byte STATE_ADD = 0;
    public static final byte STATE_WIPE = 1;

    private byte state;
    public List<ResourceLocation> knownConstellations = new ArrayList<>();
    public List<ResourceLocation> seenConstellations = new ArrayList<>();
    public List<ResourceLocation> storedConstellationPapers = new ArrayList<>();
    public Collection<ResearchProgression> researchProgression = new ArrayList<>();
    public IMajorConstellation attunedConstellation = null;
    public boolean wasOnceAttuned = false;
    public int progressTier = 0;
    public boolean doPerkAbilities = true;
    public PlayerPerkData perkData = null;
    // Serialized on the sending (server) thread by load(); encoding happens on the Netty IO
    // thread, and writing the live PlayerPerkData there races progress mutations (CME).
    private byte[] perkDataSnapshot = null;

    public PktSyncKnowledge() {}

    public PktSyncKnowledge(byte state) {
        this.state = state;
    }

    public void load(PlayerProgress progress) {
        // Copy everything here (server thread) — the encoder runs on the Netty IO thread and
        // must not touch the live progress collections.
        this.knownConstellations = new ArrayList<>(progress.getKnownConstellations());
        this.seenConstellations = new ArrayList<>(progress.getSeenConstellations());
        this.storedConstellationPapers = new ArrayList<>(progress.getStoredConstellationPapers());
        this.researchProgression = new ArrayList<>(progress.getResearchProgression());
        this.progressTier = progress.getTierReached().ordinal();
        this.attunedConstellation = progress.getAttunedConstellation();
        this.perkData = progress.getPerkData();
        this.wasOnceAttuned = progress.wasOnceAttuned();
        this.doPerkAbilities = progress.doPerkAbilities();

        FriendlyByteBuf tmp = new FriendlyByteBuf(Unpooled.buffer());
        try {
            this.perkData.write(tmp);
            this.perkDataSnapshot = new byte[tmp.readableBytes()];
            tmp.readBytes(this.perkDataSnapshot);
        } finally {
            tmp.release();
        }
    }

    @Nonnull
    @Override
    public Encoder<PktSyncKnowledge> encoder() {
        return (packet, buffer) -> {
            buffer.writeByte(packet.state);

            ByteBufUtils.writeOptional(buffer, packet.perkDataSnapshot, (buf, bytes) -> buf.writeBytes(bytes));
            ByteBufUtils.writeCollection(buffer, packet.knownConstellations, ByteBufUtils::writeResourceLocation);
            ByteBufUtils.writeCollection(buffer, packet.seenConstellations, ByteBufUtils::writeResourceLocation);
            ByteBufUtils.writeCollection(buffer, packet.storedConstellationPapers, ByteBufUtils::writeResourceLocation);
            ByteBufUtils.writeCollection(buffer, packet.researchProgression, ByteBufUtils::writeEnumValue);
            ByteBufUtils.writeOptional(buffer, packet.attunedConstellation, ByteBufUtils::writeRegistryEntry);
            buffer.writeBoolean(packet.wasOnceAttuned);
            buffer.writeInt(packet.progressTier);
            buffer.writeBoolean(packet.doPerkAbilities);
        };
    }

    @Nonnull
    @Override
    public Decoder<PktSyncKnowledge> decoder() {
        return buffer -> {
            PktSyncKnowledge pkt = new PktSyncKnowledge(buffer.readByte());

            pkt.perkData = ByteBufUtils.readOptional(buffer, buf -> PlayerPerkData.read(buf, LogicalSide.CLIENT));
            pkt.knownConstellations = ByteBufUtils.readList(buffer, ByteBufUtils::readResourceLocation);
            pkt.seenConstellations = ByteBufUtils.readList(buffer, ByteBufUtils::readResourceLocation);
            pkt.storedConstellationPapers = ByteBufUtils.readList(buffer, ByteBufUtils::readResourceLocation);
            pkt.researchProgression = ByteBufUtils.readList(buffer, buf -> ByteBufUtils.readEnumValue(buf, ResearchProgression.class));
            pkt.attunedConstellation = ByteBufUtils.readOptional(buffer, ByteBufUtils::readRegistryEntry);
            pkt.wasOnceAttuned = buffer.readBoolean();
            pkt.progressTier = buffer.readInt();
            pkt.doPerkAbilities = buffer.readBoolean();

            return pkt;
        };
    }

    @Nonnull
    @Override
    public Handler<PktSyncKnowledge> handler() {
        return new Handler<PktSyncKnowledge>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktSyncKnowledge packet, PacketContext context) {
                context.enqueueWork(() -> {
                    Player player = Minecraft.getInstance().player;
                    if (player != null) {
                        if (packet.state == STATE_ADD) {
                            ResearchSyncHelper.recieveProgressFromServer(packet, player);
                        } else {
                            ResearchHelper.updateClientResearch(null);
                        }
                    }
                });
            }

            @Override
            public void handle(PktSyncKnowledge packet, PacketContext context, LogicalSide direction) {}
        };
    }
}
