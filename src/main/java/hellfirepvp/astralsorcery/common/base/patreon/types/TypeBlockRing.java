/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base.patreon.types;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.LightmapUtil;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.base.patreon.FlareColor;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

import java.util.Map;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeBlockRing
 * Created by HellFirePvP
 * Date: 31.08.2019 / 09:44
 */
public class TypeBlockRing extends PatreonEffect {

    private final UUID playerUUID;

    private final float distance;
    private final float rotationAngle;
    private final int repetition;
    private final int rotationSpeed;
    private final float rotationPart;
    private final Map<BlockPos, BlockState> pattern;

    /*
    Based on X = 2
    variable in Z and Y direction, X towards and from player
     */
    public TypeBlockRing(UUID sessionEffectId,
                             FlareColor chosenColor,
                             UUID playerUUID,
                             float distance,
                             float rotationAngle,
                             int repeats,
                             int tickRotationSpeed,
                             Map<BlockPos, BlockState> pattern) {
        super(sessionEffectId, chosenColor);

        this.playerUUID = playerUUID;
        this.distance = distance;
        this.rotationAngle = rotationAngle;
        this.repetition = repeats;
        this.rotationSpeed = tickRotationSpeed;
        this.rotationPart = 360F / rotationSpeed;
        this.pattern = pattern;
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);

        bus.register(this);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Player pl = Minecraft.getInstance().player;
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && //First person
                pl != null && pl.getUUID().equals(playerUUID)) {
            PoseStack renderStack = event.getPoseStack();

            int alpha = 88;
            if (pl.getXRot() >= 35F) {
                alpha *= Math.max(0, (55F - pl.getXRot()) / 20F);
            }

            if (Minecraft.useShaderTransparency()) {
                RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            }

            renderStack.pushPose();
            renderStack.translate(0, -0.5, 0);
            renderStack.scale(0.5F, 0.5F, 0.5F);
            renderRingAt(renderStack, pl, alpha, event.getPartialTick().getGameTimeDeltaPartialTick(true));
            renderStack.popPose();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderPost(RenderPlayerEvent.Post ev) {
        Player player = ev.getEntity();
        if (!player.getUUID().equals(playerUUID)) {
            return;
        }

        renderRingAt(ev.getPoseStack(), player, 88, ev.getPartialTick());
    }

    @OnlyIn(Dist.CLIENT)
    private void renderRingAt(PoseStack renderStack, Player player, int alphaMultiplier, float pTicks) {
        float addedRotationAngle = 0;

        if (rotationSpeed > 1) {
            float rot = ClientScheduler.getSystemClientTick() % rotationSpeed;
            addedRotationAngle = (rot / ((float) (rotationSpeed))) * 360F + this.rotationPart * pTicks;
        }

        BlockAtlasTexture.getInstance().bindTexture();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        Blending.ADDITIVE_ALPHA.apply();

        for (int rotation = 0; rotation < 360; rotation += (360 / repetition)) {
            for (BlockPos offset : pattern.keySet()) {
                BlockState state = pattern.get(offset);

                TextureAtlasSprite tas = RenderingUtils.getParticleIcon(state, offset);
                if (tas == null) {
                    continue;
                }

                float angle = offset.getZ() * rotationAngle + rotation + addedRotationAngle;

                Vector3 dir = new Vector3(offset.getX() - distance, offset.getY(), 0);
                dir.mirror(Math.toRadians(angle), Vector3.RotAxis.Y_AXIS);
                dir.mul(new Vector3(0.2F, 0.1F, 0.2F));

                renderStack.pushPose();
                renderStack.translate(dir.getX(), dir.getY(), dir.getZ());
                renderStack.scale(0.09F, 0.09F, 0.09F);

                RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, buf -> {
                    RenderingDrawUtils.renderTexturedCubeCentralColorLighted(buf, renderStack,
                            tas.getU0(), tas.getV0(),
                            tas.getU1() - tas.getU0(), tas.getV1() - tas.getV0(),
                            255, 255, 255, alphaMultiplier, LightmapUtil.getPackedLightCoords(player.getCommandSenderWorld(), player.blockPosition()));
                });
                renderStack.popPose();
            }
        }

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

    }
}
