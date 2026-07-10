/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.base.ScreenContainerAltar;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenContainerAltarConstellation
 * Created by HellFirePvP
 * Date: 15.08.2019 / 17:31
 */
public class ScreenContainerAltarConstellation extends ScreenContainerAltar<ContainerAltarConstellation> {

    public ScreenContainerAltarConstellation(ContainerAltarConstellation screenContainer, Inventory inv, Component name) {
        super(screenContainer, inv, name, 255, 202);
    }

    @Override
    public AbstractRenderableTexture getBackgroundTexture() {
        return TexturesAS.TEX_CONTAINER_ALTAR_CONSTELLATION;
    }

    @Override
    protected void renderLabels(PoseStack renderStack, int xpos, int ypos) {
        SimpleAltarRecipe recipe = this.findRecipe(false);
        if (recipe != null) {
            ItemStack out = recipe.getOutputForRender(this.getMenuProvider().getTileEntity().getItems());
            renderStack.pushPose();
            renderStack.translate(190, 35, 0);
            renderStack.scale(2.5F, 2.5F, 1F);

            RenderingUtils.renderItemStackGUI(renderStack, out, null);

            renderStack.popPose();
        }
    }

    @Override
    public void renderGuiBackground(PoseStack renderStack, float a, int xpos, int ypos) {
        this.renderStarlightBar(renderStack, 11, 104, 232, 10);
    }
}
