/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.item.base.client.ItemHeldRender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemHeldRenderHelper
 * Created by HellFirePvP
 * Date: 28.02.2020 / 20:01
 */
public class ItemHeldEffectRenderer {

    public static final ItemHeldEffectRenderer INSTANCE = new ItemHeldEffectRenderer();

    private ItemHeldEffectRenderer() {}

    public void attachEventListeners(IEventBus bus) {
        bus.addListener(EventPriority.LOWEST, this::onHeldRender);
    }

    private void onHeldRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        float pTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        PoseStack renderStack = event.getPoseStack();

        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) {
            return;
        }


        for (EquipmentSlot type : EquipmentSlot.values()) {
            if (doHeldRender(Minecraft.getInstance().player.getItemBySlot(type), renderStack, pTicks)) {
                break;
            }
        }
    }

    private boolean doHeldRender(ItemStack heldItem, PoseStack renderStack, float pTicks) {
        if (heldItem.isEmpty()) {
            return false;
        }
        Item held = heldItem.getItem();
        if (held instanceof ItemHeldRender) {
            return ((ItemHeldRender) held).renderInHand(heldItem, renderStack, pTicks);
        }
        return false;
    }
}
