/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryEnchantments
 * Created by HellFirePvP
 * Date: 02.05.2020 / 12:43
 */
public class RegistryEnchantments {

    private RegistryEnchantments() {}

    /**
     * @see hellfirepvp.astralsorcery.common.loot.global.LootModifierScorchingHeat
     */
    public static void init() {
        register(EnchantmentsAS.NIGHT_VISION, createNightVision());
        register(EnchantmentsAS.SCORCHING_HEAT, createScorchingHeat());
    }

    private static Enchantment createNightVision() {
        return Enchantment.enchantment(Enchantment.definition(
                BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.HEAD_ARMOR_ENCHANTABLE),
                1,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.HEAD
        )).build(EnchantmentsAS.NIGHT_VISION.location());
    }

    private static Enchantment createScorchingHeat() {
        return Enchantment.enchantment(Enchantment.definition(
                BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.MINING_ENCHANTABLE),
                1,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND
        )).build(EnchantmentsAS.SCORCHING_HEAT.location());
    }

    private static <T extends Enchantment> T register(ResourceKey<Enchantment> key, T effect) {
        AstralSorcery.getProxy().getRegistryPrimer().register(Registries.ENCHANTMENT, key.location(), effect);
        return effect;
    }

}
