/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.enchantment;

import hellfirepvp.astralsorcery.common.item.base.TypeEnchantableItem;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Local replacement for the removed 1.16 EnchantmentType categories.
 */
public enum AstralEnchantmentType {

    BREAKABLE(ItemTags.DURABILITY_ENCHANTABLE,
            Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.VANISHING_CURSE),
    DIGGER(ItemTags.MINING_ENCHANTABLE,
            Enchantments.EFFICIENCY, Enchantments.SILK_TOUCH, Enchantments.FORTUNE, EnchantmentsAS.SCORCHING_HEAT),
    WEAPON(ItemTags.WEAPON_ENCHANTABLE,
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK,
            Enchantments.FIRE_ASPECT, Enchantments.LOOTING, Enchantments.SWEEPING_EDGE),
    BOW(ItemTags.BOW_ENCHANTABLE,
            Enchantments.POWER, Enchantments.PUNCH, Enchantments.FLAME, Enchantments.INFINITY),
    CROSSBOW(ItemTags.CROSSBOW_ENCHANTABLE,
            Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE, Enchantments.PIERCING),
    TRIDENT(ItemTags.TRIDENT_ENCHANTABLE,
            Enchantments.LOYALTY, Enchantments.IMPALING, Enchantments.RIPTIDE, Enchantments.CHANNELING),
    FISHING_ROD(ItemTags.FISHING_ENCHANTABLE,
            Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE),
    WEARABLE(ItemTags.EQUIPPABLE_ENCHANTABLE,
            Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE),
    ARMOR(ItemTags.ARMOR_ENCHANTABLE,
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION, Enchantments.THORNS),
    ARMOR_HEAD(ItemTags.HEAD_ARMOR_ENCHANTABLE,
            Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY, EnchantmentsAS.NIGHT_VISION),
    ARMOR_CHEST(ItemTags.CHEST_ARMOR_ENCHANTABLE),
    ARMOR_LEGS(ItemTags.LEG_ARMOR_ENCHANTABLE, Enchantments.SWIFT_SNEAK),
    ARMOR_FEET(ItemTags.FOOT_ARMOR_ENCHANTABLE,
            Enchantments.FEATHER_FALLING, Enchantments.DEPTH_STRIDER, Enchantments.FROST_WALKER, Enchantments.SOUL_SPEED);

    @Nullable
    private final TagKey<Item> itemTag;
    private final Set<ResourceKey<Enchantment>> enchantments;

    @SafeVarargs
    AstralEnchantmentType(@Nullable TagKey<Item> itemTag, ResourceKey<Enchantment>... enchantments) {
        this.itemTag = itemTag;
        this.enchantments = new HashSet<>(Arrays.asList(enchantments));
    }

    public boolean canEnchantItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this.itemTag != null && stack.is(this.itemTag)) {
            return true;
        }
        return stack.getItem() instanceof TypeEnchantableItem custom && custom.canEnchantItem(stack, this);
    }

    public boolean contains(Holder<Enchantment> enchantment) {
        return this.enchantments.stream().anyMatch(enchantment::is);
    }

    public static boolean anyCanEnchant(ItemStack stack) {
        for (AstralEnchantmentType type : values()) {
            if (type.canEnchantItem(stack)) {
                return true;
            }
        }
        return false;
    }
}
