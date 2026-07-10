/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.block.tile.BlockFlareLight;
import hellfirepvp.astralsorcery.common.block.tile.BlockTranslucentBlock;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.TileIlluminator;
import hellfirepvp.astralsorcery.common.tile.TileTranslucentBlock;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemIlluminationWand
 * Created by HellFirePvP
 * Date: 28.11.2019 / 20:57
 */
public class ItemIlluminationWand extends Item implements ItemDynamicColor, AlignmentChargeConsumer {

    private static final float COST_PER_ILLUMINATION = 650F;
    private static final float COST_PER_FLARE = 300F;

    public ItemIlluminationWand() {
        super(new Properties()
                .maxStackSize(1)
                .group(CommonProxy.ITEM_GROUP_AS));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        DyeColor color = getConfiguredColor(stack);
        tooltip.add(ColorUtils.getTranslation(color).withStyle(ColorUtils.textFormattingForDye(color)));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            return COST_PER_ILLUMINATION;
        } else {
            return COST_PER_FLARE;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Direction dir = context.getFace();
        BlockPos pos = context.getBlockPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (level.isClientSide() || player == null || stack.isEmpty() || !(stack.getItem() instanceof ItemIlluminationWand)) {
            return InteractionResult.SUCCESS;
        }

        BlockState state = level.getBlockState(pos);

        if (player.isShiftKeyDown()) {
            if (state.getBlock() instanceof BlockTranslucentBlock) {
                TileTranslucentBlock tb = MiscUtils.getTileAt(level, pos, TileTranslucentBlock.class, true);
                if (tb != null && (tb.getPlayerUUID() == null || tb.getPlayerUUID().equals(player.getUUID()))) {
                    if (tb.revert()) {
                        SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_UNHIGHLIGHT, SoundSource.BLOCKS, level, pos, 0.6F, 0.9F + random.nextFloat() * 0.2F);
                    }
                }
            } else {
                BlockEntity tile = MiscUtils.getTileAt(level, pos, BlockEntity.class, true);
                if (tile == null &&
                        !state.hasTileEntity() &&
                        player.mayUseItemAt(pos, dir, stack) &&
                        Shapes.block().equals(level.getBlockState(pos).getShape(level, pos))) {
                    if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_ILLUMINATION, false)) {
                        if (level.setBlock(pos, BlocksAS.TRANSLUCENT_BLOCK.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER)) {
                            SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_HIGHLIGHT, SoundSource.BLOCKS, level, pos, 0.6F, 0.9F + random.nextFloat() * 0.2F);
                            TileTranslucentBlock tb = MiscUtils.getTileAt(level, pos, TileTranslucentBlock.class, true);
                            if (tb != null) {
                                tb.setFakedState(state);
                                tb.setOverlayColor(ColorUtils.flareColorFromDye(getConfiguredColor(stack)));
                                tb.setPlayerUUID(player.getUUID());
                            } else {
                                //Abort, we didn't get a tileentity... for some reason.
                                level.setBlock(pos, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
                            }
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        TileIlluminator illum = MiscUtils.getTileAt(level, pos, TileIlluminator.class, true);
        if (illum != null) {
            illum.onWandUsed(stack);
            SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT, SoundSource.BLOCKS, level, pos, 0.6F, 1F);
            return InteractionResult.SUCCESS;
        }

        CollisionContext selContext = CollisionContext.forEntity(player);
        BlockPos placePos = pos;
        BlockState placeState = getPlacingState(stack);
        if (!BlockUtils.isReplaceable(level, pos)) {
            placePos = placePos.offset(dir);
        }

        if (!BlockUtils.isReplaceable(level, placePos)) {
            return InteractionResult.SUCCESS;
        }

        if (player.mayUseItemAt(placePos, dir, stack)) {
            if (level.getBlockState(placePos).equals(placeState)) {
                if (level.setBlock(placePos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER)) {
                    SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT, SoundSource.BLOCKS, level, pos, 0.6F, 1F);
                }
            } else if (placeState.isValidPosition(level, placePos) &&
                    level.noBlockCollision(placeState, placePos, selContext)) {
                if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_FLARE, false)) {
                    if (level.setBlock(placePos, placeState, Constants.BlockFlags.DEFAULT_AND_RERENDER)) {
                        SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT, SoundSource.BLOCKS, level, pos, 0.6F, 1F);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        DyeColor color = getConfiguredColor(stack);
        return ColorUtils.flareColorFromDye(color).getRGB() | 0xFF000000;
    }

    public static void setConfiguredColor(ItemStack stack, DyeColor color) {
        NBTHelper.getPersistentData(stack).putInt("color", color != null ? color.getId() : DyeColor.YELLOW.getId());
    }

    @Nonnull
    public static DyeColor getConfiguredColor(ItemStack stack) {
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (tag.contains("color")) {
            return DyeColor.byId(tag.getInt("color"));
        }
        return DyeColor.YELLOW;
    }

    @Nonnull
    public static BlockState getPlacingState(ItemStack wand) {
        return BlocksAS.FLARE_LIGHT.defaultBlockState().setValue(BlockFlareLight.COLOR, getConfiguredColor(wand));
    }
}
