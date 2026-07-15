/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockCrystalContainer;
import hellfirepvp.astralsorcery.common.block.base.BlockStarlightRecipient;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.tile.crystal.CollectorCrystalType;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.TileCelestialCrystals;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockCelestialCrystalCluster
 * Created by HellFirePvP
 * Date: 30.09.2019 / 18:00
 */
public class BlockCelestialCrystalCluster extends BlockCrystalContainer implements BlockStarlightRecipient, CustomItemBlock {

    private static final VoxelShape GROWTH_STAGE_0 = Block.box(4, 0, 5, 12, 8, 11);
    private static final VoxelShape GROWTH_STAGE_1 = Block.box(4, 0, 5, 12, 10, 11);
    private static final VoxelShape GROWTH_STAGE_2 = Block.box(2, 0, 4, 12, 12, 14);
    private static final VoxelShape GROWTH_STAGE_3 = Block.box(2, 0, 2, 14, 14, 14);
    private static final VoxelShape GROWTH_STAGE_4 = Block.box(2, 0, 2, 14, 16, 14);

    public static IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);

    public BlockCelestialCrystalCluster() {
        super(Properties.of().mapColor(CollectorCrystalType.CELESTIAL_CRYSTAL.getMaterialColor())
                .strength(3F, 3F)


                .sound(SoundType.GLASS)
                .lightLevel((state) -> 8)
                .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    @Override
    public Class<? extends BlockItem> getItemBlockClass() {
        return ItemBlockCelestialCrystalCluster.class;
    }

    @Override
    public void receiveStarlight(Level level, Random random, BlockPos pos, IWeakConstellation starlightType, double amount) {
        TileCelestialCrystals respawnCrystals = MiscUtils.getTileAt(level, pos, TileCelestialCrystals.class, false);
        if (respawnCrystals != null) {
            respawnCrystals.grow((int) (TileCelestialCrystals.TICK_GROWTH_CHANCE / amount));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        VoxelShape shape;
        switch (state.getValue(STAGE)) {
            case 4:
                shape = GROWTH_STAGE_4;
                break;
            case 3:
                shape = GROWTH_STAGE_3;
                break;
            case 2:
                shape = GROWTH_STAGE_2;
                break;
            case 1:
                shape = GROWTH_STAGE_1;
                break;
            case 0:
            default:
                shape = GROWTH_STAGE_0;
        }
        return shape.move(offset.x, offset.y, offset.z);
    }

    /*
    TODO custom states via state container
    @Override
    public Vec3 getOffset(BlockState state, BlockGetter world, BlockPos pos) {
        return super.getOffset(state, world, pos).mul(0.7, 0.7, 0.7);
    }*/

    @Override
    public BlockState updateShape(BlockState state, Direction placedAgainst, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (!this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportRigidBlock(level, pos.below());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            super.onRemove(state, level, pos, newState, isMoving);

            PktPlayEffect effect = new PktPlayEffect(PktPlayEffect.Type.SMALL_CRYSTAL_BREAK)
                    .addData(buf -> ByteBufUtils.writeVector(buf,
                            new Vector3(pos).add(state.getOffset(level, pos)).add(0.5, 0.4, 0.5)));
            PacketChannel.CHANNEL.sendToAllAround(effect, PacketChannel.pointFromPos(level, pos, 32));
        }
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
        return new TileCelestialCrystals(pos, state);
    }
}
