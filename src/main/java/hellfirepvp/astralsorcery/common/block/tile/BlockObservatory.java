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
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import hellfirepvp.astralsorcery.common.container.factory.ContainerObservatoryProvider;
import hellfirepvp.astralsorcery.common.tile.TileObservatory;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
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
 * Class: BlockObservatory
 * Created by HellFirePvP
 * Date: 16.02.2020 / 08:11
 */
public class BlockObservatory extends BaseEntityBlock implements LargeBlock, CustomItemBlock {

    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 3, 1);

    public BlockObservatory() {
        super(PropertiesMisc.defaultGoldMachinery()


                .noOcclusion()
                .strength(3F, 4F));
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
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (!worldIn.isClientSide()) {
            TileObservatory observatory = MiscUtils.getTileAt(worldIn, pos, TileObservatory.class, false);
            if (observatory != null && observatory.isFlyEnabled() && !player.isShiftKeyDown()) {
                Entity entity = observatory.findRideableObservatoryEntity();
                if (entity != null) {
                    if (player.getVehicle() != entity) {
                        player.startRiding(entity);
                    }
                    new ContainerObservatoryProvider(observatory).openFor((ServerPlayer) player);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileObservatory(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return UnsupportedBlockCodec.unsupported();
    }
}