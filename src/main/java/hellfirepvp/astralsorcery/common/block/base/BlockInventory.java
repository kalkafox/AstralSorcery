/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.CapabilityItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockInventory
 * Created by HellFirePvP
 * Date: 14.08.2019 / 07:01
 */
public abstract class BlockInventory extends BlockCrystalContainer {

    protected BlockInventory(Properties builder) {
        super(builder);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity te = MiscUtils.getTileAt(worldIn, pos, BlockEntity.class, true);
        if (te != null && !worldIn.isClientSide) {
            LazyOptional<IItemHandler> opt = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            if (opt.isPresent()) {
                ItemUtils.dropEquipment(opt.orElse(ItemUtils.EMPTY_INVENTORY), worldIn, pos);
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}
