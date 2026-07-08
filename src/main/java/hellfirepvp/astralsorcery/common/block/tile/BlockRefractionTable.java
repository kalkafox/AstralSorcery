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

    private static final VoxelShape REFRACTION_TABLE = Block.makeCuboidShape(-6, 0, -4, 22, 24, 20);
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
        return this.canPlaceAt(context) ? this.getDefaultState() : null;
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
        for (int i = 0; i < rand.nextInt(3); i++) {
            Vector3 offset = new Vector3(-5.0 / 16.0, 1.505, -3.0 / 16.0);
            int random = rand.nextInt(ColorsAS.REFRACTION_TABLE_COLORS.length);
            if (random >= ColorsAS.REFRACTION_TABLE_COLORS.length / 2) { //0-5 is left, 6-11 is right
                offset.addX(24.0 / 16.0);
            }
            offset.addZ((random % (ColorsAS.REFRACTION_TABLE_COLORS.length / 2)) * (4.0 / 16.0));
            offset.add(rand.nextFloat() * 0.1, 0, rand.nextFloat() * 0.1).add(pos);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                    .color(VFXColorFunction.constant(ColorsAS.REFRACTION_TABLE_COLORS[random]))
                    .setMaxAge(35 + rand.nextInt(30));
        }
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getHeldItem(hand);
        if (!world.isRemote()) {
            TileRefractionTable tft = MiscUtils.getTileAt(world, pos, TileRefractionTable.class, true);
            if (tft != null) {
                if (player.isSneaking()) {
                    if (!tft.getInputStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player, tft.setInputStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, player.getPosX(), player.getPosY(), player.getPosZ(), remaining);
                        }
                        return ActionResultType.SUCCESS;
                    }
                    if (!tft.getGlassStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player, tft.setGlassStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, player.getPosX(), player.getPosY(), player.getPosZ(), remaining);
                        }
                        return ActionResultType.SUCCESS;
                    }
                } else if (!held.isEmpty()) {
                    if (held.getItem() instanceof ItemParchment && tft.getParchmentCount() < 64) {
                        int leftover = tft.addParchment(held.getCount());
                        if (leftover < tft.getParchmentCount()) {
                            if (!player.isCreative()) {
                                held.setCount(leftover);
                                if (held.isEmpty()) {
                                    player.setHeldItem(hand, ItemStack.EMPTY);
                                } else {
                                    player.setHeldItem(hand, held);
                                }
                            }
                        }
                    } else if (TileRefractionTable.isValidGlassStack(held) && tft.getGlassStack().isEmpty()) {
                        ItemStack previous = tft.setGlassStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!previous.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, pos.getX() + 0.5, pos.getY() + 1.8, pos.getZ() + 0.5, previous);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            if (held.isEmpty()) {
                                player.setHeldItem(hand, ItemStack.EMPTY);
                            } else {
                                player.setHeldItem(hand, held);
                            }
                        }
                        return ActionResultType.PASS;
                    } else if (tft.getInputStack().isEmpty()) {
                        ItemStack previous = tft.setInputStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!previous.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, pos.getX() + 0.5, pos.getY() + 1.8, pos.getZ() + 0.5, previous);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            if (held.isEmpty()) {
                                player.setHeldItem(hand, ItemStack.EMPTY);
                            } else {
                                player.setHeldItem(hand, held);
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
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        TileRefractionTable te = MiscUtils.getTileAt(world, pos, TileRefractionTable.class, true);
        if (te != null && !world.isRemote) {
            te.dropContents();
        }

        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean allowsMovement(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity createNewTileEntity(BlockGetter worldIn) {
        return new TileRefractionTable();
    }
}
