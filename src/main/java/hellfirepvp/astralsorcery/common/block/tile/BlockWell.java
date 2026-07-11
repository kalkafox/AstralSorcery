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
import net.minecraft.world.ItemInteractionResult;
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
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidUtil;
import java.util.Optional;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import net.neoforged.neoforge.fluids.FluidType;

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

);
        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        VoxelShape footing = Block.box(1, 0, 1, 15, 2, 15);
        VoxelShape floor = Block.box(3, 2, 3, 13, 4, 13);
        VoxelShape basinFloor = Block.box(1, 4, 1, 15, 5, 15);
        VoxelShape w1 = Block.box(1, 5, 1, 2, 16, 14);
        VoxelShape w2 = Block.box(2, 5, 1, 15, 16, 2);
        VoxelShape w3 = Block.box(14, 5, 2, 15, 16, 15);
        VoxelShape w4 = Block.box(1, 5, 14, 14, 16, 15);

        return VoxelUtils.combineAll(BooleanOp.OR,
                footing, floor, basinFloor, w1, w2, w3, w4);
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return this.shape;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (!heldItem.isEmpty()) {
                TileWell tw = MiscUtils.getTileAt(level, pos, TileWell.class, false);
                if (tw == null) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }

                WellLiquefaction entry = RecipeTypesAS.TYPE_WELL.findRecipe(new WellLiquefactionContext(heldItem));
                if (entry != null) {
                    ItemStackHandler handle = tw.getItems();
                    if (!handle.getStackInSlot(0).isEmpty()) {
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }
                    if (!level.isEmptyBlock(pos.above())) {
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }

                    handle.setStackInSlot(0, ItemUtils.copyStackWithSize(heldItem, 1));
                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                            ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    if (heldItem.getCount() <= 0) {
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }
                }

                Optional.ofNullable(tw.getExposedFluidHandler(null))
                        .ifPresent((handler) -> {
                            FluidActionResult far = FluidUtil.tryFillContainerAndStow(heldItem,
                                    handler, new InvWrapper(player.getInventory()), FluidType.BUCKET_VOLUME, player, true);
                            if (far.shouldSwing()) {
                                player.setItemInHand(hand, far.getObject());
                                SoundHelper.playSoundAround(SoundEvents.BUCKET_FILL, level, pos, 1F, 1F);
                                tw.markForUpdate();
                            }
                        });
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileWell tw = MiscUtils.getTileAt(worldIn, pos, TileWell.class, true);
        if (tw != null && !worldIn.isClientSide) {
            ItemStack stack = tw.getItems().getStackInSlot(0);
            if (!stack.isEmpty()) {
                tw.breakCatalyst();
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        TileWell tw = MiscUtils.getTileAt(level, pos, TileWell.class, false);
        if (tw != null) {
            int fluidPart = Mth.ceil(tw.getTank().getPercentageFilled() * 8F);
            return tw.getCatalyst().isEmpty() ? fluidPart : fluidPart + 7;
        }
        return 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_149645_1_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileWell(pos, state);
    }

}
