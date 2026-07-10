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
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import hellfirepvp.astralsorcery.common.tile.TileTreeBeacon;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ToolType;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTreeBeacon
 * Created by HellFirePvP
 * Date: 04.09.2020 / 21:13
 */
public class BlockTreeBeacon extends BlockStarlightNetwork implements CustomItemBlock {

    private static final VoxelShape SHAPE = Shapes.create(3D / 16D, 0D / 16D, 3D / 16D, 13D / 16D, 16D / 16D, 13D / 16D);

    public BlockTreeBeacon() {
        super(PropertiesMisc.defaultPlant()
                .hardnessAndResistance(1.5F, 6.0F)
                .harvestLevel(1)
                .harvestTool(ToolType.AXE)
                .isRedstoneConductor(state -> 6)
                .sound(SoundType.PLANT));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        TileTreeBeacon ttb = MiscUtils.getTileAt(level, pos, TileTreeBeacon.class, true);
        if (ttb != null && !level.isClientSide() && placer instanceof ServerPlayer && !MiscUtils.isPlayerFakeMP((ServerPlayer) placer)) {
            ttb.setPlayerUUID(placer.getUUID());
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderType(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter level) {
        return new TileTreeBeacon();
    }
}
