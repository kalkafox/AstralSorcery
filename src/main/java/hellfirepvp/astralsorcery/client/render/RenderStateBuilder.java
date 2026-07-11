/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderStateUtil;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderStateBuilder
 * Created by HellFirePvP
 * Date: 05.06.2020 / 16:27
 */
public class RenderStateBuilder {

    private final RenderType.CompositeState.CompositeStateBuilder builder;
    private boolean shaderSet = false;

    private RenderStateBuilder(RenderType.CompositeState.CompositeStateBuilder builder) {
        this.builder = builder;
    }

    public static RenderStateBuilder builder() {
        return new RenderStateBuilder(RenderType.CompositeState.builder());
    }

    public RenderStateBuilder texture(AbstractRenderableTexture texture) {
        this.builder.setTextureState(texture.asState());
        return this;
    }

    public RenderStateBuilder altasTexture() {
        this.builder.setTextureState(BlockAtlasTexture.getInstance().asState());
        return this;
    }

    public RenderStateBuilder disableTexture() {
        this.builder.setTextureState(RenderStateAccess.NO_TEXTURE_STATE);
        return this;
    }

    public RenderStateBuilder shader(RenderStateShard.ShaderStateShard shader) {
        this.builder.setShaderState(shader);
        this.shaderSet = true;
        return this;
    }

    public boolean hasShader() {
        return this.shaderSet;
    }

    public RenderStateBuilder blend(Blending blendMode) {
        this.builder.setTransparencyState(blendMode.asState());
        return this;
    }

    public RenderStateBuilder smoothShade() {
        // 1.21 port: shade model state removed; smooth shading is the default in core-shader rendering.
        return this;
    }

    public RenderStateBuilder enableItemRendering() {
        // 1.21 port: diffuse lighting state removed; entity/item shaders bake diffuse lighting.
        return this;
    }

    public RenderStateBuilder disableDepth() {
        this.builder.setDepthTestState(new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS) {
            @Override
            public void setupRenderState() {
                //For some ungodly reason this might not be reset to disable depth testing by default...
                //I guess we're gonna do it manually then.
                RenderSystem.disableDepthTest();
                super.setupRenderState();
            }
        });
        return this;
    }

    public RenderStateBuilder disableDepthMask() {
        this.builder.setWriteMaskState(new RenderStateUtil.WriteMaskState(true, false));
        return this;
    }

    public RenderStateBuilder enableLighting() {
        this.builder.setLightmapState(new RenderStateShard.LightmapStateShard(true));
        return this;
    }

    public RenderStateBuilder enableDiffuseLighting() {
        // 1.21 port: diffuse lighting state removed; entity/item shaders bake diffuse lighting.
        return this;
    }

    public RenderStateBuilder enableOverlay() {
        this.builder.setOverlayState(new RenderStateShard.OverlayStateShard(true));
        return this;
    }

    public RenderStateBuilder disableCull() {
        this.builder.setCullState(new RenderStateUtil.CullState(false));
        return this;
    }

    public RenderStateBuilder alpha1arg(float alphaThreshold) {
        // 1.21 port: fixed-function alpha test removed; core shaders discard at their own thresholds.
        return this;
    }

    public RenderStateBuilder defaultAlpha() {
        return alpha1arg(1F / 255F);
    }

    public RenderStateBuilder texturing(RenderStateShard.TexturingStateShard texturing) {
        this.builder.setTexturingState(texturing);
        return this;
    }

    public RenderStateBuilder particleShaderTarget() {
        this.builder.setOutputState(RenderStateAccess.PARTICLES_TARGET_STATE);
        return this;
    }

    public RenderType.CompositeState.CompositeStateBuilder vanillaBuilder() {
        return this.builder;
    }

    public RenderType.CompositeState buildAsOverlay() {
        return this.builder.createCompositeState(true);
    }

    public RenderType.CompositeState build() {
        return this.builder.createCompositeState(false);
    }

    private static class RenderStateAccess extends RenderStateShard {

        private static final EmptyTextureStateShard NO_TEXTURE_STATE = NO_TEXTURE;
        private static final OutputStateShard PARTICLES_TARGET_STATE = PARTICLES_TARGET;

        private RenderStateAccess(String name, Runnable setup, Runnable clear) {
            super(name, setup, clear);
        }
    }
}
