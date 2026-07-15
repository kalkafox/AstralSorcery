/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import com.mojang.datafixers.util.Either;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ListEntries;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectOctans
 * Created by HellFirePvP
 * Date: 01.02.2020 / 15:45
 */
public class CEffectOctans extends CEffectAbstractList<ListEntries.CounterMaxEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("octans");
    public static OctansConfig CONFIG = new OctansConfig();

    private static boolean corruptedSkipWaterCheck = false;

    public CEffectOctans(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.octans, CONFIG.maxAmount.get(), (level, pos, state) -> {
            if (!corruptedSkipWaterCheck) {
                pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).below();
            }
            return corruptedSkipWaterCheck || (
                    level.isEmptyBlock(pos.above()) &&
                            (state.getFluidState().is(Fluids.WATER) && state.getFluidState().isSource()) ||
                            state.getBlock() instanceof BubbleColumnBlock
                    );
        });
        this.excludeRitualColumn();
    }

    @Nullable
    @Override
    public ListEntries.CounterMaxEntry recreateElement(CompoundTag tag, BlockPos pos) {
        return new ListEntries.CounterMaxEntry(pos, 1);
    }

    @Nullable
    @Override
    public ListEntries.CounterMaxEntry createElement(Level level, BlockPos pos) {
        pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).below();
        return new ListEntries.CounterMaxEntry(pos, 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

        Vector3 at = new Vector3(pos).add(0.5, 0.5, 0.5);
        at.addY(prop.getSize() * 0.75F);
        for (int i = 0; i < Math.max(1, prop.getSize() / 6); i++) {
            Vector3 vec = at.clone().add(Vector3.random().setY(0).mul(random.nextFloat() * prop.getSize()));

            Color c = MiscUtils.eitherOf(random,
                    () -> ColorsAS.CONSTELLATION_OCTANS,
                    () -> ColorsAS.CONSTELLATION_OCTANS.darker());
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(vec)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .color(VFXColorFunction.constant(c))
                    .setScaleMultiplier(0.6F + random.nextFloat() * 0.3F)
                    .setGravityStrength(0.0004F + random.nextFloat() * 0.0008F)
                    .setMaxAge(100 + random.nextInt(60));
        }
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        boolean update = false;
        if (properties.isCorrupted()) {
            corruptedSkipWaterCheck = true;
            Either<ListEntries.CounterMaxEntry, BlockPos> newEntry = this.peekNewPosition(level, pos, properties);
            corruptedSkipWaterCheck = false;
            return newEntry.mapLeft(entry -> {
                BlockState state = level.getBlockState(entry.getBlockPos());
                BlockPos offset = entry.getBlockPos().subtract(pos);
                if (level.isEmptyBlock(entry.getBlockPos()) &&
                        (this.isLinkedRitual || Math.abs(offset.getX()) > 5 || Math.abs(offset.getZ()) > 5 || offset.getY() < 0)) {
                    if (!level.dimensionType().ultraWarm()) {
                        if (level.setBlockAndUpdate(entry.getBlockPos(), Blocks.WATER.defaultBlockState())) {
                            for (int i = 0; i < 3; i++) {
                                spawnFishingDropsAt((ServerLevel) level, entry.getBlockPos());
                            }
                            level.neighborChanged(entry.getBlockPos(), Blocks.WATER, entry.getBlockPos());
                        }
                    }
                } else if (BlockUtils.isFluidBlock(state)) {
                    if (state.getBlock() == Blocks.WATER) {
                        if (random.nextInt(100) == 0) {
                            spawnFishingDropsAt((ServerLevel) level, entry.getBlockPos());
                        }
                    } else {
                        level.setBlockAndUpdate(entry.getBlockPos(), Blocks.SAND.defaultBlockState());
                    }
                } else if (state.getBlock() instanceof BubbleColumnBlock) {
                    if (random.nextInt(70) == 0) {
                        spawnFishingDropsAt((ServerLevel) level, entry.getBlockPos());
                    }
                }
                return true;
            }).left().orElse(false);
        }

        ListEntries.CounterMaxEntry entry = getRandomElementChanced();
        if (entry != null) {
            if (MiscUtils.canEntityTickAt(level, entry.getBlockPos())) {
                if (!isValid(level, entry)) {
                    removeElement(entry);
                } else {
                    sendConstellationPing(level, new Vector3(entry.getBlockPos()).add(0.5, 1, 0.5));
                    int count = entry.getCounter();
                    count++;
                    entry.setCounter(count);

                    if (count >= entry.getMaxCount()) {
                        int min = Math.min(CONFIG.minFishTickTime.get(), CONFIG.maxFishTickTime.get());
                        int max = Math.max(CONFIG.minFishTickTime.get(), CONFIG.maxFishTickTime.get());

                        int diff = Math.max(1, max - min + 1);
                        entry.setMaxCount(min + random.nextInt(diff));
                        entry.setCounter(0);

                        spawnFishingDropsAt((ServerLevel) level, entry.getBlockPos());
                    }
                }
                update = true;
            }
        }

        if (findNewPosition(level, pos, properties)
                .ifRight(attemptedPos -> sendConstellationPing(level, new Vector3(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, attemptedPos).below()).add(0.5, 0.5, 0.5)))
                .left().isPresent()) {
            update = true;
        }
        return update;
    }

    private void spawnFishingDropsAt(ServerLevel level, BlockPos pos) {
        Vector3 dropLoc = new Vector3(pos).add(0.5, 0.85, 0.5);
        ItemStack tool = new ItemStack(Items.FISHING_ROD);
        tool.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LUCK_OF_THE_SEA), 2);

        ResourceKey<LootTable> fromTable = BuiltInLootTables.FISHING_FISH;
        if (random.nextFloat() < 0.1F) {
            fromTable = BuiltInLootTables.FISHING_TREASURE;
        }

        LootParams params = new LootParams.Builder(level)
                .withLuck(random.nextInt(2) * random.nextFloat())
                .withParameter(LootContextParams.TOOL, tool)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .create(LootContextParamSets.FISHING);
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(fromTable);
        for (ItemStack loot : lootTable.getRandomItems(params, level.random)) {
            ItemEntity ei = ItemUtils.dropItemNaturally(level, dropLoc.getX(), dropLoc.getY(), dropLoc.getZ(), loot);
            Vector3 motion = new Vector3(ei.getDeltaMovement());
            motion.setY(Math.abs(motion.getY()));
            ei.setDeltaMovement(motion.toVector3d());
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class OctansConfig extends CountConfig {

        private final int defaultMinFishTickTime = 20;
        private final int defaultMaxFishTickTime = 60;

        public ModConfigSpec.IntValue minFishTickTime;
        public ModConfigSpec.IntValue maxFishTickTime;

        public OctansConfig() {
            super("octans", 8D, 1D, 64);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.minFishTickTime = cfgBuilder
                    .comment("Defines the minimum default tick-time until a fish may be fished by the ritual. Gets reduced internally the more starlight was provided at the ritual.")
                    .translation(translationKey("minFishTickTime"))
                    .defineInRange("minFishTickTime", this.defaultMinFishTickTime, 5, Integer.MAX_VALUE);

            this.maxFishTickTime = cfgBuilder
                    .comment("Defines the maximum default tick-time until a fish may be fished by the ritual. Gets reduced internally the more starlight was provided at the ritual. Has to be bigger as the minimum time; if it isn't it'll be set to the minimum.")
                    .translation(translationKey("maxFishTickTime"))
                    .defineInRange("maxFishTickTime", this.defaultMaxFishTickTime, 10, Integer.MAX_VALUE);
        }
    }
}
