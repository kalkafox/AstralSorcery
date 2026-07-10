/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.ore.BlockStarmetalOre;
import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileCelestialCrystals
 * Created by HellFirePvP
 * Date: 30.09.2019 / 18:17
 */
public class TileCelestialCrystals extends TileEntityTick implements CrystalAttributeTile {

    public static final int TICK_GROWTH_CHANCE = 18_000;

    private CrystalAttributes attributes = null;

    public TileCelestialCrystals() {
        super(TileEntityTypesAS.CELESTIAL_CRYSTAL_CLUSTER);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide()) {
            if (getGrowth() < 4 && doesSeeSky()) {
                this.tryGrowWithChance(TICK_GROWTH_CHANCE);
            }
        } else {
            BlockState downState = getLevel().getBlockState(getBlockPos().below());
            if (downState.getBlock() instanceof BlockStarmetalOre) {
                playStarmetalParticles();
            }
            if (getGrowth() == 4) {
                playFullyGrownParticles();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playStarmetalParticles() {
        if (random.nextInt(9) == 0) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(this)
                            .add(0.1, 0, 0.1)
                            .add(random.nextFloat() * 0.8, 0, random.nextFloat() * 0.8))
                    .color(VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE))
                    .setDeltaMovement(new Vector3(0, 0.02 + random.nextFloat() * 0.05F, 0))
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.15F);
        }

        if (random.nextInt(4) == 0) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(this)
                            .addY(0.05)
                            .add(random.nextFloat(), 0, random.nextFloat()))
                    .color(VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.06F + random.nextFloat() * 0.05F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playFullyGrownParticles() {
        if (random.nextInt(4) == 0) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(this)
                            .add(random.nextFloat(), random.nextFloat(), random.nextFloat()))
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .color(VFXColorFunction.WHITE)
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.15F);
        }
    }

    public void tryGrowWithChance(int growPerTickProbability) {
        BlockState downState = getLevel().getBlockState(getBlockPos().below());
        if (downState.getBlock() instanceof BlockStarmetalOre) {
            growPerTickProbability *= 0.6;

            if (random.nextInt(400) == 0) {
                getLevel().setBlock(getBlockPos().below(), CraftingConfig.CONFIG.getStarmetalRevertBlockState());
            }
        }
        float distribution = DayTimeHelper.getCurrentDaytimeDistribution(getLevel());
        growPerTickProbability *= (1F - (0.5F * distribution));

        this.grow(growPerTickProbability);
    }

    public void grow(int chance) {
        if (random.nextInt(Math.max(chance, 1)) == 0) {
            int stage = getGrowth();
            if (stage < 4) {
                setGrowth(stage + 1);
            }
        }
    }

    public int getGrowth() {
        BlockState current = getLevel().getBlockState(getBlockPos());
        return current.get(BlockCelestialCrystalCluster.STAGE);
    }

    public void setGrowth(int stage) {
        BlockState next = BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.defaultBlockState().setValue(BlockCelestialCrystalCluster.STAGE, stage);
        getLevel().setBlock(getBlockPos(), next);
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern) {
        super.writeCustomNBT(pattern);

        if (this.attributes != null) {
            this.attributes.store(pattern);
        } else {
            CrystalAttributes.storeNull(pattern);
        }
    }

    @Override
    public void readCustomNBT(CompoundTag pattern) {
        super.readCustomNBT(pattern);

        this.attributes = CrystalAttributes.getCrystalAttributes(pattern);
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        return this.attributes;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        this.attributes = attributes;
    }
}
