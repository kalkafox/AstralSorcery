/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.enchantment.dynamic;

import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.data.config.registry.AmuletEnchantmentRegistry;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantmentHelper;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.util.object.ObjectReference;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicEnchantmentHelper
 * Created by HellFirePvP
 * Date: 11.08.2019 / 19:49
 */
public class DynamicEnchantmentHelper {

    public static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        ItemStack stack = event.getStack();
        if (!canHaveDynamicEnchantment(stack)) {
            return;
        }

        List<DynamicEnchantment> context = fireEnchantmentGatheringEvent(stack);
        if (context.isEmpty()) {
            return;
        }

        Holder<Enchantment> target = event.getTargetEnchant();
        if (target != null) {
            int level = event.getEnchantments().getLevel(target);
            event.getEnchantments().set(target, getNewEnchantmentLevel(level, target, stack, context));
            return;
        }

        addNewLevels(event.getEnchantments(), stack, context, event);
    }

    public static int getNewEnchantmentLevel(int current, Holder<Enchantment> enchantment, ItemStack item, @Nullable List<DynamicEnchantment> context) {
        if (!canHaveDynamicEnchantment(item)) {
            return current;
        }

        Optional<ResourceKey<Enchantment>> key = enchantment.unwrapKey();
        if (key.isEmpty() || !AmuletEnchantmentRegistry.canBeInfluenced(key.get())) {
            return current;
        }

        List<DynamicEnchantment> modifiers = context != null ? context : fireEnchantmentGatheringEvent(item);
        for (DynamicEnchantment mod : modifiers) {
            ResourceKey<Enchantment> target = mod.getEnchantment();
            switch (mod.getType()) {
                case ADD_TO_SPECIFIC:
                    if (key.get().equals(target) && (current > 0 || canAddDynamicEnchantment(item, enchantment))) {
                        current += mod.getLevelAddition();
                    }
                    break;
                case ADD_TO_EXISTING_SPECIFIC:
                    if (key.get().equals(target) && current > 0) {
                        current += mod.getLevelAddition();
                    }
                    break;
                case ADD_TO_EXISTING_ALL:
                    if (current > 0) {
                        current += mod.getLevelAddition();
                    }
                    break;
                default:
                    break;
            }
        }
        if (enchantment.is(Enchantments.QUICK_CHARGE)) {
            current = Mth.clamp(current, 0, 5);
        }
        return current;
    }

    public static ItemEnchantments.Mutable addNewLevels(ItemEnchantments.Mutable enchantments, ItemStack stack, List<DynamicEnchantment> context, GetEnchantmentLevelEvent event) {
        Set<Holder<Enchantment>> existing = new HashSet<>(enchantments.keySet());
        for (Holder<Enchantment> enchantment : existing) {
            enchantments.set(enchantment, getNewEnchantmentLevel(enchantments.getLevel(enchantment), enchantment, stack, context));
        }

        Set<ResourceKey<Enchantment>> existingKeys = new HashSet<>();
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            enchantment.unwrapKey().ifPresent(existingKeys::add);
        }

        for (DynamicEnchantment mod : context) {
            if (mod.getType() != DynamicEnchantmentType.ADD_TO_SPECIFIC) {
                continue;
            }

            ResourceKey<Enchantment> enchantmentKey = mod.getEnchantment();
            if (enchantmentKey == null || existingKeys.contains(enchantmentKey) || !AmuletEnchantmentRegistry.canBeInfluenced(enchantmentKey)) {
                continue;
            }

            Optional<Holder.Reference<Enchantment>> enchantment = event.getHolder(enchantmentKey);
            if (enchantment.isEmpty() || !event.isTargetting(enchantmentKey) || !canAddDynamicEnchantment(stack, enchantment.get())) {
                continue;
            }

            enchantments.set(enchantment.get(), getNewEnchantmentLevel(0, enchantment.get(), stack, context));
            existingKeys.add(enchantmentKey);
        }
        return enchantments;
    }

    private static boolean canAddDynamicEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.supportsEnchantment(enchantment) || stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
    }

    @Deprecated
    public static int getNewEnchantmentLevel(int current, Enchantment enchantment, ItemStack item, @Nullable List<DynamicEnchantment> context) {
        return current;
    }

    @Deprecated
    public static ListTag modifyEnchantmentTags(ListTag existingEnchantments, ItemStack stack) {
        return existingEnchantments;
    }

    @Deprecated
    public static Map<Enchantment, Integer> addNewLevels(Map<Enchantment, Integer> enchantmentLevelMap, ItemStack stack) {
        return enchantmentLevelMap;
    }

    public static boolean canHaveDynamicEnchantment(ItemStack stack) {
        if (!EventFlags.CAN_HAVE_DYN_ENCHANTMENTS.isSet()) {
            ObjectReference<Boolean> mayHaveDynamicEnchantments = new ObjectReference<>(false);
            EventFlags.CAN_HAVE_DYN_ENCHANTMENTS.executeWithFlag(() -> {
                if (stack.isEmpty()) {
                    return;
                }
                Item item = stack.getItem();
                ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);
                if (itemKey == null) {
                    return;
                }
                try {
                    if (!item.isEnchantable(stack) || item instanceof BookItem) {
                        return;
                    }
                } catch (NullPointerException exc) {
                    // Some search/indexing paths query stacks before their auxiliary data has fully initialized.
                    return;
                }
                if (Mods.DRACONIC_EVOLUTION.owns(stack.getItem())) {
                    return;
                }
                mayHaveDynamicEnchantments.set(true);
            });
            return mayHaveDynamicEnchantments.get();
        }
        // If we ever end up here, we have a cycle somewhere, probably as a result of checking
        // if the item is enchantable or damageable relies on if enchantments are already being applied on it or not.
        // This probably means we don't want to influence the item with dynamic enchantments.
        return false;
    }

    //This is more or less just a map to say whatever we add upon.
    private static List<DynamicEnchantment> fireEnchantmentGatheringEvent(ItemStack tool) {
        Player foundEntity = AmuletEnchantmentHelper.getPlayerHavingTool(tool);
        if (foundEntity == null) {
            return new ArrayList<>();
        }
        DynamicEnchantmentEvent.Add addEvent = new DynamicEnchantmentEvent.Add(tool, foundEntity);
        if (NeoForge.EVENT_BUS.post(addEvent).isCanceled()) {
            return new ArrayList<>();
        }
        DynamicEnchantmentEvent.Modify modifyEvent = new DynamicEnchantmentEvent.Modify(tool, addEvent.getEnchantmentsToApply(), foundEntity);
        if (NeoForge.EVENT_BUS.post(modifyEvent).isCanceled()) {
            return new ArrayList<>();
        }
        return modifyEvent.getEnchantmentsToApply();
    }
}
