/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.blackmarble;

import hellfirepvp.astralsorcery.common.block.base.template.BlockBlackMarbleTemplate;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockBlackMarblePillar
 * Created by HellFirePvP
 * Date: 20.07.2019 / 19:49
 */
public class BlockBlackMarblePillar extends BlockBlackMarbleTemplate implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<PillarType> PILLAR_TYPE = EnumProperty.create("pillartype", PillarType.class);

    private final VoxelShape middleShape, bottomShape, topShape;

    public BlockBlackMarblePillar() {
        this.registerDefaultState(this.getStateDefinition().any().setValue(PILLAR_TYPE, PillarType.MIDDLE).setValue(WATERLOGGED, false));
        this.middleShape = createPillarShape();
        this.topShape    = createPillarTopShape();
        this.bottomShape = createPillarBottomShape();
    }

    protected VoxelShape createPillarShape() {
        return Block.box(2, 0, 2, 14, 16, 14);
    }

    protected VoxelShape createPillarTopShape() {
        VoxelShape x = Block.box(2, 0, 2, 14, 12, 14);
        VoxelShape top = Block.box(0, 12, 0, 16, 16, 16);

        return VoxelUtils.combineAll(BooleanOp.OR,
                x, top);
    }

    protected VoxelShape createPillarBottomShape() {
        VoxelShape x = Block.box(2, 4, 2, 14, 16, 14);
        VoxelShape bottom = Block.box(0, 0, 0, 16, 4, 16);

        return VoxelUtils.combineAll(BooleanOp.OR,
                x, bottom);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PILLAR_TYPE, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        switch (state.getValue(PILLAR_TYPE)) {
            case TOP:
                return this.topShape;
            case BOTTOM:
                return this.bottomShape;
            default:
            case MIDDLE:
                return this.middleShape;
        }
    }

    @Override
    public BlockState updateShape(BlockState thisState, Direction otherBlockFacing, BlockState otherBlockState, LevelAccessor level, BlockPos thisPos, BlockPos otherBlockPos) {
        if (thisState.getValue(WATERLOGGED)) {
            level.scheduleTick(thisPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return this.getThisState(level, thisPos).setValue(WATERLOGGED, thisState.getValue(WATERLOGGED));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos blockpos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        FluidState fluidState = level.getFluidState(blockpos);
        return this.getThisState(level, blockpos).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    private BlockState getThisState(BlockGetter level, BlockPos pos) {
        boolean hasUp   = level.getBlockState(pos.above()).getBlock()   instanceof BlockBlackMarblePillar;
        boolean hasDown = level.getBlockState(pos.below()).getBlock() instanceof BlockBlackMarblePillar;
        if (hasUp) {
            if (hasDown) {
                return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.MIDDLE);
            }
            return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.BOTTOM);
        } else if (hasDown) {
            return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.TOP);
        }
        return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.MIDDLE);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob entity) {
        return PathType.BLOCKED;
    }


    public static enum PillarType implements StringRepresentable {

        TOP,
        MIDDLE,
        BOTTOM;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return this.getSerializedName();
        }
    }
}
