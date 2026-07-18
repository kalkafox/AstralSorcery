/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.structure;

import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.TemplateStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DesertShrineStructure
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:14
 */
public class DesertShrineStructure extends TemplateStructure {

    public DesertShrineStructure(StructureTemplateManager mgr, BlockPos templatePosition) {
        super(WorldGenerationAS.Structures.DESERT_SHRINE_PIECE, mgr, WorldGenerationAS.Structures.KEY_DESERT_SHRINE, templatePosition);
        this.setYOffset(-11);
    }

    public DesertShrineStructure(StructureTemplateManager mgr, CompoundTag nbt) {
        super(WorldGenerationAS.Structures.DESERT_SHRINE_PIECE, mgr, nbt);
        this.setYOffset(-11);
    }

    @Override
    public ResourceLocation getFeatureName() {
        return WorldGenerationAS.Structures.KEY_DESERT_SHRINE;
    }
}
