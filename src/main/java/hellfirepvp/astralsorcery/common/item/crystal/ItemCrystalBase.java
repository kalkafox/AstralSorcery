/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.entity.item.EntityCrystal;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalBase
 * Created by HellFirePvP
 * Date: 21.07.2019 / 12:58
 */
public abstract class ItemCrystalBase extends Item implements CrystalAttributeGenItem {

    public ItemCrystalBase(Properties prop) {
        super(prop.maxDamage(0));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        if (!world.isRemote()) {
            CrystalAttributes attributes = getAttributes(stack);

            if (attributes == null && stack.getItem() instanceof CrystalAttributeGenItem) {
                attributes = CrystalGenerator.generateNewAttributes(stack);
                attributes.store(stack);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<Component> toolTip, TooltipFlag flag) {
        this.addCrystalPropertyToolTip(stack, toolTip);
    }

    @OnlyIn(Dist.CLIENT)
    protected CrystalAttributes.TooltipResult addCrystalPropertyToolTip(ItemStack stack, List<Component> tooltip) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            return attr.addTooltip(tooltip);
        }
        return CrystalAttributes.TooltipResult.ALL_MISSING;
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
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        EntityCrystal res = new EntityCrystal(EntityTypesAS.ITEM_CRYSTAL, world, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack);
        res.read(location.writeWithoutTypeId(new CompoundTag()));
        res.applyColor(this.getItemEntityColor(itemstack));
        if (location instanceof ItemEntity) {
            res.setReplacedEntity((ItemEntity) location);
        }
        return res;
    }

    @Override
    public int getGeneratedPropertyTiers() {
        return 5;
    }

    @Override
    public int getMaxPropertyTiers() {
        return 7;
    }

    protected Color getItemEntityColor(ItemStack stack) {
        return ColorsAS.ROCK_CRYSTAL;
    }

    public abstract ItemAttunedCrystalBase getTunedItemVariant();

    public abstract ItemCrystalBase getInertDuplicateItem();

}
