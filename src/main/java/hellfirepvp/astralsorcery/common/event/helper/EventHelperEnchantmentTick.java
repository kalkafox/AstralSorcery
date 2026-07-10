/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.enchantment.EnchantmentHelperAS;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import java.util.EnumSet;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHelperEnchantmentTick
 * Created by HellFirePvP
 * Date: 02.05.2020 / 12:56
 */
public class EventHelperEnchantmentTick implements ITickHandler {

    public static final EventHelperEnchantmentTick INSTANCE = new EventHelperEnchantmentTick();

    private EventHelperEnchantmentTick() {}

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player player = (Player) context[0];
        LogicalSide direction = (LogicalSide) context[1];

        if (direction.isServer()) {
            EnchantmentHelperAS.getHolder(player.registryAccess(), EnchantmentsAS.NIGHT_VISION).ifPresent(nightVision -> {
                int level = EnchantmentHelper.getItemEnchantmentLevel(nightVision, player.getItemBySlot(EquipmentSlot.HEAD));
                if (level > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, level - 1, true, false));
                }
            });
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "TickEnchantment Helper";
    }
}
