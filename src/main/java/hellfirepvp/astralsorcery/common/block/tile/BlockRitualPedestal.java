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
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.observerlib.api.block.BlockStructureObserver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ToolType;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockRitualPedestal
 * Created by HellFirePvP
 * Date: 09.07.2019 / 20:03
 */
public class BlockRitualPedestal extends BlockStarlightNetwork implements CustomItemBlock, BlockStructureObserver {

    private final VoxelShape shape;

    public BlockRitualPedestal() {
        super(PropertiesMarble.defaultMarble()
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));

        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        VoxelShape m1 = Block.makeCuboidShape(0, 0, 0, 16, 2, 16);
        VoxelShape m2 = Block.makeCuboidShape(11, 2, 6, 15, 6, 10);
        VoxelShape m3 = Block.makeCuboidShape(12, 2, 12, 14, 7, 14);
        VoxelShape m4 = Block.makeCuboidShape(2, 2, 12, 4, 7, 14);
        VoxelShape m5 = Block.makeCuboidShape(2, 2, 12, 4, 7, 14);
        VoxelShape m6 = Block.makeCuboidShape(12, 2, 2, 14, 7, 4);
        VoxelShape m7 = Block.makeCuboidShape(2, 2, 2, 4, 7, 4);
        VoxelShape m8 = Block.makeCuboidShape(6, 2, 6, 10, 10, 10);
        VoxelShape m9 = Block.makeCuboidShape(2, 10, 2, 14, 12, 14);
        VoxelShape m10 = Block.makeCuboidShape(6, 2, 11, 10, 6, 15);
        VoxelShape m11 = Block.makeCuboidShape(6, 2, 1, 10, 6, 5);
        VoxelShape m12 = Block.makeCuboidShape(3, 12, 11, 5, 14, 13);
        VoxelShape m13 = Block.makeCuboidShape(1, 2, 6, 5, 6, 10);
        VoxelShape m14 = Block.makeCuboidShape(3, 12, 3, 5, 14, 5);
        VoxelShape m15 = Block.makeCuboidShape(11, 12, 3, 13, 14, 5);
        VoxelShape m16 = Block.makeCuboidShape(11, 12, 11, 13, 14, 13);
        VoxelShape m17 = Block.makeCuboidShape(11, 2, 11, 15, 8, 15);
        VoxelShape m18 = Block.makeCuboidShape(11, 2, 1, 15, 8, 5);
        VoxelShape m19 = Block.makeCuboidShape(1, 2, 11, 5, 8, 15);
        VoxelShape m20 = Block.makeCuboidShape(1, 2, 1, 5, 8, 5);

        return VoxelUtils.combineAll(IBooleanFunction.OR, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10,
                m11, m12, m13, m14, m15, m16, m17, m18, m19, m20);
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rtr) {
        if (world.isRemote()) {
            return ActionResultType.SUCCESS;
        }
        TileRitualPedestal pedestal = MiscUtils.getTileAt(world, pos, TileRitualPedestal.class, true);
        if (pedestal == null) {
            return ActionResultType.PASS;
        }

        ItemStack heldItem = player.getHeldItem(hand);

        ItemStack in = pedestal.getCurrentCrystal();
        if (player.isSneaking()) {
            pedestal.tryPlaceCrystalInPedestal(ItemStack.EMPTY);
            if (player.getHeldItem(hand).isEmpty()) {
                player.setHeldItem(hand, in);
            } else {
                player.inventory.placeItemBackInInventory(world, in);
            }
        } else {
            player.setHeldItem(hand, pedestal.tryPlaceCrystalInPedestal(heldItem));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);

        TileRitualPedestal te = MiscUtils.getTileAt(world, pos, TileRitualPedestal.class, true);
        if (te != null && !world.isRemote()) {
            BlockPos toCheck = pos.up();
            BlockState other = world.getBlockState(toCheck);
            if (Block.doesSideFillSquare(other.getCollisionShape(world, pos), Direction.DOWN)) {
                ItemUtils.dropItem(world, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, te.getCurrentCrystal());
                te.tryPlaceCrystalInPedestal(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof Player) {
            TileRitualPedestal pedestal = MiscUtils.getTileAt(world, pos, TileRitualPedestal.class, true);
            if (pedestal != null) {
                pedestal.setOwner(placer.getUniqueID());
            }
        }
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
        return new TileRitualPedestal();
    }
}
