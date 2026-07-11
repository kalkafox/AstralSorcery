/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesWood;
import hellfirepvp.astralsorcery.common.tile.TileFountain;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFountain
 * Created by HellFirePvP
 * Date: 29.10.2020 / 19:54
 */
public class BlockFountain extends BaseEntityBlock implements CustomItemBlock {

    private final VoxelShape shape;

    public BlockFountain() {
        super(PropertiesWood.defaultInfusedWood());

        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        VoxelShape m1 = Block.box(0, 10, 0, 16, 16, 16);
        VoxelShape m2 = Block.box(4, 6, 4, 12, 10, 12);
        VoxelShape m3 = Block.box(2, 0, 2, 14, 4, 14);
        VoxelShape m4 = Block.box(0, 4, 0, 16, 6, 16);

        return VoxelUtils.combineAll(BooleanOp.OR, m1, m2, m3, m4);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileFountain(pos, state);
    }
}
