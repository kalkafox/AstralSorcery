/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import hellfirepvp.astralsorcery.common.lib.ContainerTypesAS;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.network.IContainerFactory;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerAltarDiscoveryProvider
 * Created by HellFirePvP
 * Date: 15.08.2019 / 16:22
 */
public class ContainerAltarConstellationProvider extends CustomContainerProvider<ContainerAltarConstellation> {

    private final TileAltar ta;

    public ContainerAltarConstellationProvider(TileAltar ta) {
        super(ContainerTypesAS.ALTAR_CONSTELLATION);
        this.ta = ta;
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf) {
        ByteBufUtils.writePos(buf, this.ta.getPos());
    }

    @Nonnull
    @Override
    public ContainerAltarConstellation createMenu(int id, Inventory plInventory, Player player) {
        return new ContainerAltarConstellation(ta, plInventory, id);
    }

    private static ContainerAltarConstellation createFromPacket(int id, Inventory plInventory, FriendlyByteBuf data) {
        BlockPos at = ByteBufUtils.readPos(data);
        Player player = plInventory.player;
        TileAltar ta = MiscUtils.getTileAt(player.getEntityWorld(), at, TileAltar.class, true);
        return new ContainerAltarConstellation(ta, plInventory, id);
    }

    public static class Factory implements IContainerFactory<ContainerAltarConstellation> {

        @Override
        public ContainerAltarConstellation create(int windowId, Inventory inv, FriendlyByteBuf data) {
            return ContainerAltarConstellationProvider.createFromPacket(windowId, inv, data);
        }
    }
}
