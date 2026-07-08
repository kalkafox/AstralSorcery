/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutationContext
 * Created by HellFirePvP
 * Date: 10.10.2019 / 19:28
 */
public class BlockTransmutationContext extends RecipeCraftingContext<BlockTransmutation, IItemHandler> {

    private final LevelAccessor world;
    private final BlockPos pos;
    private final BlockState state;
    private final IWeakConstellation constellation;

    public BlockTransmutationContext(LevelAccessor world, BlockPos pos, BlockState state, IWeakConstellation constellation) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.constellation = constellation;
    }

    public LevelAccessor getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public IWeakConstellation getConstellation() {
        return constellation;
    }
}
