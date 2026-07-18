/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.effect;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectCustomTexture
 * Created by HellFirePvP
 * Date: 26.08.2019 / 19:18
 */
public abstract class EffectCustomTexture extends MobEffect {

    protected static final Random random = new Random();
    private final Color colorAsObj;

    public EffectCustomTexture(MobEffectCategory type, Color color) {
        super(type, color.getRGB());
        this.colorAsObj = color;
    }

    public void attachEventListeners(IEventBus bus) {}

    public abstract SpriteQuery getSpriteQuery();

    // 1.21 port: custom effect icon rendering moved off MobEffect onto
    // IClientMobEffectExtensions; these two methods need to be wired through a
    // RegisterClientExtensionsEvent extension when the client HUD pass is done.
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack renderStack, int x, int y, float z) {
        float wh = 18;
        float offsetX = x + 6;
        float offsetY = y + 7;
        float red =   ((float) this.colorAsObj.getRed())   / 255F;
        float green = ((float) this.colorAsObj.getGreen()) / 255F;
        float blue =  ((float) this.colorAsObj.getBlue())  / 255F;

        SpriteSheetResource ssr = getSpriteQuery().resolveSprite();
        ssr.bindTexture();

        Tuple<Float, Float> uvTpl = ssr.getUVOffset(ClientScheduler.getClientTick());
        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, offsetX, offsetY, z, wh, wh)
                    .color(red, green, blue, 1F)
                    .tex(uvTpl.getA(), uvTpl.getB(), ssr.getUWidth(), ssr.getVWidth())
                    .draw();
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void renderHUDEffect(MobEffectInstance effect, GuiGraphics gui, PoseStack renderStack, int x, int y, float z, float alpha) {
        float wh = 18;
        float offsetX = x + 3;
        float offsetY = y + 3;
        float red =   ((float) this.colorAsObj.getRed())   / 255F;
        float green = ((float) this.colorAsObj.getGreen()) / 255F;
        float blue =  ((float) this.colorAsObj.getBlue())  / 255F;

        SpriteSheetResource ssr = getSpriteQuery().resolveSprite();
        ssr.bindTexture();

        Tuple<Float, Float> uvTpl = ssr.getUVOffset(ClientScheduler.getClientTick());
        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, offsetX, offsetY, z, wh, wh)
                    .color(red, green, blue, 1F)
                    .tex(uvTpl.getA(), uvTpl.getB(), ssr.getUWidth(), ssr.getVWidth())
                    .draw();
        });
    }
}
