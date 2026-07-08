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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureManager;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SmallShrineStructure
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:20
 */
public class SmallShrineStructure extends TemplateStructure {

    public SmallShrineStructure(StructureManager mgr, BlockPos templatePosition) {
        super(WorldGenerationAS.Structures.SMALL_SHRINE_PIECE, mgr, templatePosition);
    }

    public SmallShrineStructure(StructureManager mgr, CompoundTag nbt) {
        super(WorldGenerationAS.Structures.SMALL_SHRINE_PIECE, mgr, nbt);
    }

    @Override
    public ResourceLocation getStructureName() {
        return WorldGenerationAS.Structures.KEY_SMALL_SHRINE;
    }
}
