/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.network.base.PacketContext;
import hellfirepvp.astralsorcery.common.network.channel.BufferedReplyChannel;
import hellfirepvp.astralsorcery.common.network.channel.SimpleSendChannel;
import hellfirepvp.astralsorcery.common.network.login.client.PktLoginAcknowledge;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncDataHolder;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncGateway;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncPerkInformation;
import hellfirepvp.astralsorcery.common.network.play.client.*;
import hellfirepvp.astralsorcery.common.network.play.server.*;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * NeoForge payload transport retaining Astral Sorcery's packet codecs.
 */
public final class PacketChannel {

    private static final String NET_COMM_VERSION = "1";
    private static final Map<Integer, ASPacket<?>> PACKETS_BY_ID = new LinkedHashMap<>();
    private static final Map<Class<?>, Integer> IDS_BY_CLASS = new LinkedHashMap<>();

    public static final SimpleSendChannel CHANNEL = new BufferedReplyChannel();

    private PacketChannel() {}

    public static void registerPackets() {
        PACKETS_BY_ID.clear();
        IDS_BY_CLASS.clear();

        register(PktLoginSyncDataHolder::new);
        register(PktLoginSyncGateway::new);
        register(PktLoginSyncPerkInformation::new);
        register(PktLoginAcknowledge::new);

        register(PktOreScan::new);
        register(PktPlayEffect::new);
        register(PktProgressionUpdate::new);
        register(PktShootEntity::new);
        register(PktSyncCharge::new);
        register(PktSyncData::new);
        register(PktSyncKnowledge::new);
        register(PktSyncModifierSource::new);
        register(PktSyncPerkActivity::new);
        register(PktSyncStepAssist::new);
        register(PktUpdateGateways::new);
        register(PktOpenGui::new);

        register(PktAttunePlayerConstellation::new);
        register(PktClearBlockStorageStack::new);
        register(PktDiscoverConstellation::new);
        register(PktEngraveGlass::new);
        register(PktPerkGemModification::new);
        register(PktRequestPerkSealAction::new);
        register(PktRequestSeed::new);
        register(PktRequestTeleport::new);
        register(PktRotateTelescope::new);
        register(PktUnlockPerk::new);
        register(PktToggleClientOption::new);
        register(PktRevokeGatewayAccess::new);
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar(NET_COMM_VERSION)
                .playBidirectional(Envelope.TYPE, Envelope.STREAM_CODEC,
                        (data, context) -> data.packet().handler().accept(data.packet(), new PacketContext(context)));
    }

    private static void register(Supplier<? extends ASPacket<?>> factory) {
        ASPacket<?> packet = factory.get();
        int id = PACKETS_BY_ID.size();
        PACKETS_BY_ID.put(id, packet);
        IDS_BY_CLASS.put(packet.getClass(), id);
    }

    public static Envelope envelope(ASPacket<?> packet) {
        Integer id = IDS_BY_CLASS.get(packet.getClass());
        if (id == null) {
            throw new IllegalArgumentException("Unregistered Astral Sorcery packet " + packet.getClass().getName());
        }
        return new Envelope(id, packet);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void encode(RegistryFriendlyByteBuf buffer, Envelope envelope) {
        buffer.writeVarInt(envelope.id());
        ((ASPacket) envelope.packet()).encoder().accept(envelope.packet(), buffer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Envelope decode(RegistryFriendlyByteBuf buffer) {
        int id = buffer.readVarInt();
        ASPacket prototype = PACKETS_BY_ID.get(id);
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown Astral Sorcery packet id " + id);
        }
        return new Envelope(id, (ASPacket<?>) prototype.decoder().apply(buffer));
    }

    public static TargetPoint pointFromPos(Level level, Vec3i pos, double range) {
        return pointFromPos(level.dimension(), pos, range);
    }

    public static TargetPoint pointFromPos(ResourceKey<Level> level, Vec3i pos, double range) {
        return new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), range, level);
    }

    public record TargetPoint(double x, double y, double z, double range, ResourceKey<Level> dimension) {}

    public record Envelope(int id, ASPacket<?> packet) implements CustomPacketPayload {

        public static final Type<Envelope> TYPE = new Type<>(AstralSorcery.key("packet"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Envelope> STREAM_CODEC =
                StreamCodec.of(PacketChannel::encode, PacketChannel::decode);

        @Override
        public Type<Envelope> type() {
            return TYPE;
        }
    }
}
