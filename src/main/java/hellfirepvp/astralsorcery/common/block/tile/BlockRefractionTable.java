/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.base.LargeBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesWood;
import hellfirepvp.astralsorcery.common.item.ItemParchment;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.tile.TileRefractionTable;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockRefractionTable
 * Created by HellFirePvP
 * Date: 26.04.2020 / 20:17
 */
public class BlockRefractionTable extends BaseEntityBlock implements CustomItemBlock, LargeBlock {

    private static final VoxelShape REFRACTION_TABLE = Block.box(-6, 0, -4, 22, 24, 20);
    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 1, 1);

    public BlockRefractionTable() {
        super(PropertiesWood.defaultInfusedWood()
                .notSolid());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return REFRACTION_TABLE;
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

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random random) {
        for (int i = 0; i < random.nextInt(3); i++) {
            Vector3 offset = new Vector3(-5.0 / 16.0, 1.505, -3.0 / 16.0);
            int colorIndex = random.nextInt(ColorsAS.REFRACTION_TABLE_COLORS.length);
            if (colorIndex >= ColorsAS.REFRACTION_TABLE_COLORS.length / 2) { //0-5 is left, 6-11 is right
                offset.addX(24.0 / 16.0);
            }
            offset.addZ((colorIndex % (ColorsAS.REFRACTION_TABLE_COLORS.length / 2)) * (4.0 / 16.0));
            offset.add(random.nextFloat() * 0.1, 0, random.nextFloat() * 0.1).add(pos);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.constant(ColorsAS.REFRACTION_TABLE_COLORS[colorIndex]))
                    .setMaxAge(35 + random.nextInt(30));
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            TileRefractionTable tft = MiscUtils.getTileAt(level, pos, TileRefractionTable.class, true);
            if (tft != null) {
                if (player.isShiftKeyDown()) {
                    if (!tft.getInputStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player, tft.setInputStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, player.getX(), player.getY(), player.getZ(), remaining);
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                    if (!tft.getGlassStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player, tft.setGlassStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, player.getX(), player.getY(), player.getZ(), remaining);
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                } else if (!held.isEmpty()) {
                    if (held.getItem() instanceof ItemParchment && tft.getParchmentCount() < 64) {
                        int leftover = tft.addParchment(held.getCount());
                        if (leftover < tft.getParchmentCount()) {
                            if (!player.isCreative()) {
                                held.setCount(leftover);
                                if (held.isEmpty()) {
                                    player.setItemInHand(hand, ItemStack.EMPTY);
                                } else {
                                    player.setItemInHand(hand, held);
                                }
                            }
                        }
                    } else if (TileRefractionTable.isValidGlassStack(held) && tft.getGlassStack().isEmpty()) {
                        ItemStack cameFrom = tft.setGlassStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!cameFrom.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 1.8, pos.getZ() + 0.5, cameFrom);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            if (held.isEmpty()) {
                                player.setItemInHand(hand, ItemStack.EMPTY);
                            } else {
                                player.setItemInHand(hand, held);
                            }
                        }
                        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
                    } else if (tft.getInputStack().isEmpty()) {
                        ItemStack cameFrom = tft.setInputStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!cameFrom.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 1.8, pos.getZ() + 0.5, cameFrom);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            if (held.isEmpty()) {
                                player.setItemInHand(hand, ItemStack.EMPTY);
                            } else {
                                player.setItemInHand(hand, held);
                            }
                        }
                    } else {
                        AstralSorcery.getProxy().openGui(player, GuiType.REFRACTION_TABLE, pos);
                    }
                } else {
                    AstralSorcery.getProxy().openGui(player, GuiType.REFRACTION_TABLE, pos);
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        TileRefractionTable te = MiscUtils.getTileAt(level, pos, TileRefractionTable.class, true);
        if (te != null && !level.isClientSide) {
            te.dropContents();
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRefractionTable(pos, state);
    }
}
