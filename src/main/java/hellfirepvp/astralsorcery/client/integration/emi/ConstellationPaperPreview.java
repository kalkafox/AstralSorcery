/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.integration.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import hellfirepvp.astralsorcery.client.integration.ConstellationPaperPreviewRenderer;
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
        if (!Screen.hasShiftDown()) {
            return;
        }

        ItemStack stack = EmiApi.getHoveredStack(event.getMouseX(), event.getMouseY(), true)
                .getStack()
                .getEmiStacks()
                .stream()
                .map(EmiStack::getItemStack)
                .filter(itemStack -> !itemStack.isEmpty())
                .findFirst()
                .orElse(ItemStack.EMPTY);
        ConstellationPaperPreviewRenderer.render(event.getGuiGraphics(), stack,
                event.getMouseX(), event.getMouseY());
    }
}
