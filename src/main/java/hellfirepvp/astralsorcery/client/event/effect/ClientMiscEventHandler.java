/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event.effect;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.client.util.obj.WavefrontObject;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;

import java.util.zip.GZIPInputStream;
import com.mojang.math.Axis;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientMiscEventHandler
 * Created by HellFirePvP
 * Date: 18.07.2019 / 22:03
 */
public class ClientMiscEventHandler {

    private static boolean attemptLoad = false;
    private static WavefrontObject obj;
    private static ResourceLocation tex = AstralSorcery.key("textures/model/texw.png");
    private static VertexBuffer vboR, vboL;

    private ClientMiscEventHandler() {}

    //Obligatory, dev gimmick
    @OnlyIn(Dist.CLIENT)
    static void onRender(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        if (player == null) return;
        if (player.getUUID().hashCode() != 1529485240) return;

        if (!attemptLoad) {
            attemptLoad = true;
            ResourceLocation mod = ResourceLocation.parse(AstralSorcery.MODID + ":models/obj/modelassec.obj");
            try {
                obj = new WavefrontObject("astralSorcery:wingsrender", new GZIPInputStream(Minecraft.getInstance().getResourceManager().getResource(mod).orElseThrow().open()));
            } catch (Exception exc) {}
        }
        if (attemptLoad && obj == null) {
            return;
        }

        if (player.isPassenger() || player.isFallFlying()) return;

        Vec3 motion = player.getDeltaMovement();

        boolean f = player.getAbilities().flying;
        float ma = f ? 15 : 5;
        float r = (ma * (Math.abs((ClientScheduler.getClientTick() % 80) - 40) / 40F)) +
                ((65 - ma) * Math.max(0, Math.min(1, (float) new Vector3(motion.x, 0, motion.z).length())));
        float rot = RenderingVectorUtils.interpolateRotation(player.yBodyRotO, player.yBodyRot, event.getPartialTick());

        PoseStack renderStack = event.getPoseStack();
        renderStack.pushPose();
        float swimAngle = player.getSwimAmount(event.getPartialTick());
        if (swimAngle > 0) {
            float waterPitch = player.isInWater() ? -90.0F - player.getXRot() : -90.0F;
            float bodySwimAngle = Mth.lerp(swimAngle, 0.0F, waterPitch);
            renderStack.mulPose(Axis.YP.rotationDegrees(180 - rot));
            renderStack.mulPose(Axis.XP.rotationDegrees(bodySwimAngle));
            if (player.isVisuallySwimming()) {
                renderStack.translate(0, -1, 0.3F);
            }
        } else {
            renderStack.mulPose(Axis.YP.rotationDegrees(180 - rot));
        }

        renderStack.scale(0.07F, 0.07F, 0.07F);
        renderStack.translate(0, 5.5, 0.7 - ((r / ma) * (f ? 0.5D : 0.2D)));

        if (vboR == null) {
            vboR = obj.batchOnly(buf -> buf, "wR");
        }
        if (vboL == null) {
            vboL = obj.batchOnly(buf -> buf, "wL");
        }


        RenderTypesAS.MODEL_DEMON_WINGS.setupRenderState();
        RenderSystem.setShaderTexture(0, tex);

        renderStack.pushPose();
        renderStack.mulPose(Axis.YN.rotationDegrees(20 + r));
        drawWingVbo(vboR, renderStack);
        renderStack.popPose();

        renderStack.pushPose();
        renderStack.mulPose(Axis.YP.rotationDegrees(20 + r));
        drawWingVbo(vboL, renderStack);
        renderStack.popPose();

        BlockAtlasTexture.getInstance().bindTexture();
        RenderTypesAS.MODEL_DEMON_WINGS.clearRenderState();

        renderStack.popPose();
    }

    private static void drawWingVbo(VertexBuffer vbo, PoseStack renderStack) {
        if (vbo == null) {
            return;
        }
        org.joml.Matrix4f modelView = new org.joml.Matrix4f(RenderSystem.getModelViewMatrix()).mul(renderStack.last().pose());
        vbo.bind();
        vbo.drawWithShader(modelView, RenderSystem.getProjectionMatrix(),
                hellfirepvp.astralsorcery.client.util.RenderingUtils.shaderFor(RenderTypesAS.POSITION_COLOR_TEX_NORMAL).get());
        VertexBuffer.unbind();
    }
}
