/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile.altar;

import hellfirepvp.astralsorcery.common.block.base.LargeBlock;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAltarRadiance
 * Created by HellFirePvP
 * Date: 12.08.2019 / 21:59
 */
public class BlockAltarRadiance extends BlockAltar implements LargeBlock {

    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 1, 1);

    private final VoxelShape shape;

    public BlockAltarRadiance() {
        super(AltarType.RADIANCE);
        this.shape = createShape();
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

    protected VoxelShape createShape() {
        VoxelShape base = Block.box(0, 0, 0, 16, 4, 16);
        VoxelShape pillar = Block.box(4, 4, 4, 12, 8, 12);
        VoxelShape head = Block.box(0, 8, 0, 16, 16, 16);

        return VoxelUtils.combineAll(BooleanOp.OR, base, pillar, head);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return this.shape;
    }
}
