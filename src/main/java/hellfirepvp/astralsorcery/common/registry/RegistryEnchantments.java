/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
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
     * Enchantments are a datapack registry since 1.21: definitions are loaded
     * from data JSON instead of code registration. This bootstrap emits those
     * JSONs through datagen (TODO 1.21: wire into a
     * DatapackBuiltinEntriesProvider once datagen is ported).
     * The in-game behavior stays code-driven; see
     * {@link hellfirepvp.astralsorcery.common.loot.global.LootModifierScorchingHeat}
     * and the night vision handling in EventHelperEnchantmentTick.
     */
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        register(context, EnchantmentsAS.NIGHT_VISION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
                1,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.HEAD
        )));
        register(context, EnchantmentsAS.SCORCHING_HEAT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                1,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND
        )));
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

}
