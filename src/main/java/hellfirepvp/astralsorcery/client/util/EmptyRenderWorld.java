/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
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
    private final LevelLightEngine lightEngine = new LevelLightEngine(new LightChunkGetter() {
        @Override
        public LightChunk getChunkForLighting(int chunkX, int chunkZ) {
            throw new UnsupportedOperationException("Empty render world has no chunks");
        }

        @Override
        public EmptyRenderWorld getLevel() {
            return EmptyRenderWorld.this;
        }
    }, false, false);

    public EmptyRenderWorld(Supplier<Biome> biomeSupplier) {
        this.biome = biomeSupplier.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShade(Direction direction, boolean shade) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return lightEngine;
    }

    @Override
    public int getBlockTint(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return colorResolverIn.getColor(biome, blockPosIn.getX(), blockPosIn.getZ());
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return this.getMaxLightLevel();
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int skyDarken) {
        return this.getMaxLightLevel();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight() {
        return 384;
    }

    @Override
    public int getMinBuildHeight() {
        return -64;
    }
}
