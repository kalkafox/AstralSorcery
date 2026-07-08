/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataSerializersAS
 * Created by HellFirePvP
 * Date: 06.07.2019 / 19:12
 */
public class DataSerializersAS {

    private DataSerializersAS() {}

    public static EntityDataSerializer<Long> LONG;
    public static EntityDataSerializer<Vector3> VECTOR;
    public static EntityDataSerializer<FluidStack> FLUID;

}
