/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.container;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.base.ScreenContainerAltar;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerAltarTrait;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenContainerAltarRadiance
 * Created by HellFirePvP
 * Date: 15.08.2019 / 17:33
 */
public class ScreenContainerAltarRadiance extends ScreenContainerAltar<ContainerAltarTrait> {

    private static final Random random = new Random();

    public ScreenContainerAltarRadiance(ContainerAltarTrait screenContainer, Inventory inv, Component name) {
        super(screenContainer, inv, name, 255, 202);
    }

    @Override
    public AbstractRenderableTexture getBackgroundTexture() {
        return TexturesAS.TEX_CONTAINER_ALTAR_RADIANCE;
    }

    @Override
    protected void renderLabels(PoseStack renderStack, int xpos, int mouse) {
        SimpleAltarRecipe recipe = this.findRecipe(false);
        if (recipe != null) {
            ItemStack out = recipe.getOutputForRender(this.getMenuProvider().getTileEntity().getItems());
            renderStack.pushPose();
            renderStack.translate(190, 35, 0);
            renderStack.scale(2.5F, 2.5F, 1F);

            RenderingUtils.renderItemStackGUI(renderStack, out, null);

            renderStack.popPose();
        }

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.disableDepthTest();

        float pTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        TexturesAS.TEX_STAR_1.bindTexture();
        random.setSeed(0x889582997FF29A92L);
        for (int i = 0; i < 18; i++) {

            int x = random.nextInt(54);
            int y = random.nextInt(54);

            float brightness = 0.3F + (RenderingConstellationUtils.stdFlicker(ClientScheduler.getClientTick(), pTicks, 10 + random.nextInt(20))) * 0.6F;

            RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, 15 + x, 39 + y, this.getBlitOffset(), 5, 5)
                        .color(brightness, brightness, brightness, brightness)
                        .draw();
            });
        }

        TileAltar altar = this.getMenuProvider().getTileEntity();
        IConstellation c = altar.getFocusedConstellation();
        if (c != null && altar.hasMultiblock() && ResearchHelper.getClientProgress().hasConstellationDiscovered(c)) {
            random.setSeed(0x61FF25A5B7C24109L);

            RenderingConstellationUtils.renderConstellationIntoGUI(c.getConstellationColor(), c, renderStack,
                    16, 41, this.getBlitOffset(),
                    58, 58,
                    2, () -> 0.2F + 0.8F * RenderingConstellationUtils.conCFlicker(Minecraft.getInstance().level.getDayTime(), pTicks, 5 + random.nextInt(5)),
                    true, false);
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    @Override
    public void renderGuiBackground(PoseStack renderStack, float a, int xpos, int ypos) {
        this.renderStarlightBar(renderStack, 11, 104, 232, 10);
    }
}
