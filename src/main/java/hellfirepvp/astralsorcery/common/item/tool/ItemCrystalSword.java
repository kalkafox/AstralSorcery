/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.crystal.CalculationContext;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.enchantment.AstralEnchantmentType;
import hellfirepvp.astralsorcery.common.item.base.TypeEnchantableItem;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalSword
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:34
 */
public class ItemCrystalSword extends SwordItem implements CrystalAttributeItem, TypeEnchantableItem {

    public ItemCrystalSword() {
        super(CrystalToolTier.getInstance(), new Properties()
                .setNoRepair()
                .durability(CrystalToolTier.getInstance().getUses()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            attr.addTooltip(tooltip, CalculationContext.Builder.properties()
                    .addUsage(CrystalPropertiesAS.Usages.USE_TOOL_DURABILITY)
                    .addUsage(CrystalPropertiesAS.Usages.USE_TOOL_EFFECTIVENESS)
                    .build());
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            return CrystalCalculations.getToolDurability(super.getMaxDamage(stack), stack);
        }
        return super.getMaxDamage(stack);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return super.supportsEnchantment(stack, enchantment) ||
                AstralEnchantmentType.WEAPON.contains(enchantment) ||
                AstralEnchantmentType.BREAKABLE.contains(enchantment);
    }

    public float getAttackDamage() {
        return CrystalToolTier.getInstance().getAttackDamage();
    }

    public float getAttackDamage(ItemStack stack) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            return CrystalCalculations.getToolEfficiency(this.getAttackDamage(), stack);
        } else {
            return this.getAttackDamage();
        }
    }

    private double getAttackSpeed() {
        return -2.4;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes(ItemStack stack) {
        return CrystalAttributes.getCrystalAttributes(stack);
    }

    @Override
    public void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes) {
        if (attributes != null) {
            attributes.store(stack);
        } else {
            CrystalAttributes.storeNull(stack);
        }
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack, AstralEnchantmentType type) {
        return type == AstralEnchantmentType.BREAKABLE || type == AstralEnchantmentType.WEAPON;
    }

    @Override
    public int getEnchantmentValue() {
        return CrystalToolTier.getInstance().getEnchantmentValue();
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return CrystalToolTier.getInstance().getEnchantmentValue();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, this.getAttackDamage(stack), AttributeModifier.Operation.ADD_VALUE),
                        net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, this.getAttackSpeed(), AttributeModifier.Operation.ADD_VALUE),
                        net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .build();
    }
}
