/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import hellfirepvp.observerlib.api.client.StructureRenderLightManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EmptyRenderWorld
 * Created by HellFirePvP
 * Date: 18.07.2019 / 16:35
 */
public class EmptyRenderWorld implements BlockAndTintGetter {

    private final Biome biome;

    public EmptyRenderWorld(Supplier<Biome> biomeSupplier) {
        this.biome = biomeSupplier.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float func_230487_a_(Direction direction, boolean b) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return new StructureRenderLightManager(this.getMaxLightLevel());
    }

    @Override
    public int hasChunksAt(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return colorResolverIn.getColor(biome, blockPosIn.getX(), blockPosIn.getZ());
    }

    @Override
    public int getLightFor(LightLayer lightType, BlockPos blockPos) {
        return this.getMaxLightLevel();
    }

    @Nullable
    @Override
    public BlockEntity getTileEntity(BlockPos blockPos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return Fluids.EMPTY.defaultBlockState();
    }
}
