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

    private final LevelAccessor level;
    private final BlockPos pos;
    private final BlockState state;
    private final IWeakConstellation constellation;

    public BlockTransmutationContext(LevelAccessor level, BlockPos pos, BlockState state, IWeakConstellation constellation) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.constellation = constellation;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public IWeakConstellation getConstellation() {
        return constellation;
    }
}
