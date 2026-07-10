/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.event.effect.GatewayUIRenderHandler;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.GatewayUI;
import hellfirepvp.astralsorcery.client.util.MouseUtil;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestTeleport;
import hellfirepvp.astralsorcery.common.network.play.client.PktRevokeGatewayAccess;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GatewayInteractionHandler
 * Created by HellFirePvP
 * Date: 12.09.2020 / 22:15
 */
public class GatewayInteractionHandler {

    private static final Random random = new Random();

    public static GatewayUI.GatewayEntry focusingEntry = null;
    public static int focusTicks = 0;

    private static double fovPre = 0;

    public static void attachEventListeners(IEventBus eventBus) {
        eventBus.addListener(GatewayInteractionHandler::doClientTick);
        eventBus.addListener(EventPriority.LOWEST, GatewayInteractionHandler::renderTickPre);
        eventBus.addListener(EventPriority.LOWEST, GatewayInteractionHandler::renderTickPost);
        eventBus.addListener(GatewayInteractionHandler::onAccessRevoke);
    }

    private static void onAccessRevoke(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        Level level = event.getLevel();
        if (player == null || level == null || !level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        GatewayUI ui = GatewayUIRenderHandler.getInstance().getCurrentUI();
        if (ui == null) {
            return;
        }
        GatewayCache.GatewayNode node = ui.getThisGatewayNode();
        if (node == null || !node.isLocked() || node.getOwner() == null || node.getAllowedUsers().isEmpty()) {
            return;
        }
        TileCelestialGateway gateway = MiscUtils.getTileAt(level, Vector3.atEntityCorner(player).toBlockPos(), TileCelestialGateway.class, true);
        if (gateway == null || !gateway.hasMultiblock() || !gateway.doesSeeSky()) {
            return;
        }

        BlockPos clickedPos = event.getBlockPos();
        MapStream.of(node.getAllowedUsers())
                .filter(tpl -> TileCelestialGateway.getAllowedUserOffset(tpl.getA())
                        .offset(node.getBlockPos())
                        .below()
                        .equals(clickedPos))
                .findAny()
                .map(Tuple::getB)
                .ifPresent(playerRef -> {
                    PktRevokeGatewayAccess pkt = new PktRevokeGatewayAccess(level.dimension(), gateway.getBlockPos(), playerRef.getPlayerUUID());
                    PacketChannel.CHANNEL.sendToServer(pkt);
                });
    }

    private static void doClientTick(ClientTickEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        Level level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        GatewayUI ui = GatewayUIRenderHandler.getInstance().getCurrentUI();
        if (ui == null) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        TileCelestialGateway gateway = MiscUtils.getTileAt(level, Vector3.atEntityCorner(player).toBlockPos(), TileCelestialGateway.class, true);
        if (gateway == null || !gateway.hasMultiblock() || !gateway.doesSeeSky()) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        GatewayUI.GatewayEntry entry = GatewayUIRenderHandler.getInstance().
                findMatchingEntry(Mth.wrapDegrees(player.getYRot()), Mth.wrapDegrees(player.getXRot()));
        if (entry == null) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        if (!Minecraft.getInstance().options.keyUse.isDown() &&
                !Minecraft.getInstance().options.keyShift.isDown()) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        if (focusingEntry != null && !entry.equals(focusingEntry)) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        focusingEntry = entry;
        focusTicks++;

        Vector3 dir = focusingEntry.getRelativePos().clone().add(ui.getRenderCenter()).subtract(player.getWantedY(1F));
        Vector3 mov = dir.clone().normalize().mul(0.25F).negate();
        Vector3 pos = focusingEntry.getRelativePos().clone().add(ui.getRenderCenter());

        DyeColor nodeColor = focusingEntry.getNode().getColor();
        Color gatewayColor = ColorUtils.flareColorFromDye(nodeColor == null ? DyeColor.YELLOW : nodeColor);
        if (focusTicks <= 40) {
            pos = focusingEntry.getRelativePos().clone().mul(0.8).add(ui.getRenderCenter());

            float perc = ((float) focusTicks) / 40;
            List<Vector3> positions = MiscUtils.getCirclePositions(pos, dir.clone().negate(), random.nextFloat() * 0.2 + 0.4, random.nextInt(6) + 25);
            for (int i = 0; i < positions.size(); i++) {
                float pc = ((float) i) / ((float) positions.size());
                if (pc >= perc) continue;

                Color color = MiscUtils.eitherOf(random, Color.WHITE, gatewayColor, gatewayColor.brighter());
                Vector3 at = positions.get(i);
                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_GATEWAY_PARTICLE)
                        .spawn(at)
                        .setScaleMultiplier(0.08F)
                        .color(VFXColorFunction.constant(color));
                if (random.nextInt(3) == 0) {
                    Vector3 to = pos.clone().subtract(at);
                    to.normalize().mul(0.02);
                    p.setDeltaMovement(to)
                            .setAlphaMultiplier(0.1F);
                }
            }

            positions = MiscUtils.getCirclePositions(pos, dir, random.nextFloat() * 0.2 + 0.4, random.nextInt(6) + 25);
            Collections.reverse(positions);
            for (int i = 0; i < positions.size(); i++) {
                float pc = ((float) i) / ((float) positions.size());
                if (pc >= perc) continue;

                Color color = MiscUtils.eitherOf(random, Color.WHITE, gatewayColor, gatewayColor.brighter());
                Vector3 at = positions.get(i);
                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_GATEWAY_PARTICLE)
                        .spawn(at)
                        .setScaleMultiplier(0.08F)
                        .color(VFXColorFunction.constant(color));
                if (random.nextInt(3) == 0) {
                    Vector3 to = pos.clone().subtract(at);
                    to.normalize().mul(0.02);
                    p.setDeltaMovement(to)
                            .setAlphaMultiplier(0.1F);
                }
            }
        } else {
            for (Vector3 v : MiscUtils.getCirclePositions(pos, dir, random.nextFloat() * 0.3 + 0.2, random.nextInt(20) + 30)) {

                Color color = MiscUtils.eitherOf(random, Color.WHITE, gatewayColor, gatewayColor.brighter());
                Vector3 m = mov.clone().mul(0.5 + random.nextFloat() * 0.5);
                EffectHelper.of(EffectTemplatesAS.GENERIC_GATEWAY_PARTICLE)
                        .spawn(v)
                        .setScaleMultiplier(0.1F)
                        .setDeltaMovement(m)
                        .color(VFXColorFunction.constant(color));
            }
        }

        if (focusTicks > 95) {
            Minecraft.getInstance().player.getZ(false);
            PktRequestTeleport pkt = new PktRequestTeleport(focusingEntry.getNodeDimension(), focusingEntry.getNode().getBlockPos());
            PacketChannel.CHANNEL.sendToServer(pkt);
            focusingEntry = null;
            focusTicks = 0;
        }
    }

    private static void renderTickPre(RenderFrameEvent.Pre event) {
        GatewayUI ui = GatewayUIRenderHandler.getInstance().getCurrentUI();
        if (ui == null) {
            return;
        }

        fovPre = Minecraft.getInstance().options.fov;
        if(focusTicks < 80) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float percDone = 1F - ((focusTicks - 80F + partialTick) / 15F);
        percDone = (float) Math.pow(percDone, 2.4F);
        float targetFov = 10F;
        double diff = fovPre - targetFov;
        Minecraft.getInstance().options.fov = Math.max(targetFov, targetFov + diff * percDone);
    }

    private static void renderTickPost(RenderFrameEvent.Post event) {
        if (GatewayUIRenderHandler.getInstance().getCurrentUI() != null) {
            Minecraft.getInstance().options.fov = fovPre;
        }
    }
}
