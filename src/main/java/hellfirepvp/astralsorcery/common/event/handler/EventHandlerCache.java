/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHandler;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.client.util.AreaOfInfluencePreview;
import hellfirepvp.astralsorcery.client.util.camera.ClientCameraManager;
import hellfirepvp.astralsorcery.common.auxiliary.gateway.CelestialGatewayHandler;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.data.config.entry.GeneralConfig;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchSyncHelper;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperInvulnerability;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperSpawnDeny;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperTemporaryFlight;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncDataHolder;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncGateway;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncPerkInformation;
import hellfirepvp.astralsorcery.common.perk.*;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightUpdateHandler;
import hellfirepvp.astralsorcery.common.util.time.TimeStopController;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHandlerCache
 * Created by HellFirePvP
 * Date: 31.08.2019 / 11:29
 */
public class EventHandlerCache {

    private EventHandlerCache() {}

    public static void attachListeners(IEventBus eventBus) {
        eventBus.register(EventHandlerCache.class);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        AreaOfInfluencePreview.INSTANCE.clearClient();
        EffectHandler.cleanUp();
        ScreenJournalProgression.resetJournal();
        ClientCameraManager.INSTANCE.removeAllAndCleanup();

        SyncDataHolder.clear(LogicalSide.CLIENT);
        PerkTree.PERK_TREE.clearCache(LogicalSide.CLIENT);
        PerkLevelManager.clearCache(LogicalSide.CLIENT);
        PerkAttributeHelper.clearClient();
        PerkAttributeType.clearCache(LogicalSide.CLIENT);
        PerkEffectHelper.clientClearAllPerks();
        ModifierManager.clearClientCache();
        PerkCooldownHelper.clearCache(LogicalSide.CLIENT);
        CelestialGatewayHandler.INSTANCE.updateClientCache(null);

        WorldSeedCache.clearClient();
        SkyHandler.getInstance().clientClearCache();
        AstralSorcery.log.info("Client cache cleared!");
    }

    public static void onServerStart() {

    }

    public static void onServerStop() {
        SyncDataHolder.clear(LogicalSide.SERVER);
        PerkTree.PERK_TREE.clearCache(LogicalSide.SERVER);
        PerkAttributeHelper.clearServer();
        PerkAttributeType.clearCache(LogicalSide.SERVER);
        PerkCooldownHelper.clearCache(LogicalSide.SERVER);

        StarlightTransmissionHandler.getInstance().clearServer();
        StarlightUpdateHandler.getInstance().clearServer();
        EventHelperTemporaryFlight.clearServer();
        EventHelperSpawnDeny.clearServer();
        EventHelperInvulnerability.clearServer();
        ResearchHelper.saveAndClearServerCache();
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        LevelAccessor w = event.getLevel();
        if (w instanceof Level) {
            Level level = (Level) w;

            SyncDataHolder.clearWorld(level);
            StarlightTransmissionHandler.getInstance().informWorldUnload(level);
            TimeStopController.onWorldUnload(level);
            SkyHandler.getInstance().informWorldUnload(level);
        }
    }

    @SubscribeEvent
    public static void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (GeneralConfig.CONFIG.giveJournalOnJoin.get() && !progress.didReceiveTome()) {
            if (player.getInventory().add(new ItemStack(ItemsAS.TOME))) {
                ResearchManager.setTomeReceived(player);
            }
        }

        // The 1.16 login-phase handshake is gone; its full-sync packets are sent here instead,
        // before the knowledge sync below, so the client has the perk tree, gateways, and data
        // holders by the time later packets reference them.
        PacketChannel.CHANNEL.sendToPlayer(player, PktLoginSyncDataHolder.makeLogin());
        PacketChannel.CHANNEL.sendToPlayer(player, PktLoginSyncGateway.makeLogin());
        PacketChannel.CHANNEL.sendToPlayer(player, PktLoginSyncPerkInformation.makeLogin());

        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        PerkEffectHelper.onPlayerConnectEvent(player);
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        EventHelperTemporaryFlight.onDisconnect(player);
        PerkEffectHelper.onPlayerDisconnectEvent(player);
        ModifierManager.onDisconnect(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        PerkEffectHelper.onPlayerCloneEvent((ServerPlayer) event.getOriginal(), (ServerPlayer) event.getEntity());
    }
}
