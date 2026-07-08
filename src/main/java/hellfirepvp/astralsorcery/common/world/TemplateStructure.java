/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world;

import hellfirepvp.astralsorcery.common.world.marker.MarkerManagerAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.StructureManager;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TemplateStructure
 * Created by HellFirePvP
 * Date: 18.11.2020 / 20:45
 */
public abstract class TemplateStructure extends TemplateStructurePiece {

    private int yOffset = 0;

    public TemplateStructure(StructurePieceType structurePieceTypeIn, StructureManager mgr, BlockPos templatePosition) {
        super(structurePieceTypeIn, 0);
        this.templatePosition = templatePosition;
        this.loadTemplate(mgr);
    }

    public TemplateStructure(StructurePieceType structurePieceTypeIn, StructureManager mgr, CompoundTag nbt) {
        super(structurePieceTypeIn, nbt);
        this.loadTemplate(mgr);
    }

    private void loadTemplate(StructureManager mgr) {
        StructureTemplate tpl = mgr.getTemplateDefaulted(this.getStructureName());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
        this.setup(tpl, this.templatePosition, settings);
    }

    public <T extends TemplateStructure> T setYOffset(int yOffset) {
        this.yOffset = yOffset;
        return (T) this;
    }

    public abstract ResourceLocation getStructureName();

    @Override
    public boolean func_230383_a_(WorldGenLevel world, StructureManager mgr, ChunkGenerator gen, Random rand, BoundingBox box, ChunkPos chunkPos, BlockPos structCenter) {
        BoundingBox genBox = new BoundingBox(box);
        genBox.offset(0, this.yOffset, 0);

        BlockPos original = this.templatePosition;
        this.templatePosition = original.up(this.yOffset);
        try {
            return super.func_230383_a_(world, mgr, gen, rand, genBox, chunkPos, structCenter.up(yOffset));
        } finally {
            this.templatePosition = original;
            this.placeSettings.setBoundingBox(box);
            this.boundingBox = this.template.getMutableBoundingBox(this.placeSettings, this.templatePosition);
        }
    }

    @Override
    protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, Random rand, BoundingBox sbb) {
        if (sbb.isVecInside(pos)) {
            MarkerManagerAS.handleMarker(function, pos, worldIn, rand, boundingBox);
        }
    }
}
