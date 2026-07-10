/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.useables;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemPerkSeal
 * Created by HellFirePvP
 * Date: 09.08.2019 / 07:15
 */
public class ItemPerkSeal extends Item {

    public ItemPerkSeal() {
        super(new Properties()
                .durability(0)
                .stacksTo(16)
);
    }

    public static int getPlayerSealCount(Player player) {
        return getPlayerSealCount(player.getCapability(Capabilities.ItemHandler.ENTITY));
    }

    public static int getPlayerSealCount(IItemHandler inv) {
        if (inv == null) {
            return 0;
        }
        return ItemUtils.findItemsInInventory(inv, new ItemStack(ItemsAS.PERK_SEAL), false)
                .stream().mapToInt(ItemStack::getCount).sum();
    }

    public static boolean useSeal(Player player, boolean simulate) {
        return useSeal((IItemHandlerModifiable) player.getCapability(Capabilities.ItemHandler.ENTITY), simulate);
    }

    public static boolean useSeal(IItemHandlerModifiable inv, boolean simulate) {
        if (inv == null) {
            return false;
        }
        return ItemUtils.consumeFromInventory(inv, new ItemStack(ItemsAS.PERK_SEAL), simulate);
    }

}
