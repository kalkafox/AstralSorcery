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
import hellfirepvp.astralsorcery.common.block.properties.PropertiesGlass;
import hellfirepvp.astralsorcery.common.tile.TileIlluminator;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockIlluminator
 * Created by HellFirePvP
 * Date: 04.04.2020 / 16:50
 */
public class BlockIlluminator extends BaseEntityBlock implements CustomItemBlock {

    private final VoxelShape shape;

    public BlockIlluminator() {
        super(PropertiesGlass.coatedGlass()
                .lightLevel(state -> 10)

);
        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        List<VoxelShape> shapeByIndex = new ArrayList<>();
        for (int xx = 0; xx < 3; xx++) {
            for (int yy = 0; yy < 3; yy++) {
                for (int zz = 0; zz < 3; zz++) {
                    shapeByIndex.add(Block.box(
                            1 + xx * 5, 1 + yy * 5, 1 + zz * 5,
                            5 + xx * 5, 5 + yy * 5, 5 + zz * 5));
                }
            }
        }

        return VoxelUtils.combineAll(BooleanOp.OR, shapeByIndex);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && placer instanceof Player) {
            TileIlluminator illuminator = MiscUtils.getTileAt(level, pos, TileIlluminator.class, true);
            if (illuminator != null)  {
                illuminator.setPlayerPlaced(true);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return this.shape;
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileIlluminator(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return UnsupportedBlockCodec.unsupported();
    }
}