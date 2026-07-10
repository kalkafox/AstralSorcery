/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.enchantment.AstralEnchantmentType;
import hellfirepvp.astralsorcery.common.item.base.TypeEnchantableItem;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalPickaxe
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:03
 */
public class ItemCrystalPickaxe extends ItemCrystalTierItem implements TypeEnchantableItem {

    public ItemCrystalPickaxe() {
        super(BlockTags.MINEABLE_WITH_PICKAXE, new Properties(), ItemAbilities.DEFAULT_PICKAXE_ACTIONS);
    }

    @Override
    public boolean canEnchant(ItemStack stack, AstralEnchantmentType type) {
        return type == AstralEnchantmentType.BREAKABLE || type == AstralEnchantmentType.DIGGER;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return super.supportsEnchantment(stack, enchantment) ||
                AstralEnchantmentType.DIGGER.contains(enchantment) ||
                AstralEnchantmentType.BREAKABLE.contains(enchantment);
    }

    @Override
    double getAttackDamage() {
        return 5;
    }

    @Override
    double getAttackSpeed() {
        return -1;
    }
}
