/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.starlight.transmission;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ITransmissionReceiver
 * Created by HellFirePvP
 * Date: 05.08.2016 / 13:43
 */
public interface ITransmissionReceiver extends IPrismTransmissionNode {

    @Override
    default public List<NodeConnection<IPrismTransmissionNode>> queryNext(WorldNetworkHandler handler) {
        return new LinkedList<>();
    }

    @Override
    default public void notifyLink(Level world, BlockPos to) {}

    @Override
    default public boolean notifyUnlink(Level world, BlockPos to) {
        return false;
    }

    public void onStarlightReceive(Level world, IWeakConstellation type, double amount);

}
