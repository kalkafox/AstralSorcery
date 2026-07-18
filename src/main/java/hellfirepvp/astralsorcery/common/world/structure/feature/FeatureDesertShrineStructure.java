/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.structure.feature;

import com.mojang.serialization.MapCodec;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.TemplateStructure;
import hellfirepvp.astralsorcery.common.world.TemplateStructureFeature;
import hellfirepvp.astralsorcery.common.world.structure.DesertShrineStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FeatureDesertShrineStructure
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:52
 */
public class FeatureDesertShrineStructure extends TemplateStructureFeature {

    public static final MapCodec<FeatureDesertShrineStructure> CODEC = simpleCodec(FeatureDesertShrineStructure::new);

    public FeatureDesertShrineStructure(StructureSettings settings) {
        super(settings, WorldGenerationAS.Config.CFG_DESERT_SHRINE);
    }

    @Override
    protected TemplateStructure createPiece(StructureTemplateManager mgr, BlockPos pos) {
        return new DesertShrineStructure(mgr, pos);
    }

    @Override
    public StructureType<?> type() {
        return WorldGenerationAS.Structures.TYPE_DESERT_SHRINE;
    }
}
