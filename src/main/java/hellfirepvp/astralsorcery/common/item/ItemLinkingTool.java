/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.link.IItemLinkingTool;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemLinkingTool
 * Created by HellFirePvP
 * Date: 24.08.2019 / 13:51
 */
public class ItemLinkingTool extends Item implements IItemLinkingTool {

    public ItemLinkingTool() {
        super(new Properties()
                .stacksTo(1)
);
    }

    @Override
    public boolean shouldInterceptBlockInteract(LogicalSide direction, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public boolean shouldInterceptEntityInteract(LogicalSide direction, Player player, InteractionHand hand, Entity interacted) {
        return interacted instanceof Player;
    }

    @Override
    public boolean doBlockInteract(LogicalSide direction, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        Level level = player.getCommandSenderWorld();
        if (!level.isClientSide()) {
            LinkHandler.LinkSession user = LinkHandler.getActiveSession(player);
            if (user != null && user.getType() == LinkHandler.LinkType.ENTITY) {
                LinkHandler.RightClickResult result = LinkHandler.onInteractBlock(player, level, pos, player.isShiftKeyDown());
                if (result.shouldProcess()) {
                    LinkHandler.processInteraction(result, player, level, pos);
                    return true;
                }
            }
            LinkHandler.RightClickResult result = LinkHandler.onInteractBlock(player, level, pos, player.isShiftKeyDown());
            if (result.shouldProcess()) {
                LinkHandler.processInteraction(result, player, level, pos);
            }
        } else {
            player.swing(hand);
        }
        return true;
    }

    @Override
    public boolean doEntityInteract(LogicalSide direction, Player player, InteractionHand hand, Entity interacted) {
        if (!(interacted instanceof LivingEntity)) {
            return false;
        }
        LivingEntity target = (LivingEntity) interacted;
        Level level = player.getCommandSenderWorld();
        if (!level.isClientSide()) {
            LinkHandler.LinkSession user = LinkHandler.getActiveSession(player);
            if (user == null || user.getType() == LinkHandler.LinkType.ENTITY) {
                LinkHandler.RightClickResult result = LinkHandler.onInteractEntity(player, target);
                if (result.shouldProcess()) {
                    LinkHandler.processInteraction(result, player, level, BlockPos.ZERO);
                }
            }
        } else {
            player.swing(hand);
        }
        return true;
    }
}
