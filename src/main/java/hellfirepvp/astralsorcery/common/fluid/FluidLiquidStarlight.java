/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.fluid;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.ForgeFlowingFluid;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidLiquidStarlight
 * Created by HellFirePvP
 * Date: 20.09.2019 / 21:32
 */
public abstract class FluidLiquidStarlight extends ForgeFlowingFluid {

    private FluidLiquidStarlight(Properties properties) {
        super(properties);
    }

    public static FluidAttributes.Builder addAttributes(FluidAttributes.Builder attributeBuilder) {
        return attributeBuilder
                .rarity(Rarity.EPIC)
                .luminosity(15)
                .density(1001)
                .viscosity(300)
                .temperature(40);
    }

    public static class Flowing extends FluidLiquidStarlight {

        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateContainer().any().setValue(LEVEL_1_8, 7));
        }

        protected void createBlockStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(LEVEL_1_8);
        }

        public int getLevel(FluidState state) {
            return state.get(LEVEL_1_8);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends FluidLiquidStarlight {

        public Source(Properties properties) {
            super(properties);
        }

        public int getLevel(FluidState state) {
            return 8;
        }

        public boolean isSource(FluidState state) {
            return true;
        }

    }

}
