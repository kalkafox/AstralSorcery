/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockInventory;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.tile.TileInfuser;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ToolType;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockInfuser
 * Created by HellFirePvP
 * Date: 09.11.2019 / 19:22
 */
public class BlockInfuser extends BlockInventory implements CustomItemBlock {

    private static final VoxelShape INFUSER = VoxelShapes.create(0D / 16D, 0D / 16D, 0D / 16D, 16D / 16D, 12D / 16D, 16D / 16D);

    public BlockInfuser() {
        super(PropertiesMarble.defaultMarble()
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return INFUSER;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isRemote) {
            ItemStack held = player.getHeldItem(hand);
            TileInfuser ti = MiscUtils.getTileAt(world, pos, TileInfuser.class, true);
            if (ti != null) {
                ItemStack stored = ti.getItemInput();
                if (!held.isEmpty()) {
                    if (!stored.isEmpty()) {
                        player.inventory.placeItemBackInInventory(world, stored);
                        ti.setItemInput(ItemStack.EMPTY);
                        ti.markForUpdate();
                    }

                    if (!world.isAirBlock(pos.up())) {
                        return ActionResultType.PASS;
                    }

                    ti.setItemInput(ItemUtils.copyStackWithSize(held, 1));
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    if (!player.isCreative()) {
                        held.shrink(1);
                    }
                    ti.markForUpdate();
                } else {
                    if (!stored.isEmpty()) {
                        player.inventory.placeItemBackInInventory(world, stored);
                        ti.setItemInput(ItemStack.EMPTY);
                        ti.markForUpdate();
                    }
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState state, Level world, BlockPos pos) {
        TileInfuser ti = MiscUtils.getTileAt(world, pos, TileInfuser.class, false);
        if (ti != null) {
            return ti.getItemInput().isEmpty() ? 0 : 15;
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
        return new TileInfuser();
    }
}
