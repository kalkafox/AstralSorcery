/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.network.NetworkEvent;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktToggleClientOption
 * Created by HellFirePvP
 * Date: 13.05.2020 / 19:25
 */
public class PktToggleClientOption extends ASPacket<PktToggleClientOption> {

    private Option option;

    public PktToggleClientOption() {}

    public PktToggleClientOption(Option option) {
        this.option = option;
    }

    @Nonnull
    @Override
    public Encoder<PktToggleClientOption> encoder() {
        return (pkt, buf) -> ByteBufUtils.writeEnumValue(buf, pkt.option);
    }

    @Nonnull
    @Override
    public Decoder<PktToggleClientOption> decoder() {
        return buf -> new PktToggleClientOption(ByteBufUtils.readEnumValue(buf, Option.class));
    }

    @Nonnull
    @Override
    public Handler<PktToggleClientOption> handler() {
        return new Handler<PktToggleClientOption>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktToggleClientOption packet, NetworkEvent.Context context) {}

            @Override
            public void handleServer(PktToggleClientOption packet, NetworkEvent.Context context) {
                ServerPlayer player = context.getSender();
                switch (packet.option) {
                    case DISABLE_PERK_ABILITIES:
                        if (ResearchManager.togglePerkAbilities(player)) {
                            PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
                            if (prog.isValid()) {
                                Component status;
                                if (prog.doPerkAbilities()) {
                                    status = Component.translatable("astralsorcery.progress.perk_abilities.enable").withStyle(ChatFormatting.GREEN);
                                } else {
                                    status = Component.translatable("astralsorcery.progress.perk_abilities.disable").withStyle(ChatFormatting.RED);
                                }
                                player.sendSystemMessage(Component.translatable("astralsorcery.progress.perk_abilities", status).withStyle(ChatFormatting.GRAY));
                            }
                        }
                        break;
                }
            }

            @Override
            public void handle(PktToggleClientOption packet, NetworkEvent.Context context, LogicalSide direction) {}
        };
    }

    public static enum Option {

        DISABLE_PERK_ABILITIES

    }
}
