/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import com.mojang.serialization.MapCodec;
import hellfirepvp.astralsorcery.common.block.base.UnsupportedBlockCodec;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.base.LargeBlock;
import hellfirepvp.astralsorcery.common.block.base.TickingEntityBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAttunementAltar
 * Created by HellFirePvP
 * Date: 17.11.2019 / 07:43
 */
public class BlockAttunementAltar extends BaseEntityBlock implements CustomItemBlock, LargeBlock, TickingEntityBlock {

    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 1, 1);
    private static final VoxelShape ATTUNEMENT_ALTAR = Block.box(-2, 0, -2, 18, 6, 18);
    private static final VoxelShape ATTUNEMENT_ALTAR_COLLISION = Block.box(0, 0, 0, 16, 6, 16);

    public BlockAttunementAltar() {
        super(PropertiesMarble.defaultMarble()
                .lightLevel((state) -> 4)

);
    }

    @Override
    public AABB getBlockSpace() {
        return PLACEMENT_BOX;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.canPlaceAt(context) ? this.defaultBlockState() : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ATTUNEMENT_ALTAR;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ATTUNEMENT_ALTAR_COLLISION;
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileAttunementAltar(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return UnsupportedBlockCodec.unsupported();
    }
}