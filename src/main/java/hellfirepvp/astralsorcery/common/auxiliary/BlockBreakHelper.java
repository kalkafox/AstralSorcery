/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.tick.TickTokenMap;
import hellfirepvp.observerlib.common.util.tick.TickManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.observerlib.common.util.tick.TickEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockBreakHelper
 * Created by HellFirePvP
 * Date: 21.09.2019 / 18:35
 */
public class BlockBreakHelper {

    private static final Map<ResourceKey<Level>, TickTokenMap<BlockPos, BreakEntry>> breakMap = new HashMap<>();

    public static void addProgress(Level level, BlockPos pos, float percStrength, Supplier<Float> expectedHardness) {
        TickTokenMap<BlockPos, BreakEntry> map = breakMap.computeIfAbsent(level.dimension(), key -> {
            TickTokenMap<BlockPos, BreakEntry> tkMap = new TickTokenMap<>(TickEvent.Type.SERVER);
            AstralSorcery.getProxy().getTickManager().register(tkMap);
            return tkMap;
        });

        BreakEntry breakProgress = map.get(pos);
        if (breakProgress == null) {
            Float hardness = expectedHardness.get();
            if (hardness == null) {
                return;
            }
            breakProgress = new BreakEntry(expectedHardness.get(), level, pos, level.getBlockState(pos));
            map.put(pos, breakProgress);
        }

        breakProgress.breakProgress -= percStrength;
        breakProgress.idleTimeout = 0;
    }

    public static void clearServerCache() {
        TickManager mgr = AstralSorcery.getProxy().getTickManager();
        breakMap.values().forEach(mgr::unregister);
        breakMap.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static void blockBreakAnimation(PktPlayEffect pktPlayEffect) {
        BlockPos pos = ByteBufUtils.readPos(pktPlayEffect.getExtraData());
        int id = pktPlayEffect.getExtraData().readInt();
        BlockState state = Block.getStateById(id);

        RenderingUtils.playBlockBreakParticles(pos, state, state);
    }

    public static class BreakEntry implements TickTokenMap.TickMapToken<Float>, CEffectAbstractList.ListEntry {

        private float breakProgress;
        private final LevelAccessor level;
        private BlockPos pos;
        private BlockState expected;

        private int idleTimeout;

        public BreakEntry(@Nonnull Float value, LevelAccessor level, BlockPos at, BlockState expectedToBreak) {
            this.breakProgress = value;
            this.level = level;
            this.pos = at;
            this.expected = expectedToBreak;
        }

        @Override
        public int getRemainingTimeout() {
            return (breakProgress <= 0 || idleTimeout >= 20) ? 0 : 1;
        }

        @Override
        public void tick() {
            idleTimeout++;
        }

        @Override
        public void onTimeout() {
            if (breakProgress > 0) {
                return;
            }

            BlockState nowAt = level.getBlockState(pos);
            if (level instanceof ServerLevel && BlockUtils.matchStateExact(expected, nowAt)) {
                BlockUtils.breakBlockWithoutPlayer((ServerLevel) level, pos, level.getBlockState(pos), ItemStack.EMPTY,
                        true, true);
            }
        }

        @Override
        public Float getValue() {
            return breakProgress;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            this.breakProgress = nbt.getFloat("breakProgress");
            this.pos = NBTHelper.readBlockPosFromNBT(nbt);
            this.expected = Block.getStateById(nbt.getInt("expectedStateId"));
        }

        @Override
        public void save(CompoundTag nbt) {
            nbt.putFloat("breakProgress", this.breakProgress);
            NBTHelper.writeBlockPosToNBT(this.pos, nbt);
            nbt.putInt("expectedStateId", Block.getStateId(this.expected));
        }

    }
}
