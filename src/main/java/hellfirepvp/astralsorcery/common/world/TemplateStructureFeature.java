/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Base class of the shrine structures: places a single template piece at a
 * random surface position in the starting chunk. The mod's TOML "enabled"
 * flag is applied here; spacing and biome selection are datapack-driven
 * (structure set / structure JSON) since 1.21.
 *
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TemplateStructureFeature
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:22
 */
public abstract class TemplateStructureFeature extends Structure {

    @Nullable
    private final FeatureGenerationConfig config;

    protected TemplateStructureFeature(StructureSettings settings, @Nullable FeatureGenerationConfig config) {
        super(settings);
        this.config = config;
    }

    protected abstract TemplateStructure createPiece(StructureTemplateManager mgr, BlockPos pos);

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (this.config != null && !this.config.isEnabled()) {
            return Optional.empty();
        }
        ChunkPos chunkPos = context.chunkPos();
        WorldgenRandom random = context.random();
        int x = chunkPos.getMinBlockX() + random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + random.nextInt(16);
        int y = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.MOTION_BLOCKING, context.heightAccessor(), context.randomState());
        BlockPos pos = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(pos, builder ->
                builder.addPiece(this.createPiece(context.structureTemplateManager(), pos))));
    }
}
