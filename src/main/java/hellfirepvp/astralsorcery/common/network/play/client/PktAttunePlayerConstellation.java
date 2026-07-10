/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunePlayerRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active.ActivePlayerAttunementRecipe;
import hellfirepvp.astralsorcery.common.network.base.ASPacket;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktAttuneConstellation
 * Created by HellFirePvP
 * Date: 02.06.2019 / 11:35
 */
public class PktAttunePlayerConstellation extends ASPacket<PktAttunePlayerConstellation> {

    private IMajorConstellation attunement = null;
    private ResourceKey<Level> level = null;
    private BlockPos at = BlockPos.ZERO;

    public PktAttunePlayerConstellation() {}

    public PktAttunePlayerConstellation(IMajorConstellation attunement, ResourceKey<Level> level, BlockPos at) {
        this.attunement = attunement;
        this.level = level;
        this.at = at;
    }

    @Nonnull
    @Override
    public Encoder<PktAttunePlayerConstellation> encoder() {
        return (packet, buffer) -> {
            ByteBufUtils.writeRegistryEntry(buffer, packet.attunement);
            ByteBufUtils.writeVanillaRegistryEntry(buffer, packet.level);
            ByteBufUtils.writePos(buffer, packet.at);
        };
    }

    @Nonnull
    @Override
    public Decoder<PktAttunePlayerConstellation> decoder() {
        return buffer -> {
            PktAttunePlayerConstellation pkt = new PktAttunePlayerConstellation();

            pkt.attunement = ByteBufUtils.readRegistryEntry(buffer);
            pkt.level = ByteBufUtils.readVanillaRegistryEntry(buffer);
            pkt.at = ByteBufUtils.readPos(buffer);

            return pkt;
        };
    }

    @Nonnull
    @Override
    public Handler<PktAttunePlayerConstellation> handler() {
        return (packet, context, direction) -> {
            context.enqueueWork(() -> {
                IMajorConstellation cst = packet.attunement;
                if (cst != null) {
                    MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
                    if (srv.levelKeys().contains(packet.level)) {
                        Level level = srv.getLevel(packet.level);
                        TileAttunementAltar ta = MiscUtils.getTileAt(level, packet.at, TileAttunementAltar.class, false);
                        if (ta != null && ta.getActiveRecipe() instanceof ActivePlayerAttunementRecipe) {
                            if (context.getSender().getUUID().equals(((ActivePlayerAttunementRecipe) ta.getActiveRecipe()).getPlayerUUID()) &&
                                    AttunePlayerRecipe.isEligablePlayer(context.getSender(), ta.getActiveConstellation())) {

                                ta.finishActiveRecipe();
                            }
                        }
                    }
                }
            });
        };
    }
}
