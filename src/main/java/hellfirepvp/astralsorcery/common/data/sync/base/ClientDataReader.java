/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.sync.base;

import net.minecraft.nbt.CompoundTag;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientDataReader
 * Created by HellFirePvP
 * Date: 27.08.2019 / 06:29
 */
public abstract class ClientDataReader<C extends ClientData<C>> {

    public abstract void readFromIncomingFullSync(C data, CompoundTag compound);

    public abstract void readFromIncomingDiff(C data, CompoundTag compound);

}
