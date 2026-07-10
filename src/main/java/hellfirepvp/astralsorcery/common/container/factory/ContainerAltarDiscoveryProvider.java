/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import hellfirepvp.astralsorcery.common.container.ContainerAltarDiscovery;
import hellfirepvp.astralsorcery.common.lib.ContainerTypesAS;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.IContainerFactory;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerAltarDiscoveryProvider
 * Created by HellFirePvP
 * Date: 15.08.2019 / 16:22
 */
public class ContainerAltarDiscoveryProvider extends CustomContainerProvider<ContainerAltarDiscovery> {

    private final TileAltar ta;

    public ContainerAltarDiscoveryProvider(TileAltar ta) {
        super(ContainerTypesAS.ALTAR_DISCOVERY);
        this.ta = ta;
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf) {
        ByteBufUtils.writePos(buf, this.ta.getBlockPos());
    }

    @Nonnull
    @Override
    public ContainerAltarDiscovery createMenu(int id, Inventory plInventory, Player player) {
        return new ContainerAltarDiscovery(ta, plInventory, id);
    }

    private static ContainerAltarDiscovery createFromPacket(int id, Inventory plInventory, FriendlyByteBuf data) {
        BlockPos at = ByteBufUtils.readPos(data);
        Player player = plInventory.player;
        TileAltar ta = MiscUtils.getTileAt(player.getCommandSenderWorld(), at, TileAltar.class, true);
        return new ContainerAltarDiscovery(ta, plInventory, id);
    }

    public static class Factory implements IContainerFactory<ContainerAltarDiscovery> {

        @Override
        public ContainerAltarDiscovery create(int containerId, Inventory inv, FriendlyByteBuf data) {
            return ContainerAltarDiscoveryProvider.createFromPacket(containerId, inv, data);
        }
    }
}
