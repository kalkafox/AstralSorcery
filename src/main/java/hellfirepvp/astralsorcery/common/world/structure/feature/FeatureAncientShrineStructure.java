/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.structure.feature;

import hellfirepvp.astralsorcery.common.world.TemplateStructureFeature;
import hellfirepvp.astralsorcery.common.world.structure.AncientShrineStructure;
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
 * Class: FeatureAncientShrineStructure
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:52
 */
public class FeatureAncientShrineStructure extends TemplateStructureFeature {

    @Override
    public IStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return Start::new;
    }

    public static class Start extends StructureStart<NoneFeatureConfiguration> {

        public Start(Structure<NoneFeatureConfiguration> config, int chunkPosX, int chunkPosZ, BoundingBox bounds, int ref, long seed) {
            super(config, chunkPosX, chunkPosZ, bounds, ref, seed);
        }

        @Override
        public void func_230364_a_(RegistryAccess registries, ChunkGenerator gen, StructureManager mgr, int chunkX, int chunkZ, Biome biome, NoneFeatureConfiguration cfg) {
            int x = chunkX * 16 + rand.nextInt(16);
            int z = chunkZ * 16 + rand.nextInt(16);
            int y = gen.getHeight(x, z, Heightmap.Type.MOTION_BLOCKING);
            AncientShrineStructure structure = new AncientShrineStructure(mgr, new BlockPos(x, y, z));
            this.components.add(structure);
            this.recalculateStructureSize();
        }
    }
}
