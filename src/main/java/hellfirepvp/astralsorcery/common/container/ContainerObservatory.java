/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.ContainerTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileObservatory;
import net.minecraft.world.entity.player.Player;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerObservatory
 * Created by HellFirePvP
 * Date: 16.02.2020 / 09:59
 */
public class ContainerObservatory extends ContainerTileEntity<TileObservatory> {

    public ContainerObservatory(TileObservatory observatory, int containerId) {
        super(observatory, ContainerTypesAS.OBSERVATORY, containerId);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
}
