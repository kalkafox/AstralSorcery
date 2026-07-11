/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import hellfirepvp.astralsorcery.common.container.ContainerTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerBaseScreen
 * Created by HellFirePvP
 * Date: 03.08.2019 / 16:08
 */
public abstract class ContainerBaseScreen<T extends BlockEntity, C extends ContainerTileEntity<T>> extends AbstractContainerScreen<C> {

    public ContainerBaseScreen(C screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        super.render(graphics, mouseX, mouseY, pTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        BlockEntity te = this.getMenu().getTileEntity();
        if (te.isRemoved() || !this.getMenu().stillValid(Minecraft.getInstance().player)) {
            this.onClose();
        }
    }
}
