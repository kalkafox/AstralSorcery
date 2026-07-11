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
import hellfirepvp.astralsorcery.common.item.block.ItemBlockLens;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.TileLens;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockLens
 * Created by HellFirePvP
 * Date: 24.08.2019 / 22:26
 */
public class BlockLens extends BlockStarlightNetwork implements CustomItemBlock {

    private static final VoxelShape LENS_DOWN =  Shapes.create(2.5D / 16D, 0,          2.5D / 16D, 13.5D / 16D, 14.5D / 16D, 13.5D / 16D);
    private static final VoxelShape LENS_UP =    Shapes.create(2.5D / 16D, 1.5D / 16D, 2.5D / 16D, 13.5D / 16D, 1,           13.5D / 16D);
    private static final VoxelShape LENS_NORTH = Shapes.create(2.5D / 16D, 2.5D / 16D, 0,          13.5D / 16D, 13.5D / 16D, 14.5D / 16D);
    private static final VoxelShape LENS_SOUTH = Shapes.create(2.5D / 16D, 2.5D / 16D, 1.5D / 16D, 13.5D / 16D, 13.5D / 16D, 1);
    private static final VoxelShape LENS_EAST =  Shapes.create(1.5D / 16D, 2.5D / 16D, 2.5D / 16D, 1,           13.5D / 16D, 13.5D / 16D);
    private static final VoxelShape LENS_WEST =  Shapes.create(0,          2.5D / 16D, 2.5D / 16D, 14.5D / 16D, 13.5D / 16D, 13.5D / 16D);

    public static EnumProperty<Direction> PLACED_AGAINST = EnumProperty.create("against", Direction.class);

    public BlockLens() {
        super(PropertiesGlass.coatedGlass()
);
        registerDefaultState(this.getStateContainer().any().setValue(PLACED_AGAINST, Direction.DOWN));
    }

    @Override
    public Class<? extends BlockItem> getItemBlockClass() {
        return ItemBlockLens.class;
    }

    @Override
    public void onBlockHarvested(Level level, BlockPos pos, BlockState state, Player player) {
        TileLens lens = MiscUtils.getTileAt(level, pos, TileLens.class, true);
        if (lens != null && !level.isClientSide() && !player.isCreative()) {
            if (lens.getColorType() != null) {
                ItemStack drop = lens.getColorType().getStack();
                ItemUtils.dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
            }
        }
        super.onBlockHarvested(level, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player.isShiftKeyDown()) {
            TileLens lens = MiscUtils.getTileAt(level, pos, TileLens.class, true);
            if (lens != null && lens.getColorType() != null) {
                ItemStack drop = lens.getColorType().getStack();
                if (player.getItemInHand(hand).isEmpty()) {
                    player.setItemInHand(hand, drop);
                } else {
                    if (!player.getInventory().getArmor(drop)) {
                        ItemUtils.dropItem(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                    }
                }
                SoundHelper.playSoundAround(SoundsAS.BLOCK_COLOREDLENS_ATTACH, level, pos, 0.8F, 1.5F);
                lens.setColorType(null);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLACED_AGAINST);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(PLACED_AGAINST, context.getClickedFace().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.get(PLACED_AGAINST)) {
            case UP:
                return LENS_UP;
            case NORTH:
                return LENS_NORTH;
            case SOUTH:
                return LENS_SOUTH;
            case WEST:
                return LENS_WEST;
            case EAST:
                return LENS_EAST;
            default:
            case DOWN:
                return LENS_DOWN;
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderType(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter worldIn) {
        return new TileLens();
    }
}
