/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import hellfirepvp.astralsorcery.common.container.ContainerObservatory;
import hellfirepvp.astralsorcery.common.lib.ContainerTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileObservatory;
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
 * Class: ContainerObservatoryProvider
 * Created by HellFirePvP
 * Date: 16.02.2020 / 10:00
 */
public class ContainerObservatoryProvider extends CustomContainerProvider<ContainerObservatory> {

    private final TileObservatory observatory;

    public ContainerObservatoryProvider(TileObservatory observatory) {
        super(ContainerTypesAS.OBSERVATORY);
        this.observatory = observatory;
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf) {
        ByteBufUtils.writePos(buf, this.observatory.getBlockPos());
    }

    @Nonnull
    @Override
    public ContainerObservatory createMenu(int containerId, Inventory plInventory, Player player) {
        return new ContainerObservatory(this.observatory, containerId);
    }

    private static ContainerObservatory createFromPacket(int containerId, Inventory plInventory, FriendlyByteBuf data) {
        BlockPos at = ByteBufUtils.readPos(data);
        Player player = plInventory.player;
        TileObservatory observatory = MiscUtils.getTileAt(player.getCommandSenderWorld(), at, TileObservatory.class, true);
        return new ContainerObservatory(observatory, containerId);
    }

    public static class Factory implements IContainerFactory<ContainerObservatory> {

        @Override
        public ContainerObservatory create(int containerId, Inventory inv, FriendlyByteBuf data) {
            return ContainerObservatoryProvider.createFromPacket(containerId, inv, data);
        }
    }
}
