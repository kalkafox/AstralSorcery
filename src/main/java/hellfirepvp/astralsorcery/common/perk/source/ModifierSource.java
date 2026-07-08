/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.source;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModifierSource
 * Created by HellFirePvP
 * Date: 31.03.2020 / 20:45
 */
//Each ModifierSource should be a AttributeModifierProvider in some way or subclass.
public interface ModifierSource {

    boolean canApplySource(Player player, LogicalSide dist);

    void onRemove(Player player, LogicalSide dist);

    void onApply(Player player, LogicalSide dist);

    boolean isEqual(ModifierSource other);

    ResourceLocation getProviderName();

}
