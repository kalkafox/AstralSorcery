/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.integration.jei;

import hellfirepvp.astralsorcery.client.integration.ConstellationPaperPreviewRenderer;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class ConstellationPaperPreview {

    private ConstellationPaperPreview() {}

    public static void attachEventListeners(IEventBus eventBus) {
        eventBus.addListener(ConstellationPaperPreview::onScreenRender);
    }

    private static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!Screen.hasShiftDown() || IntegrationJEI.runtime == null) {
            return;
        }

        IClickableIngredient<?> ingredient = IntegrationJEI.runtime.getScreenHelper()
                .getClickableIngredientUnderMouse(event.getScreen(), event.getMouseX(), event.getMouseY())
                .filter(clickable -> clickable.getTypedIngredient().getIngredient() instanceof ItemStack)
                .findFirst()
                .orElse(null);
        if (ingredient == null || !(ingredient.getTypedIngredient().getIngredient() instanceof ItemStack stack)) {
            return;
        }
        ConstellationPaperPreviewRenderer.render(event.getGuiGraphics(), stack,
                event.getMouseX(), event.getMouseY());
    }
}
