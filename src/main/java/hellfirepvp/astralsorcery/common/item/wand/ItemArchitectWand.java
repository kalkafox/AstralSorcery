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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingOverlayUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import hellfirepvp.astralsorcery.common.item.base.client.ItemHeldRender;
import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.block.BlockGeometry;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
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
 * Class: ItemArchitectWand
 * Created by HellFirePvP
 * Date: 28.02.2020 / 21:28
 */
public class ItemArchitectWand extends Item implements ItemBlockStorage, ItemOverlayRender, ItemHeldRender, AlignmentChargeConsumer {

    private static final float COST_PER_PLACEMENT = 8F;

    public ItemArchitectWand() {
        super(new Properties()
                .stacksTo(1)
);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(getPlaceMode(stack).getDisplay().withStyle(ChatFormatting.GOLD));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        PlaceMode mode = getPlaceMode(stack);
        return getPlayerPlaceableStates(player, stack).size() * COST_PER_PLACEMENT * mode.getPlaceCostMulitplier();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderInHand(ItemStack stack, PoseStack renderStack, float pTicks) {
        Player player = Minecraft.getInstance().player;
        Map<BlockPos, BlockState> placeStates = getPlayerPlaceableStates(player, stack);
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
        Player player = context.getPlayer();
        ItemStack held = player.getItemInHand(context.getHand());
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide() || !(player instanceof ServerPlayer) || held.isEmpty()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            ItemBlockStorage.storeBlockState(held, level, pos);
            return InteractionResult.SUCCESS;
        } else {
            return attemptPlaceBlocks(level, player, held).getResult();
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        PlaceMode mode = getPlaceMode(held);
        if (player.isShiftKeyDown()) {
            PlaceMode nextMode = mode.next();
            setPlaceMode(held, nextMode);
            player.move(nextMode.getDisplay(), true);
            return InteractionResultHolder.success(held);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(held);
        }
        return attemptPlaceBlocks(level, player, held);
    }

    private InteractionResultHolder<ItemStack> attemptPlaceBlocks(Level level, Player player, ItemStack held) {
        Map<BlockPos, BlockState> placeStates = getPlayerPlaceableStates(player, held);
        if (placeStates.isEmpty()) {
            return InteractionResultHolder.fail(held);
        }

        Map<BlockState, Tuple<ItemStack, Integer>> availableStacks = MapStream.of(ItemBlockStorage.getInventoryMatching(player, held))
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
                if (ItemUtils.consumeFromPlayerInventory(player, held, extractable, true)) {
                    canExtract = true;
                }
            }
            if (!canExtract) {
                continue;
            }

            if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_PLACEMENT, true) &&
                    (player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, held, extractable, true)) &&
                    MiscUtils.canPlayerPlaceBlockPos(player, stateToPlace, placePos, Direction.UP) &&
                    (player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, held, extractable, false)) &&
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_PLACEMENT, false) &&
                    level.setBlock(placePos, stateToPlace)) {
                PktPlayEffect ev = new PktPlayEffect(PktPlayEffect.Type.BLOCK_EFFECT)
                        .addData(buf -> {
                            ByteBufUtils.writePos(buf, placePos);
                            ByteBufUtils.writeBlockState(buf, stateToPlace);
                        });
                PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(level, placePos, 32));
            }
        }
        return InteractionResultHolder.success(held);
    }

    @Nonnull
    private Map<BlockPos, BlockState> getPlayerPlaceableStates(Player player, ItemStack stack) {
        PlaceMode mode = getPlaceMode(stack);
        Level level = player.getCommandSenderWorld();

        BlockHitResult rtr = MiscUtils.rayTraceLookBlock(player, ClipContext.BlockMode.OUTLINE, ClipContext.FluidMode.ANY, 60F);
        if (rtr == null && mode.needsOffset()) {
            return new HashMap<>();
        }

        Map<BlockPos, BlockState> placeStates;
        if (rtr != null) {
            Direction placingAgainst = rtr.getFace();
            BlockPos at = rtr.getBlockPos().offset(rtr.getFace());
            placeStates = getPlaceStates(player, level, at, placingAgainst, stack);
        } else {
            placeStates = getPlaceStates(player, level, null, null, stack);
        }
        return placeStates;
    }

    @Nonnull
    private Map<BlockPos, BlockState> getPlaceStates(Player placer, Level level, @Nullable BlockPos origin, @Nullable Direction placingAgainst, ItemStack refStack) {
        Map<BlockState, Tuple<ItemStack, Integer>> tplStates = ItemBlockStorage.getInventoryMatching(placer, refStack);
        PlaceMode placeMode = getPlaceMode(refStack);
        Map<BlockPos, BlockState> placeables = Maps.newHashMap();

        int totalItems = 0;
        if (placer.isCreative()) {
            totalItems = Integer.MAX_VALUE;
        } else {
            for (Tuple<ItemStack, Integer> amountTpl : tplStates.values()) {
                totalItems += (amountTpl.getB() == -1 ? 500_000 : amountTpl.getB());
            }
        }

        List<BlockPos> foundPositions = placeMode.generatePlacementPositions(level, placer, placingAgainst, origin);
        if (foundPositions.isEmpty()) {
            return placeables; //It.. shouldn't actually be empty here, ever. Should at least have 1 entry.
        }
        foundPositions = foundPositions.subList(0, Math.min(foundPositions.size(), totalItems));

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

            MiscUtils.executeWithChunk(level, pos, () -> {
                if (BlockUtils.isReplaceable(level, pos)) {

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
            });
        }
        return placeables;
    }

    public static void setPlaceMode(@Nonnull ItemStack stack, @Nonnull PlaceMode mode) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemArchitectWand)) {
            return;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        nbt.putInt("placeMode", mode.ordinal());
    }

    @Nonnull
    public static PlaceMode getPlaceMode(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemArchitectWand)) {
            return PlaceMode.TOWARDS_PLAYER;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        return MiscUtils.getEnumEntry(PlaceMode.class, nbt.getInt("placeMode"));
    }

    public static enum PlaceMode {

        TOWARDS_PLAYER("towards", true, 3F) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player, Direction placedAgainst, BlockPos center) {
                List<BlockPos> blocks = new ArrayList<>();
                double cmpFrom, cmpTo;
                switch (placedAgainst.getAxis()) {
                    case X:
                        cmpFrom = center.getX();
                        cmpTo = player.getX();
                        break;
                    case Y:
                        cmpFrom = center.getY();
                        cmpTo = player.getY();
                        break;
                    case Z:
                        cmpFrom = center.getZ();
                        cmpTo = player.getZ();
                        break;
                    default:
                        return Lists.newLinkedList();
                }
                int length = (int) Math.min(20, Math.abs(cmpFrom + 0.5 - cmpTo));
                for (int i = 0; i < length; i++) {
                    BlockPos at = center.offset(placedAgainst, i);
                    if (MiscUtils.executeWithChunk(level, at, () -> !BlockUtils.isReplaceable(level, at), true)) {
                        break;
                    }
                    blocks.add(at);
                }
                return blocks;
            }
        },
        FROM_PLAYER("line", false) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player, Direction placedAgainst, BlockPos center) {
                BlockPos origin = player.position().below();
                HitResult result = player.pick(60F, 1F, false);
                BlockPos hit;
                if (result instanceof BlockHitResult) {
                    hit = ((BlockHitResult) result).getBlockPos();
                } else {
                    hit = new BlockPos(result.getHitVec());
                }
                List<BlockPos> lineState = new ArrayList<>();
                RaytraceAssist rta = new RaytraceAssist(origin, hit);
                rta.forEachBlockPos(pos -> {
                    return MiscUtils.executeWithChunk(level, pos, () -> {
                        if (BlockUtils.isReplaceable(level, pos)) {
                            lineState.add(pos);
                            return true;
                        }
                        return false;
                    }, false);
                });
                return lineState;
            }
        },
        H_PLANE("plane", true) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player, Direction placedAgainst, BlockPos center) {
                return MiscUtils.transformList(BlockGeometry.getPlane(Direction.UP, 5), at -> at.add(center));
            }
        },
        V_PLANE("wall", true) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player, Direction placedAgainst, BlockPos center) {
                return MiscUtils.transformList(BlockGeometry.getPlane(player.getDirection(), 5), at -> at.offset(center));
            }
        },
        SPHERE("sphere", true, 0.2F) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player, Direction placedAgainst, BlockPos center) {
                return MiscUtils.transformList(BlockGeometry.getSphere(5), at -> at.offset(center));
            }
        },
        SPHERE_HOLLOW("sphere_hollow", true, 0.5F) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player, Direction placedAgainst, BlockPos center) {
                return MiscUtils.transformList(BlockGeometry.getHollowSphere(5, 4), at -> at.offset(center));
            }
        };

        private final String name;
        private final boolean needsOffset;
        private final float placeCostMulitplier;

        PlaceMode(String name, boolean needsOffset) {
            this(name, needsOffset, 1F);
        }

        PlaceMode(String name, boolean needsOffset, float placeCostMultiplier) {
            this.name = name;
            this.needsOffset = needsOffset;
            this.placeCostMulitplier = placeCostMultiplier;
        }

        public MutableComponent getName() {
            return Component.translatable("astralsorcery.misc.architect.mode." + this.name);
        }

        public MutableComponent getDisplay() {
            return Component.translatable("astralsorcery.misc.architect.mode", this.getName());
        }

        public float getPlaceCostMulitplier() {
            return placeCostMulitplier;
        }

        public boolean needsOffset() {
            return needsOffset;
        }

        public abstract List<BlockPos> generatePlacementPositions(Level level, Player player, Direction placedAgainst, BlockPos center);

        @Nonnull
        private PlaceMode next() {
            int next = (this.ordinal() + 1) % values().length;
            return MiscUtils.getEnumEntry(PlaceMode.class, next);
        }

    }
}
