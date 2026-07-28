/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event.effect;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fStack;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHandler;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
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
    private RenderTarget preCloudDepth;

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
        RenderLevelStageEvent.Stage stage = event.getStage();
        if (Minecraft.useShaderTransparency()) {
            if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
                renderFabulous(event);
            }
            return;
        }

        // Clouds are drawn after AFTER_PARTICLES, so effects rendered there get painted over by them.
        // AFTER_WEATHER is the last stage inside LevelRenderer#renderLevel that still supplies the
        // camera-transformed PoseStack and keeps the camera matrix on the RenderSystem modelview stack.
        if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            capturePreCloudDepth();
            return;
        }
        if (stage != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        if (this.preCloudDepth != null) {
            // Fancy/Fast draw clouds straight into the main target, depth included. Put the depth
            // buffer back to its pre-cloud contents so effects still occlude against terrain but
            // are no longer hidden by clouds in front of them.
            RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
            mainTarget.copyDepthFrom(this.preCloudDepth);
            mainTarget.bindWrite(false);
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        // The event PoseStack already carries the camera transform at this stage; effects
        // subtract the camera position themselves per-vertex.
        EffectHandler.getInstance().render(event.getPoseStack(), partialTick);
        RenderingDrawUtils.renderQueuedLightRayFans();

        restoreLevelRenderState();
    }

    /**
     * Fabulous composites water, particles, clouds and weather from separate targets and orders them
     * by the depth each target recorded. Astral's effect render types deliberately never write depth,
     * so whichever layer they land in reports the depth of the terrain behind them and every
     * translucent layer in front wins the sort - the effects end up behind water and clouds.
     * <p>
     * Sidestep the composite entirely: draw at AFTER_LEVEL, which GameRenderer dispatches once the
     * transparency chain has already run and rebound the main target. The camera matrix has been
     * popped off the modelview stack by then and the event carries no PoseStack, so both are rebuilt
     * here from the event's model view matrix.
     */
    private void renderFabulous(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        RenderTarget translucentTarget = minecraft.levelRenderer.getTranslucentTarget();
        if (translucentTarget != null) {
            // The composite only blits color, so the main depth buffer still holds the opaque pass
            // alone. Take the translucent target's depth instead - terrain plus water, no clouds -
            // to match what the Fancy path tests against.
            mainTarget.copyDepthFrom(translucentTarget);
            mainTarget.bindWrite(false);
        }

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        try {
            PoseStack renderStack = new PoseStack();
            EffectHandler.getInstance().render(renderStack, partialTick);
            RenderingDrawUtils.renderQueuedLightRayFans();
        } finally {
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        restoreLevelRenderState();
    }

    // Complex effects use several custom render types. Restore the state expected by whatever
    // renders after them once their buffers have been flushed.
    private void restoreLevelRenderState() {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
    }

    private void capturePreCloudDepth() {
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        if (this.preCloudDepth == null) {
            this.preCloudDepth = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
        } else if (this.preCloudDepth.width != mainTarget.width || this.preCloudDepth.height != mainTarget.height) {
            this.preCloudDepth.resize(mainTarget.width, mainTarget.height, Minecraft.ON_OSX);
        }
        this.preCloudDepth.copyDepthFrom(mainTarget);
        mainTarget.bindWrite(false);
    }

}
