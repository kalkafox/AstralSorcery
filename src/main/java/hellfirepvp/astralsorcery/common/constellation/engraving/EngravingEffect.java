/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.engraving;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.enchantment.AstralEnchantmentType;
import hellfirepvp.astralsorcery.common.enchantment.EnchantmentHelperAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.registry.internal.AbstractAstralRegistryEntry;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EngravingEffect
 * Created by HellFirePvP
 * Date: 01.05.2020 / 11:37
 */
public class EngravingEffect extends AbstractAstralRegistryEntry<EngravingEffect> {

    private final List<ApplicableEffect> effects = new ArrayList<>();

    public EngravingEffect(IConstellation cst) {
        this.setRegistryName(cst.getRegistryName());
    }

    public EngravingEffect addEffect(ApplicableEffect potion) {
        this.effects.add(potion);
        return this;
    }

    public List<ApplicableEffect> getApplicableEffects(@Nonnull ItemStack stack) {
        return this.effects.stream()
                .filter(effects -> effects.supports(stack))
                .collect(Collectors.toList());
    }

    public static interface ApplicableEffect {

        public boolean supports(@Nonnull ItemStack stack);

        public ItemStack apply(@Nonnull ItemStack stack, float percent, Random rand);

    }

    public static class ModifierEffect implements ApplicableEffect {

        private final Supplier<PerkAttributeType> modifier;
        private final ModifierType type;
        private final float min, max;

        private final List<AstralEnchantmentType> applicableTypes = new ArrayList<>();
        private boolean formatToInteger = false;

        public ModifierEffect(Supplier<PerkAttributeType> modifier, ModifierType type, float min, float max) {
            this.modifier = modifier;
            this.type = type;
            this.min = min;
            this.max = max;
        }

        public ModifierEffect addApplicableType(AstralEnchantmentType type) {
            this.applicableTypes.add(type);
            return this;
        }

        public ModifierEffect formatResultAsInteger() {
            this.formatToInteger = true;
            return this;
        }

        @Override
        public boolean supports(@Nonnull ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (!DynamicModifierHelper.getStaticModifiers(stack).isEmpty()) {
                return false;
            }
            if (this.applicableTypes.isEmpty()) {
                for (AstralEnchantmentType type : AstralEnchantmentType.values()) {
                    if (type.canEnchantItem(stack)) {
                        return true;
                    }
                }
            }
            for (AstralEnchantmentType type : this.applicableTypes) {
                if (type.canEnchantItem(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public ItemStack apply(@Nonnull ItemStack stack, float percent, Random rand) {
            float rValue = percent * (Math.max(0, this.max - this.min));
            if (this.formatToInteger) {
                rValue = Math.round(rValue);
            }
            DynamicModifierHelper.addModifier(stack, UUID.randomUUID(), this.modifier.get(), this.type, this.min + rValue);
            return stack;
        }
    }

    public static class EnchantmentEffect implements ApplicableEffect {

        private final Supplier<ResourceKey<Enchantment>> enchantment;
        private final int min, max;
        private boolean ignoreCompat = false;

        public EnchantmentEffect(Supplier<ResourceKey<Enchantment>> enchantment, int min, int max) {
            this.enchantment = enchantment;
            this.min = min;
            this.max = max;
        }

        public EnchantmentEffect setIgnoreCompatibility() {
            this.ignoreCompat = true;
            return this;
        }

        public boolean isIgnoreCompatibility() {
            return this.ignoreCompat;
        }

        @Override
        public boolean supports(@Nonnull ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            Optional<Holder.Reference<Enchantment>> holder = EnchantmentHelperAS.getHolder(this.enchantment.get());
            if (holder.isEmpty()) {
                return false;
            }
            if (stack.getItem() instanceof BookItem) {
                return true;
            }
            if (!(stack.getItem() instanceof EnchantedBookItem) && !stack.supportsEnchantment(holder.get())) {
                return false;
            }
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            Holder<Enchantment> toApply = holder.get();
            for (var entry : enchantments.entrySet()) {
                Holder<Enchantment> applied = entry.getKey();
                if (toApply.equals(applied)) {
                    return false;
                }
                if (this.ignoreCompat) {
                    continue;
                }
                if (!areCompatible(toApply, applied) && !(stack.getItem() instanceof EnchantedBookItem)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack apply(@Nonnull ItemStack stack, float percent, Random rand) {
            Optional<Holder.Reference<Enchantment>> holder = EnchantmentHelperAS.getHolder(this.enchantment.get());
            if (holder.isEmpty()) {
                return stack;
            }
            int level = this.min + Math.round(percent * (Math.max(0, this.max - this.min)));
            if (stack.getItem() instanceof BookItem) {
                stack = ItemUtils.changeItem(stack, Items.ENCHANTED_BOOK);
            }
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            Holder<Enchantment> newEnch = holder.get();
            if (!this.ignoreCompat) {
                boolean hasIncompat = false;
                for (Holder<Enchantment> e : enchantments.keySet()) {
                    if (e.equals(newEnch) || !areCompatible(e, newEnch)) {
                        hasIncompat = true;
                        break;
                    }
                }
                if (hasIncompat) {
                    return stack;
                }
            }
            EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(newEnch, level));
            return stack;
        }

        private static boolean areCompatible(Holder<Enchantment> first, Holder<Enchantment> second) {
            if ((first.is(EnchantmentsAS.SCORCHING_HEAT) && second.is(Enchantments.SILK_TOUCH)) ||
                    (first.is(Enchantments.SILK_TOUCH) && second.is(EnchantmentsAS.SCORCHING_HEAT))) {
                return false;
            }
            return Enchantment.areCompatible(first, second);
        }
    }

    public static class PotionEffect implements ApplicableEffect {

        private final Supplier<Holder<MobEffect>> effect;
        private final int min, max;

        public PotionEffect(Supplier<Holder<MobEffect>> effect, int min, int max) {
            this.effect = effect;
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean supports(@Nonnull ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (!(stack.getItem() instanceof PotionItem)) {
                return false;
            }
            return !containsEffect(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY), this.effect.get());
        }

        @Override
        public ItemStack apply(@Nonnull ItemStack stack, float percent, Random rand) {
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            Holder<MobEffect> effect = this.effect.get();
            if (!containsEffect(contents, effect)) {
                int amp = this.min + Math.round(percent * (Math.max(0, this.max - this.min)));
                int dur = 3 * 60 * 20 + Math.round(rand.nextFloat() * 4 * 60 * 20);
                contents = contents.withEffectAdded(new MobEffectInstance(effect, dur, amp, true, false, true));
            }
            Holder<MobEffect> cheatDeath = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(EffectsAS.EFFECT_CHEAT_DEATH);
            if (!containsEffect(contents, cheatDeath) && rand.nextInt(30) == 0) {
                contents = contents.withEffectAdded(new MobEffectInstance(cheatDeath, 3 * 60 * 20 + Math.round(rand.nextFloat() * 4 * 60 * 20), 0, true, false, true));
            }
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(contents.potion(), Optional.of(ColorsAS.DYE_ORANGE.getRGB()), contents.customEffects()));
            stack.set(DataComponents.CUSTOM_NAME, Component.translatable("potion.astralsorcery.crafted.name").withStyle(ChatFormatting.GOLD));
            return stack;
        }

        private static boolean containsEffect(PotionContents contents, Holder<MobEffect> effect) {
            for (MobEffectInstance instance : contents.getAllEffects()) {
                if (instance.is(effect)) {
                    return true;
                }
            }
            return false;
        }
    }
}
