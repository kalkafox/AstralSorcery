/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.base;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ASPacket
 * Created by HellFirePvP
 * Date: 01.06.2019 / 17:57
 */
public abstract class ASPacket<T extends ASPacket<T>> {

    protected static Random rand = new Random();

    @Nonnull
    public abstract Encoder<T> encoder();

    @Nonnull
    public abstract Decoder<T> decoder();

    @Nonnull
    public abstract Handler<T> handler();

    public static interface Encoder<T extends ASPacket<T>> extends BiConsumer<T, FriendlyByteBuf> {}

    public static interface Decoder<T extends ASPacket<T>> extends Function<FriendlyByteBuf, T> {}

    public static interface Handler<T extends ASPacket<T>> extends BiConsumer<T, PacketContext> {

        @Override
        default void accept(T t, PacketContext ctx) {
            switch (ctx.getReceptionSide()) {
                case CLIENT:
                    this.handleClient(t, ctx);
                    break;
                case SERVER:
                    this.handleServer(t, ctx);
                    break;
            }
        }

        @OnlyIn(Dist.CLIENT)
        default void handleClient(T packet, PacketContext context) {
            this.handle(packet, context, LogicalSide.CLIENT);
        }

        default void handleServer(T packet, PacketContext context) {
            this.handle(packet, context, LogicalSide.SERVER);
        }

        void handle(T packet, PacketContext context, LogicalSide side);

    }

    protected final void replyWith(T packet, PacketContext ctx) {
        PacketChannel.CHANNEL.reply(packet, ctx);
    }
}
