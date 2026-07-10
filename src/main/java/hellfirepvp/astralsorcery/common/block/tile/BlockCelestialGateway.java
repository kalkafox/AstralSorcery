/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesGlass;
import hellfirepvp.astralsorcery.common.item.ItemAquamarine;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.observerlib.api.block.BlockStructureObserver;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ForgeHooks;
import net.neoforged.neoforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockCelestialGateway
 * Created by HellFirePvP
 * Date: 10.09.2020 / 16:46
 */
public class BlockCelestialGateway extends BaseEntityBlock implements CustomItemBlock, BlockStructureObserver {

    private static final VoxelShape SHAPE = Shapes.create(1D / 16D, 0D / 16D, 1D / 16D, 15D / 16D, 1D / 16D, 15D / 16D);

    public BlockCelestialGateway() {
        super(PropertiesGlass.coatedGlass()
                .isRedstoneConductor((state) -> 12)
                .hardnessAndResistance(-1F, 3600000.0F)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        DyeColor color = getColor(stack);
        if (color != null) {
            tooltip.add(ColorUtils.getTranslation(color).withStyle(ColorUtils.textFormattingForDye(color)));
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack stack = new ItemStack(BlocksAS.GATEWAY);
        TileCelestialGateway gateway = MiscUtils.getTileAt(level, pos, TileCelestialGateway.class, true);
        if (gateway != null) {
            if (gateway.hasCustomName()) {
                stack.setLastHealthTime(gateway.getDisplayName());
            }
            gateway.getColor().ifPresent(color -> setColor(stack, color));
        }
        return stack;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TileCelestialGateway gateway = MiscUtils.getTileAt(level, pos, TileCelestialGateway.class, false);
        if (gateway != null &&
                gateway.getOwner() != null &&
                gateway.getOwner().isAlwaysExperienceDropper(player)) {

            if (gateway.isLocked()) {
                if (!level.isClientSide()) {
                    ItemStack remaining = ItemUtils.dropItemToPlayer(player, new ItemStack(ItemsAS.AQUAMARINE));
                    if (!remaining.isEmpty()) {
                        ItemUtils.dropItemNaturally(level, player.getX(), player.getY(), player.getZ(), remaining);
                    }
                    gateway.unlock();
                }
                return InteractionResult.SUCCESS;
            } else {
                ItemStack held = player.getItemInHand(hand);
                if (held.getItem() instanceof ItemAquamarine) {
                    if (!level.isClientSide()) {
                        held.shrink(1);
                        gateway.lock();
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        TileCelestialGateway gateway = MiscUtils.getTileAt(level, pos, TileCelestialGateway.class, true);
        if (gateway != null) {
            if (stack.hasCustomHoverName()) {
                gateway.setDisplayText(stack.getDisplayName());
            }
            DyeColor color = getColor(stack);
            if (color != null) {
                gateway.setColor(color);
            }
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        TileCelestialGateway gateway = MiscUtils.getTileAt(level, pos, TileCelestialGateway.class, true);
        if (gateway != null) {
            if (!gateway.isLocked() || (gateway.getOwner() != null && gateway.getOwner().isAlwaysExperienceDropper(player))) {
                int i = ForgeHooks.isCorrectToolForDrops(state, player, level, pos) ? 30 : 100;
                return player.getDigSpeed(state, pos) / 2.5F / i;
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    //TODO custom states via state container
    //@Override
    //public float getBlockHardness(BlockState blockState, BlockGetter world, BlockPos pos) {
    //    TileCelestialGateway gateway = MiscUtils.getTileAt(world, pos, TileCelestialGateway.class, true);
    //    if (gateway != null && gateway.isLocked() && gateway.getOwner() != null) {
    //        //Assume this is non-player related hardness. In which case, it cannot be mined to begin with.
    //        return -1;
    //    }
    //    return this.blockHardness;
    //}

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean hasPos) {
        if (state != newState && !level.isClientSide()) {
            DataAS.DOMAIN_AS.getData(level, DataAS.KEY_GATEWAY_CACHE).removePosition(level, pos);
            TileCelestialGateway gateway = MiscUtils.getTileAt(level, pos, TileCelestialGateway.class, true);
            if (gateway != null && gateway.isLocked()) {
                ItemUtils.dropItemNaturally(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ItemsAS.AQUAMARINE));
            }
        }

        super.onRemove(state, level, pos, newState, hasPos);
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
        TileCelestialGateway gateway = MiscUtils.getTileAt(level, pos, TileCelestialGateway.class, true);
        if (gateway != null && gateway.isLocked()) {
            return true;
        }
        return hasSolidSideOnTop(level, pos.below());
    }

    @Nullable
    public static DyeColor getColor(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem) ||
                !(((BlockItem) stack.getItem()).getBlock() instanceof BlockCelestialGateway)) {
            return null;
        }

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (!tag.contains("color")) {
            return null;
        }
        return NBTHelper.readEnum(tag, "color", DyeColor.class);
    }

    public static void setColor(ItemStack stack, @Nullable DyeColor color) {
        if (!(stack.getItem() instanceof BlockItem) ||
                !(((BlockItem) stack.getItem()).getBlock() instanceof BlockCelestialGateway)) {
            return;
        }

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (color == null) {
            tag.remove("color");
        } else {
            NBTHelper.writeEnum(tag, "color", color);
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
    public BlockEntity newBlockEntity(BlockGetter level) {
        return new TileCelestialGateway();
    }
}
