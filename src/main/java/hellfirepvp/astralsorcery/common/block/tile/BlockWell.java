/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockStarlightNetwork;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefactionContext;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileWell;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ToolType;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.CapabilityFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockWell
 * Created by HellFirePvP
 * Date: 30.06.2019 / 22:26
 */
public class BlockWell extends BlockStarlightNetwork implements CustomItemBlock {

    private final VoxelShape shape;

    public BlockWell() {
        super(PropertiesMarble.defaultMarble()
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));
        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        VoxelShape footing = Block.makeCuboidShape(1, 0, 1, 15, 2, 15);
        VoxelShape floor = Block.makeCuboidShape(3, 2, 3, 13, 4, 13);
        VoxelShape basinFloor = Block.makeCuboidShape(1, 4, 1, 15, 5, 15);
        VoxelShape w1 = Block.makeCuboidShape(1, 5, 1, 2, 16, 14);
        VoxelShape w2 = Block.makeCuboidShape(2, 5, 1, 15, 16, 2);
        VoxelShape w3 = Block.makeCuboidShape(14, 5, 2, 15, 16, 15);
        VoxelShape w4 = Block.makeCuboidShape(1, 5, 14, 14, 16, 15);

        return VoxelUtils.combineAll(IBooleanFunction.OR,
                footing, floor, basinFloor, w1, w2, w3, w4);
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return this.shape;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isRemote()) {
            ItemStack heldItem = player.getHeldItem(hand);
            if (!heldItem.isEmpty()) {
                TileWell tw = MiscUtils.getTileAt(world, pos, TileWell.class, false);
                if (tw == null) {
                    return ActionResultType.PASS;
                }

                WellLiquefaction entry = RecipeTypesAS.TYPE_WELL.findRecipe(new WellLiquefactionContext(heldItem));
                if (entry != null) {
                    ItemStackHandler handle = tw.getInventory();
                    if (!handle.getStackInSlot(0).isEmpty()) {
                        return ActionResultType.PASS;
                    }
                    if (!world.isAirBlock(pos.up())) {
                        return ActionResultType.PASS;
                    }

                    handle.setStackInSlot(0, ItemUtils.copyStackWithSize(heldItem, 1));
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                            ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    if (heldItem.getCount() <= 0) {
                        player.setHeldItem(hand, ItemStack.EMPTY);
                    }
                }

                tw.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)
                        .ifPresent((handler) -> {
                            FluidActionResult far = FluidUtil.tryFillContainerAndStow(heldItem,
                                    handler, new InvWrapper(player.inventory), FluidAttributes.BUCKET_VOLUME, player, true);
                            if (far.isSuccess()) {
                                player.setHeldItem(hand, far.getResult());
                                SoundHelper.playSoundAround(SoundEvents.ITEM_BUCKET_FILL, world, pos, 1F, 1F);
                                tw.markForUpdate();
                            }
                        });
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileWell tw = MiscUtils.getTileAt(worldIn, pos, TileWell.class, true);
        if (tw != null && !worldIn.isRemote) {
            ItemStack stack = tw.getInventory().getStackInSlot(0);
            if (!stack.isEmpty()) {
                tw.breakCatalyst();
            }
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState state, Level world, BlockPos pos) {
        TileWell tw = MiscUtils.getTileAt(world, pos, TileWell.class, false);
        if (tw != null) {
            int fluidPart = MathHelper.ceil(tw.getTank().getPercentageFilled() * 8F);
            return tw.getCatalyst().isEmpty() ? fluidPart : fluidPart + 7;
        }
        return 0;
    }

    @Override
    public boolean allowsMovement(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderType(BlockState p_149645_1_) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createNewTileEntity(BlockGetter worldIn) {
        return new TileWell();
    }

}
