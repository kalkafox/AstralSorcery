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
import hellfirepvp.astralsorcery.common.block.properties.PropertiesGlass;
import hellfirepvp.astralsorcery.common.tile.TileSpectralRelay;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockSpectralRelay
 * Created by HellFirePvP
 * Date: 14.08.2019 / 06:53
 */
public class BlockSpectralRelay extends BlockStarlightNetwork implements CustomItemBlock {

    private static final VoxelShape RELAY = Block.box(2, 0, 2, 14, 2, 14);

    public BlockSpectralRelay() {
        super(PropertiesGlass.coatedGlass()
                .isRedstoneConductor(state -> 4));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RELAY;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            ItemStack held = player.getItemInHand(hand);
            TileSpectralRelay tar = MiscUtils.getTileAt(level, pos, TileSpectralRelay.class, true);
            if (tar != null) {
                TileInventory inv = tar.getItems();
                if (!held.isEmpty()) {
                    if (!inv.getStackInSlot(0).isEmpty()) {
                        ItemStack stack = inv.getStackInSlot(0);
                        player.inventory.hurtArmor(level, stack);
                        inv.setStackInSlot(0, ItemStack.EMPTY);
                        tar.markForUpdate();
                        TileSpectralRelay.cascadeRelayProximityUpdates(level, pos);
                    }

                    if (!level.isEmptyBlock(pos.above())) {
                        return InteractionResult.PASS;
                    }

                    inv.setStackInSlot(0, ItemUtils.copyStackWithSize(held, 1));
                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    if (!player.isCreative()) {
                        held.shrink(1);
                    }
                    tar.updateAltarLinkState();
                    TileSpectralRelay.cascadeRelayProximityUpdates(level, pos);
                    tar.markForUpdate();
                } else {
                    if (!inv.getStackInSlot(0).isEmpty()) {
                        ItemStack stack = inv.getStackInSlot(0);
                        player.inventory.hurtArmor(level, stack);
                        inv.setStackInSlot(0, ItemStack.EMPTY);
                        TileSpectralRelay.cascadeRelayProximityUpdates(level, pos);
                        tar.markForUpdate();
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, worldIn, pos, newState, isMoving);
        if (!worldIn.isClientSide()) {
            TileSpectralRelay.cascadeRelayProximityUpdates(worldIn, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction placedAgainst, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (!this.isValidPosition(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean isValidPosition(BlockState state, LevelReader level, BlockPos pos) {
        return hasSolidSideOnTop(level, pos.below());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState state, Level level, BlockPos pos) {
        TileSpectralRelay tsr = MiscUtils.getTileAt(level, pos, TileSpectralRelay.class, false);
        if (tsr != null) {
            return tsr.getItems().getStackInSlot(0).isEmpty() ? 0 : 15;
        }
        return 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderType(BlockState p_149645_1_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter worldIn) {
        return new TileSpectralRelay();
    }
}
