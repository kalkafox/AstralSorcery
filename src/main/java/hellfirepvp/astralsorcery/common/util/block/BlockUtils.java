/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import hellfirepvp.astralsorcery.common.util.RegistryHelper;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockUtils
 * Created by HellFirePvP
 * Date: 30.05.2019 / 15:50
 */
public class BlockUtils {

    @Nonnull
    public static List<ItemStack> getDrops(ServerLevel level, BlockPos pos, int harvestFortune, Random random) {
        return getDrops(level, pos, harvestFortune, random, ItemStack.EMPTY);
    }

    @Nonnull
    public static List<ItemStack> getDrops(ServerLevel level, BlockPos pos, int harvestFortune, Random random, ItemStack tool) {
        return getDrops(level, pos, level.getBlockState(pos), harvestFortune, random, tool);
    }

    @Nonnull
    public static List<ItemStack> getDrops(ServerLevel level, BlockPos pos, BlockState state, int harvestFortune, Random random, ItemStack tool) {
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, MiscUtils.getTileAt(level, pos, BlockEntity.class, true))
                .withLuck(harvestFortune);
        return state.getDrops(builder);
    }

    @Nonnull
    public static BlockPos getWorldTopPos(LevelHeightAccessor level, BlockPos at) {
        BlockPos it = at;
        while (!level.isOutsideBuildHeight(it)) {
            it = it.above();
        }
        return it;
    }

    public static BlockPos firstSolidDown(BlockGetter level, BlockPos at) {
        BlockState state = level.getBlockState(at);
        while (at.getY() > 0 && !state.blocksMotion() && state.getFluidState().isEmpty()) {
            at = at.below();
            state = level.getBlockState(at);
        }
        return at;
    }

    public static boolean isReplaceable(Level level, BlockPos pos) {
        return isReplaceable(level, pos, level.getBlockState(pos));
    }

    public static boolean isReplaceable(Level level, BlockPos pos, BlockState state) {
        if (level.isEmptyBlock(pos)) {
            return true;
        }
        BlockPlaceContext ctx = TestBlockUseContext.getHandContext(level, null, InteractionHand.MAIN_HAND, pos, Direction.UP);
        return state.canBeReplaced(ctx);
    }

    //Same as PlayerEntity#getDigSpeed, but without firing an event and not position-based
    public static float getSimpleBreakSpeed(LivingEntity entity, ItemStack tool, BlockState state) {
        float breakSpeed = tool.getDestroySpeed(state);
        if (breakSpeed > 1.0F) {
            int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(
                    entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.EFFICIENCY), entity);
            if (efficiencyLevel > 0 && !tool.isEmpty()) {
                breakSpeed += efficiencyLevel * efficiencyLevel + 1;
            }
        }

        if (MobEffectUtil.hasDigSpeed(entity)) {
            breakSpeed *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(entity) + 1F) * 0.2F;
        }

        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float fatigueMultiplier;
            switch (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0:
                    fatigueMultiplier = (float) Math.pow(0.3F, 1);
                    break;
                case 1:
                    fatigueMultiplier = (float) Math.pow(0.3F, 2);
                    break;
                case 2:
                    fatigueMultiplier = (float) Math.pow(0.3F, 3);
                    break;
                case 3:
                default:
                    fatigueMultiplier = (float) Math.pow(0.3F, 4);
            }

            breakSpeed *= fatigueMultiplier;
        }

        if (entity.isEyeInFluid(FluidTags.WATER) && EnchantmentHelper.getEnchantmentLevel(
                entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.AQUA_AFFINITY), entity) <= 0) {
            breakSpeed /= 5.0F;
        }

        if (!entity.onGround()) {
            breakSpeed /= 5.0F;
        }
        return breakSpeed;
    }

    public static boolean isFluidBlock(Level level, BlockPos pos) {
        return isFluidBlock(level.getBlockState(pos));
    }

    public static boolean isFluidBlock(BlockState state) {
        return state == state.getFluidState().createLegacyBlock();
    }

    @Nullable
    public static BlockState getMatchingState(Collection<BlockState> applicableStates, @Nullable BlockState test) {
        for (BlockState state : applicableStates) {
            if (matchStateExact(state, test)) {
                return state;
            }
        }
        return null;
    }

    public static boolean matchStateExact(@Nullable BlockState state, @Nullable BlockState stateToTest) {
        if (state == null) {
            return stateToTest == null;
        } else if (stateToTest == null) {
            return false;
        }

        if (!RegistryHelper.getKey(state.getBlock()).equals(RegistryHelper.getKey(stateToTest.getBlock()))) {
            return false;
        }

        for (Property<?> prop : state.getProperties()) {
            Comparable<?> original = state.getValue(prop);
            try {
                Comparable<?> test = stateToTest.getValue(prop);
                if (!original.equals(test)) {
                    return false;
                }
            } catch (Exception exc) {
                return false;
            }
        }
        return true;
    }

    public static boolean canToolBreakBlockWithoutPlayer(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ItemStack stack) {
        if (state.getDestroySpeed(level, pos) == -1) {
            return false;
        }
        if (!state.requiresCorrectToolForDrops()) {
            return true;
        }
        // harvest tool/level checks are tag-driven in 1.21; the stack knows both
        return stack.isCorrectToolForDrops(state);
    }

    public static boolean breakBlockWithPlayer(BlockPos pos, ServerPlayer playerMP) {
        return playerMP.gameMode.destroyBlock(pos);
    }

    //Copied from ForgeHooks.onBlockBreak & PlayerInteractionManager.tryHarvestBlock
    //Duplicate break functionality without a active player.
    //Emulates a FakePlayer - attempts without a player as harvester in case a fakeplayer leads to issues.
    public static boolean breakBlockWithoutPlayer(ServerLevel level, BlockPos pos) {
        return breakBlockWithoutPlayer(level, pos, level.getBlockState(pos), ItemStack.EMPTY, true, false);
    }

    @Deprecated
    public static boolean breakBlockWithoutPlayer(ServerLevel level, BlockPos pos, BlockState stateBroken, ItemStack heldItem, boolean breakBlock, boolean ignoreHarvestRestrictions, boolean playEffects) {
        return breakBlockWithoutPlayer(level, pos, stateBroken, heldItem, breakBlock, ignoreHarvestRestrictions);
    }

    public static boolean breakBlockWithoutPlayer(ServerLevel level, BlockPos pos, BlockState stateBroken, ItemStack heldItem, boolean breakBlock, boolean ignoreHarvestRestrictions) {
        FakePlayer fakePlayer = AstralSorcery.getProxy().getASFakePlayerServer(level);
        try {
            // 1.21 port: IItemExtension#canPlayerBreakBlockWhileHolding/ItemStack#onBlockStartBreak were
            // removed from NeoForge without a direct replacement - the pre-check they gated is dropped,
            // same as the surrounding best-effort emulation of a real player break. BreakEvent also no
            // longer carries an editable xp-to-drop field (see ServerPlayerGameMode#destroyBlock in
            // 1.21.1 - xp is now dropped internally by the block's own loot-table experience function,
            // triggered further down via Block#playerDestroy, same as vanilla does).
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, stateBroken, fakePlayer);
            NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled()) {
                return false;
            }
        } catch (Exception exc) {
            return false;
        }

        boolean harvestable = true;
        try {
            if (!ignoreHarvestRestrictions) {
                harvestable = stateBroken.canHarvestBlock(level, pos, fakePlayer);
            }
        } catch (Exception exc) {
            return false;
        }

        ItemStack heldCopy = heldItem.isEmpty() ? ItemStack.EMPTY : heldItem.copy();
        try {
            heldCopy.mineBlock(level, stateBroken, pos, fakePlayer);
        } catch (Exception exc) {
            return false;
        }

        boolean wasCapturingStates = level.captureBlockSnapshots;
        List<BlockSnapshot> previousCapturedStates = new ArrayList<>(level.capturedBlockSnapshots);

        level.captureBlockSnapshots = true;
        try {
            if (breakBlock) {
                if (!stateBroken.onDestroyedByPlayer(level, pos, fakePlayer, harvestable, Fluids.EMPTY.defaultFluidState())) {
                    restoreWorldState(level, wasCapturingStates, previousCapturedStates);
                    return false;
                }
            }
            // 1.21 port: Block#onBlockHarvested/#onPlayerDestroy were removed from vanilla; block removal
            // and growth-neighbor handling now happen inside onDestroyedByPlayer/playerDestroy below.
        } catch (Exception exc) {
            restoreWorldState(level, wasCapturingStates, previousCapturedStates);
            return false;
        }

        if (harvestable) {
            try {
                BlockEntity tileentity = MiscUtils.getTileAt(level, pos, BlockEntity.class, true);
                ItemStack harvestStack = heldCopy.isEmpty() ? ItemStack.EMPTY : heldCopy.copy();
                stateBroken.getBlock().playerDestroy(level, fakePlayer, pos, stateBroken, tileentity, harvestStack);
            } catch (Exception exc) {
                restoreWorldState(level, wasCapturingStates, previousCapturedStates);
                return false;
            }
        }

        BlockDropCaptureAssist.startCapturing();
        try {
            //Capturing block snapshots is aids. don't try that at home kids.
            level.captureBlockSnapshots = false;
            level.restoringBlockSnapshots = true;
            level.capturedBlockSnapshots.forEach((s) -> s.restore());
            level.restoringBlockSnapshots = false;
            level.capturedBlockSnapshots.forEach((s) -> level.setBlockAndUpdate(s.getPos(), Blocks.AIR.defaultBlockState()));
        } finally {
            BlockDropCaptureAssist.getCapturedStacksAndStop(); //Discard

            //Restore previous state
            level.capturedBlockSnapshots.clear();
            level.captureBlockSnapshots = wasCapturingStates;
            level.capturedBlockSnapshots.addAll(previousCapturedStates);
        }
        return true;
    }

    private static void restoreWorldState(Level level, boolean prevCaptureFlag, List<BlockSnapshot> prevSnapshots) {
        level.captureBlockSnapshots = false;

        level.restoringBlockSnapshots = true;
        level.capturedBlockSnapshots.forEach((s) -> s.restore());
        level.restoringBlockSnapshots = false;

        level.capturedBlockSnapshots.clear();

        level.captureBlockSnapshots = prevCaptureFlag;
        level.capturedBlockSnapshots.addAll(prevSnapshots);
    }
}
