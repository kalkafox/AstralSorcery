/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.auxiliary.link;

import net.minecraft.network.chat.Component;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LinkHandler
 * Created by HellFirePvP
 * Date: 30.06.2019 / 20:57
 */
public class LinkHandler implements ITickHandler {

    private static final LinkHandler instance = new LinkHandler();

    private static final Map<UUID, LinkSession> players = new HashMap<>();

    private LinkHandler() {}

    public static LinkHandler getInstance() {
        return instance;
    }

    @Nullable
    public static LinkSession getActiveSession(Player player) {
        return players.get(player.getUUID());
    }

    @Nonnull
    public static RightClickResult onInteractEntity(Player clicked, LivingEntity entity) {
        LinkSession user = LinkSession.entity(entity);
        players.put(clicked.getUUID(), user);
        return new RightClickResult(RightClickResultType.SELECT_START, user);
    }

    @Nonnull
    public static RightClickResult onInteractBlock(Player clicked, Level level, BlockPos pos, boolean sneak) {
        UUID playerUUID = clicked.getUUID();
        if (!players.containsKey(playerUUID)) {
            LinkableTileEntity tile = MiscUtils.getTileAt(level, pos, LinkableTileEntity.class, true);
            if (tile == null) {
                return new RightClickResult(RightClickResultType.NONE, null);
            }

            LinkSession user = LinkSession.tile(tile);
            players.put(playerUUID, user);
            return new RightClickResult(RightClickResultType.SELECT_START, user);
        } else {
            LinkSession user = players.get(playerUUID);
            if (user.getType() == LinkType.ENTITY) {
                LinkableTileEntity tile = MiscUtils.getTileAt(level, pos, LinkableTileEntity.class, true);
                if (tile == null) {
                    players.remove(playerUUID);
                    return new RightClickResult(RightClickResultType.NONE, null);
                } else {
                    user.setSelected(tile);
                    return new RightClickResult(RightClickResultType.TRY_LINK, user);
                }
            } else if (sneak) {
                return new RightClickResult(RightClickResultType.TRY_UNLINK, user);
            } else {
                return new RightClickResult(RightClickResultType.TRY_LINK, user);
            }
        }
    }

    public static void processInteraction(RightClickResult result, Player playerIn, Level level, BlockPos pos) {
        LinkSession user = result.getLinkingSession();
        LinkableTileEntity tile = user.getSelectedTile();
        String linkedToName;
        switch (result.getType()) {
            case SELECT_START:
                if (user.getType() == LinkType.ENTITY) {
                    playerIn.sendSystemMessage(Component.translatable("astralsorcery.misc.link.start",
                            result.getLinkingSession().getSelectedEntity().getDisplayName()).withStyle(ChatFormatting.GREEN));
                } else {
                    String name = tile.getUnLocalizedDisplayName();
                    if (tile.onSelect(playerIn)) {
                        if (name != null) {
                            playerIn.sendSystemMessage(Component.translatable("astralsorcery.misc.link.start",
                                    Component.translatable(name)).withStyle(ChatFormatting.GREEN));
                        }
                    }
                }
                break;
            case TRY_LINK:
                BlockEntity te = MiscUtils.getTileAt(level, pos, BlockEntity.class, true);
                linkedToName = "astralsorcery.misc.link.link.block";
                if (te instanceof LinkableTileEntity) {
                    if (!((LinkableTileEntity) te).doesAcceptLinks()) {
                        return;
                    }
                    String unloc = ((LinkableTileEntity) te).getUnLocalizedDisplayName();
                    if (unloc != null) {
                        linkedToName = unloc;
                    }
                }

                if (user.getType() == LinkType.ENTITY && te instanceof LinkableTileEntity) {
                    LinkableTileEntity linkTarget = (LinkableTileEntity) te;
                    LivingEntity linked = user.getSelectedEntity();
                    if (linkTarget.tryLinkEntity(playerIn, linked)) {
                        linkTarget.onEntityLinkCreate(playerIn, linked);
                    }
                } else {
                    if (tile.tryLinkBlock(playerIn, pos)) {
                        tile.onBlockLinkCreate(playerIn, pos);
                        String linkedFrom = tile.getUnLocalizedDisplayName();
                        if (linkedFrom != null) {
                            playerIn.sendSystemMessage(Component.translatable("astralsorcery.misc.link.link",
                                    Component.translatable(linkedFrom),
                                    Component.translatable(linkedToName))
                                    .withStyle(ChatFormatting.GREEN));
                        }
                    }
                }
                break;
            case TRY_UNLINK:
                if (tile.tryUnlink(playerIn, pos)) {
                    linkedToName = "astralsorcery.misc.link.link.block";
                    te = MiscUtils.getTileAt(level, pos, BlockEntity.class, true);
                    if (te instanceof LinkableTileEntity) {
                        String unloc = ((LinkableTileEntity) te).getUnLocalizedDisplayName();
                        if (unloc != null) {
                            linkedToName = unloc;
                        }
                    }
                    String linkedFrom = tile.getUnLocalizedDisplayName();
                    if (linkedFrom != null) {
                        playerIn.sendSystemMessage(Component.translatable("astralsorcery.misc.link.unlink",
                                Component.translatable(linkedFrom),
                                Component.translatable(linkedToName))
                                .withStyle(ChatFormatting.GREEN));
                    }
                }
                break;
            case NONE:
                break;
            default:
                break;
        }
    }
    @Override
    public void tick(TickEvent.Type type, Object... context) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        Iterator<UUID> iterator = players.keySet().iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            LinkSession user = players.get(uuid);
            Player player = server.getPlayerList().getPlayer(uuid);
            if (player == null) {
                iterator.remove();
                continue;
            }

            boolean needsRemoval = MiscUtils.getMainOrOffHand(player, stack -> stack.getItem() instanceof IItemLinkingTool) == null;

            switch (user.getType()) {
                case ENTITY:
                    LivingEntity entity = user.getSelectedEntity();
                    if (!entity.isAlive() || !entity.getCommandSenderWorld().dimension().equals(player.getCommandSenderWorld().dimension())) {
                        needsRemoval = true;
                    }
                    break;
                case BLOCK:
                    if (!user.getSelectedTile().getLinkWorld().dimension().equals(player.getCommandSenderWorld().dimension())) {
                        needsRemoval = true;
                    }
                    break;
            }
            if (needsRemoval) {
                iterator.remove();
                player.sendSystemMessage(Component.translatable("astralsorcery.misc.link.stop")
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.SERVER);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "LinkHandler";
    }

    public static class LinkSession {

        private final LinkType type;
        private LinkableTileEntity selected;
        private final LivingEntity entity;

        private LinkSession(LinkType type, LinkableTileEntity selected, LivingEntity entity) {
            this.type = type;
            this.selected = selected;
            this.entity = entity;
        }

        public static LinkSession tile(LinkableTileEntity selected) {
            return new LinkSession(LinkType.BLOCK, selected, null);
        }

        public static LinkSession entity(LivingEntity entity) {
            return new LinkSession(LinkType.ENTITY, null, entity);
        }

        public LinkType getType() {
            return type;
        }

        @Nullable
        public LinkableTileEntity getSelectedTile() {
            return selected;
        }

        public void setSelected(LinkableTileEntity selected) {
            this.selected = selected;
        }

        @Nullable
        public LivingEntity getSelectedEntity() {
            return entity;
        }
    }

    public static class RightClickResult {

        private final RightClickResultType type;
        private final LinkSession linkingSession;

        RightClickResult(RightClickResultType type, LinkSession linkingSession) {
            this.type = type;
            this.linkingSession = linkingSession;
        }

        public RightClickResultType getType() {
            return type;
        }

        public LinkSession getLinkingSession() {
            return linkingSession;
        }

        public boolean shouldProcess() {
            return this.getType() != RightClickResultType.NONE;
        }
    }

    public static enum LinkType {

        ENTITY,
        BLOCK

    }

    public static enum RightClickResultType {

        SELECT_START,
        TRY_LINK,
        TRY_UNLINK,
        NONE

    }

}
