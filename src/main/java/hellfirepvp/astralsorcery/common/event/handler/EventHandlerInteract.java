/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.item.base.OverrideInteractItem;
import hellfirepvp.astralsorcery.common.tile.base.TileOwned;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHandlerInteract
 * Created by HellFirePvP
 * Date: 24.08.2019 / 15:58
 */
public class EventHandlerInteract {

    private EventHandlerInteract() {}

    public static void attachListeners(IEventBus eventBus) {
        eventBus.addListener(EventHandlerInteract::onBlockInteract);
        eventBus.addListener(EventHandlerInteract::onEntityInteract);
        eventBus.addListener(EventHandlerInteract::onSinglePlace);
        eventBus.addListener(EventHandlerInteract::onMultiPlace);
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack held = event.getItemStack();
        if (held.getItem() instanceof OverrideInteractItem) {
            OverrideInteractItem item = (OverrideInteractItem) held.getItem();
            if (item.shouldInterceptEntityInteract(event.getSide(), event.getPlayer(), event.getHand(), event.getTarget()) &&
                    item.doEntityInteract(event.getSide(), event.getPlayer(), event.getHand(), event.getTarget())) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (held.getItem() instanceof OverrideInteractItem) {
            OverrideInteractItem item = (OverrideInteractItem) held.getItem();
            if (item.shouldInterceptBlockInteract(event.getSide(), event.getPlayer(), event.getHand(), event.getBlockPos(), event.getFace()) &&
                    item.doBlockInteract(event.getSide(), event.getPlayer(), event.getHand(), event.getBlockPos(), event.getFace())) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static void onSinglePlace(BlockEvent.EntityPlaceEvent event) {
        if (event instanceof BlockEvent.EntityMultiPlaceEvent) {
            return; //Handled 1 method below.
        }
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || !(event.getEntity() instanceof Player)) {
            return;
        }
        handleOwnerPlacement(level, event.getBlockPos(), (Player) event.getEntity());
    }

    private static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || !(event.getEntity() instanceof Player)) {
            return;
        }
        Player placer = (Player) event.getEntity();
        for (BlockSnapshot snapshot : event.getReplacedBlockSnapshots()) {
            handleOwnerPlacement(level, snapshot.getBlockPos(), placer);
        }
    }

    private static void handleOwnerPlacement(LevelAccessor level, BlockPos pos, Player placer) {
        TileOwned owned = MiscUtils.getTileAt(level, pos, TileOwned.class, true);
        if (owned != null) {
            owned.setOwner(placer);
        }
    }
}
