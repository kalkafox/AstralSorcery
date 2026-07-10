/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockDynamicColor;
import hellfirepvp.astralsorcery.common.block.base.BlockStarlightNetwork;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesGlass;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockPrism;
import hellfirepvp.astralsorcery.common.item.lens.LensColorType;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.TilePrism;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ToolType;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockPrism
 * Created by HellFirePvP
 * Date: 24.08.2019 / 23:10
 */
public class BlockPrism extends BlockStarlightNetwork implements CustomItemBlock, BlockDynamicColor {

    private static final VoxelShape PRISM_DOWN =  Shapes.create(3D / 16D, 0,      3D / 16D, 13D / 16D, 14D / 16D, 13D / 16D);
    private static final VoxelShape PRISM_UP =    Shapes.create(3D / 16D, 2D / 16D, 3D / 16D, 13D / 16D, 1,       13D / 16D);
    private static final VoxelShape PRISM_NORTH = Shapes.create(3D / 16D, 3D / 16D, 0,      13D / 16D, 13D / 16D, 14D / 16D);
    private static final VoxelShape PRISM_SOUTH = Shapes.create(3D / 16D, 3D / 16D, 2D / 16D, 13D / 16D, 13D / 16D, 1);
    private static final VoxelShape PRISM_EAST =  Shapes.create(2D / 16D, 3D / 16D, 3D / 16D, 1,       13D / 16D, 13D / 16D);
    private static final VoxelShape PRISM_WEST =  Shapes.create(0,      3D / 16D, 3D / 16D, 14D / 16D, 13D / 16D, 13D / 16D);
    
    public static EnumProperty<Direction> PLACED_AGAINST = EnumProperty.create("against", Direction.class);
    public static BooleanProperty HAS_COLORED_LENS = BooleanProperty.create("has_lens");

    public BlockPrism() {
        super(PropertiesGlass.coatedGlass()
                .harvestTool(ToolType.PICKAXE));
        registerDefaultState(this.getStateContainer().any().setValue(PLACED_AGAINST, Direction.DOWN).setValue(HAS_COLORED_LENS, false));
    }

    @Override
    public Class<? extends BlockItem> getItemBlockClass() {
        return ItemBlockPrism.class;
    }

    @Override
    public void onBlockHarvested(Level level, BlockPos pos, BlockState state, Player player) {
        TilePrism lens = MiscUtils.getTileAt(level, pos, TilePrism.class, true);
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
            TilePrism lens = MiscUtils.getTileAt(level, pos, TilePrism.class, true);
            if (lens != null && lens.getColorType() != null) {
                ItemStack drop = lens.getColorType().getStack();
                if (!player.isCreative()) {
                    if (player.getItemInHand(hand).isEmpty()) {
                        player.setHeldItem(hand, drop);
                    } else {
                        if (!player.inventory.getArmor(drop)) {
                            ItemUtils.dropItem(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                        }
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
        builder.add(PLACED_AGAINST, HAS_COLORED_LENS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(PLACED_AGAINST, context.getFace().getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 3) { //prism_colored_all.json
            return 0xFFFFFFFF;
        }
        TilePrism prism = MiscUtils.getTileAt(level, pos, TilePrism.class, false);
        if (prism != null) {
            LensColorType type = prism.getColorType();
            if (type != null) {
                return type.getColor().getRGB();
            }
        }
        return 0xFFFFFFFF;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.get(PLACED_AGAINST)) {
            case UP:
                return PRISM_UP;
            case NORTH:
                return PRISM_NORTH;
            case SOUTH:
                return PRISM_SOUTH;
            case WEST:
                return PRISM_WEST;
            case EAST:
                return PRISM_EAST;
            default:
            case DOWN:
                return PRISM_DOWN;
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
        return new TilePrism();
    }
}
