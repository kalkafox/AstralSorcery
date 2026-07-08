package hellfirepvp.astralsorcery.common.network.base;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Compatibility view over NeoForge's payload context.
 */
public final class PacketContext {

    private final IPayloadContext context;

    public PacketContext(IPayloadContext context) {
        this.context = context;
    }

    public LogicalSide getReceptionSide() {
        return context.flow() == PacketFlow.CLIENTBOUND ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    @Nullable
    public ServerPlayer getSender() {
        return context.player() instanceof ServerPlayer player ? player : null;
    }

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return context.enqueueWork(work);
    }

    public void reply(ASPacket<?> packet) {
        context.reply(PacketChannel.envelope(packet));
    }
}
