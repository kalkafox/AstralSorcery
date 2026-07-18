/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicEnchantmentEvent
 * Created by HellFirePvP
 * Date: 11.08.2019 / 20:39
 */
public class DynamicEnchantmentEvent {

    //The event to ADD new dynamic enchantments
    public static class Add extends Event implements ICancellableEvent {

        private final List<DynamicEnchantment> enchantmentsToApply = new LinkedList<>();
        private final ItemStack itemStack;
        private final Player resolvedPlayer;

        public Add(ItemStack itemStack, @Nonnull Player player) {
            this.itemStack = itemStack;
            this.resolvedPlayer = player;
        }

        public ItemStack createForEnchantment() {
            return itemStack;
        }

        @Nonnull
        public Player getResolvedPlayer() {
            return resolvedPlayer;
        }

        public List<DynamicEnchantment> getEnchantmentsToApply() {
            return enchantmentsToApply;
        }
    }

    //The event to MODIFY or REACT to previously defined/added dynamic enchantments + enchantments
    public static class Modify extends Event implements ICancellableEvent {

        private final List<DynamicEnchantment> enchantmentsToApply;
        private final ItemStack itemStack;
        private final Player resolvedPlayer;

        public Modify(ItemStack itemStack, List<DynamicEnchantment> enchantmentsToApply, @Nonnull Player resolvedPlayer) {
            this.itemStack = itemStack;
            this.enchantmentsToApply = enchantmentsToApply;
            this.resolvedPlayer = resolvedPlayer;
        }

        @Nonnull
        public Player getResolvedPlayer() {
            return resolvedPlayer;
        }

        public ItemStack createForEnchantment() {
            return itemStack;
        }

        public List<DynamicEnchantment> getEnchantmentsToApply() {
            return enchantmentsToApply;
        }
    }
}
