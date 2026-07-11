/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHandler;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinParticleManager
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 */
@Mixin(ParticleEngine.class)
public class MixinParticleManager {

    @Inject(
            method = "renderParticles(Lcom/mojang/blaze3d/matrix/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$Impl;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
            at = @At("RETURN"),
            remap = false
    )
    public void render(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera ari, float pTicks, Frustum clippingHelper, CallbackInfo ci) {
        EffectHandler.getInstance().render(matrixStack, pTicks);

        //Setup GL states again
        //Seriously, keep a clean GL state for once mojang.
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableDepthTest();
        GlStateManager.enableTexture();
    }
}
