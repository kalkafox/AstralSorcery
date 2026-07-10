/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import net.minecraft.network.chat.FormattedText;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderingOverlayUtils
 * Created by HellFirePvP
 * Date: 28.02.2020 / 21:41
 */
public class RenderingOverlayUtils {

    public static void renderDefaultItemDisplay(PoseStack renderStack, List<Tuple<ItemStack, Integer>> items) {
        int heightNormal  =  26;
        int heightSplit = 13;
        int width   =  26;
        int offsetX =  30;
        int offsetY =  15;

        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        //Draw background frame
        int tempY = offsetY;
        for (int i = 0; i < items.size(); i++) {
            boolean first = i == 0;
            boolean last = i + 1 == items.size();
            float currentY = tempY;

            if (first) {
                //Draw upper half of the 1st slot
                TexturesAS.TEX_OVERLAY_ITEM_FRAME.bindTexture();
                RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX, buf -> {
                    Matrix4f offset = renderStack.last().pose();
                    buf.vertex(offset, offsetX,            currentY + heightSplit, 10).tex(0, 0.5F).endVertex();
                    buf.vertex(offset, offsetX + width, currentY + heightSplit, 10).tex(1, 0.5F).endVertex();
                    buf.vertex(offset, offsetX + width,    currentY,               10).tex(1, 0)  .endVertex();
                    buf.vertex(offset, offsetX,               currentY,               10).tex(0, 0)  .endVertex();
                });
                tempY += heightSplit;
            } else {
                //Draw lower half and upper next half of the sequence
                TexturesAS.TEX_OVERLAY_ITEM_FRAME_EXTENSION.bindTexture();
                RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX, buf -> {
                    Matrix4f offset = renderStack.last().pose();
                    buf.vertex(offset, offsetX,            currentY + heightNormal, 10).tex(0, 1).endVertex();
                    buf.vertex(offset, offsetX + width, currentY + heightNormal, 10).tex(1, 1).endVertex();
                    buf.vertex(offset, offsetX + width,    currentY,                10).tex(1, 0).endVertex();
                    buf.vertex(offset, offsetX,               currentY,                10).tex(0, 0).endVertex();
                });
                tempY += heightNormal;
            }
            if (last) {
                float drawY = tempY;
                //Draw lower half of the slot
                TexturesAS.TEX_OVERLAY_ITEM_FRAME.bindTexture();
                RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX, buf -> {
                    Matrix4f offset = renderStack.last().pose();
                    buf.vertex(offset, offsetX,            drawY + heightSplit, 10).tex(0, 1)  .endVertex();
                    buf.vertex(offset, offsetX + width, drawY + heightSplit, 10).tex(1, 1)  .endVertex();
                    buf.vertex(offset, offsetX + width,    drawY,               10).tex(1, 0.5F).endVertex();
                    buf.vertex(offset, offsetX,               drawY,               10).tex(0, 0.5F).endVertex();
                });
                tempY += heightSplit;
            }
        }

        RenderSystem.disableBlend();
        BlockAtlasTexture.getInstance().bindTexture();

        //Draw itemstacks on frame
        tempY = offsetY;
        for (Tuple<ItemStack, Integer> stackTpl : items) {
            renderStack.pushPose();
            renderStack.translate(offsetX + 5, tempY + 5, 0);
            RenderingUtils.renderItemStackGUI(renderStack, stackTpl.getA(), null);
            renderStack.popPose();

            tempY += heightNormal;
        }

        //Draw itemstack counts
        renderStack.pushPose();
        renderStack.translate(offsetX + 14, offsetY + 16, 0);
        int txtColor = 0x00DDDDDD;
        for (Tuple<ItemStack, Integer> stackTpl : items) {
            ItemStack stack = stackTpl.getA();
            Font fr;
            if ((fr = stack.getItem().getFont(stack)) == null) {
                fr = font;
            }
            String amountStr = String.valueOf(stackTpl.getB());
            if (stackTpl.getB() == -1) {
                amountStr = "\u221E"; //+Inf
            }
            FormattedText prop = Component.literal(amountStr);
            int length = font.getStringPropertyWidth(prop);

            renderStack.pushPose();
            renderStack.translate(-length / 3F, 0, 500);
            renderStack.scale(0.7F, 0.7F, 1F);
            if (amountStr.length() > 3) {
                renderStack.scale(0.9F, 0.9F, 1F);
            }
            RenderingDrawUtils.renderStringAt(fr, renderStack, prop, txtColor);
            renderStack.popPose();

            renderStack.translate(0, heightNormal, 0);
        }
        renderStack.popPose();
    }

}
