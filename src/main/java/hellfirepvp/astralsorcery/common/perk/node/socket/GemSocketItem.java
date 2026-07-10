/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.socket;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.modifier.DynamicAttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GemSocketItem
 * Created by HellFirePvP
 * Date: 29.11.2020 / 10:57
 */
public interface GemSocketItem {

    /**
     * Called when a itemstack is inserted into the given socket perk.
     * Note: This will only occur if {@link #canBeInserted} ended up returning true.
     * Note: only called on {@link LogicalSide#SERVER}
     *
     * @param stack
     * @param perk
     * @param player
     * @param progress
     */
    default <T extends AbstractPerk & GemSocketPerk> void onInsert(ItemStack stack, T perk, Player player, PlayerProgress progress) {
        return;
    }

    /**
     * Called when the itemstack is removed from the socket perk.
     * Note: only called on {@link LogicalSide#SERVER}
     *
     * @param stack
     * @param perk
     * @param player
     * @param progress
     */
    default <T extends AbstractPerk & GemSocketPerk> void onExtract(ItemStack stack, T perk, Player player, PlayerProgress progress) {
        return;
    }

    /**
     * Test if this itemstack can be inserted into a gem socket.
     * Note: Gem items can always be taken off, regardless of what this returns.
     * Note: Called on both server and clientside.
     *
     * @param stack the gem stack
     * @param perk
     * @param player
     * @param progress
     * @param side
     * @return if the gem socket can be *inserted*
     */
    default <T extends AbstractPerk & GemSocketPerk> boolean canBeInserted(ItemStack stack, T perk, Player player, PlayerProgress progress, LogicalSide direction) {
        return true;
    }

    /**
     * Gets the set of modifiers this gem provides when socketed.
     * The modifiers themselves should never change for the given item.
     *
     * @param stack
     * @param perk
     * @param player
     * @param side
     * @return the list of modifiers
     */
    default <T extends AbstractPerk & GemSocketPerk> List<DynamicAttributeModifier> getModifiers(ItemStack stack, T perk, Player player, LogicalSide direction) {
        return new ArrayList<>();
    }

    /**
     * Add additional text to the perk's tooltip.
     *
     * @param stack
     * @param perk
     * @param toolTip
     */
    default <T extends AbstractPerk & GemSocketPerk> void addTooltip(ItemStack stack, T perk, List<MutableComponent> toolTip) {}

}
