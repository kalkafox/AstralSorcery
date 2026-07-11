/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenCustomContainer
 * Created by HellFirePvP
 * Date: 15.08.2019 / 14:56
 */
public abstract class ScreenCustomContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements MenuAccess<T> {

    private final int sWidth, sHeight;

    public ScreenCustomContainer(T screenContainer, Inventory inv, Component name, int width, int height) {
        super(screenContainer, inv, name);
        this.sWidth = width;
        this.sHeight = height;
    }

    public abstract AbstractRenderableTexture getBackgroundTexture();

    @Override
    protected void init() {
        this.xSize = sWidth;
        this.ySize = sHeight;
        super.init();
    }

    @Override
    public T getMenuProvider() {
        return this.container;
    }


    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        this.renderBackground(renderStack);
        super.render(renderStack, xpos, ypos, pTicks);
        this.renderHoveredTooltip(renderStack, xpos, ypos);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(PoseStack renderStack, float a, int xpos, int ypos) {
        this.getBackgroundTexture().bindTexture();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderingUtils.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, this.leftPos, this.topPos, this.getBlitOffset(), this.sWidth, this.sHeight).draw();
        });
    }
}
