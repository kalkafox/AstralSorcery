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
import hellfirepvp.astralsorcery.common.data.config.base.ConfiguredBlockStateList;
import hellfirepvp.astralsorcery.common.data.config.registry.OreBlockRarityRegistry;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockStateList;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockLayerPositionGenerator;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockPositionGenerator;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockRandomPositionGenerator;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectMineralis
 * Created by HellFirePvP
 * Date: 01.02.2020 / 10:54
 */
public class CEffectMineralis extends CEffectAbstractList<ListEntries.PosEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("mineralis");
    public static MineralisConfig CONFIG = new MineralisConfig(new BlockStateList().add(Blocks.STONE));

    public CEffectMineralis(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.mineralis, CONFIG.maxAmount.get(), (level, pos, state) -> true);
        this.excludeRitualColumn();
        this.selectSphericalPositions();
    }

    @Nonnull
    @Override
    protected BlockPositionGenerator createPositionStrategy() {
        return new BlockLayerPositionGenerator();
    }

    @Nonnull
    @Override
    protected BlockPositionGenerator selectPositionStrategy(BlockPositionGenerator defaultGenerator, ConstellationEffectProperties properties) {
        if (!properties.isCorrupted()) {
            return new BlockRandomPositionGenerator();
        }
        return defaultGenerator;
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
        ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

        if (random.nextFloat() < 0.6F) {
            Color c = MiscUtils.eitherOf(random,
                    () -> ColorsAS.CONSTELLATION_MINERALIS,
                    () -> ColorsAS.CONSTELLATION_MINERALIS.brighter());
            Vector3 at = Vector3.random().normalize().mul(random.nextFloat() * prop.getSize()).add(pos).add(0.5, 0.5, 0.5);
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setDeltaMovement(Vector3.random().mul(0.05F))
                    .color(VFXColorFunction.constant(c))
                    .setScaleMultiplier(0.5F + random.nextFloat() * 0.25F)
                    .setMaxAge(50 + random.nextInt(40));
        }
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        return this.peekNewPosition(level, pos, properties).mapLeft(entry -> {
            BlockPos at = entry.getBlockPos();
            BlockState atState = level.getBlockState(at);
            if (properties.isCorrupted()) {
                boolean generateOre = random.nextInt(25) == 0;
                if (atState.isAir() || (generateOre && atState.getBlock() == Blocks.STONE)) {
                    if (generateOre) {
                        Block ore = OreBlockRarityRegistry.MINERALIS_RITUAL.getRandomBlock(random);
                        if (ore != null) {
                            return level.setBlockAndUpdate(at, ore.defaultBlockState());
                        } else {
                            return level.setBlockAndUpdate(at, Blocks.STONE.defaultBlockState());
                        }
                    } else {
                        return level.setBlockAndUpdate(at, Blocks.STONE.defaultBlockState());
                    }
                }
            } else {
                if (CONFIG.replaceableStates.test(atState)) {
                    Block ore = OreBlockRarityRegistry.MINERALIS_RITUAL.getRandomBlock(random);
                    if (ore != null) {
                        return level.setBlockAndUpdate(at, ore.defaultBlockState());
                    } else {
                        sendConstellationPing(level, new Vector3(at).add(0.5, 0.5, 0.5));
                    }
                } else {
                    sendConstellationPing(level, new Vector3(at).add(0.5, 0.5, 0.5));
                }
            }
            return false;
        }).ifRight(attemptedBreak -> {
            sendConstellationPing(level, new Vector3(attemptedBreak).add(0.5, 0.5, 0.5));
        }).left().orElse(false);
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class MineralisConfig extends CountConfig {

        private final BlockStateList defaultReplaceableStates;

        private ConfiguredBlockStateList replaceableStates;

        public MineralisConfig(BlockStateList defaultReplaceableStates) {
            super("mineralis", 5D, 2D, 1);
            this.defaultReplaceableStates = defaultReplaceableStates;
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.replaceableStates = this.defaultReplaceableStates.getAsConfig(
                    cfgBuilder, "replaceableStates", translationKey("replaceableStates"),
                    "Defines the blockstates that may be replaced by generated ore from the ritual."
            );
        }
    }
}
