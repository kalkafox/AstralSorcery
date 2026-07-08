/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.armor;

import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ArmorMaterialImbuedLeather
 * Created by HellFirePvP
 * Date: 17.02.2020 / 19:16
 */
public class ArmorMaterialImbuedLeather implements ArmorMaterial {

    @Override
    public int getDurability(EquipmentSlot slot) {
        return 486;
    }

    @Override
    public int getDamageReductionAmount(EquipmentSlot slot) {
        switch (slot) {
            case CHEST:
                return 7;
        }
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 24;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return ArmorMaterial.LEATHER.getSoundEvent();
    }

    @Override
    public Ingredient getRepairMaterial() {
        return Ingredient.fromItems(ItemsAS.STARDUST);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getName() {
        return "imbued_leather";
    }

    @Override
    public float getToughness() {
        return 1.5F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0F;
    }
}
