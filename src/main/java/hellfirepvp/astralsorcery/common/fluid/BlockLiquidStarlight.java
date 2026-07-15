/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.fluid;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.nojson.LiquidStarlightCraftingRegistry;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.EventHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockLiquidStarlight
 * Created by HellFirePvP
 * Date: 20.09.2019 / 21:21
 */
public class BlockLiquidStarlight extends LiquidBlock {

    public BlockLiquidStarlight(FlowingFluid fluid) {
        super(fluid, Block.Properties.of()
                .noCollission()
                .lightLevel(state -> 15)
                .strength(100.0F)
                .noLootTable());
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (state.getValue(LEVEL) != 0) {
            return;
        }

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
        } else if (entity instanceof ItemEntity) {
            LiquidStarlightCraftingRegistry.tryCraft((ItemEntity) entity, pos);

            if (!level.isClientSide() &&((ItemEntity) entity).getItem().isEmpty()) {
                entity.discard();
            }
        }
    }

    protected void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (this.reactWithNeighbors(worldIn, pos, state)) {
            Fluid fluid = state.getFluidState().getType();
            worldIn.scheduleTick(pos, fluid, fluid.getTickDelay(worldIn));
        }
    }

    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (this.reactWithNeighbors(worldIn, pos, state)) {
            Fluid fluid = state.getFluidState().getType();
            worldIn.scheduleTick(pos, fluid, fluid.getTickDelay(worldIn));
        }
    }

    private boolean reactWithNeighbors(Level level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            FluidState otherState = level.getFluidState(neighborPos);
            Fluid otherFluid = otherState.getType();
            if (otherFluid instanceof FlowingFluid) {
                otherFluid = ((FlowingFluid) otherFluid).getSource();
            }
            if (otherFluid instanceof EmptyFluid || otherFluid.equals(this.fluid.getSource())) {
                continue;
            }

            BlockState generate;
            boolean isHot = otherState.getFluidType().getTemperature(otherState, level, neighborPos) > 600;
            if (isHot) {
                if (CraftingConfig.CONFIG.liquidStarlightInteractionSand.get()) {
                    generate = Blocks.SAND.defaultBlockState();
                    if (CraftingConfig.CONFIG.liquidStarlightInteractionAquamarine.get() && level.random.nextInt(800) == 0) {
                        generate = BlocksAS.AQUAMARINE_SAND_ORE.defaultBlockState();
                    }
                } else {
                    generate = Blocks.COBBLESTONE.defaultBlockState();
                }
            } else {
                if (CraftingConfig.CONFIG.liquidStarlightInteractionIce.get()) {
                    generate = Blocks.PACKED_ICE.defaultBlockState();
                } else {
                    generate = Blocks.COBBLESTONE.defaultBlockState();
                }
            }

            level.setBlockAndUpdate(pos, EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, generate));
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int fluidLevel = state.getValue(LEVEL);
        double percHeight = 1D - (((double) fluidLevel + 1) / 8D);
        playLiquidStarlightBlockEffect(random, new Vector3(pos).addY(percHeight * random.nextFloat()), 1F);
        playLiquidStarlightBlockEffect(random, new Vector3(pos).addY(percHeight * random.nextFloat()), 1F);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playLiquidStarlightBlockEffect(RandomSource random, Vector3 at, float blockSize) {
        if (random.nextInt(3) == 0) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at.clone().add(
                            0.5 + random.nextFloat() * (blockSize / 2) * (random.nextBoolean() ? 1 : -1),
                            0,
                            0.5 + random.nextFloat() * (blockSize / 2) * (random.nextBoolean() ? 1 : -1)))
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.06F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .color(VFXColorFunction.constant(ColorsAS.ROCK_CRYSTAL));
        }
    }
}
