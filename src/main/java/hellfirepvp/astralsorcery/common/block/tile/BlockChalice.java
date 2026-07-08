/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ToolType;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockChalice
 * Created by HellFirePvP
 * Date: 09.11.2019 / 19:18
 */
public class BlockChalice extends BaseEntityBlock implements CustomItemBlock {

    private static final VoxelShape CHALICE = VoxelShapes.create(2D / 16D, 0D / 16D, 2D / 16D, 14D / 16D, 14D / 16D, 14D / 16D);

    public BlockChalice() {
        super(PropertiesMisc.defaultGoldMachinery()
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return CHALICE;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        ItemStack interact = player.getHeldItem(hand);
        TileChalice tc = MiscUtils.getTileAt(world, pos, TileChalice.class, true);
        if (tc != null) {
            IFluidHandlerItem handlerItem = FluidUtil.getFluidHandler(interact).orElse(null);
            if (handlerItem != null) {
                if (!world.isRemote()) {
                    FluidStack st = FluidUtil.getFluidContained(interact).orElse(FluidStack.EMPTY);
                    if (st.isEmpty()) {
                        //Fill the stack from the tile?
                        FluidActionResult far = FluidUtil.tryFillContainer(interact, tc.getTankAccess(), FluidAttributes.BUCKET_VOLUME, player, true);
                        if (far.isSuccess()) {
                            if (!player.isCreative()) {
                                interact.shrink(1);
                                player.setHeldItem(hand, interact);
                                player.inventory.placeItemBackInInventory(world, far.getResult());
                            }
                        }
                    } else {
                        //Drain from stack into tile?
                        FluidActionResult far = FluidUtil.tryEmptyContainer(interact, tc.getTankAccess(), FluidAttributes.BUCKET_VOLUME, player, true);
                        if (far.isSuccess()) {
                            if (!player.isCreative()) {
                                interact.shrink(1);
                                player.setHeldItem(hand, interact);
                                player.inventory.placeItemBackInInventory(world, far.getResult());
                            }
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState state, Level world, BlockPos pos) {
        TileChalice tc = MiscUtils.getTileAt(world, pos, TileChalice.class, false);
        if (tc != null) {
            return MathHelper.ceil(tc.getTank().getPercentageFilled() * 15F);
        }
        return 0;
    }

    @Override
    public boolean allowsMovement(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createNewTileEntity(BlockGetter worldIn) {
        return new TileChalice();
    }
}
