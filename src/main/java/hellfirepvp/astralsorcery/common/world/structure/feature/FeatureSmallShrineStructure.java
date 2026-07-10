/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.structure.feature;

import hellfirepvp.astralsorcery.common.world.TemplateStructureFeature;
import hellfirepvp.astralsorcery.common.world.structure.SmallShrineStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.StructureManager;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FeatureSmallShrineStructure
 * Created by HellFirePvP
 * Date: 18.11.2020 / 22:12
 */
public class FeatureSmallShrineStructure extends TemplateStructureFeature {

    @Override
    public IStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return Start::new;
    }

    public static class Start extends StructureStart<NoneFeatureConfiguration> {

        public Start(Structure<NoneFeatureConfiguration> config, int chunkX, int chunkZ, BoundingBox bounds, int ref, long seed) {
            super(config, chunkX, chunkZ, bounds, ref, seed);
        }

        @Override
        public void generatePieces(RegistryAccess BUILTIN, ChunkGenerator gen, StructureManager mgr, int chunkX, int chunkZ, Biome biome, NoneFeatureConfiguration cfg) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = gen.getHeight(x, z, Heightmap.Type.MOTION_BLOCKING);
            SmallShrineStructure structure = new SmallShrineStructure(mgr, new BlockPos(x, y, z));
            this.pieces.add(structure);
            this.calculateBoundingBox();
        }
    }
}
