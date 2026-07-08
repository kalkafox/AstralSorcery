/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: OverrideInteractItem
 * Created by HellFirePvP
 * Date: 24.08.2019 / 16:35
 */
public interface OverrideInteractItem {

    boolean shouldInterceptBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face);

    default boolean shouldInterceptEntityInteract(LogicalSide side, Player player, InteractionHand hand, Entity interacted) {
        return false;
    }

    //Returning true cancels the event
    boolean doBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face);

    //Returning true cancels the event
    default boolean doEntityInteract(LogicalSide side, Player player, InteractionHand hand, Entity interacted) {
        return false;
    }

}
