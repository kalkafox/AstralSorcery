/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.container.ContainerTileEntity;
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
    public void render(PoseStack renderStack, int mouseX, int mouseY, float pTicks) {
        this.renderBackground(renderStack);
        super.render(renderStack, mouseX, mouseY, pTicks);
        this.renderHoveredTooltip(renderStack, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();

        BlockEntity te = this.container.getTileEntity();
        if (te.isRemoved() || !this.container.canInteractWith(Minecraft.getInstance().player)) {
            this.closeScreen();
        }
    }
}
