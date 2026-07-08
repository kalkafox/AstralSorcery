/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGameOverlayEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: OverlayRenderHelper
 * Created by HellFirePvP
 * Date: 23.02.2020 / 17:15
 */
public class OverlayRenderer {

    public static final OverlayRenderer INSTANCE = new OverlayRenderer();

    private OverlayRenderer() {}

    public void attachEventListeners(IEventBus bus) {
        bus.addListener(EventPriority.LOW, this::onOverlayRender);
    }

    private void onOverlayRender(RenderGameOverlayEvent.Post event) {
        float pTicks = event.getPartialTicks();
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().world == null) {
            return;
        }

        PoseStack renderStack = event.getPoseStack();
        for (EquipmentSlot type : EquipmentSlotType.values()) {
            if (doHudRender(renderStack, player.getItemStackFromSlot(type), pTicks)) {
                break;
            }
        }
    }

    private boolean doHudRender(PoseStack renderStack, ItemStack heldItem, float pTicks) {
        if (heldItem.isEmpty()) {
            return false;
        }
        Item held = heldItem.getItem();
        if (held instanceof ItemOverlayRender) {
            return ((ItemOverlayRender) held).renderOverlay(renderStack, heldItem, pTicks);
        }
        return false;
    }
}
