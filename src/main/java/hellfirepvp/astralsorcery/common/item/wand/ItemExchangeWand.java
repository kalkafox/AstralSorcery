/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingOverlayUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.data.config.entry.WandsConfig;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import hellfirepvp.astralsorcery.common.item.base.client.ItemHeldRender;
import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemExchangeWand
 * Created by HellFirePvP
 * Date: 28.02.2020 / 21:04
 */
public class ItemExchangeWand extends Item implements ItemBlockStorage, ItemOverlayRender, ItemHeldRender, AlignmentChargeConsumer {

    private static final float COST_PER_EXCHANGE = 5F;

    public ItemExchangeWand() {
        super(new Properties()
                .stacksTo(1)
);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(getSizeMode(stack).getDisplay().withStyle(ChatFormatting.GOLD));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0;
    }

    // 1.21 port: harvest tool/level overrides are gone; isCorrectToolForDrops
    // below already grants full harvest capability.

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        BlockHitResult location = MiscUtils.rayTraceLookBlock(player, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        if (location == null) {
            return 0F;
        }
        return getPlaceStates(player, player.getCommandSenderWorld(), location.getBlockPos(), stack).size() * COST_PER_EXCHANGE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderInHand(ItemStack stack, PoseStack renderStack, float pTicks) {
        BlockHitResult location = MiscUtils.rayTraceLookBlock(Minecraft.getInstance().player, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        if (location == null) {
            return true;
        }
        Level level = Minecraft.getInstance().level;
        BlockPos at = location.getBlockPos();
        Map<BlockPos, BlockState> placeStates = getPlaceStates(Minecraft.getInstance().player, level, at, stack);
        if (placeStates.isEmpty()) {
            return true;
        }

        BlockAtlasTexture.getInstance().bindTexture();

        int[] fullBright = new int[] { 15, 15 };
        BufferDecoratorBuilder decorator = BufferDecoratorBuilder.withLightmap((skyLight, blockLight) -> fullBright);
        Vector3 offset = RenderingVectorUtils.getStandardTranslationRemovalVector(pTicks);

        RenderSystem.enableBlend();
        Blending.ADDITIVEDARK.apply();
        RenderSystem.disableDepthTest();

        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK, buf -> {
            placeStates.forEach((pos, state) -> {
                renderStack.pushPose();
                renderStack.translate(pos.getX() - offset.getX() + 0.1F, pos.getY() - offset.getY() + 0.1F, pos.getZ() - offset.getZ() + 0.1F);
                renderStack.scale(0.8F, 0.8F, 0.8F);
                RenderingUtils.renderSimpleBlockModel(state, renderStack, decorator.decorate(buf), pos, null, false);
                renderStack.popPose();
            });
        });

        RenderSystem.enableDepthTest();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderOverlay(PoseStack renderStack, ItemStack stack, float pTicks) {
        List<Tuple<ItemStack, Integer>> foundStacks = ItemBlockStorage.getInventoryMatchingItemStacks(Minecraft.getInstance().player, stack);
        RenderingOverlayUtils.renderDefaultItemDisplay(renderStack, foundStacks);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide() || !(player instanceof ServerPlayer) || stack.isEmpty()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            ItemBlockStorage.storeBlockState(stack, level, pos);
            return InteractionResult.SUCCESS;
        }

        // availableStacks should already contain enough to fill whatever placeStates has precalculated
        Map<BlockPos, BlockState> placeStates = getPlaceStates(player, level, pos, stack);
        Map<BlockState, Tuple<ItemStack, Integer>> availableStacks = MapStream.of(ItemBlockStorage.getInventoryMatching(player, stack))
                .filter(tpl -> placeStates.containsValue(tpl.getA()))
                .collect(Collectors.toMap(Tuple::getA, Tuple::getB));

        for (BlockPos placePos : placeStates.keySet()) {
            BlockState stateToPlace = placeStates.get(placePos);
            Tuple<ItemStack, Integer> availableStack = availableStacks.get(stateToPlace);
            if (availableStack == null) {
                continue;
            }

            ItemStack extractable = ItemUtils.copyStackWithSize(availableStack.getA(), 1);
            boolean canExtract = player.isCreative();
            if (!canExtract) {
                if (ItemUtils.consumeFromPlayerInventory(player, stack, extractable, true)) {
                    canExtract = true;
                }
            }
            if (!canExtract) {
                continue;
            }

            BlockState prevState = level.getBlockState(placePos);
            if ((player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, stack, extractable, true)) &&
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_EXCHANGE, false) &&
                    ((ServerPlayer) player).gameMode.destroyBlock(placePos) &&
                    MiscUtils.canPlayerPlaceBlockPos(player, stateToPlace, placePos, Direction.UP) &&
                    (player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, stack, extractable, false)) &&
                    level.setBlockAndUpdate(placePos, stateToPlace)) {
                PktPlayEffect ev = new PktPlayEffect(PktPlayEffect.Type.BLOCK_EFFECT)
                        .addData(buf -> {
                            ByteBufUtils.writePos(buf, placePos);
                            ByteBufUtils.writeBlockState(buf, prevState);
                        });
                PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(level, placePos, 32));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack held = playerIn.getItemInHand(handIn);
        if (playerIn.isShiftKeyDown()) {
            SizeMode nextMode = getSizeMode(held).next();
            setSizeMode(held, nextMode);
            playerIn.displayClientMessage(nextMode.getDisplay(), true);
        }
        return InteractionResultHolder.success(held);
    }

    @Nonnull
    private Map<BlockPos, BlockState> getPlaceStates(Player placer, Level level, BlockPos origin, ItemStack refStack) {
        Map<BlockState, Tuple<ItemStack, Integer>> tplStates = ItemBlockStorage.getInventoryMatching(placer, refStack);
        BlockState atState = level.getBlockState(origin);
        SizeMode mode = getSizeMode(refStack);
        Map<BlockPos, BlockState> placeables = Maps.newHashMap();

        BlockState match = BlockUtils.getMatchingState(tplStates.keySet(), atState);
        if (match != null && tplStates.size() <= 1) {
            return placeables; //If trying to replace a block with its identical block.
        }
        float hardness = atState.getDestroySpeed(level, origin);
        int cfgHardness = WandsConfig.CONFIG.exchangeWandMaxHardness.get();
        if (hardness == -1 || (cfgHardness != -1 && hardness > cfgHardness)) {
            return placeables; //Don't break/exchange too hard or unbreakable blocks.
        }

        int totalItems = 0;
        if (placer.isCreative()) {
            totalItems = Integer.MAX_VALUE;
        } else {
            for (Tuple<ItemStack, Integer> amountTpl : tplStates.values()) {
                totalItems += (amountTpl.getB() == -1 ? 500_000 : amountTpl.getB());
            }
        }

        List<BlockPos> foundPositions = BlockDiscoverer.discoverBlocksWithSameStateAround(level, origin, true, mode.getBlockSearchExtent(), totalItems, false);
        if (foundPositions.isEmpty()) {
            return placeables; //It.. shouldn't actually be empty here, ever. Should at least have 1 entry.
        }

        Map<BlockState, Integer> placeAmounts = Maps.newHashMap();
        for (BlockState state : tplStates.keySet()) {
            placeAmounts.put(state, placer.isCreative() ? Integer.MAX_VALUE : tplStates.get(state).getB());
        }
        List<BlockState> placeableStates = Lists.newArrayList(placeAmounts.keySet());
        Random random = ItemBlockStorage.getPreviewRandomFromWorld(level);

        for (BlockPos pos : foundPositions) {
            Collections.shuffle(placeableStates, random);
            BlockState toPlace = Iterables.getFirst(placeableStates, null);

            if (toPlace == null) {
                continue;
            }
            if (!placer.isCreative()) {
                int count = placeAmounts.get(toPlace);
                count--;
                if (count <= 0) {
                    placeAmounts.remove(toPlace);
                    placeableStates.remove(toPlace);
                } else {
                    placeAmounts.put(toPlace, count);
                }
            }

            placeables.put(pos, toPlace);
        }
        return placeables;
    }

    public static void setSizeMode(@Nonnull ItemStack stack, @Nonnull SizeMode mode) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemExchangeWand)) {
            return;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        nbt.putInt("sizeMode", mode.ordinal());
    }

    @Nonnull
    public static SizeMode getSizeMode(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemExchangeWand)) {
            return SizeMode.RANGE_2;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        return MiscUtils.getEnumEntry(SizeMode.class, nbt.getInt("sizeMode"));
    }

    public static enum SizeMode {

        RANGE_2(2),
        RANGE_3(3),
        RANGE_4(4),
        RANGE_5(5);

        private final int searchRadius;

        SizeMode(int searchRadius) {
            this.searchRadius = searchRadius;
        }

        public int getBlockSearchExtent() {
            return searchRadius;
        }

        public MutableComponent getName() {
            return Component.translatable("astralsorcery.misc.exchange.size." + this.searchRadius);
        }

        public MutableComponent getDisplay() {
            return Component.translatable("astralsorcery.misc.exchange.size", this.getName());
        }

        @Nonnull
        private SizeMode next() {
            int next = (this.ordinal() + 1) % values().length;
            return MiscUtils.getEnumEntry(SizeMode.class, next);
        }
    }
}
