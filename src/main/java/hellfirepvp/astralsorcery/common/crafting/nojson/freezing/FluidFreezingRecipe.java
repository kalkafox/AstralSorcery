/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.freezing;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidAttributes;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidFreezingRecipe
 * Created by HellFirePvP
 * Date: 30.11.2019 / 21:09
 */
public class FluidFreezingRecipe extends BlockFreezingRecipe {

    public FluidFreezingRecipe() {
        super(AstralSorcery.key("all_fluids_freezing"),
                (level, pos, state) -> state.getFluidState().isSource() &&
                        state.getFluidState().getBlockState().equals(state),
                (access, state) -> {
                    FluidAttributes fAttr = state.getFluidState().getType().getAttributes();
                    if (fAttr.getTemperature(access.getLevel(), access) <= 300) {
                        return Blocks.ICE.defaultBlockState();
                    } else if (fAttr.getTemperature(access.getLevel(), access) >= 500) {
                        return Blocks.OBSIDIAN.defaultBlockState();
                    }
                    return state;
                });
    }
}
