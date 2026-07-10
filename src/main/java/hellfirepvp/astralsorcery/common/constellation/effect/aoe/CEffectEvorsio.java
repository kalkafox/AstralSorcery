/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ListEntries;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockPositionGenerator;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockSpherePositionGenerator;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectEvorsio
 * Created by HellFirePvP
 * Date: 24.11.2019 / 10:03
 */
public class CEffectEvorsio extends CEffectAbstractList<ListEntries.PosEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("evorsio");
    public static EvorsioConfig CONFIG = new EvorsioConfig();

    public CEffectEvorsio(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.evorsio, 1, (level, pos, state) -> true);
        this.excludeRitualPositions();
    }

    @Nonnull
    @Override
    protected BlockPositionGenerator createPositionStrategy() {
        return new BlockSpherePositionGenerator();
    }

    @Nullable
    @Override
    public ListEntries.PosEntry recreateElement(CompoundTag tag, BlockPos pos) {
        return new ListEntries.PosEntry(pos);
    }

    @Nullable
    @Override
    public ListEntries.PosEntry createElement(Level level, BlockPos pos) {
        return new ListEntries.PosEntry(pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        float addY = 1F;
        if (!pedestal.getBlockPos().equals(pos)) {
            addY = 0F;
        }
        Vector3 motion = Vector3.random().mul(0.1);
        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(pos).add(0.5, 0.5, 0.5).addY(addY))
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .setDeltaMovement(motion)
                .color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_EVORSIO))
                .setScaleMultiplier(0.3F + random.nextFloat() * 0.4F)
                .setMaxAge(50);
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        return this.peekNewPosition(level, pos, properties).mapLeft(newEntry -> {
            BlockPos at = newEntry.getBlockPos();

            if (properties.isCorrupted()) {
                if (at.getY() < pos.getY() && level.isEmptyBlock(at)) {
                    double distance = pos.distSqr(at) / (properties.getSize() * properties.getSize());
                    BlockState state = Blocks.COBBLESTONE.defaultBlockState();
                    if (distance >= 0.85F && random.nextInt(4) == 0) {
                        state = Blocks.DIRT.defaultBlockState();
                    }
                    if (distance <= 0.25F) {
                        state = Blocks.STONE.defaultBlockState();
                    } else if (distance <= 0.1F && random.nextInt(5) == 0) {
                        state = Blocks.OBSIDIAN.defaultBlockState();
                    }
                    level.setBlock(at, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
                }
                return false;
            }

            TileRitualPedestal pedestal = getPedestal(level, pos);
            if (pedestal != null) {
                BlockState state = level.getBlockState(at);
                if (this.canBreakBlock(level, at, state, buildFilter(pedestal))) {
                    BlockDropCaptureAssist.startCapturing();
                    try {
                        BlockUtils.breakBlockWithoutPlayer((ServerLevel) level, at, state,
                                ItemStack.EMPTY, true, true);
                    } finally {
                        NonNullList<ItemStack> captured = BlockDropCaptureAssist.getCapturedStacksAndStop();
                        captured.forEach((stack) -> ItemUtils.dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack));
                    }
                    return true;
                } else {
                    sendConstellationPing(level, new Vector3(at).add(0.5, 0.5, 0.5));
                }
            }
            return false;
        }).ifRight(attemptedBreak -> {
            sendConstellationPing(level, new Vector3(attemptedBreak).add(0.5, 0.5, 0.5));
        }).left().orElse(false);
    }

    private boolean canBreakBlock(Level level, BlockPos pos, BlockState state, Predicate<BlockState> blacklist) {
        if (blacklist.test(state)) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0 || hardness >= 75) {
            return false;
        }
        return !state.isAir();
    }

    private Predicate<BlockState> buildFilter(TileRitualPedestal pedestal) {
        List<Predicate<BlockState>> filteredBlocks = pedestal.getConfiguredBlockStates().stream()
                .map(state -> (Predicate<BlockState>) state::equals)
                .collect(Collectors.toList());
        this.addDefaultBreakBlacklist(filteredBlocks);
        return state -> {
            for (Predicate<BlockState> filterTest : filteredBlocks) {
                if (filterTest.test(state)) {
                    return true;
                }
            }
            return false;
        };
    }

    private void addDefaultBreakBlacklist(List<Predicate<BlockState>> out) {
        out.add((state) -> state.getBlock().equals(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL));
        out.add((state) -> state.getBlock().equals(BlocksAS.ROCK_COLLECTOR_CRYSTAL));
        out.add((state) -> state.getBlock().equals(BlocksAS.LENS));
        out.add((state) -> state.getBlock().equals(BlocksAS.PRISM));
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class EvorsioConfig extends Config {

        public EvorsioConfig() {
            super("evorsio", 6D, 1D);
        }
    }
}
