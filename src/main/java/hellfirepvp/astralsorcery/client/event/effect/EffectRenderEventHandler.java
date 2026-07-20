/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHandler;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectRenderEventHandler
 * Created by HellFirePvP
 * Date: 30.05.2019 / 13:40
 */
public class EffectRenderEventHandler {

    private static final EffectRenderEventHandler INSTANCE = new EffectRenderEventHandler();

    private EffectRenderEventHandler() {}

    public static EffectRenderEventHandler getInstance() {
        return INSTANCE;
    }

    public void attachEventListeners(IEventBus bus) {
        bus.addListener(this::onDebugText);
        bus.addListener(EventPriority.LOW, this::onRenderEffects);
        bus.addListener(ClientMiscEventHandler::onRender);
        bus.addListener(EventPriority.LOW, GatewayUIRenderHandler.getInstance()::render);
    }

    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        registrar.accept(GatewayUIRenderHandler.getInstance());
    }

    private void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            event.getLeft().add("");
            //event.getLeft().add(TextFormatting.BLUE + "[AstralSorcery]" + TextFormatting.RESET + " Use Local persistent data: " + PersistentDataManager.INSTANCE.usePersistent());
            event.getLeft().add(ChatFormatting.BLUE + "[AstralSorcery]" + ChatFormatting.RESET + " EffectHandler:");
            event.getLeft().add(ChatFormatting.BLUE + "[AstralSorcery]" + ChatFormatting.RESET + " > Complex effects: " + EffectHandler.getInstance().getEffectCount());
        }
    }

    private void onRenderEffects(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        // The event PoseStack already carries the camera transform at this stage; effects
        // subtract the camera position themselves per-vertex.
        EffectHandler.getInstance().render(event.getPoseStack(), partialTick);

        // Complex effects use several custom render types. Restore the state expected by the
        // remainder of LevelRenderer after their buffers have been flushed.
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
    }

}
