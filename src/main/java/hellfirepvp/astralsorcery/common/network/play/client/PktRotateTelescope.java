/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.client.screen.ScreenTelescope;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.tile.TileTelescope;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nonnull;
import java.util.Optional;
import hellfirepvp.astralsorcery.common.network.base.PacketContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktRotateTelescope
 * Created by HellFirePvP
 * Date: 02.06.2019 / 14:58
 */
public class PktRotateTelescope extends ASPacket<PktRotateTelescope> {

    private boolean isClockwise = false;
    private ResourceKey<Level> dim = null;
    private BlockPos pos = BlockPos.ZERO;

    public PktRotateTelescope() {}

    public PktRotateTelescope(boolean isClockwise, ResourceKey<Level> dim, BlockPos pos) {
        this.isClockwise = isClockwise;
        this.dim = dim;
        this.pos = pos;
    }
    @Nonnull
    @Override
    public Encoder<PktRotateTelescope> encoder() {
        return (packet, buffer) -> {
            buffer.writeBoolean(packet.isClockwise);
            ByteBufUtils.writeOptional(buffer, packet.dim, ByteBufUtils::writeVanillaRegistryEntry);
            ByteBufUtils.writePos(buffer, packet.pos);
        };
    }

    @Nonnull
    @Override
    public Decoder<PktRotateTelescope> decoder() {
        return buffer -> {
            PktRotateTelescope pkt = new PktRotateTelescope();

            pkt.isClockwise = buffer.readBoolean();
            pkt.dim = ByteBufUtils.readOptional(buffer, ByteBufUtils::readVanillaRegistryEntry);
            pkt.pos = ByteBufUtils.readPos(buffer);

            return pkt;
        };
    }

    @Nonnull
    @Override
    public Handler<PktRotateTelescope> handler() {
        return new Handler<PktRotateTelescope>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktRotateTelescope packet, PacketContext context) {
                context.enqueueWork(() -> {
                    Optional<Level> clWorld = LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT);
                    clWorld.ifPresent(level -> {
                        TileTelescope tt = MiscUtils.getTileAt(level, packet.pos, TileTelescope.class, false);
                        if(tt != null) {
                            tt.setRotation(packet.isClockwise ? tt.getRotation().nextClockWise() : tt.getRotation().nextCounterClockWise());
                        }
                    });
                    if (Minecraft.getInstance().screen instanceof ScreenTelescope) {
                        ((ScreenTelescope) Minecraft.getInstance().screen).handleRotationChange(packet.isClockwise);
                    }
                });
            }

            @Override
            public void handle(PktRotateTelescope packet, PacketContext context, LogicalSide direction) {
                context.enqueueWork(() -> {
                    //TODO 1.16.2 re-check once worlds are not all constantly loaded
                    MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
                    Level level = srv.getLevel(packet.dim);

                    TileTelescope tt = MiscUtils.getTileAt(level, packet.pos, TileTelescope.class, false);
                    if(tt != null) {
                        tt.setRotation(packet.isClockwise ? tt.getRotation().nextClockWise() : tt.getRotation().nextCounterClockWise());
                        packet.replyWith(new PktRotateTelescope(packet.isClockwise, packet.dim, packet.pos), context);
                    }
                });
            }
        };
    }
}
