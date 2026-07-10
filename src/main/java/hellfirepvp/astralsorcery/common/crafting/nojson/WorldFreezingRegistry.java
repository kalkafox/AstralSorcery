/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson;

import hellfirepvp.astralsorcery.common.crafting.nojson.freezing.BlockFreezingRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.freezing.FluidFreezingRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.freezing.WorldFreezingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldFreezingRegistry
 * Created by HellFirePvP
 * Date: 30.11.2019 / 19:10
 */
public class WorldFreezingRegistry extends CustomRecipeRegistry<WorldFreezingRecipe> {

    public static final WorldFreezingRegistry INSTANCE = new WorldFreezingRegistry();

    @Override
    public void init() {
        this.register(BlockFreezingRecipe.of(Blocks.FIRE, Blocks.AIR.defaultBlockState()));
        this.register(BlockFreezingRecipe.of(Blocks.AIR.defaultBlockState(), Blocks.ICE.defaultBlockState()));
        this.register(BlockFreezingRecipe.of(Blocks.CAVE_AIR.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState()));

        this.register(new FluidFreezingRecipe());
    }

    @Nullable
    public WorldFreezingRecipe getRecipeFor(Level level, BlockPos pos) {
        return this.getRecipes()
                .stream()
                .filter(recipe -> recipe.canFreeze(level, pos))
                .findFirst()
                .orElse(null);
    }

}
