/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.fluid.FluidLiquidStarlight;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidsAS
 * Created by HellFirePvP
 * Date: 20.09.2019 / 21:39
 */
public class FluidsAS {

    private FluidsAS() {}

    public static FluidType LIQUID_STARLIGHT_FLUID_TYPE;

    public static BaseFlowingFluid.Properties LIQUID_STARLIGHT_PROPERTIES;

    public static FluidLiquidStarlight.Flowing LIQUID_STARLIGHT_FLOWING;
    public static FluidLiquidStarlight.Source LIQUID_STARLIGHT_SOURCE;

}
