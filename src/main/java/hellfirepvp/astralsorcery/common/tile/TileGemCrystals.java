/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.tile.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileGemCrystals
 * Created by HellFirePvP
 * Date: 16.11.2019 / 16:45
 */
public class TileGemCrystals extends TileEntityTick {

    public static final int TICK_GROWTH_CHANCE = 10_000;

    public TileGemCrystals() {
        super(TileEntityTypesAS.GEM_CRYSTAL_CLUSTER);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide()) {
            if (getGrowth().getGrowthStage() < 2 && doesSeeSky()) {
                this.tryGrowWithChance(TICK_GROWTH_CHANCE);
            } else if (getGrowth().getGrowthStage() == 2) {
                if (random.nextInt(4000) == 0) {
                    this.setGrowth(getGrowth().shrink());
                }
            }
        } else {
            if (getGrowth().getGrowthStage() == 2) {
                playHarvestEffects();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playHarvestEffects() {
        Vector3 pos = new Vector3(this)
                .add(0.5, 0.5, 0.5)
                .add(this.getBlockState().getOffset(getLevel(), getBlockPos()));
        MiscUtils.applyRandomOffset(pos, random, 0.5F);

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(pos)
                .color(VFXColorFunction.constant(getGrowth().getDisplayColor()))
                .setScaleMultiplier(0.1F + random.nextFloat() * 0.05F)
                .setMaxAge(15 + random.nextInt(5));
    }

    public void tryGrowWithChance(int growPerTickProbability) {
        float distribution = DayTimeHelper.getCurrentDaytimeDistribution(getLevel());
        growPerTickProbability *= (1F - (0.2F * distribution));

        this.grow(growPerTickProbability);
    }

    public void grow(int chance) {
        if (random.nextInt(Math.max(chance, 1)) == 0) {
            setGrowth(getGrowth().grow(getLevel()));
        }
    }

    public BlockGemCrystalCluster.GrowthStageType getGrowth() {
        BlockState current = getLevel().getBlockState(getBlockPos());
        return current.get(BlockGemCrystalCluster.STAGE);
    }

    public void setGrowth(BlockGemCrystalCluster.GrowthStageType stage) {
        BlockState next = BlocksAS.GEM_CRYSTAL_CLUSTER.defaultBlockState().setValue(BlockGemCrystalCluster.STAGE, stage);
        getLevel().setBlock(getBlockPos(), next);
    }
}
