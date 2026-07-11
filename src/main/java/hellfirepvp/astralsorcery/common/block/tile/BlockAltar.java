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
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.block.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.container.factory.*;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAltar
 * Created by HellFirePvP
 * Date: 12.08.2019 / 20:00
 */
public abstract class BlockAltar extends BlockStarlightNetwork implements CustomItemBlock {

    private final AltarType type;

    public BlockAltar(AltarType type) {
        super(PropertiesMarble.defaultMarble()

);

        this.type = type;
    }

    public AltarType getAltarType() {
        return type;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer) {
            TileAltar altar = MiscUtils.getTileAt(level, pos, TileAltar.class, true);
            if (altar != null) {
                CustomContainerProvider<?> provider;
                switch (altar.getAltarType()) {
                    case DISCOVERY:
                        provider = new ContainerAltarDiscoveryProvider(altar);

                        if (!ResearchHelper.getProgress(player, LogicalSide.SERVER)
                                .getTierReached().isThisLaterOrEqual(ProgressionTier.BASIC_CRAFT)) {
                            ResearchManager.informCrafted(player, new ItemStack(BlocksAS.ALTAR_DISCOVERY));
                        }
                        break;
                    case ATTUNEMENT:
                        provider = new ContainerAltarAttunementProvider(altar);
                        break;
                    case CONSTELLATION:
                        provider = new ContainerAltarConstellationProvider(altar);
                        break;
                    case RADIANCE:
                        provider = new ContainerAltarRadianceProvider(altar);
                        break;
                    default:
                        provider = null;
                        break;
                }

                if (provider != null) {
                    provider.openFor((ServerPlayer) player);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!(newState.getBlock() instanceof BlockAltar)) {
            TileAltar ta = MiscUtils.getTileAt(worldIn, pos, TileAltar.class, true);
            if (ta != null && !worldIn.isClientSide) {
                ItemUtils.dropEquipment(ta.getItems(), worldIn, pos);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        } else {
            AltarType thisType = ((BlockAltar)    state.getBlock()).type;
            AltarType thatType = ((BlockAltar) newState.getBlock()).type;
            if (thisType != thatType) {
                TileAltar ta = MiscUtils.getTileAt(worldIn, pos, TileAltar.class, true);
                if (ta != null) {
                    ta.updateType(thatType, false);
                }
            }
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
        return new TileAltar(pos, state).updateType(this.type, true);
    }
}
