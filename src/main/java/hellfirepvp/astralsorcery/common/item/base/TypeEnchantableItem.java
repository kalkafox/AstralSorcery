/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.base;

import hellfirepvp.astralsorcery.common.enchantment.AstralEnchantmentType;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeEnchantableItem
 * Created by HellFirePvP
 * Date: 03.12.2020 / 20:48
 */
public interface TypeEnchantableItem {

    boolean canEnchant(ItemStack stack, AstralEnchantmentType type);

}
