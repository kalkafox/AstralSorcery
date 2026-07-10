/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.marker;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.tile.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import hellfirepvp.astralsorcery.common.util.Constants;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MarkerManagerAS
 * Created by HellFirePvP
 * Date: 18.11.2020 / 20:48
 */
public class MarkerManagerAS {

    public static void handleMarker(String marker, BlockPos pos, LevelAccessor genWorld, Random random, BoundingBox box) {
        switch (marker) {
            case "brick_shrine_chest":
                if (random.nextBoolean()) {
                    makeChest(genWorld, pos, LootAS.SHRINE_CHEST, random, box);
                } else {
                    genWorld.setBlock(pos, BlocksAS.MARBLE_BRICKS.defaultBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                }
                break;
            case "shrine_chest":
                if (random.nextBoolean()) {
                    makeChest(genWorld, pos, LootAS.SHRINE_CHEST, random, box);
                } else {
                    genWorld.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                }
                break;
            case "random_top_block":
                if (random.nextFloat() < 0.7F) {
                    genWorld.setBlock(pos, genWorld.getBiome(pos).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), Constants.BlockFlags.BLOCK_UPDATE);
                } else {
                    genWorld.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                }
                break;
            case "crystal":
                makeCollectorCrystal(genWorld, pos, random, box);
                break;
        }
    }

    private static void makeCollectorCrystal(LevelAccessor level, BlockPos pos, Random random, BoundingBox box) {
        if (box.isInside(pos) && level.getBlockState(pos).getBlock() != BlocksAS.ROCK_COLLECTOR_CRYSTAL) {
            level.setBlock(pos, BlocksAS.ROCK_COLLECTOR_CRYSTAL.defaultBlockState(), Constants.BlockFlags.BLOCK_UPDATE);

            TileCollectorCrystal tcc = MiscUtils.getTileAt(level, pos, TileCollectorCrystal.class, true);
            if (tcc != null) {
                IMajorConstellation cst = MiscUtils.getRandomEntry(ConstellationRegistry.getMajorConstellations(), random);
                tcc.setAttributes(CrystalPropertiesAS.WORLDGEN_SHRINE_COLLECTOR_ATTRIBUTES);
                tcc.setAttunedConstellation(cst);
            }
        }
    }

    private static void makeChest(LevelAccessor level, BlockPos pos, ResourceLocation tableName, Random random, BoundingBox box) {
        if (box.isInside(pos) && level.getBlockState(pos).getBlock() != Blocks.CHEST) {
            BlockState chest = StructurePiece.correctFacing(level, pos, Blocks.CHEST.defaultBlockState());

            level.setBlock(pos, chest, Constants.BlockFlags.BLOCK_UPDATE);
            // Static setLootTable used instead of manual tile fetch -> member setLootTable to provide compatibility with Lootr.
            RandomizableContainerBlockEntity.setLootTable(level, random, pos, tableName);
        }
    }
}
