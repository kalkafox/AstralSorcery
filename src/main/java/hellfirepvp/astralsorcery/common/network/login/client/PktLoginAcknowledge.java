/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.login.client;

import hellfirepvp.astralsorcery.common.network.base.ASLoginPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import hellfirepvp.astralsorcery.common.network.base.PacketContext;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktLoginAcknowledge
 * Created by HellFirePvP
 * Date: 24.08.2019 / 20:07
 */
public class PktLoginAcknowledge extends ASLoginPacket<PktLoginAcknowledge> {

    public PktLoginAcknowledge() {}

    @Nonnull
    @Override
    public Encoder<PktLoginAcknowledge> encoder() {
        return (pktLoginAcknowledge, buffer) -> {};
    }

    @Nonnull
    @Override
    public Decoder<PktLoginAcknowledge> decoder() {
        return buf -> new PktLoginAcknowledge();
    }

    @Nonnull
    @Override
    public Handler<PktLoginAcknowledge> handler() {
        return new Handler<PktLoginAcknowledge>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktLoginAcknowledge packet, PacketContext context) {
                //ping
                acknowledge(context); //pong
            }

            @Override
            public void handle(PktLoginAcknowledge packet, PacketContext context, LogicalSide direction) {}
        };
    }
}
